package org.demo.com.subscriptionsapp.service;

import lombok.RequiredArgsConstructor;
import org.demo.com.subscriptionsapp.api.dto.order.CreateOrder;
import org.demo.com.subscriptionsapp.api.dto.order.Order;
import org.demo.com.subscriptionsapp.api.dto.order.SearchOrderCriteria;
import org.demo.com.subscriptionsapp.api.dto.order.UpdateOrder;
import org.demo.com.subscriptionsapp.api.error.BadRequestException;
import org.demo.com.subscriptionsapp.api.error.NotFoundException;
import org.demo.com.subscriptionsapp.domain.converter.OrderConverter;
import org.demo.com.subscriptionsapp.domain.entity.OrderEntity;
import org.demo.com.subscriptionsapp.domain.entity.UserEntity;
import org.demo.com.subscriptionsapp.domain.enums.OrderStatus;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;
import org.demo.com.subscriptionsapp.repository.OrderRepository;
import org.demo.com.subscriptionsapp.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<Order> listOrder(SearchOrderCriteria searchOrderCriteria) {
        return orderRepository.findAll(orderSpecification(searchOrderCriteria), searchOrderCriteria.toPageable())
                .map(OrderConverter::toModel);
    }

    private Specification<OrderEntity> orderSpecification(SearchOrderCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), criteria.getUserId()));
            }
            if (criteria.getOrderStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("orderStatus"), criteria.getOrderStatus()));
            }
            return predicates.isEmpty()
                    ? criteriaBuilder.conjunction()
                    : criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return OrderConverter.toModel(orderRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Order", id)));
    }

    @Transactional
    public Order createOrder(CreateOrder createOrder) {
        if (!userRepository.existsById(createOrder.getUserId())) {
            throw NotFoundException.of("User", createOrder.getUserId());
        }

        OrderEntity orderEntity = OrderEntity.builder()
                .userId(createOrder.getUserId())
                .amount(createOrder.getAmount())
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .build();
        return OrderConverter.toModel(orderRepository.save(orderEntity));
    }

    @Transactional
    public Order updateOrder(Long id, UpdateOrder updateOrder) {
        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Order", id));

        UserEntity user = userRepository.findById(orderEntity.getUserId())
                .orElseThrow(() -> NotFoundException.of("User", orderEntity.getUserId()));
        if (user.getUserAccountStatus() == UserAccountStatus.DEACTIVATED) {
            throw new BadRequestException("User is deactivated: " + user.getId());
        }

        if (updateOrder.getOrderStatus() != null) {
            orderEntity.setOrderStatus(updateOrder.getOrderStatus());
        }

        return OrderConverter.toModel(orderEntity);
    }
}
