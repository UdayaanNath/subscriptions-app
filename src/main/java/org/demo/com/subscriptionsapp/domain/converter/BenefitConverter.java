package org.demo.com.subscriptionsapp.domain.converter;

import org.demo.com.subscriptionsapp.api.dto.benefit.Benefit;
import org.demo.com.subscriptionsapp.domain.entity.BenefitEntity;

public final class BenefitConverter {

    private BenefitConverter() {
    }

    public static Benefit toModel(BenefitEntity entity) {
        if (entity == null) {
            return null;
        }
        return Benefit.builder()
                .id(entity.getId())
                .subscriptionId(entity.getSubscriptionId())
                .benefitType(entity.getBenefitType())
                .name(entity.getName())
                .description(entity.getDescription())
                .discountPercent(entity.getDiscountPercent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
