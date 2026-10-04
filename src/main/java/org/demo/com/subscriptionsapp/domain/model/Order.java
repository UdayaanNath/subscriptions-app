package org.demo.com.subscriptionsapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.demo.com.subscriptionsapp.domain.enums.OrderStatus;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseModel {

    private Long userId;

    private long amount;

    @Setter
    private OrderStatus orderStatus;
}
