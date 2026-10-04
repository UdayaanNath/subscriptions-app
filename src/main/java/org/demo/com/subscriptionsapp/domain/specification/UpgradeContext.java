package org.demo.com.subscriptionsapp.domain.specification;

import org.demo.com.subscriptionsapp.domain.enums.UserCohort;

public record UpgradeContext(long orderCount, long monthlyOrderValue, UserCohort cohort) {
}
