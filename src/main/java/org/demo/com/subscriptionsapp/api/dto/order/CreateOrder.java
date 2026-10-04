package org.demo.com.subscriptionsapp.api.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateOrder {

    @NotNull(message = "User ID is required")
    @Positive
    private Long userId;

    @PositiveOrZero(message = "Amount must be zero or a positive number")
    private long amount;
}
