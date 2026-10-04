package org.demo.com.subscriptionsapp.domain.converter;

import org.demo.com.subscriptionsapp.domain.entity.SubscriptionEntity;
import org.demo.com.subscriptionsapp.api.dto.subscription.Subscription;

public final class SubscriptionConverter {

    private SubscriptionConverter() {
    }

    public static Subscription toModel(SubscriptionEntity entity) {
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

    public static SubscriptionEntity toEntity(Subscription model) {
        if (model == null) {
            return null;
        }
        return SubscriptionEntity.builder()
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
