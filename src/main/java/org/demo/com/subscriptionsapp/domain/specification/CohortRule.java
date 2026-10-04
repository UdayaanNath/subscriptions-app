package org.demo.com.subscriptionsapp.domain.specification;

import org.demo.com.subscriptionsapp.domain.enums.UserCohort;

import java.util.List;

public class CohortRule implements UpgradeRule {

    private final List<UserCohort> allowedCohorts;

    public CohortRule(List<UserCohort> allowedCohorts) {
        this.allowedCohorts = List.copyOf(allowedCohorts);
    }

    @Override
    public boolean isSatisfiedBy(UpgradeContext context) {
        return allowedCohorts.contains(context.cohort());
    }
}
