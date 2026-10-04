package org.demo.com.subscriptionsapp.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.com.subscriptionsapp.api.dto.subscription.CreateSubscription;
import org.demo.com.subscriptionsapp.api.dto.subscription.SearchSubscriptionCriteria;
import org.demo.com.subscriptionsapp.api.dto.subscription.Subscription;
import org.demo.com.subscriptionsapp.api.dto.subscription.UpdateSubscription;
import org.demo.com.subscriptionsapp.service.SubscriptionService;
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
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<Subscription> createSubscription(@Valid @RequestBody CreateSubscription createSubscription) {
        Subscription subscription = subscriptionService.createSubscription(createSubscription);
        return new ResponseEntity<>(subscription, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Subscription> updateSubscription(@PathVariable Long id, @Valid @RequestBody UpdateSubscription updateSubscription) {
        Subscription subscription = subscriptionService.updateSubscription(id, updateSubscription);
        return new ResponseEntity<>(subscription, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<Subscription>> listSubscriptions(SearchSubscriptionCriteria searchSubscriptionCriteria) {
        Page<Subscription> subscriptions = subscriptionService.getAllSubscriptions(searchSubscriptionCriteria);
        return new ResponseEntity<>(subscriptions, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subscription> getSubscription(@PathVariable Long id) {
        Subscription subscription = subscriptionService.getSubscriptionById(id);
        return new ResponseEntity<>(subscription, HttpStatus.OK);
    }

}
