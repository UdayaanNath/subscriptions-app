package org.demo.com.subscriptionsapp.domain.specification;

public class MonthlyOrderValueRule implements UpgradeRule {

    private final long minimumMonthlyOrderValue;

    public MonthlyOrderValueRule(long minimumMonthlyOrderValue) {
        this.minimumMonthlyOrderValue = minimumMonthlyOrderValue;
    }

    @Override
    public boolean isSatisfiedBy(UpgradeContext context) {
        return context.monthlyOrderValue() >= minimumMonthlyOrderValue;
    }
}
