package org.demo.com.subscriptionsapp.service;

import lombok.RequiredArgsConstructor;
import org.demo.com.subscriptionsapp.api.dto.subscription.CreateSubscription;
import org.demo.com.subscriptionsapp.api.dto.subscription.SearchSubscriptionCriteria;
import org.demo.com.subscriptionsapp.api.dto.subscription.Subscription;
import org.demo.com.subscriptionsapp.api.dto.subscription.UpdateSubscription;
import org.demo.com.subscriptionsapp.api.error.NotFoundException;
import org.demo.com.subscriptionsapp.domain.converter.SubscriptionConverter;
import org.demo.com.subscriptionsapp.domain.entity.SubscriptionEntity;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;
import org.demo.com.subscriptionsapp.repository.SubscriptionRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public Page<Subscription> getAllSubscriptions(SearchSubscriptionCriteria searchSubscriptionCriteria) {
        return subscriptionRepository.findAll(subscriptionSpecification(searchSubscriptionCriteria), searchSubscriptionCriteria.toPageable())
                .map(SubscriptionConverter::toModel);
    }

    private Specification<SubscriptionEntity> subscriptionSpecification(SearchSubscriptionCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getSubscriptionName() != null && !criteria.getSubscriptionName().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("subscriptionName")),
                        "%" + criteria.getSubscriptionName().toLowerCase() + "%"));
            }
            if (criteria.getSubscriptionTier() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subscriptionTier"), criteria.getSubscriptionTier()));
            }
            if (criteria.getSubscriptionPlan() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subscriptionPlan"), criteria.getSubscriptionPlan()));
            }
            if (criteria.getSubscriptionStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subscriptionStatus"), criteria.getSubscriptionStatus()));
            }
            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }
            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    @Transactional
    public Subscription createSubscription(CreateSubscription createSubscription) {
        SubscriptionEntity subscriptionEntity = SubscriptionEntity.builder()
                .subscriptionName(createSubscription.getSubscriptionName())
                .subscriptionTier(createSubscription.getSubscriptionTier())
                .subscriptionPlan(createSubscription.getSubscriptionPlan())
                .price(createSubscription.getPrice())
                .subscriptionStatus(SubscriptionStatus.ACTIVE)
                .build();
        return SubscriptionConverter.toModel(subscriptionRepository.save(subscriptionEntity));
    }

    @Transactional(readOnly = true)
    public Subscription getSubscriptionById(Long id) {
        return SubscriptionConverter.toModel(subscriptionRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Subscription", id)));
    }

    @Transactional
    public Subscription updateSubscription(Long id, UpdateSubscription updateSubscription) {
        SubscriptionEntity subscriptionEntity = subscriptionRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Subscription", id));

        if(updateSubscription.getSubscriptionStatus()!=null) {
            subscriptionEntity.setSubscriptionStatus(updateSubscription.getSubscriptionStatus());
        }

        if(updateSubscription.getPrice()!=null) {
            subscriptionEntity.setPrice(updateSubscription.getPrice());
        }

        return SubscriptionConverter.toModel(subscriptionEntity);
    }
}
