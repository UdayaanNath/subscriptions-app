package org.demo.com.subscriptionsapp.api.dto.user;

import lombok.Getter;
import lombok.Setter;
import org.demo.com.subscriptionsapp.api.dto.searchCriteria.BaseSearchCriteria;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;

@Getter
@Setter
public class SearchUserCriteria extends BaseSearchCriteria {
    private String username;
    private UserAccountStatus userAccountStatus = UserAccountStatus.ACTIVE;
}
