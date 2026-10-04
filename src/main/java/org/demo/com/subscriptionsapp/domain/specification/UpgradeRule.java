package org.demo.com.subscriptionsapp.domain.specification;

public interface UpgradeRule {

    boolean isSatisfiedBy(UpgradeContext context);
}
