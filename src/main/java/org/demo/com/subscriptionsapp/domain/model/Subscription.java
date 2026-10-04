package org.demo.com.subscriptionsapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionPlan;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription extends BaseModel {

    private String subscriptionName;

    private SubscriptionTier subscriptionTier;

    private SubscriptionPlan subscriptionPlan;

    @Setter
    private long price;

    @Setter
    private SubscriptionStatus subscriptionStatus;
}
