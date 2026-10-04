package org.demo.com.subscriptionsapp.domain.converter;

import org.demo.com.subscriptionsapp.domain.model.User;

public final class UserConverter {

    private UserConverter() {
    }

    public static User toModel(org.demo.com.subscriptionsapp.domain.entity.User entity) {
        if (entity == null) {
            return null;
        }
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .userAccountStatus(entity.getUserAccountStatus())
                .build();
    }

    public static org.demo.com.subscriptionsapp.domain.entity.User toEntity(User model) {
        if (model == null) {
            return null;
        }
        return org.demo.com.subscriptionsapp.domain.entity.User.builder()
                .id(model.getId())
                .username(model.getUsername())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .userAccountStatus(model.getUserAccountStatus())
                .build();
    }
}
