package org.demo.com.subscriptionsapp.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.demo.com.subscriptionsapp.api.dto.user.CreateUser;
import org.demo.com.subscriptionsapp.api.dto.user.SearchUserCriteria;
import org.demo.com.subscriptionsapp.api.dto.user.UpdateUser;
import org.demo.com.subscriptionsapp.api.dto.user.User;
import org.demo.com.subscriptionsapp.api.error.BadRequestException;
import org.demo.com.subscriptionsapp.api.error.NotFoundException;
import org.demo.com.subscriptionsapp.config.SubscriptionConfig;
import org.demo.com.subscriptionsapp.domain.entity.MembershipEntity;
import org.demo.com.subscriptionsapp.domain.entity.SubscriptionEntity;
import org.demo.com.subscriptionsapp.domain.entity.UserEntity;
import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionPlan;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;
import org.demo.com.subscriptionsapp.domain.enums.UserCohort;
import org.demo.com.subscriptionsapp.repository.MembershipRepository;
import org.demo.com.subscriptionsapp.repository.SubscriptionRepository;
import org.demo.com.subscriptionsapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MembershipRepository membershipRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private SubscriptionConfig subscriptionConfig;
    @InjectMocks
    private UserService userService;

    @Test
    void listMapsUsersAndIgnoresABlankUsername() {
        UserEntity alice = user(1L, "alice", UserAccountStatus.ACTIVE);
        when(userRepository.findAll(org.mockito.ArgumentMatchers.<Specification<UserEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alice)));

        SearchUserCriteria criteria = new SearchUserCriteria();
        criteria.setUsername("   ");
        criteria.setUserAccountStatus(UserAccountStatus.ACTIVE);
        Page<User> page = userService.listUser(criteria);

        assertEquals(1, page.getTotalElements());
        assertEquals("alice", page.getContent().get(0).getUsername());

        Specification<UserEntity> specification = captureSpecification();
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Predicate equal = mock(Predicate.class);
        Predicate combined = mock(Predicate.class);
        @SuppressWarnings("unchecked")
        Root<UserEntity> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        Path<Object> status = mock(Path.class);
        when(root.get("userAccountStatus")).thenReturn(status);
        when(criteriaBuilder.equal(status, UserAccountStatus.ACTIVE)).thenReturn(equal);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(combined);

        specification.toPredicate(root, query(), criteriaBuilder);

        verify(criteriaBuilder, never()).like(any(), anyString());
        verify(criteriaBuilder).equal(status, UserAccountStatus.ACTIVE);
    }

    @Test
    void listWithNoFiltersUsesAConjunction() {
        when(userRepository.findAll(org.mockito.ArgumentMatchers.<Specification<UserEntity>>any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        SearchUserCriteria criteria = new SearchUserCriteria();
        criteria.setUsername(null);
        criteria.setUserAccountStatus(null);

        userService.listUser(criteria);

        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);
        captureSpecification().toPredicate(root(), query(), criteriaBuilder);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void getUserReturnsTheMatch() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "alice", UserAccountStatus.ACTIVE)));

        User user = userService.getUser(1L);

        assertEquals(1L, user.getId());
        assertEquals("alice", user.getUsername());
    }

    @Test
    void getUserRejectsAnUnknownId() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUser(99L));

        assertEquals("User not found: 99", exception.getReason());
    }

    @Test
    void createUserRejectsADuplicateUsername() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.createUser(CreateUser.builder().username("alice").build()));

        assertEquals("Username already exists: alice", exception.getReason());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserRejectsSignupWhenNoFreePlanIsActive() {
        when(userRepository.existsByUsername("new")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(invocation -> withId(invocation.getArgument(0), 8L));
        when(subscriptionRepository.findFirstBySubscriptionTierAndSubscriptionStatus(
                SubscriptionTier.FREE, SubscriptionStatus.ACTIVE)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.createUser(CreateUser.builder().username("new").build()));

        assertEquals("Free subscription not found", exception.getReason());
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void createUserStartsAnActiveStandardUserOnTheFreePlan() {
        when(userRepository.existsByUsername("new")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(invocation -> withId(invocation.getArgument(0), 8L));
        when(subscriptionConfig.getDefaultFreeTierDays()).thenReturn(36500);
        when(subscriptionRepository.findFirstBySubscriptionTierAndSubscriptionStatus(
                SubscriptionTier.FREE, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(subscription(3L, SubscriptionTier.FREE)));

        User created = userService.createUser(CreateUser.builder().username("new").build());

        assertEquals(8L, created.getId());
        assertEquals(UserAccountStatus.ACTIVE, created.getUserAccountStatus());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(UserCohort.STANDARD, userCaptor.getValue().getCohort());

        ArgumentCaptor<MembershipEntity> membershipCaptor = ArgumentCaptor.forClass(MembershipEntity.class);
        verify(membershipRepository).save(membershipCaptor.capture());
        MembershipEntity membership = membershipCaptor.getValue();
        assertEquals(8L, membership.getUserId());
        assertEquals(3L, membership.getSubscriptionId());
        assertEquals(MembershipStatus.ACTIVE, membership.getMembershipStatus());
        long days = (membership.getExpireAt().getTime() - System.currentTimeMillis()) / 86_400_000L;
        assertTrue(days >= 36499 && days <= 36500);
    }

    @Test
    void updateUserRejectsAnUnknownId() {
        when(userRepository.findById(4L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUser(4L, UpdateUser.builder().userAccountStatus(UserAccountStatus.ACTIVE).build()));

        assertEquals("User not found: 4", exception.getReason());
    }

    @Test
    void updateUserRejectsASecondDeactivation() {
        when(userRepository.findById(4L)).thenReturn(Optional.of(user(4L, "dave", UserAccountStatus.DEACTIVATED)));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> userService.updateUser(4L, UpdateUser.builder().userAccountStatus(UserAccountStatus.DEACTIVATED).build()));

        assertEquals("User already deactivated: dave", exception.getReason());
        verify(membershipRepository, never()).findByUserId(any());
    }

    @Test
    void updateToActiveDoesNotCancelMemberships() {
        UserEntity alice = user(1L, "alice", UserAccountStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));

        User updated = userService.updateUser(1L, UpdateUser.builder().userAccountStatus(UserAccountStatus.ACTIVE).build());

        assertEquals(UserAccountStatus.ACTIVE, updated.getUserAccountStatus());
        verify(membershipRepository, never()).findByUserId(any());
    }

    @Test
    void deactivationCancelsOnlyUnexpiredMemberships() {
        UserEntity alice = user(1L, "alice", UserAccountStatus.ACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        MembershipEntity future = membership(MembershipStatus.ACTIVE, daysFromNow(10));
        MembershipEntity alreadyExpired = membership(MembershipStatus.EXPIRED, daysFromNow(10));
        MembershipEntity past = membership(MembershipStatus.ACTIVE, daysFromNow(-2));
        MembershipEntity cancelledFuture = membership(MembershipStatus.CANCELLED, daysFromNow(10));
        when(membershipRepository.findByUserId(1L)).thenReturn(List.of(future, alreadyExpired, past, cancelledFuture));

        User updated = userService.updateUser(1L,
                UpdateUser.builder().userAccountStatus(UserAccountStatus.DEACTIVATED).build());

        assertEquals(UserAccountStatus.DEACTIVATED, updated.getUserAccountStatus());
        assertEquals(MembershipStatus.CANCELLED, future.getMembershipStatus());
        assertEquals(MembershipStatus.CANCELLED, cancelledFuture.getMembershipStatus());
        assertEquals(MembershipStatus.EXPIRED, alreadyExpired.getMembershipStatus());
        assertEquals(MembershipStatus.ACTIVE, past.getMembershipStatus());
    }

    private Specification<UserEntity> captureSpecification() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Specification<UserEntity>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(userRepository).findAll(captor.capture(), any(Pageable.class));
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private static Root<UserEntity> root() {
        return mock(Root.class);
    }

    @SuppressWarnings("unchecked")
    private static CriteriaQuery<UserEntity> query() {
        return mock(CriteriaQuery.class);
    }

    private static UserEntity user(Long id, String username, UserAccountStatus status) {
        return UserEntity.builder()
                .id(id)
                .username(username)
                .userAccountStatus(status)
                .cohort(UserCohort.STANDARD)
                .build();
    }

    private static UserEntity withId(UserEntity entity, Long id) {
        return UserEntity.builder()
                .id(id)
                .username(entity.getUsername())
                .userAccountStatus(entity.getUserAccountStatus())
                .cohort(entity.getCohort())
                .build();
    }

    private static SubscriptionEntity subscription(Long id, SubscriptionTier tier) {
        return SubscriptionEntity.builder()
                .id(id)
                .subscriptionName("Free Yearly")
                .subscriptionTier(tier)
                .subscriptionPlan(SubscriptionPlan.YEARLY)
                .price(0)
                .subscriptionStatus(SubscriptionStatus.ACTIVE)
                .build();
    }

    private static MembershipEntity membership(MembershipStatus status, Date expireAt) {
        return MembershipEntity.builder()
                .id(1L)
                .userId(1L)
                .subscriptionId(3L)
                .membershipStatus(status)
                .expireAt(expireAt)
                .build();
    }

    private static Date daysFromNow(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }
}
