package org.demo.com.subscriptionsapp.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.demo.com.subscriptionsapp.api.dto.membership.CreateMembership;
import org.demo.com.subscriptionsapp.api.dto.membership.Membership;
import org.demo.com.subscriptionsapp.api.dto.membership.SearchMembershipCriteria;
import org.demo.com.subscriptionsapp.api.error.BadRequestException;
import org.demo.com.subscriptionsapp.api.error.NotFoundException;
import org.demo.com.subscriptionsapp.config.SubscriptionConfig;
import org.demo.com.subscriptionsapp.domain.entity.MembershipEntity;
import org.demo.com.subscriptionsapp.domain.entity.SubscriptionEntity;
import org.demo.com.subscriptionsapp.domain.entity.UserEntity;
import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;
import org.demo.com.subscriptionsapp.domain.enums.OrderStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionPlan;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;
import org.demo.com.subscriptionsapp.domain.enums.UserCohort;
import org.demo.com.subscriptionsapp.domain.specification.UpgradeContext;
import org.demo.com.subscriptionsapp.domain.specification.UpgradeRule;
import org.demo.com.subscriptionsapp.domain.specification.UpgradeRuleFactory;
import org.demo.com.subscriptionsapp.repository.MembershipRepository;
import org.demo.com.subscriptionsapp.repository.OrderRepository;
import org.demo.com.subscriptionsapp.repository.SubscriptionRepository;
import org.demo.com.subscriptionsapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock
    private MembershipRepository membershipRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private SubscriptionConfig subscriptionConfig;
    @Mock
    private UpgradeRuleFactory upgradeRuleFactory;
    @InjectMocks
    private MembershipService membershipService;

    @BeforeEach
    void tierOrder() {
        lenient().when(subscriptionConfig.getTierOrder()).thenReturn(List.of(
                SubscriptionTier.FREE, SubscriptionTier.SILVER, SubscriptionTier.GOLD, SubscriptionTier.PREMIUM));
    }

    @Test
    void listFiltersUserSubscriptionAndStatus() {
        when(membershipRepository.findAll(org.mockito.ArgumentMatchers.<Specification<MembershipEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(membership(1L, 1L, 3L, MembershipStatus.ACTIVE))));
        SearchMembershipCriteria criteria = new SearchMembershipCriteria();
        criteria.setUserId(1L);
        criteria.setSubscriptionId(3L);
        criteria.setMembershipStatus(MembershipStatus.ACTIVE);

        Membership listed = membershipService.listMemberships(criteria).getContent().get(0);

        assertEquals(1L, listed.getId());
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Predicate predicate = mock(Predicate.class);
        when(criteriaBuilder.equal(any(), org.mockito.ArgumentMatchers.<Object>any())).thenReturn(predicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        @SuppressWarnings("unchecked")
        Root<MembershipEntity> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        Path<Object> path = mock(Path.class);
        when(root.get(any(String.class))).thenReturn(path);
        capture().toPredicate(root, query(), criteriaBuilder);
        verify(criteriaBuilder).equal(path, 1L);
        verify(criteriaBuilder).equal(path, 3L);
        verify(criteriaBuilder).equal(path, MembershipStatus.ACTIVE);
    }

    @Test
    void listWithNoFiltersUsesAConjunction() {
        when(membershipRepository.findAll(org.mockito.ArgumentMatchers.<Specification<MembershipEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        SearchMembershipCriteria criteria = new SearchMembershipCriteria();
        criteria.setMembershipStatus(null);

        membershipService.listMemberships(criteria);

        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        when(criteriaBuilder.conjunction()).thenReturn(mock(Predicate.class));
        capture().toPredicate(root(), query(), criteriaBuilder);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void getRejectsAnUnknownId() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> membershipService.getMembership(1L));

        assertEquals("Membership not found: 1", exception.getReason());
    }

    @Test
    void createRejectsAMissingUser() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> membershipService.createMembership(request(9L, 4L)));

        assertEquals("User not found: 9", exception.getReason());
    }

    @Test
    void createRejectsADeactivatedUser() {
        when(userRepository.findById(4L)).thenReturn(Optional.of(user(4L, UserAccountStatus.DEACTIVATED, UserCohort.STANDARD)));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> membershipService.createMembership(request(4L, 4L)));

        assertEquals("User is deactivated: 4", exception.getReason());
    }

    @Test
    void createRejectsAMissingSubscription() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserAccountStatus.ACTIVE, UserCohort.EARLY_ADOPTER)));
        when(subscriptionRepository.findById(40L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> membershipService.createMembership(request(2L, 40L)));

        assertEquals("Subscription not found: 40", exception.getReason());
    }

    @Test
    void createRejectsAStoppedSubscription() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserAccountStatus.ACTIVE, UserCohort.EARLY_ADOPTER)));
        when(subscriptionRepository.findById(6L)).thenReturn(Optional.of(
                plan(6L, SubscriptionTier.SILVER, SubscriptionPlan.YEARLY, SubscriptionStatus.STOPPED)));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> membershipService.createMembership(request(2L, 6L)));

        assertEquals("Subscription is stopped: 6", exception.getReason());
    }

    @Test
    void createCancelsEveryExistingMembershipAndSetsExpiryFromThePlan() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserAccountStatus.ACTIVE, UserCohort.EARLY_ADOPTER)));
        when(subscriptionRepository.findById(6L)).thenReturn(Optional.of(
                plan(6L, SubscriptionTier.SILVER, SubscriptionPlan.YEARLY, SubscriptionStatus.ACTIVE)));
        MembershipEntity previous = membership(2L, 2L, 4L, MembershipStatus.ACTIVE);
        MembershipEntity expired = membership(6L, 2L, 1L, MembershipStatus.EXPIRED);
        when(membershipRepository.findByUserId(2L)).thenReturn(List.of(previous, expired));
        when(membershipRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Membership created = membershipService.createMembership(request(2L, 6L));

        assertEquals(MembershipStatus.CANCELLED, previous.getMembershipStatus());
        assertEquals(MembershipStatus.CANCELLED, expired.getMembershipStatus());
        assertEquals(MembershipStatus.ACTIVE, created.getMembershipStatus());
        assertEquals(6L, created.getSubscriptionId());
        long days = (created.getExpireAt().getTime() - System.currentTimeMillis()) / 86_400_000L;
        assertTrue(days >= 364 && days <= 365);
    }

    @Test
    void renewRejectsAMissingMembershipAndACancelledOne() {
        when(membershipRepository.findById(4L)).thenReturn(Optional.empty());
        assertEquals("Membership not found: 4",
                assertThrows(NotFoundException.class, () -> membershipService.renewMembership(4L)).getReason());

        when(membershipRepository.findById(4L)).thenReturn(Optional.of(membership(4L, 4L, 6L, MembershipStatus.CANCELLED)));
        assertEquals("Membership is already cancelled: 4",
                assertThrows(BadRequestException.class, () -> membershipService.renewMembership(4L)).getReason());
    }

    @Test
    void renewRejectsAMissingOrStoppedSubscription() {
        when(membershipRepository.findById(2L)).thenReturn(Optional.of(membership(2L, 2L, 4L, MembershipStatus.ACTIVE)));
        when(subscriptionRepository.findById(4L)).thenReturn(Optional.empty());
        assertEquals("Subscription not found: 4",
                assertThrows(NotFoundException.class, () -> membershipService.renewMembership(2L)).getReason());

        when(subscriptionRepository.findById(4L)).thenReturn(Optional.of(
                plan(4L, SubscriptionTier.SILVER, SubscriptionPlan.MONTHLY, SubscriptionStatus.STOPPED)));
        assertEquals("Subscription is stopped: 4",
                assertThrows(BadRequestException.class, () -> membershipService.renewMembership(2L)).getReason());
    }

    @Test
    void renewExtendsAnExpiredMembershipFromToday() {
        MembershipEntity expired = membership(2L, 2L, 4L, MembershipStatus.EXPIRED);
        when(membershipRepository.findById(2L)).thenReturn(Optional.of(expired));
        when(subscriptionRepository.findById(4L)).thenReturn(Optional.of(
                plan(4L, SubscriptionTier.SILVER, SubscriptionPlan.MONTHLY, SubscriptionStatus.ACTIVE)));

        Membership renewed = membershipService.renewMembership(2L);

        assertEquals(MembershipStatus.EXPIRED, renewed.getMembershipStatus());
        long days = (renewed.getExpireAt().getTime() - System.currentTimeMillis()) / 86_400_000L;
        assertTrue(days >= 29 && days <= 30);
    }

    @Test
    void upgradeRejectsInactiveMembershipsDeactivatedUsersAndStoppedPlans() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.empty());
        assertEquals("Membership not found: 1",
                assertThrows(NotFoundException.class, () -> membershipService.upgradeMembership(1L)).getReason());

        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership(1L, 1L, 3L, MembershipStatus.EXPIRED)));
        assertEquals("Membership is not active: 1",
                assertThrows(BadRequestException.class, () -> membershipService.upgradeMembership(1L)).getReason());

        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership(1L, 1L, 3L, MembershipStatus.ACTIVE)));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertEquals("User not found: 1",
                assertThrows(NotFoundException.class, () -> membershipService.upgradeMembership(1L)).getReason());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserAccountStatus.DEACTIVATED, UserCohort.STANDARD)));
        assertEquals("User is deactivated: 1",
                assertThrows(BadRequestException.class, () -> membershipService.upgradeMembership(1L)).getReason());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserAccountStatus.ACTIVE, UserCohort.STANDARD)));
        when(subscriptionRepository.findById(3L)).thenReturn(Optional.empty());
        assertEquals("Subscription not found: 3",
                assertThrows(NotFoundException.class, () -> membershipService.upgradeMembership(1L)).getReason());

        when(subscriptionRepository.findById(3L)).thenReturn(Optional.of(
                plan(3L, SubscriptionTier.FREE, SubscriptionPlan.YEARLY, SubscriptionStatus.STOPPED)));
        assertEquals("Subscription is stopped: 3",
                assertThrows(BadRequestException.class, () -> membershipService.upgradeMembership(1L)).getReason());
    }

    @Test
    void upgradeRejectsTheTopTierAndAMemberWhoMissesTheNextRules() {
        MembershipEntity membership = membership(5L, 5L, 12L, MembershipStatus.ACTIVE);
        when(membershipRepository.findById(5L)).thenReturn(Optional.of(membership));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L, UserAccountStatus.ACTIVE, UserCohort.EMPLOYEE)));
        when(subscriptionRepository.findById(12L)).thenReturn(Optional.of(
                plan(12L, SubscriptionTier.PREMIUM, SubscriptionPlan.YEARLY, SubscriptionStatus.ACTIVE)));

        assertEquals("Membership is already at the highest tier",
                assertThrows(BadRequestException.class, () -> membershipService.upgradeMembership(5L)).getReason());

        when(subscriptionRepository.findById(3L)).thenReturn(Optional.of(
                plan(3L, SubscriptionTier.FREE, SubscriptionPlan.YEARLY, SubscriptionStatus.ACTIVE)));
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership(1L, 1L, 3L, MembershipStatus.ACTIVE)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserAccountStatus.ACTIVE, UserCohort.STANDARD)));
        UpgradeRule silver = mock(UpgradeRule.class);
        when(upgradeRuleFactory.ruleFor(SubscriptionTier.SILVER)).thenReturn(silver);
        when(silver.isSatisfiedBy(any(UpgradeContext.class))).thenReturn(false);
        when(orderRepository.countByUserIdAndOrderStatus(1L, OrderStatus.DELIVERED)).thenReturn(0L);
        when(orderRepository.sumAmountByUserIdAndOrderStatusAndCreatedAtBetween(eq(1L), eq(OrderStatus.DELIVERED), any(), any()))
                .thenReturn(0L);

        assertEquals("User is not eligible for tier SILVER",
                assertThrows(BadRequestException.class, () -> membershipService.upgradeMembership(1L)).getReason());
    }

    @Test
    void upgradeMovesOneTierWhenTheNextRulesPass() {
        MembershipEntity membership = membership(2L, 2L, 4L, MembershipStatus.ACTIVE);
        when(membershipRepository.findById(2L)).thenReturn(Optional.of(membership));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserAccountStatus.ACTIVE, UserCohort.EARLY_ADOPTER)));
        when(subscriptionRepository.findById(4L)).thenReturn(Optional.of(
                plan(4L, SubscriptionTier.SILVER, SubscriptionPlan.MONTHLY, SubscriptionStatus.ACTIVE)));
        UpgradeRule gold = mock(UpgradeRule.class);
        when(upgradeRuleFactory.ruleFor(SubscriptionTier.GOLD)).thenReturn(gold);
        when(gold.isSatisfiedBy(any(UpgradeContext.class))).thenReturn(true);
        when(orderRepository.countByUserIdAndOrderStatus(2L, OrderStatus.DELIVERED)).thenReturn(16L);
        when(orderRepository.sumAmountByUserIdAndOrderStatusAndCreatedAtBetween(eq(2L), eq(OrderStatus.DELIVERED), any(), any()))
                .thenReturn(5000L);
        when(subscriptionRepository.findFirstBySubscriptionTierAndSubscriptionStatus(SubscriptionTier.GOLD, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(plan(7L, SubscriptionTier.GOLD, SubscriptionPlan.MONTHLY, SubscriptionStatus.ACTIVE)));

        Membership upgraded = membershipService.upgradeMembership(2L);

        assertEquals(7L, upgraded.getSubscriptionId());
        ArgumentCaptor<Date> start = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> end = ArgumentCaptor.forClass(Date.class);
        verify(orderRepository).sumAmountByUserIdAndOrderStatusAndCreatedAtBetween(
                eq(2L), eq(OrderStatus.DELIVERED), start.capture(), end.capture());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start.getValue());
        assertEquals(1, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        assertTrue(end.getValue().after(start.getValue()));
    }

    @Test
    void upgradeRejectsWhenTheNextTierHasNoActivePlan() {
        MembershipEntity membership = membership(2L, 2L, 4L, MembershipStatus.ACTIVE);
        when(membershipRepository.findById(2L)).thenReturn(Optional.of(membership));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserAccountStatus.ACTIVE, UserCohort.EARLY_ADOPTER)));
        when(subscriptionRepository.findById(4L)).thenReturn(Optional.of(
                plan(4L, SubscriptionTier.SILVER, SubscriptionPlan.MONTHLY, SubscriptionStatus.ACTIVE)));
        UpgradeRule gold = mock(UpgradeRule.class);
        when(upgradeRuleFactory.ruleFor(SubscriptionTier.GOLD)).thenReturn(gold);
        when(gold.isSatisfiedBy(any(UpgradeContext.class))).thenReturn(true);
        when(orderRepository.countByUserIdAndOrderStatus(2L, OrderStatus.DELIVERED)).thenReturn(16L);
        when(orderRepository.sumAmountByUserIdAndOrderStatusAndCreatedAtBetween(eq(2L), eq(OrderStatus.DELIVERED), any(), any()))
                .thenReturn(5000L);
        when(subscriptionRepository.findFirstBySubscriptionTierAndSubscriptionStatus(SubscriptionTier.GOLD, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertEquals("Active subscription not found for tier GOLD",
                assertThrows(NotFoundException.class, () -> membershipService.upgradeMembership(2L)).getReason());
    }

    @Test
    void downgradeRejectsTheLowestTierAndAMemberWhoStillQualifies() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership(1L, 1L, 3L, MembershipStatus.ACTIVE)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserAccountStatus.ACTIVE, UserCohort.STANDARD)));
        when(subscriptionRepository.findById(3L)).thenReturn(Optional.of(
                plan(3L, SubscriptionTier.FREE, SubscriptionPlan.YEARLY, SubscriptionStatus.ACTIVE)));

        assertEquals("Membership is already at the lowest tier",
                assertThrows(BadRequestException.class, () -> membershipService.downgradeMembership(1L)).getReason());

        when(membershipRepository.findById(2L)).thenReturn(Optional.of(membership(2L, 2L, 4L, MembershipStatus.ACTIVE)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserAccountStatus.ACTIVE, UserCohort.EARLY_ADOPTER)));
        when(subscriptionRepository.findById(4L)).thenReturn(Optional.of(
                plan(4L, SubscriptionTier.SILVER, SubscriptionPlan.MONTHLY, SubscriptionStatus.ACTIVE)));
        UpgradeRule silver = mock(UpgradeRule.class);
        when(upgradeRuleFactory.ruleFor(SubscriptionTier.SILVER)).thenReturn(silver);
        when(silver.isSatisfiedBy(any(UpgradeContext.class))).thenReturn(true);
        when(orderRepository.countByUserIdAndOrderStatus(2L, OrderStatus.DELIVERED)).thenReturn(6L);
        when(orderRepository.sumAmountByUserIdAndOrderStatusAndCreatedAtBetween(eq(2L), eq(OrderStatus.DELIVERED), any(), any()))
                .thenReturn(1000L);

        assertEquals("User still meets SILVER requirements",
                assertThrows(BadRequestException.class, () -> membershipService.downgradeMembership(2L)).getReason());
    }

    @Test
    void downgradeMovesOneTierWhenCurrentRulesFail() {
        when(membershipRepository.findById(2L)).thenReturn(Optional.of(membership(2L, 2L, 4L, MembershipStatus.ACTIVE)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserAccountStatus.ACTIVE, UserCohort.EARLY_ADOPTER)));
        when(subscriptionRepository.findById(4L)).thenReturn(Optional.of(
                plan(4L, SubscriptionTier.SILVER, SubscriptionPlan.MONTHLY, SubscriptionStatus.ACTIVE)));
        UpgradeRule silver = mock(UpgradeRule.class);
        when(upgradeRuleFactory.ruleFor(SubscriptionTier.SILVER)).thenReturn(silver);
        when(silver.isSatisfiedBy(any(UpgradeContext.class))).thenReturn(false);
        when(orderRepository.countByUserIdAndOrderStatus(2L, OrderStatus.DELIVERED)).thenReturn(1L);
        when(orderRepository.sumAmountByUserIdAndOrderStatusAndCreatedAtBetween(eq(2L), eq(OrderStatus.DELIVERED), any(), any()))
                .thenReturn(0L);
        when(subscriptionRepository.findFirstBySubscriptionTierAndSubscriptionStatus(SubscriptionTier.FREE, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(plan(1L, SubscriptionTier.FREE, SubscriptionPlan.MONTHLY, SubscriptionStatus.ACTIVE)));

        Membership downgraded = membershipService.downgradeMembership(2L);

        assertEquals(1L, downgraded.getSubscriptionId());
        assertEquals(MembershipStatus.ACTIVE, downgraded.getMembershipStatus());
    }

    @Test
    void downgradeTreatsAnUnknownTierAsTheBottom() {
        when(subscriptionConfig.getTierOrder()).thenReturn(List.of(SubscriptionTier.SILVER));
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership(1L, 1L, 3L, MembershipStatus.ACTIVE)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserAccountStatus.ACTIVE, UserCohort.STANDARD)));
        when(subscriptionRepository.findById(3L)).thenReturn(Optional.of(
                plan(3L, SubscriptionTier.FREE, SubscriptionPlan.YEARLY, SubscriptionStatus.ACTIVE)));

        assertEquals("Membership is already at the lowest tier",
                assertThrows(BadRequestException.class, () -> membershipService.downgradeMembership(1L)).getReason());
    }

    @Test
    void cancelRejectsAMissingOrAlreadyCancelledMembershipAndCancelsTheOthers() {
        when(membershipRepository.findById(4L)).thenReturn(Optional.empty());
        assertEquals("Membership not found: 4",
                assertThrows(NotFoundException.class, () -> membershipService.cancelMemberShip(4L)).getReason());

        when(membershipRepository.findById(4L)).thenReturn(Optional.of(membership(4L, 4L, 6L, MembershipStatus.CANCELLED)));
        assertEquals("Membership is already cancelled: 4",
                assertThrows(BadRequestException.class, () -> membershipService.cancelMemberShip(4L)).getReason());

        when(membershipRepository.findById(6L)).thenReturn(Optional.of(membership(6L, 5L, 1L, MembershipStatus.EXPIRED)));
        Membership cancelled = membershipService.cancelMemberShip(6L);
        assertEquals(MembershipStatus.CANCELLED, cancelled.getMembershipStatus());
        verify(membershipRepository, never()).save(any());
    }

    private Specification<MembershipEntity> capture() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Specification<MembershipEntity>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(membershipRepository).findAll(captor.capture(), any(Pageable.class));
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private static Root<MembershipEntity> root() {
        return mock(Root.class);
    }

    @SuppressWarnings("unchecked")
    private static CriteriaQuery<MembershipEntity> query() {
        return mock(CriteriaQuery.class);
    }

    private static CreateMembership request(Long userId, Long subscriptionId) {
        return CreateMembership.builder().userId(userId).subscriptionId(subscriptionId).build();
    }

    private static UserEntity user(Long id, UserAccountStatus status, UserCohort cohort) {
        return UserEntity.builder().id(id).username("user" + id).userAccountStatus(status).cohort(cohort).build();
    }

    private static MembershipEntity membership(Long id, Long userId, Long subscriptionId, MembershipStatus status) {
        return MembershipEntity.builder()
                .id(id)
                .userId(userId)
                .subscriptionId(subscriptionId)
                .membershipStatus(status)
                .expireAt(new Date())
                .build();
    }

    private static SubscriptionEntity plan(Long id, SubscriptionTier tier, SubscriptionPlan plan, SubscriptionStatus status) {
        return SubscriptionEntity.builder()
                .id(id)
                .subscriptionName(tier.name())
                .subscriptionTier(tier)
                .subscriptionPlan(plan)
                .price(10)
                .subscriptionStatus(status)
                .build();
    }
}
