package org.demo.com.subscriptionsapp.domain.converter;

import org.demo.com.subscriptionsapp.domain.model.Order;

public final class OrderConverter {

    private OrderConverter() {
    }

    public static Order toModel(org.demo.com.subscriptionsapp.domain.entity.Order entity) {
        if (entity == null) {
            return null;
        }
        return Order.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .amount(entity.getAmount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .orderStatus(entity.getOrderStatus())
                .build();
    }

    public static org.demo.com.subscriptionsapp.domain.entity.Order toEntity(Order model) {
        if (model == null) {
            return null;
        }
        return org.demo.com.subscriptionsapp.domain.entity.Order.builder()
                .id(model.getId())
                .userId(model.getUserId())
                .amount(model.getAmount())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .orderStatus(model.getOrderStatus())
                .build();
    }
}
