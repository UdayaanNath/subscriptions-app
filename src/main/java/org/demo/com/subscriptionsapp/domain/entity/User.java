package org.demo.com.subscriptionsapp.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;

@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @NotBlank(message = "Username cannot be blank")
    @Column(name = "username", nullable = false, unique = true, updatable = false, length = 50)
    private String username;

    @Setter
    @NotNull(message = "User account status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "user_account_status", nullable = false)
    private UserAccountStatus userAccountStatus;
}
