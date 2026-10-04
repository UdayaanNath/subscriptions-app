package org.demo.com.subscriptionsapp.api.dto.subscription;

import lombok.Getter;
import lombok.Setter;
import org.demo.com.subscriptionsapp.api.dto.searchCriteria.BaseSearchCriteria;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionPlan;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;

@Getter
@Setter
public class SearchSubscriptionCriteria extends BaseSearchCriteria {
    private String subscriptionName;
    private SubscriptionTier subscriptionTier;
    private SubscriptionPlan subscriptionPlan;
    private Long minPrice;
    private Long maxPrice;
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.ACTIVE;
}
