package org.demo.com.subscriptionsapp.service;

import lombok.RequiredArgsConstructor;
import org.demo.com.subscriptionsapp.api.dto.user.CreateUser;
import org.demo.com.subscriptionsapp.api.dto.user.SearchUserCriteria;
import org.demo.com.subscriptionsapp.api.dto.user.UpdateUser;
import org.demo.com.subscriptionsapp.api.dto.user.User;
import org.demo.com.subscriptionsapp.api.error.BadRequestException;
import org.demo.com.subscriptionsapp.api.error.NotFoundException;
import org.demo.com.subscriptionsapp.config.SubscriptionConfig;
import org.demo.com.subscriptionsapp.domain.converter.UserConverter;
import org.demo.com.subscriptionsapp.domain.entity.MembershipEntity;
import org.demo.com.subscriptionsapp.domain.entity.SubscriptionEntity;
import org.demo.com.subscriptionsapp.domain.entity.UserEntity;
import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;
import org.demo.com.subscriptionsapp.domain.enums.UserCohort;
import org.demo.com.subscriptionsapp.repository.MembershipRepository;
import org.demo.com.subscriptionsapp.repository.SubscriptionRepository;
import org.demo.com.subscriptionsapp.repository.UserRepository;
import org.demo.com.subscriptionsapp.util.DateUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionConfig subscriptionConfig;

    @Transactional(readOnly = true)
    public Page<User> listUser(SearchUserCriteria searchUserCriteria) {
        return userRepository.findAll(userSpecification(searchUserCriteria), searchUserCriteria.toPageable())
                .map(UserConverter::toModel);
    }

    private Specification<UserEntity> userSpecification(SearchUserCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getUsername() != null && !criteria.getUsername().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")),
                        "%" + criteria.getUsername().toLowerCase() + "%"));
            }
            if (criteria.getUserAccountStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userAccountStatus"), criteria.getUserAccountStatus()));
            }
            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return UserConverter.toModel(userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", id)));
    }

    @Transactional
    public User createUser(CreateUser createUser) {
        if (userRepository.existsByUsername(createUser.getUsername())) {
            throw new BadRequestException("Username already exists: " + createUser.getUsername());
        }

        UserEntity userEntity = UserEntity.builder()
                .username(createUser.getUsername())
                .userAccountStatus(UserAccountStatus.ACTIVE)
                .cohort(UserCohort.STANDARD)
                .build();
        userEntity = userRepository.save(userEntity);

        SubscriptionEntity freeSubscription = subscriptionRepository
                .findFirstBySubscriptionTierAndSubscriptionStatus(SubscriptionTier.FREE, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Free subscription not found"));

        MembershipEntity membershipEntity = MembershipEntity.builder()
                .userId(userEntity.getId())
                .subscriptionId(freeSubscription.getId())
                .expireAt(DateUtils.addDays(new Date(), subscriptionConfig.getDefaultFreeTierDays()))
                .membershipStatus(MembershipStatus.ACTIVE)
                .build();
        membershipRepository.save(membershipEntity);

        return UserConverter.toModel(userEntity);
    }

    @Transactional
    public User updateUser(Long id, UpdateUser updateUser) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", id));

        if(userEntity.getUserAccountStatus() == UserAccountStatus.DEACTIVATED) {
            throw new BadRequestException("User already deactivated: "+userEntity.getUsername());
        }

        if (userEntity.getUserAccountStatus() != UserAccountStatus.DEACTIVATED
                && updateUser.getUserAccountStatus() == UserAccountStatus.DEACTIVATED) {
            cancelMembershipsThatAreNotExpired(userEntity.getId());
        }

        userEntity.setUserAccountStatus(updateUser.getUserAccountStatus());
        return UserConverter.toModel(userEntity);
    }

    private void cancelMembershipsThatAreNotExpired(Long userId) {
        Date now = new Date();
        List<MembershipEntity> memberships = membershipRepository.findByUserId(userId);
        for (MembershipEntity membership : memberships) {
            if (membership.getMembershipStatus() != MembershipStatus.EXPIRED
                    && membership.getExpireAt().after(now)) {
                membership.setMembershipStatus(MembershipStatus.CANCELLED);
            }
        }
    }
}
