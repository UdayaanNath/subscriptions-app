package org.demo.com.subscriptionsapp.service;

import org.demo.com.subscriptionsapp.api.dto.benefit.Benefit;
import org.demo.com.subscriptionsapp.api.dto.benefit.CreateBenefit;
import org.demo.com.subscriptionsapp.api.dto.benefit.SearchBenefitCriteria;
import org.demo.com.subscriptionsapp.api.dto.benefit.UpdateBenefit;
import org.demo.com.subscriptionsapp.api.error.NotFoundException;
import org.demo.com.subscriptionsapp.domain.entity.BenefitEntity;
import org.demo.com.subscriptionsapp.domain.entity.MembershipEntity;
import org.demo.com.subscriptionsapp.domain.enums.BenefitType;
import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;
import org.demo.com.subscriptionsapp.repository.BenefitRepository;
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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BenefitServiceTest {

    @Mock
    private BenefitRepository benefitRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MembershipRepository membershipRepository;
    @InjectMocks
    private BenefitService benefitService;

    @Test
    void listRejectsAnUnknownUser() {
        when(userRepository.existsById(99L)).thenReturn(false);
        SearchBenefitCriteria criteria = criteria(99L);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> benefitService.listBenefits(criteria));

        assertEquals("User not found: 99", exception.getReason());
        verify(membershipRepository, never()).findByUserIdAndMembershipStatusAndExpireAtAfter(any(), any(), any());
    }

    @Test
    void listReturnsAnEmptyPageWhenTheUserHasNoActiveUnexpiredMembership() {
        when(userRepository.existsById(4L)).thenReturn(true);
        when(membershipRepository.findByUserIdAndMembershipStatusAndExpireAtAfter(
                eq(4L), eq(MembershipStatus.ACTIVE), any(Date.class))).thenReturn(List.of());

        Page<Benefit> page = benefitService.listBenefits(criteria(4L));

        assertTrue(page.isEmpty());
        verify(benefitRepository, never()).findBySubscriptionIdIn(any(), any());
    }

    @Test
    void listUsesDistinctSubscriptionIdsFromActiveMemberships() {
        when(userRepository.existsById(5L)).thenReturn(true);
        when(membershipRepository.findByUserIdAndMembershipStatusAndExpireAtAfter(
                eq(5L), eq(MembershipStatus.ACTIVE), any(Date.class))).thenReturn(List.of(
                membership(11L), membership(11L), membership(12L)));
        when(benefitRepository.findBySubscriptionIdIn(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(benefit(1L, 11L, BenefitType.FREE_DELIVERY,  null))));

        Page<Benefit> page = benefitService.listBenefits(criteria(5L));

        assertEquals(1, page.getTotalElements());
        assertEquals(BenefitType.FREE_DELIVERY, page.getContent().get(0).getBenefitType());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Long>> ids = ArgumentCaptor.forClass(Collection.class);
        verify(benefitRepository).findBySubscriptionIdIn(ids.capture(), any(Pageable.class));
        assertEquals(List.of(11L, 12L), List.copyOf(ids.getValue()));
    }

    @Test
    void getRejectsAnUnknownBenefit() {
        when(benefitRepository.findById(9L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> benefitService.getBenefit(9L));

        assertEquals("Benefit not found: 9", exception.getReason());
    }

    @Test
    void createRejectsAnUnknownSubscription() {
        when(subscriptionRepository.existsById(40L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> benefitService.createBenefit(CreateBenefit.builder()
                        .subscriptionId(40L)
                        .benefitType(BenefitType.PRIORITY_SUPPORT)
                        .name("Priority support")
                        .build()));

        assertEquals("Subscription not found: 40", exception.getReason());
        verify(benefitRepository, never()).save(any());
    }

    @Test
    void createStoresABenefitWithoutADiscount() {
        when(subscriptionRepository.existsById(3L)).thenReturn(true);
        when(benefitRepository.save(any())).thenAnswer(invocation -> {
            BenefitEntity entity = invocation.getArgument(0);
            entity.setUpdatedAt(new Date());
            return BenefitEntity.builder()
                    .id(30L)
                    .subscriptionId(entity.getSubscriptionId())
                    .benefitType(entity.getBenefitType())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .discountPercent(entity.getDiscountPercent())
                    .build();
        });

        Benefit created = benefitService.createBenefit(CreateBenefit.builder()
                .subscriptionId(3L)
                .benefitType(BenefitType.FREE_DELIVERY)
                .name("Free delivery")
                .description("Free delivery on eligible orders")
                .build());

        assertEquals(30L, created.getId());
        assertEquals(null, created.getDiscountPercent());
        assertEquals("Free delivery", created.getName());
    }

    @Test
    void updateChangesOnlyPresentFields() {
        BenefitEntity entity = benefit(8L, 3L, BenefitType.FREE_DELIVERY, null);
        entity.setName("Free delivery");
        entity.setDescription("old");
        when(benefitRepository.findById(8L)).thenReturn(Optional.of(entity));

        Benefit unchanged = benefitService.updateBenefit(8L, UpdateBenefit.builder().build());
        assertEquals(BenefitType.FREE_DELIVERY, unchanged.getBenefitType());
        assertEquals("Free delivery", unchanged.getName());
        assertEquals("old", unchanged.getDescription());

        Benefit updated = benefitService.updateBenefit(8L, UpdateBenefit.builder()
                .benefitType(BenefitType.DISCOUNT)
                .name("Gold discount")
                .description("10 percent")
                .discountPercent(0)
                .build());
        assertEquals(BenefitType.DISCOUNT, updated.getBenefitType());
        assertEquals("Gold discount", updated.getName());
        assertEquals("10 percent", updated.getDescription());
        assertEquals(0, updated.getDiscountPercent());
    }

    @Test
    void updateRejectsAnUnknownBenefit() {
        when(benefitRepository.findById(8L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> benefitService.updateBenefit(8L, UpdateBenefit.builder().name("x").build()));

        assertEquals("Benefit not found: 8", exception.getReason());
    }

    @Test
    void deleteRejectsAnUnknownBenefitAndDeletesAKnownOne() {
        when(benefitRepository.existsById(8L)).thenReturn(false);
        assertEquals("Benefit not found: 8",
                assertThrows(NotFoundException.class, () -> benefitService.deleteBenefit(8L)).getReason());
        verify(benefitRepository, never()).deleteById(any());

        when(benefitRepository.existsById(8L)).thenReturn(true);
        benefitService.deleteBenefit(8L);
        verify(benefitRepository).deleteById(8L);
    }

    private static SearchBenefitCriteria criteria(Long userId) {
        SearchBenefitCriteria criteria = new SearchBenefitCriteria();
        criteria.setUserId(userId);
        return criteria;
    }

    private static MembershipEntity membership(Long subscriptionId) {
        return MembershipEntity.builder()
                .id(subscriptionId)
                .userId(5L)
                .subscriptionId(subscriptionId)
                .membershipStatus(MembershipStatus.ACTIVE)
                .expireAt(new Date(System.currentTimeMillis() + 86_400_000L))
                .build();
    }

    private static BenefitEntity benefit(Long id, Long subscriptionId, BenefitType type, Integer discount) {
        return BenefitEntity.builder()
                .id(id)
                .subscriptionId(subscriptionId)
                .benefitType(type)
                .name(type.name())
                .discountPercent(discount)
                .build();
    }
}
