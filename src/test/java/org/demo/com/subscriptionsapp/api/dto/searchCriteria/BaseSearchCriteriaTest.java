package org.demo.com.subscriptionsapp.api.dto.searchCriteria;

import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;
import org.demo.com.subscriptionsapp.domain.enums.OrderStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;
import org.demo.com.subscriptionsapp.api.dto.membership.SearchMembershipCriteria;
import org.demo.com.subscriptionsapp.api.dto.order.SearchOrderCriteria;
import org.demo.com.subscriptionsapp.api.dto.subscription.SearchSubscriptionCriteria;
import org.demo.com.subscriptionsapp.api.dto.user.SearchUserCriteria;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BaseSearchCriteriaTest {

    @Test
    void omittedSizeUsesTwentyAndNegativePageBecomesZero() {
        BaseSearchCriteria criteria = new BaseSearchCriteria();
        criteria.setPage(-4);
        criteria.setSize(0);

        Pageable pageable = criteria.toPageable();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.ASC, "id"), pageable.getSort());
    }

    @Test
    void negativeSizeUsesTheDefaultAndAPositiveSizeIsKept() {
        BaseSearchCriteria negative = new BaseSearchCriteria();
        negative.setPage(2);
        negative.setSize(-1);
        assertEquals(2, negative.toPageable().getPageNumber());
        assertEquals(20, negative.toPageable().getPageSize());

        BaseSearchCriteria custom = new BaseSearchCriteria();
        custom.setSize(7);
        assertEquals(7, custom.toPageable().getPageSize());
    }

    @Test
    void listDefaultsMatchTheDocumentedStatuses() {
        assertEquals(UserAccountStatus.ACTIVE, new SearchUserCriteria().getUserAccountStatus());
        assertEquals(OrderStatus.DELIVERED, new SearchOrderCriteria().getOrderStatus());
        assertEquals(MembershipStatus.ACTIVE, new SearchMembershipCriteria().getMembershipStatus());
        assertEquals(SubscriptionStatus.ACTIVE, new SearchSubscriptionCriteria().getSubscriptionStatus());
        assertNull(new SearchSubscriptionCriteria().getMinPrice());
        assertNull(new SearchSubscriptionCriteria().getMaxPrice());
    }
}
