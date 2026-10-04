package org.demo.com.subscriptionsapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.demo.com.subscriptionsapp.domain.enums.OrderStatus;

import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;

    private Long userId;

    private long amount;

    private Date createdAt;

    @Setter
    private Date updatedAt;

    @Setter
    private OrderStatus orderStatus;
}
