package org.demo.com.subscriptionsapp.domain.converter;

import org.demo.com.subscriptionsapp.api.dto.benefit.Benefit;
import org.demo.com.subscriptionsapp.api.dto.membership.Membership;
import org.demo.com.subscriptionsapp.api.dto.order.Order;
import org.demo.com.subscriptionsapp.api.dto.subscription.Subscription;
import org.demo.com.subscriptionsapp.api.dto.user.User;
import org.demo.com.subscriptionsapp.domain.entity.BenefitEntity;
import org.demo.com.subscriptionsapp.domain.entity.MembershipEntity;
import org.demo.com.subscriptionsapp.domain.entity.OrderEntity;
import org.demo.com.subscriptionsapp.domain.entity.SubscriptionEntity;
import org.demo.com.subscriptionsapp.domain.entity.UserEntity;
import org.demo.com.subscriptionsapp.domain.enums.BenefitType;
import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;
import org.demo.com.subscriptionsapp.domain.enums.OrderStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionPlan;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;
import org.demo.com.subscriptionsapp.domain.enums.UserCohort;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConverterTest {

    private final Date createdAt = new Date(1_000);
    private final Date updatedAt = new Date(2_000);
    private final Date expireAt = new Date(3_000);

    @Test
    void convertersReturnNullForNullInput() {
        assertNull(UserConverter.toModel(null));
        assertNull(UserConverter.toEntity(null));
        assertNull(OrderConverter.toModel(null));
        assertNull(OrderConverter.toEntity(null));
        assertNull(SubscriptionConverter.toModel(null));
        assertNull(SubscriptionConverter.toEntity(null));
        assertNull(MembershipConverter.toModel(null));
        assertNull(MembershipConverter.toEntity(null));
        assertNull(BenefitConverter.toModel(null));
    }

    @Test
    void userRoundTripKeepsAccountFields() {
        UserEntity entity = UserEntity.builder()
                .id(1L)
                .username("alice")
                .userAccountStatus(UserAccountStatus.ACTIVE)
                .cohort(UserCohort.STANDARD)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        User model = UserConverter.toModel(entity);
        UserEntity restored = UserConverter.toEntity(model);

        assertEquals("alice", model.getUsername());
        assertEquals(UserAccountStatus.ACTIVE, model.getUserAccountStatus());
        assertEquals(entity.getId(), restored.getId());
        assertEquals(entity.getUsername(), restored.getUsername());
        assertEquals(entity.getUserAccountStatus(), restored.getUserAccountStatus());
    }

    @Test
    void orderRoundTripKeepsAmountAndStatus() {
        OrderEntity entity = OrderEntity.builder()
                .id(3L)
                .userId(2L)
                .amount(0)
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        Order model = OrderConverter.toModel(entity);
        assertEquals(0, model.getAmount());
        assertEquals(OrderStatus.PENDING_PAYMENT, OrderConverter.toEntity(model).getOrderStatus());
        assertEquals(2L, OrderConverter.toEntity(model).getUserId());
    }

    @Test
    void subscriptionRoundTripKeepsCatalogueFields() {
        SubscriptionEntity entity = SubscriptionEntity.builder()
                .id(4L)
                .subscriptionName("Silver Monthly")
                .subscriptionTier(SubscriptionTier.SILVER)
                .subscriptionPlan(SubscriptionPlan.MONTHLY)
                .price(499)
                .subscriptionStatus(SubscriptionStatus.STOPPED)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        Subscription model = SubscriptionConverter.toModel(entity);
        SubscriptionEntity restored = SubscriptionConverter.toEntity(model);

        assertEquals(SubscriptionTier.SILVER, restored.getSubscriptionTier());
        assertEquals(SubscriptionPlan.MONTHLY, restored.getSubscriptionPlan());
        assertEquals(499, restored.getPrice());
        assertEquals(SubscriptionStatus.STOPPED, restored.getSubscriptionStatus());
        assertEquals(model.getSubscriptionName(), restored.getSubscriptionName());
    }

    @Test
    void membershipRoundTripKeepsExpiry() {
        MembershipEntity entity = MembershipEntity.builder()
                .id(8L)
                .userId(1L)
                .subscriptionId(3L)
                .expireAt(expireAt)
                .membershipStatus(MembershipStatus.EXPIRED)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        Membership model = MembershipConverter.toModel(entity);
        MembershipEntity restored = MembershipConverter.toEntity(model);

        assertEquals(expireAt, model.getExpireAt());
        assertEquals(MembershipStatus.EXPIRED, restored.getMembershipStatus());
        assertEquals(3L, restored.getSubscriptionId());
    }

    @Test
    void benefitMapsOptionalDiscount() {
        Benefit model = BenefitConverter.toModel(BenefitEntity.builder()
                .id(9L)
                .subscriptionId(3L)
                .benefitType(BenefitType.DISCOUNT)
                .name("Silver discount")
                .description(null)
                .discountPercent(5)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build());

        assertEquals(BenefitType.DISCOUNT, model.getBenefitType());
        assertNull(model.getDescription());
        assertEquals(5, model.getDiscountPercent());
    }
}
