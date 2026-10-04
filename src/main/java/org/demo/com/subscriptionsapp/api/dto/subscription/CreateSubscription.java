package org.demo.com.subscriptionsapp.api.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionPlan;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;

@Getter
@Builder
@AllArgsConstructor
public class CreateSubscription {

    @NotBlank(message = "Subscription name cannot be blank")
    private String subscriptionName;

    @NotNull(message = "Subscription tier is required")
    private SubscriptionTier subscriptionTier;

    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan subscriptionPlan;

    @PositiveOrZero(message = "Price must be zero or a positive number")
    private long price;
}
