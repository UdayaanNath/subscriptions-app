package org.demo.com.subscriptionsapp.config;

import lombok.Getter;
import lombok.Setter;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;
import org.demo.com.subscriptionsapp.domain.enums.UserCohort;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "app.subscription")
@Getter
@Setter
public class SubscriptionConfig {

    private int defaultFreeTierDays;

    private List<SubscriptionTier> tierOrder;

    private Map<SubscriptionTier, TierRule> tierRules;

    @Getter
    @Setter
    public static class TierRule {

        private int minOrderCount;

        private long minMonthlyOrderValue;

        private List<UserCohort> cohorts;
    }
}
