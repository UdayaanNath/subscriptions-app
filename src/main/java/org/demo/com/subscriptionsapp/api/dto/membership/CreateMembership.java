package org.demo.com.subscriptionsapp.api.dto.membership;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;

@Getter
@Builder
@AllArgsConstructor
public class CreateMembership {

    @NotNull(message = "User ID is required")
    @Positive
    private Long userId;

    @NotNull(message = "Subscription ID is required")
    @Positive
    private Long subscriptionId;
}
