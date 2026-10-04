package org.demo.com.subscriptionsapp.api.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.demo.com.subscriptionsapp.domain.enums.OrderStatus;

import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
public class Order {

    private Long id;

    @NotNull(message = "User ID is required")
    @Positive
    private Long userId;

    @PositiveOrZero(message = "Amount must be zero or a positive number")
    private long amount;

    private Date createdAt;

    private Date updatedAt;

    @NotNull(message = "Order status is required")
    private OrderStatus orderStatus;
}
