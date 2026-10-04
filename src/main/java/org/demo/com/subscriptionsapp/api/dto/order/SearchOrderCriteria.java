package org.demo.com.subscriptionsapp.api.dto.order;

import lombok.Getter;
import lombok.Setter;
import org.demo.com.subscriptionsapp.api.dto.searchCriteria.BaseSearchCriteria;
import org.demo.com.subscriptionsapp.domain.enums.OrderStatus;

@Getter
@Setter
public class SearchOrderCriteria extends BaseSearchCriteria {
    private Long userId;
    private OrderStatus orderStatus = OrderStatus.DELIVERED;
}
