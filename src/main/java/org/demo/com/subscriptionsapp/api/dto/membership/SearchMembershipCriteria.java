package org.demo.com.subscriptionsapp.api.dto.membership;

import lombok.Getter;
import lombok.Setter;
import org.demo.com.subscriptionsapp.api.dto.searchCriteria.BaseSearchCriteria;
import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;

@Getter
@Setter
public class SearchMembershipCriteria extends BaseSearchCriteria {
    private Long userId;
    private Long subscriptionId;
    private MembershipStatus membershipStatus = MembershipStatus.ACTIVE;
}
