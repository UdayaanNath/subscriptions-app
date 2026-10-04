package org.demo.com.subscriptionsapp.domain.specification;

public class MinimumOrderCountRule implements UpgradeRule {

    private final int exclusiveMinimum;

    public MinimumOrderCountRule(int exclusiveMinimum) {
        this.exclusiveMinimum = exclusiveMinimum;
    }

    @Override
    public boolean isSatisfiedBy(UpgradeContext context) {
        return context.orderCount() > exclusiveMinimum;
    }
}
