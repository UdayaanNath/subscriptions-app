package org.demo.com.subscriptionsapp.api.dto.membership;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;

import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
public class Membership {

    private Long id;

    @NotNull(message = "User ID is required")
    @Positive
    private Long userId;

    @NotNull(message = "Subscription ID is required")
    @Positive
    private Long subscriptionId;

    private Date createdAt;

    @NotNull(message = "Expiration date is required")
    private Date expireAt;

    private Date updatedAt;

    @NotNull(message = "Membership status is required")
    private MembershipStatus membershipStatus;
}
