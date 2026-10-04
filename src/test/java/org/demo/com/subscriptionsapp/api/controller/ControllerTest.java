package org.demo.com.subscriptionsapp.api.controller;

import org.demo.com.subscriptionsapp.api.dto.benefit.Benefit;
import org.demo.com.subscriptionsapp.api.dto.benefit.CreateBenefit;
import org.demo.com.subscriptionsapp.api.dto.benefit.SearchBenefitCriteria;
import org.demo.com.subscriptionsapp.api.dto.benefit.UpdateBenefit;
import org.demo.com.subscriptionsapp.api.dto.membership.CreateMembership;
import org.demo.com.subscriptionsapp.api.dto.membership.Membership;
import org.demo.com.subscriptionsapp.api.dto.membership.SearchMembershipCriteria;
import org.demo.com.subscriptionsapp.api.dto.order.CreateOrder;
import org.demo.com.subscriptionsapp.api.dto.order.Order;
import org.demo.com.subscriptionsapp.api.dto.order.SearchOrderCriteria;
import org.demo.com.subscriptionsapp.api.dto.order.UpdateOrder;
import org.demo.com.subscriptionsapp.api.dto.subscription.CreateSubscription;
import org.demo.com.subscriptionsapp.api.dto.subscription.SearchSubscriptionCriteria;
import org.demo.com.subscriptionsapp.api.dto.subscription.Subscription;
import org.demo.com.subscriptionsapp.api.dto.subscription.UpdateSubscription;
import org.demo.com.subscriptionsapp.api.dto.user.CreateUser;
import org.demo.com.subscriptionsapp.api.dto.user.SearchUserCriteria;
import org.demo.com.subscriptionsapp.api.dto.user.UpdateUser;
import org.demo.com.subscriptionsapp.api.dto.user.User;
import org.demo.com.subscriptionsapp.domain.enums.BenefitType;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionPlan;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;
import org.demo.com.subscriptionsapp.service.BenefitService;
import org.demo.com.subscriptionsapp.service.MembershipService;
import org.demo.com.subscriptionsapp.service.OrderService;
import org.demo.com.subscriptionsapp.service.SubscriptionService;
import org.demo.com.subscriptionsapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ControllerTest {

    private final UserService userService = mock(UserService.class);
    private final SubscriptionService subscriptionService = mock(SubscriptionService.class);
    private final OrderService orderService = mock(OrderService.class);
    private final MembershipService membershipService = mock(MembershipService.class);
    private final BenefitService benefitService = mock(BenefitService.class);

    private final UserController userController = new UserController(userService);
    private final SubscriptionController subscriptionController = new SubscriptionController(subscriptionService);
    private final OrderController orderController = new OrderController(orderService);
    private final MembershipController membershipController = new MembershipController(membershipService);
    private final BenefitController benefitController = new BenefitController(benefitService);

    @Test
    void userEndpointsDelegateAndUseTheDocumentedStatuses() {
        CreateUser createUser = CreateUser.builder().username("ada").build();
        UpdateUser updateUser = UpdateUser.builder().userAccountStatus(UserAccountStatus.DEACTIVATED).build();
        SearchUserCriteria criteria = new SearchUserCriteria();
        User user = User.builder().id(1L).username("ada").build();
        Page<User> page = Page.empty();
        when(userService.createUser(createUser)).thenReturn(user);
        when(userService.updateUser(1L, updateUser)).thenReturn(user);
        when(userService.listUser(criteria)).thenReturn(page);
        when(userService.getUser(1L)).thenReturn(user);

        assertEquals(HttpStatus.CREATED, userController.createUser(createUser).getStatusCode());
        assertEquals(HttpStatus.OK, userController.updateUser(1L, updateUser).getStatusCode());
        assertSame(page, userController.listUser(criteria).getBody());
        assertSame(user, userController.getUser(1L).getBody());
    }

    @Test
    void subscriptionEndpointsDelegate() {
        CreateSubscription create = CreateSubscription.builder()
                .subscriptionName("Gold")
                .subscriptionTier(SubscriptionTier.GOLD)
                .subscriptionPlan(SubscriptionPlan.MONTHLY)
                .price(10)
                .build();
        UpdateSubscription update = UpdateSubscription.builder().price(11L).build();
        SearchSubscriptionCriteria criteria = new SearchSubscriptionCriteria();
        Subscription subscription = Subscription.builder().id(7L).subscriptionName("Gold").build();
        when(subscriptionService.createSubscription(create)).thenReturn(subscription);
        when(subscriptionService.updateSubscription(7L, update)).thenReturn(subscription);
        when(subscriptionService.getAllSubscriptions(criteria)).thenReturn(Page.empty());
        when(subscriptionService.getSubscriptionById(7L)).thenReturn(subscription);

        assertEquals(HttpStatus.CREATED, subscriptionController.createSubscription(create).getStatusCode());
        assertSame(subscription, subscriptionController.updateSubscription(7L, update).getBody());
        assertEquals(HttpStatus.OK, subscriptionController.listSubscriptions(criteria).getStatusCode());
        assertSame(subscription, subscriptionController.getSubscription(7L).getBody());
    }

    @Test
    void orderEndpointsDelegate() {
        CreateOrder create = CreateOrder.builder().userId(2L).amount(640).build();
        UpdateOrder update = UpdateOrder.builder().build();
        SearchOrderCriteria criteria = new SearchOrderCriteria();
        Order order = Order.builder().id(3L).userId(2L).amount(640).build();
        when(orderService.createOrder(create)).thenReturn(order);
        when(orderService.updateOrder(3L, update)).thenReturn(order);
        when(orderService.listOrder(criteria)).thenReturn(Page.empty());
        when(orderService.getOrder(3L)).thenReturn(order);

        assertEquals(HttpStatus.CREATED, orderController.createOrder(create).getStatusCode());
        assertSame(order, orderController.updateOrder(3L, update).getBody());
        assertEquals(HttpStatus.OK, orderController.listOrder(criteria).getStatusCode());
        assertSame(order, orderController.getOrder(3L).getBody());
    }

    @Test
    void membershipEndpointsDelegateIncludingTierActions() {
        CreateMembership create = CreateMembership.builder().userId(2L).subscriptionId(6L).build();
        SearchMembershipCriteria criteria = new SearchMembershipCriteria();
        Membership membership = Membership.builder().id(2L).userId(2L).subscriptionId(6L).build();
        when(membershipService.createMembership(create)).thenReturn(membership);
        when(membershipService.renewMembership(2L)).thenReturn(membership);
        when(membershipService.upgradeMembership(2L)).thenReturn(membership);
        when(membershipService.downgradeMembership(2L)).thenReturn(membership);
        when(membershipService.cancelMemberShip(2L)).thenReturn(membership);
        when(membershipService.listMemberships(criteria)).thenReturn(Page.empty());
        when(membershipService.getMembership(2L)).thenReturn(membership);

        assertEquals(HttpStatus.CREATED, membershipController.createMembership(create).getStatusCode());
        assertSame(membership, membershipController.renewMembership(2L).getBody());
        assertSame(membership, membershipController.upgradeMembership(2L).getBody());
        assertSame(membership, membershipController.downgradeMembership(2L).getBody());
        assertSame(membership, membershipController.cancelMemberShip(2L).getBody());
        assertEquals(HttpStatus.OK, membershipController.listMembership(criteria).getStatusCode());
        assertSame(membership, membershipController.getMembership(2L).getBody());
        verify(membershipService).upgradeMembership(2L);
        verify(membershipService).downgradeMembership(2L);
    }

    @Test
    void benefitEndpointsDelegateAndDeleteReturnsNoContent() {
        CreateBenefit create = CreateBenefit.builder()
                .subscriptionId(3L)
                .benefitType(BenefitType.FREE_DELIVERY)
                .name("Free delivery")
                .build();
        UpdateBenefit update = UpdateBenefit.builder().discountPercent(10).build();
        SearchBenefitCriteria criteria = new SearchBenefitCriteria();
        Benefit benefit = Benefit.builder().id(8L).name("Free delivery").build();
        when(benefitService.createBenefit(create)).thenReturn(benefit);
        when(benefitService.updateBenefit(8L, update)).thenReturn(benefit);
        when(benefitService.listBenefits(criteria)).thenReturn(Page.empty());
        when(benefitService.getBenefit(8L)).thenReturn(benefit);

        assertEquals(HttpStatus.CREATED, benefitController.createBenefit(create).getStatusCode());
        assertSame(benefit, benefitController.updateBenefit(8L, update).getBody());
        assertEquals(HttpStatus.NO_CONTENT, benefitController.deleteBenefit(8L).getStatusCode());
        assertEquals(HttpStatus.OK, benefitController.listBenefits(criteria).getStatusCode());
        assertSame(benefit, benefitController.getBenefit(8L).getBody());
        verify(benefitService).deleteBenefit(8L);
    }
}
