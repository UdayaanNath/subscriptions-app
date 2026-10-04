package org.demo.com.subscriptionsapp.domain.converter;

import org.demo.com.subscriptionsapp.domain.model.Subscription;

public final class SubscriptionConverter {

    private SubscriptionConverter() {
    }

    public static Subscription toModel(org.demo.com.subscriptionsapp.domain.entity.Subscription entity) {
        if (entity == null) {
            return null;
        }
        return Subscription.builder()
                .id(entity.getId())
                .subscriptionName(entity.getSubscriptionName())
                .subscriptionTier(entity.getSubscriptionTier())
                .subscriptionPlan(entity.getSubscriptionPlan())
                .price(entity.getPrice())
                .subscriptionStatus(entity.getSubscriptionStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static org.demo.com.subscriptionsapp.domain.entity.Subscription toEntity(Subscription model) {
        if (model == null) {
            return null;
        }
        return org.demo.com.subscriptionsapp.domain.entity.Subscription.builder()
                .id(model.getId())
                .subscriptionName(model.getSubscriptionName())
                .subscriptionTier(model.getSubscriptionTier())
                .subscriptionPlan(model.getSubscriptionPlan())
                .price(model.getPrice())
                .subscriptionStatus(model.getSubscriptionStatus())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }
}
