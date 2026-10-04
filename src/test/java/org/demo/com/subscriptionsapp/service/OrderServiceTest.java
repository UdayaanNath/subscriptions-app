package org.demo.com.subscriptionsapp.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.demo.com.subscriptionsapp.api.dto.order.CreateOrder;
import org.demo.com.subscriptionsapp.api.dto.order.Order;
import org.demo.com.subscriptionsapp.api.dto.order.SearchOrderCriteria;
import org.demo.com.subscriptionsapp.api.dto.order.UpdateOrder;
import org.demo.com.subscriptionsapp.api.error.BadRequestException;
import org.demo.com.subscriptionsapp.api.error.NotFoundException;
import org.demo.com.subscriptionsapp.domain.entity.OrderEntity;
import org.demo.com.subscriptionsapp.domain.entity.UserEntity;
import org.demo.com.subscriptionsapp.domain.enums.OrderStatus;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;
import org.demo.com.subscriptionsapp.domain.enums.UserCohort;
import org.demo.com.subscriptionsapp.repository.OrderRepository;
import org.demo.com.subscriptionsapp.repository.UserRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private OrderService orderService;

    @Test
    void listFiltersByUserAndStatus() {
        when(orderRepository.findAll(org.mockito.ArgumentMatchers.<Specification<OrderEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order(3L, 2L, 640, OrderStatus.DELIVERED))));
        SearchOrderCriteria criteria = new SearchOrderCriteria();
        criteria.setUserId(2L);
        criteria.setOrderStatus(OrderStatus.DELIVERED);

        Order listed = orderService.listOrder(criteria).getContent().get(0);

        assertEquals(3L, listed.getId());
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        Predicate predicate = mock(Predicate.class);
        when(criteriaBuilder.equal(any(), org.mockito.ArgumentMatchers.<Object>any())).thenReturn(predicate);
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(predicate);
        @SuppressWarnings("unchecked")
        Root<OrderEntity> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        Path<Object> path = mock(Path.class);
        when(root.get(any(String.class))).thenReturn(path);

        capture().toPredicate(root, query(), criteriaBuilder);

        verify(criteriaBuilder).equal(path, 2L);
        verify(criteriaBuilder).equal(path, OrderStatus.DELIVERED);
    }

    @Test
    void listWithNoFiltersUsesAConjunction() {
        when(orderRepository.findAll(org.mockito.ArgumentMatchers.<Specification<OrderEntity>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        SearchOrderCriteria criteria = new SearchOrderCriteria();
        criteria.setUserId(null);
        criteria.setOrderStatus(null);

        orderService.listOrder(criteria);

        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        when(criteriaBuilder.conjunction()).thenReturn(mock(Predicate.class));
        capture().toPredicate(root(), query(), criteriaBuilder);
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).equal(any(), any());
    }

    @Test
    void getOrderRejectsAnUnknownId() {
        when(orderRepository.findById(3L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> orderService.getOrder(3L));

        assertEquals("Order not found: 3", exception.getReason());
    }

    @Test
    void createOrderRejectsAMissingUser() {
        when(userRepository.existsById(99L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> orderService.createOrder(CreateOrder.builder().userId(99L).amount(10).build()));

        assertEquals("User not found: 99", exception.getReason());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrderStartsAtPendingPaymentIncludingAZeroAmount() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            OrderEntity entity = invocation.getArgument(0);
            return order(20L, entity.getUserId(), entity.getAmount(), entity.getOrderStatus());
        });

        Order created = orderService.createOrder(CreateOrder.builder().userId(2L).amount(0).build());

        assertEquals(20L, created.getId());
        assertEquals(0, created.getAmount());
        assertEquals(OrderStatus.PENDING_PAYMENT, created.getOrderStatus());
    }

    @Test
    void updateOrderRejectsAnUnknownOrder() {
        when(orderRepository.findById(9L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> orderService.updateOrder(9L, UpdateOrder.builder().orderStatus(OrderStatus.DELIVERED).build()));

        assertEquals("Order not found: 9", exception.getReason());
    }

    @Test
    void updateOrderRejectsAMissingOwner() {
        when(orderRepository.findById(9L)).thenReturn(Optional.of(order(9L, 4L, 100, OrderStatus.PENDING_PAYMENT)));
        when(userRepository.findById(4L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> orderService.updateOrder(9L, UpdateOrder.builder().orderStatus(OrderStatus.DELIVERED).build()));

        assertEquals("User not found: 4", exception.getReason());
    }

    @Test
    void updateOrderRejectsADeactivatedOwner() {
        when(orderRepository.findById(9L)).thenReturn(Optional.of(order(9L, 4L, 100, OrderStatus.PENDING_PAYMENT)));
        when(userRepository.findById(4L)).thenReturn(Optional.of(user(4L, UserAccountStatus.DEACTIVATED)));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> orderService.updateOrder(9L, UpdateOrder.builder().orderStatus(OrderStatus.DELIVERED).build()));

        assertEquals("User is deactivated: 4", exception.getReason());
    }

    @Test
    void updateOrderChangesStatusAndLeavesItWhenOmitted() {
        OrderEntity entity = order(3L, 2L, 640, OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(entity));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserAccountStatus.ACTIVE)));

        Order unchanged = orderService.updateOrder(3L, UpdateOrder.builder().build());
        assertEquals(OrderStatus.PENDING_PAYMENT, unchanged.getOrderStatus());

        Order moved = orderService.updateOrder(3L, UpdateOrder.builder().orderStatus(OrderStatus.IN_TRANSIT).build());
        assertEquals(OrderStatus.IN_TRANSIT, moved.getOrderStatus());
        assertEquals(640, moved.getAmount());
    }

    private Specification<OrderEntity> capture() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Specification<OrderEntity>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(orderRepository).findAll(captor.capture(), any(Pageable.class));
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private static Root<OrderEntity> root() {
        return mock(Root.class);
    }

    @SuppressWarnings("unchecked")
    private static CriteriaQuery<OrderEntity> query() {
        return mock(CriteriaQuery.class);
    }

    private static OrderEntity order(Long id, Long userId, long amount, OrderStatus status) {
        return OrderEntity.builder().id(id).userId(userId).amount(amount).orderStatus(status).build();
    }

    private static UserEntity user(Long id, UserAccountStatus status) {
        return UserEntity.builder()
                .id(id)
                .username("user")
                .userAccountStatus(status)
                .cohort(UserCohort.STANDARD)
                .build();
    }
}
