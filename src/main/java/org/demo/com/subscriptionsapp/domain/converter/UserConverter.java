package org.demo.com.subscriptionsapp.domain.converter;

import org.demo.com.subscriptionsapp.domain.entity.UserEntity;
import org.demo.com.subscriptionsapp.api.dto.user.User;

public final class UserConverter {

    private UserConverter() {
    }

    public static User toModel(UserEntity entity) {
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

    public static UserEntity toEntity(User model) {
        if (model == null) {
            return null;
        }
        return UserEntity.builder()
                .id(model.getId())
                .username(model.getUsername())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .userAccountStatus(model.getUserAccountStatus())
                .build();
    }
}
