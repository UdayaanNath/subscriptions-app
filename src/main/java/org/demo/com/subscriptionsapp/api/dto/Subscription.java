package org.demo.com.subscriptionsapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionPlan;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;

import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    private Long id;

    private String subscriptionName;

    private SubscriptionTier subscriptionTier;

    private SubscriptionPlan subscriptionPlan;

    @Setter
    private long price;

    @Setter
    private SubscriptionStatus subscriptionStatus;

    private Date createdAt;

    @Setter
    private Date updatedAt;
}
