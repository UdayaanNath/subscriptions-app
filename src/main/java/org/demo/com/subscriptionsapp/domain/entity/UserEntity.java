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
import org.demo.com.subscriptionsapp.domain.enums.UserCohort;

@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {

    @NotBlank(message = "Username cannot be blank")
    @Column(name = "username", nullable = false, unique = true, updatable = false, length = 50)
    private String username;

    @Setter
    @NotNull(message = "User account status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "user_account_status", nullable = false)
    private UserAccountStatus userAccountStatus;

    @NotNull(message = "User cohort is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "cohort", nullable = false)
    private UserCohort cohort;
}
