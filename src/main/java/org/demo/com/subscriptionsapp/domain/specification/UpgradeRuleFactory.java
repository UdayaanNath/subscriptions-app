package org.demo.com.subscriptionsapp.domain.specification;

import org.demo.com.subscriptionsapp.config.SubscriptionConfig;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UpgradeRuleFactory {

    private final SubscriptionConfig subscriptionConfig;

    public UpgradeRuleFactory(SubscriptionConfig subscriptionConfig) {
        this.subscriptionConfig = subscriptionConfig;
    }

    public UpgradeRule ruleFor(SubscriptionTier tier) {
        SubscriptionConfig.TierRule tierRule = subscriptionConfig.getTierRules().get(tier);
        if (tierRule == null) {
            throw new IllegalStateException("No upgrade rules configured for tier " + tier);
        }
        return new AndRule(List.of(
                new MinimumOrderCountRule(tierRule.getMinOrderCount()),
                new MonthlyOrderValueRule(tierRule.getMinMonthlyOrderValue()),
                new CohortRule(tierRule.getCohorts())
        ));
    }
}
