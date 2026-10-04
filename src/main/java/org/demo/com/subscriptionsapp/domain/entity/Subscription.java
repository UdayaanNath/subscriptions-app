package org.demo.com.subscriptionsapp.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionPlan;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;

@Entity
@Table(name = "subscriptions")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription extends BaseEntity {

    @NotBlank(message = "Subscription name cannot be blank")
    @Column(name = "subscription_name", nullable = false, length = 100)
    private String subscriptionName;

    @NotNull(message = "Subscription tier is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_tier", nullable = false)
    private SubscriptionTier subscriptionTier;

    @NotNull(message = "Subscription plan is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    @Setter
    @PositiveOrZero(message = "Price must be zero or a positive number")
    @Column(name = "price", nullable = false)
    private long price;

    @Setter
    @NotNull(message = "Subscription status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false)
    private SubscriptionStatus subscriptionStatus;
}
