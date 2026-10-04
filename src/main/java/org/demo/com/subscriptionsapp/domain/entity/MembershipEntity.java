package org.demo.com.subscriptionsapp.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;

import java.util.Date;

@Entity
@Table(name = "memberships", indexes = {
        @Index(name = "idx_memberships_user_id", columnList = "user_id"),
        @Index(name = "idx_memberships_subscription_id", columnList = "subscription_id")
})
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipEntity extends BaseEntity {

    @NotNull(message = "User ID is required")
    @Positive
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Setter
    @NotNull(message = "Subscription ID is required")
    @Positive
    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Setter
    @NotNull(message = "Expiration date is required")
    @Column(name = "expire_at", nullable = false)
    private Date expireAt;

    @Setter
    @NotNull(message = "Membership status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "membership_status", nullable = false)
    private MembershipStatus membershipStatus;
}
