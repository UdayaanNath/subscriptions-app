package org.demo.com.subscriptionsapp.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.demo.com.subscriptionsapp.api.dto.subscription.CreateSubscription;
import org.demo.com.subscriptionsapp.api.dto.subscription.SearchSubscriptionCriteria;
import org.demo.com.subscriptionsapp.api.dto.subscription.Subscription;
import org.demo.com.subscriptionsapp.api.dto.subscription.UpdateSubscription;
import org.demo.com.subscriptionsapp.api.error.NotFoundException;
import org.demo.com.subscriptionsapp.domain.entity.SubscriptionEntity;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionPlan;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;
import org.demo.com.subscriptionsapp.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void listAppliesTierPlanStatusAndPriceBounds() {
        when(subscriptionRepository.findAll(org.mockito.ArgumentMatchers.<Specification<SubscriptionEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(plan(4L, SubscriptionTier.SILVER, 499, SubscriptionStatus.ACTIVE))));
        SearchSubscriptionCriteria criteria = new SearchSubscriptionCriteria();
        criteria.setSubscriptionName(" silver ");
        criteria.setSubscriptionTier(SubscriptionTier.SILVER);
        criteria.setSubscriptionPlan(SubscriptionPlan.MONTHLY);
        criteria.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        criteria.setMinPrice(100L);
        criteria.setMaxPrice(500L);

        Subscription first = subscriptionService.getAllSubscriptions(criteria).getContent().get(0);

        assertEquals(4L, first.getId());
        assertEquals(499, first.getPrice());

        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Predicate predicate = mock(Predicate.class);
        when(criteriaBuilder.like(any(), anyString())).thenReturn(predicate);
        when(criteriaBuilder.lower(any())).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
        when(criteriaBuilder.equal(any(), org.mockito.ArgumentMatchers.<Object>any())).thenReturn(predicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(), any(Long.class))).thenReturn(predicate);
        when(criteriaBuilder.lessThanOrEqualTo(any(), any(Long.class))).thenReturn(predicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        @SuppressWarnings("unchecked")
        Root<SubscriptionEntity> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        Path<Object> path = mock(Path.class);
        when(root.get(anyString())).thenReturn(path);

        capture().toPredicate(root, query(), criteriaBuilder);

        verify(criteriaBuilder).like(any(), org.mockito.ArgumentMatchers.eq("% silver %".toLowerCase()));
        verify(criteriaBuilder).greaterThanOrEqualTo(any(), org.mockito.ArgumentMatchers.eq(100L));
        verify(criteriaBuilder).lessThanOrEqualTo(any(), org.mockito.ArgumentMatchers.eq(500L));
    }

    @Test
    void listSkipsBlankNameAndOmittedPrices() {
        when(subscriptionRepository.findAll(org.mockito.ArgumentMatchers.<Specification<SubscriptionEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        SearchSubscriptionCriteria criteria = new SearchSubscriptionCriteria();
        criteria.setSubscriptionName(" ");
        criteria.setSubscriptionStatus(null);
        criteria.setMinPrice(null);
        criteria.setMaxPrice(null);

        subscriptionService.getAllSubscriptions(criteria);

        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Predicate conjunction = mock(Predicate.class);
        when(criteriaBuilder.conjunction()).thenReturn(conjunction);
        capture().toPredicate(root(), query(), criteriaBuilder);

        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).greaterThanOrEqualTo(any(), any(Long.class));
        verify(criteriaBuilder, never()).like(any(), anyString());
    }

    @Test
    void createStartsThePlanAsActive() {
        when(subscriptionRepository.save(any())).thenAnswer(invocation -> {
            SubscriptionEntity entity = invocation.getArgument(0);
            return plan(11L, entity.getSubscriptionTier(), entity.getPrice(), entity.getSubscriptionStatus());
        });

        Subscription created = subscriptionService.createSubscription(CreateSubscription.builder()
                .subscriptionName("Demo Gold Monthly")
                .subscriptionTier(SubscriptionTier.GOLD)
                .subscriptionPlan(SubscriptionPlan.MONTHLY)
                .price(0)
                .build());

        assertEquals(11L, created.getId());
        assertEquals(SubscriptionStatus.ACTIVE, created.getSubscriptionStatus());
        assertEquals(0, created.getPrice());
    }

    @Test
    void getRejectsAnUnknownId() {
        when(subscriptionRepository.findById(40L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> subscriptionService.getSubscriptionById(40L));

        assertEquals("Subscription not found: 40", exception.getReason());
    }

    @Test
    void updateChangesOnlyTheFieldsThatArePresent() {
        SubscriptionEntity entity = plan(4L, SubscriptionTier.SILVER, 499, SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findById(4L)).thenReturn(Optional.of(entity));

        Subscription unchanged = subscriptionService.updateSubscription(4L, UpdateSubscription.builder().build());
        assertEquals(499, unchanged.getPrice());
        assertEquals(SubscriptionStatus.ACTIVE, unchanged.getSubscriptionStatus());

        Subscription stopped = subscriptionService.updateSubscription(4L, UpdateSubscription.builder()
                .price(0L)
                .subscriptionStatus(SubscriptionStatus.STOPPED)
                .build());
        assertEquals(0, stopped.getPrice());
        assertEquals(SubscriptionStatus.STOPPED, stopped.getSubscriptionStatus());
    }

    @Test
    void updateRejectsAnUnknownId() {
        when(subscriptionRepository.findById(4L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> subscriptionService.updateSubscription(4L, UpdateSubscription.builder().price(1L).build()));

        assertEquals("Subscription not found: 4", exception.getReason());
    }

    private Specification<SubscriptionEntity> capture() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Specification<SubscriptionEntity>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(subscriptionRepository).findAll(captor.capture(), any(Pageable.class));
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private static Root<SubscriptionEntity> root() {
        return mock(Root.class);
    }

    @SuppressWarnings("unchecked")
    private static CriteriaQuery<SubscriptionEntity> query() {
        return mock(CriteriaQuery.class);
    }

    private static SubscriptionEntity plan(Long id, SubscriptionTier tier, long price, SubscriptionStatus status) {
        return SubscriptionEntity.builder()
                .id(id)
                .subscriptionName(tier.name())
                .subscriptionTier(tier)
                .subscriptionPlan(SubscriptionPlan.MONTHLY)
                .price(price)
                .subscriptionStatus(status)
                .build();
    }
}
