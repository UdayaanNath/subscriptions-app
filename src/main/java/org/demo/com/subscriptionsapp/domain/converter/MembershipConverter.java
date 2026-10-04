package org.demo.com.subscriptionsapp.domain.converter;

import org.demo.com.subscriptionsapp.domain.entity.MembershipEntity;
import org.demo.com.subscriptionsapp.api.dto.membership.Membership;

public final class MembershipConverter {

    private MembershipConverter() {
    }

    public static Membership toModel(MembershipEntity entity) {
        if (entity == null) {
            return null;
        }
        return Membership.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .subscriptionId(entity.getSubscriptionId())
                .createdAt(entity.getCreatedAt())
                .expireAt(entity.getExpireAt())
                .updatedAt(entity.getUpdatedAt())
                .membershipStatus(entity.getMembershipStatus())
                .build();
    }

    public static MembershipEntity toEntity(Membership model) {
        if (model == null) {
            return null;
        }
        return MembershipEntity.builder()
                .id(model.getId())
                .userId(model.getUserId())
                .subscriptionId(model.getSubscriptionId())
                .createdAt(model.getCreatedAt())
                .expireAt(model.getExpireAt())
                .updatedAt(model.getUpdatedAt())
                .membershipStatus(model.getMembershipStatus())
                .build();
    }
}
