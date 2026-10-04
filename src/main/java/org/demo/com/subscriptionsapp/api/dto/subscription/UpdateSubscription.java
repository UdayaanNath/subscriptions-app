package org.demo.com.subscriptionsapp.api.dto.subscription;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;

@Getter
@Builder
@AllArgsConstructor
public class UpdateSubscription {
    @PositiveOrZero(message = "Price must be zero or a positive number")
    private Long price;

    private SubscriptionStatus subscriptionStatus;
}
