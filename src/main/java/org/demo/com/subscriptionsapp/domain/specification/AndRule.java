package org.demo.com.subscriptionsapp.domain.specification;

import java.util.List;

public class AndRule implements UpgradeRule {

    private final List<UpgradeRule> rules;

    public AndRule(List<UpgradeRule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public boolean isSatisfiedBy(UpgradeContext context) {
        return rules.stream().allMatch(rule -> rule.isSatisfiedBy(context));
    }
}
