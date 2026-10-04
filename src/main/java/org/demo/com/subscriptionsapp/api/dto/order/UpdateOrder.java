package org.demo.com.subscriptionsapp.api.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.demo.com.subscriptionsapp.domain.enums.OrderStatus;

@Getter
@Builder
@AllArgsConstructor
public class UpdateOrder {

    @NotNull(message = "Order status is required")
    private OrderStatus orderStatus;
}
