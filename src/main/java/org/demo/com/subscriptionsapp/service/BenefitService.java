package org.demo.com.subscriptionsapp.service;

import lombok.RequiredArgsConstructor;
import org.demo.com.subscriptionsapp.api.dto.benefit.Benefit;
import org.demo.com.subscriptionsapp.api.dto.benefit.CreateBenefit;
import org.demo.com.subscriptionsapp.api.dto.benefit.SearchBenefitCriteria;
import org.demo.com.subscriptionsapp.api.dto.benefit.UpdateBenefit;
import org.demo.com.subscriptionsapp.api.error.NotFoundException;
import org.demo.com.subscriptionsapp.domain.converter.BenefitConverter;
import org.demo.com.subscriptionsapp.domain.entity.BenefitEntity;
import org.demo.com.subscriptionsapp.domain.entity.MembershipEntity;
import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;
import org.demo.com.subscriptionsapp.repository.BenefitRepository;
import org.demo.com.subscriptionsapp.repository.MembershipRepository;
import org.demo.com.subscriptionsapp.repository.SubscriptionRepository;
import org.demo.com.subscriptionsapp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BenefitService {

    private final BenefitRepository benefitRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    @Transactional(readOnly = true)
    public Page<Benefit> listBenefits(SearchBenefitCriteria criteria) {
        if (!userRepository.existsById(criteria.getUserId())) {
            throw NotFoundException.of("User", criteria.getUserId());
        }

        List<Long> subscriptionIds = membershipRepository
                .findByUserIdAndMembershipStatusAndExpireAtAfter(
                        criteria.getUserId(), MembershipStatus.ACTIVE, new Date())
                .stream()
                .map(MembershipEntity::getSubscriptionId)
                .distinct()
                .toList();
        if (subscriptionIds.isEmpty()) {
            return Page.empty(criteria.toPageable());
        }
        return benefitRepository.findBySubscriptionIdIn(subscriptionIds, criteria.toPageable())
                .map(BenefitConverter::toModel);
    }

    @Transactional(readOnly = true)
    public Benefit getBenefit(Long id) {
        return BenefitConverter.toModel(benefitRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Benefit", id)));
    }

    @Transactional
    public Benefit createBenefit(CreateBenefit createBenefit) {
        if (!subscriptionRepository.existsById(createBenefit.getSubscriptionId())) {
            throw NotFoundException.of("Subscription", createBenefit.getSubscriptionId());
        }
        BenefitEntity benefitEntity = BenefitEntity.builder()
                .subscriptionId(createBenefit.getSubscriptionId())
                .benefitType(createBenefit.getBenefitType())
                .name(createBenefit.getName())
                .description(createBenefit.getDescription())
                .discountPercent(createBenefit.getDiscountPercent())
                .build();
        return BenefitConverter.toModel(benefitRepository.save(benefitEntity));
    }

    @Transactional
    public Benefit updateBenefit(Long id, UpdateBenefit updateBenefit) {
        BenefitEntity benefitEntity = benefitRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Benefit", id));
        if (updateBenefit.getBenefitType() != null) {
            benefitEntity.setBenefitType(updateBenefit.getBenefitType());
        }
        if (updateBenefit.getName() != null) {
            benefitEntity.setName(updateBenefit.getName());
        }
        if (updateBenefit.getDescription() != null) {
            benefitEntity.setDescription(updateBenefit.getDescription());
        }
        if (updateBenefit.getDiscountPercent() != null) {
            benefitEntity.setDiscountPercent(updateBenefit.getDiscountPercent());
        }
        return BenefitConverter.toModel(benefitEntity);
    }

    @Transactional
    public void deleteBenefit(Long id) {
        if (!benefitRepository.existsById(id)) {
            throw NotFoundException.of("Benefit", id);
        }
        benefitRepository.deleteById(id);
    }
}
