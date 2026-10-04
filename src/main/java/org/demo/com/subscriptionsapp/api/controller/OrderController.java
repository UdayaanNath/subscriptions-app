package org.demo.com.subscriptionsapp.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.com.subscriptionsapp.api.dto.order.CreateOrder;
import org.demo.com.subscriptionsapp.api.dto.order.Order;
import org.demo.com.subscriptionsapp.api.dto.order.SearchOrderCriteria;
import org.demo.com.subscriptionsapp.api.dto.order.UpdateOrder;
import org.demo.com.subscriptionsapp.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrder createOrder) {
        Order order = orderService.createOrder(createOrder);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @Valid @RequestBody UpdateOrder updateOrder) {
        Order order = orderService.updateOrder(id, updateOrder);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<Order>> listOrder(SearchOrderCriteria searchOrderCriteria) {
        Page<Order> orders = orderService.listOrder(searchOrderCriteria);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}
