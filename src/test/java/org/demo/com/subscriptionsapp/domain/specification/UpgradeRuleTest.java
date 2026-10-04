package org.demo.com.subscriptionsapp.domain.specification;

import org.demo.com.subscriptionsapp.config.SubscriptionConfig;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;
import org.demo.com.subscriptionsapp.domain.enums.UserCohort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpgradeRuleTest {

    @Test
    void orderCountMustBeStrictlyGreaterThanTheMinimum() {
        MinimumOrderCountRule rule = new MinimumOrderCountRule(5);

        assertFalse(rule.isSatisfiedBy(context(5, 10_000, UserCohort.STANDARD)));
        assertTrue(rule.isSatisfiedBy(context(6, 0, UserCohort.STANDARD)));
    }

    @Test
    void freeTierAllowsAMemberWithNoOrders() {
        MinimumOrderCountRule rule = new MinimumOrderCountRule(-1);

        assertTrue(rule.isSatisfiedBy(context(0, 0, UserCohort.STANDARD)));
    }

    @Test
    void monthlyValueIncludesTheExactMinimum() {
        MonthlyOrderValueRule rule = new MonthlyOrderValueRule(1000);

        assertFalse(rule.isSatisfiedBy(context(10, 999, UserCohort.STANDARD)));
        assertTrue(rule.isSatisfiedBy(context(10, 1000, UserCohort.STANDARD)));
        assertTrue(new MonthlyOrderValueRule(0).isSatisfiedBy(context(0, 0, UserCohort.STANDARD)));
    }

    @Test
    void cohortMustBeInTheAllowedList() {
        CohortRule rule = new CohortRule(List.of(UserCohort.EARLY_ADOPTER, UserCohort.EMPLOYEE));

        assertTrue(rule.isSatisfiedBy(context(1, 1, UserCohort.EMPLOYEE)));
        assertFalse(rule.isSatisfiedBy(context(1, 1, UserCohort.STANDARD)));
        assertFalse(new CohortRule(List.of()).isSatisfiedBy(context(1, 1, UserCohort.EMPLOYEE)));
    }

    @Test
    void andRuleRequiresEveryRule() {
        UpgradeRule passing = context -> true;
        UpgradeRule failing = context -> false;

        assertTrue(new AndRule(List.of()).isSatisfiedBy(context(0, 0, UserCohort.STANDARD)));
        assertTrue(new AndRule(List.of(passing, passing)).isSatisfiedBy(context(0, 0, UserCohort.STANDARD)));
        assertFalse(new AndRule(List.of(passing, failing)).isSatisfiedBy(context(0, 0, UserCohort.STANDARD)));
    }

    @Test
    void factoryCombinesTheConfiguredTierRules() {
        SubscriptionConfig.TierRule silver = new SubscriptionConfig.TierRule();
        silver.setMinOrderCount(5);
        silver.setMinMonthlyOrderValue(1000);
        silver.setCohorts(List.of(UserCohort.EARLY_ADOPTER, UserCohort.EMPLOYEE));

        SubscriptionConfig config = new SubscriptionConfig();
        config.setTierRules(Map.of(SubscriptionTier.SILVER, silver));
        UpgradeRule rule = new UpgradeRuleFactory(config).ruleFor(SubscriptionTier.SILVER);

        assertFalse(rule.isSatisfiedBy(context(5, 1000, UserCohort.EARLY_ADOPTER)));
        assertFalse(rule.isSatisfiedBy(context(6, 999, UserCohort.EARLY_ADOPTER)));
        assertFalse(rule.isSatisfiedBy(context(6, 1000, UserCohort.STANDARD)));
        assertTrue(rule.isSatisfiedBy(context(6, 1000, UserCohort.EARLY_ADOPTER)));
    }

    @Test
    void factoryRejectsATierWithNoRules() {
        SubscriptionConfig config = new SubscriptionConfig();
        config.setTierRules(Map.of());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new UpgradeRuleFactory(config).ruleFor(SubscriptionTier.GOLD));

        assertEquals("No upgrade rules configured for tier GOLD", exception.getMessage());
    }

    private static UpgradeContext context(long orderCount, long monthlyOrderValue, UserCohort cohort) {
        return new UpgradeContext(orderCount, monthlyOrderValue, cohort);
    }
}
