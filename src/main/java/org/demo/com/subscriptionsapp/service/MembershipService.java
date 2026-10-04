package org.demo.com.subscriptionsapp.service;

import lombok.RequiredArgsConstructor;
import org.demo.com.subscriptionsapp.api.dto.membership.CreateMembership;
import org.demo.com.subscriptionsapp.api.dto.membership.Membership;
import org.demo.com.subscriptionsapp.api.dto.membership.SearchMembershipCriteria;
import org.demo.com.subscriptionsapp.api.error.BadRequestException;
import org.demo.com.subscriptionsapp.api.error.NotFoundException;
import org.demo.com.subscriptionsapp.config.SubscriptionConfig;
import org.demo.com.subscriptionsapp.domain.converter.MembershipConverter;
import org.demo.com.subscriptionsapp.domain.entity.MembershipEntity;
import org.demo.com.subscriptionsapp.domain.entity.SubscriptionEntity;
import org.demo.com.subscriptionsapp.domain.entity.UserEntity;
import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;
import org.demo.com.subscriptionsapp.domain.enums.OrderStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;
import org.demo.com.subscriptionsapp.domain.specification.UpgradeContext;
import org.demo.com.subscriptionsapp.domain.specification.UpgradeRuleFactory;
import org.demo.com.subscriptionsapp.repository.MembershipRepository;
import org.demo.com.subscriptionsapp.repository.OrderRepository;
import org.demo.com.subscriptionsapp.repository.SubscriptionRepository;
import org.demo.com.subscriptionsapp.repository.UserRepository;
import org.demo.com.subscriptionsapp.util.DateUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final OrderRepository orderRepository;
    private final SubscriptionConfig subscriptionConfig;
    private final UpgradeRuleFactory upgradeRuleFactory;

    @Transactional(readOnly = true)
    public Page<Membership> listMemberships(SearchMembershipCriteria searchMembershipCriteria) {
        return membershipRepository.findAll(membershipSpecification(searchMembershipCriteria), searchMembershipCriteria.toPageable())
                .map(MembershipConverter::toModel);
    }

    private Specification<MembershipEntity> membershipSpecification(SearchMembershipCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), criteria.getUserId()));
            }
            if (criteria.getSubscriptionId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subscriptionId"), criteria.getSubscriptionId()));
            }
            if (criteria.getMembershipStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("membershipStatus"), criteria.getMembershipStatus()));
            }
            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    @Transactional(readOnly = true)
    public Membership getMembership(Long id) {
        return MembershipConverter.toModel(membershipRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Membership", id)));
    }

    @Transactional
    public Membership createMembership(CreateMembership createMembership) {
        UserEntity user = userRepository.findById(createMembership.getUserId())
                .orElseThrow(() -> NotFoundException.of("User", createMembership.getUserId()));
        if (user.getUserAccountStatus() == UserAccountStatus.DEACTIVATED) {
            throw new BadRequestException("User is deactivated: " + user.getId());
        }

        SubscriptionEntity subscription = subscriptionRepository.findById(createMembership.getSubscriptionId())
                .orElseThrow(() -> NotFoundException.of("Subscription", createMembership.getSubscriptionId()));
        if (subscription.getSubscriptionStatus() == SubscriptionStatus.STOPPED) {
            throw new BadRequestException("Subscription is stopped: " + subscription.getId());
        }

        List<MembershipEntity> existingMemberships = membershipRepository.findByUserId(user.getId());
        for (MembershipEntity existingMembership : existingMemberships) {
            existingMembership.setMembershipStatus(MembershipStatus.CANCELLED);
        }

        MembershipEntity membershipEntity = MembershipEntity.builder()
                .userId(user.getId())
                .subscriptionId(subscription.getId())
                .expireAt(DateUtils.calculateExpiryDate(new Date(), subscription.getSubscriptionPlan()))
                .membershipStatus(MembershipStatus.ACTIVE)
                .build();
        return MembershipConverter.toModel(membershipRepository.save(membershipEntity));
    }

    @Transactional
    public Membership renewMembership(Long id) {
        MembershipEntity membershipEntity = membershipRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Membership", id));

        if(membershipEntity.getMembershipStatus() == MembershipStatus.CANCELLED) {
            throw new BadRequestException("Membership is already cancelled: "+membershipEntity.getId());
        }

        SubscriptionEntity subscription = subscriptionRepository.findById(membershipEntity.getSubscriptionId())
                .orElseThrow(() -> NotFoundException.of("Subscription", membershipEntity.getSubscriptionId()));
        if (subscription.getSubscriptionStatus() == SubscriptionStatus.STOPPED) {
            throw new BadRequestException("Subscription is stopped: " + subscription.getId());
        }

        membershipEntity.setExpireAt(DateUtils.calculateExpiryDate(new Date(), subscription.getSubscriptionPlan()));
        return MembershipConverter.toModel(membershipEntity);
    }

    @Transactional
    public Membership upgradeMembership(Long id) {
        MembershipEntity membershipEntity = loadUpgradeableMembership(id);
        UserEntity user = loadActiveUser(membershipEntity.getUserId());
        SubscriptionEntity currentSubscription = loadOfferedSubscription(membershipEntity.getSubscriptionId());
        SubscriptionTier nextTier = adjacentTier(currentSubscription.getSubscriptionTier(), 1);
        if (nextTier == null) {
            throw new BadRequestException("Membership is already at the highest tier");
        }
        if (!upgradeRuleFactory.ruleFor(nextTier).isSatisfiedBy(upgradeContext(user))) {
            throw new BadRequestException("User is not eligible for tier " + nextTier);
        }
        return moveToTier(membershipEntity, nextTier);
    }

    @Transactional
    public Membership downgradeMembership(Long id) {
        MembershipEntity membershipEntity = loadUpgradeableMembership(id);
        UserEntity user = loadActiveUser(membershipEntity.getUserId());
        SubscriptionEntity currentSubscription = loadOfferedSubscription(membershipEntity.getSubscriptionId());
        SubscriptionTier currentTier = currentSubscription.getSubscriptionTier();
        SubscriptionTier previousTier = adjacentTier(currentTier, -1);
        if (previousTier == null) {
            throw new BadRequestException("Membership is already at the lowest tier");
        }
        if (upgradeRuleFactory.ruleFor(currentTier).isSatisfiedBy(upgradeContext(user))) {
            throw new BadRequestException("User still meets " + currentTier + " requirements");
        }
        return moveToTier(membershipEntity, previousTier);
    }

    private Membership moveToTier(MembershipEntity membershipEntity, SubscriptionTier tier) {
        SubscriptionEntity targetSubscription = subscriptionRepository
                .findFirstBySubscriptionTierAndSubscriptionStatus(tier, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Active subscription not found for tier " + tier));
        membershipEntity.setSubscriptionId(targetSubscription.getId());
        membershipEntity.setExpireAt(DateUtils.calculateExpiryDate(new Date(), targetSubscription.getSubscriptionPlan()));
        return MembershipConverter.toModel(membershipEntity);
    }

    private MembershipEntity loadUpgradeableMembership(Long id) {
        MembershipEntity membershipEntity = membershipRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Membership", id));
        if (membershipEntity.getMembershipStatus() != MembershipStatus.ACTIVE) {
            throw new BadRequestException("Membership is not active: " + id);
        }
        return membershipEntity;
    }

    private UserEntity loadActiveUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.of("User", userId));
        if (user.getUserAccountStatus() == UserAccountStatus.DEACTIVATED) {
            throw new BadRequestException("User is deactivated: " + user.getId());
        }
        return user;
    }

    private SubscriptionEntity loadOfferedSubscription(Long subscriptionId) {
        SubscriptionEntity subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> NotFoundException.of("Subscription", subscriptionId));
        if (subscription.getSubscriptionStatus() == SubscriptionStatus.STOPPED) {
            throw new BadRequestException("Subscription is stopped: " + subscription.getId());
        }
        return subscription;
    }

    private SubscriptionTier adjacentTier(SubscriptionTier current, int offset) {
        List<SubscriptionTier> tierOrder = subscriptionConfig.getTierOrder();
        int index = tierOrder.indexOf(current);
        int targetIndex = index + offset;
        if (index < 0 || targetIndex < 0 || targetIndex >= tierOrder.size()) {
            return null;
        }
        return tierOrder.get(targetIndex);
    }

    private UpgradeContext upgradeContext(UserEntity user) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date monthStart = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        Date nextMonthStart = calendar.getTime();

        long orderCount = orderRepository.countByUserIdAndOrderStatus(user.getId(), OrderStatus.DELIVERED);
        long monthlyOrderValue = orderRepository.sumAmountByUserIdAndOrderStatusAndCreatedAtBetween(
                user.getId(), OrderStatus.DELIVERED, monthStart, nextMonthStart);
        return new UpgradeContext(orderCount, monthlyOrderValue, user.getCohort());
    }

    @Transactional
    public Membership cancelMemberShip(Long id) {
        MembershipEntity membershipEntity = membershipRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Membership", id));

        if(membershipEntity.getMembershipStatus() == MembershipStatus.CANCELLED) {
            throw new BadRequestException("Membership is already cancelled: "+membershipEntity.getId());
        }

        membershipEntity.setMembershipStatus(MembershipStatus.CANCELLED);

        return MembershipConverter.toModel(membershipEntity);
    }
}
