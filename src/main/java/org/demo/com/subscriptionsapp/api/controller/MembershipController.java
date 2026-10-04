package org.demo.com.subscriptionsapp.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.com.subscriptionsapp.api.dto.membership.CreateMembership;
import org.demo.com.subscriptionsapp.api.dto.membership.Membership;
import org.demo.com.subscriptionsapp.api.dto.membership.SearchMembershipCriteria;
import org.demo.com.subscriptionsapp.service.MembershipService;
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

@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @PostMapping
    public ResponseEntity<Membership> createMembership(@Valid @RequestBody CreateMembership createMembership) {
        Membership membership = membershipService.createMembership(createMembership);
        return new ResponseEntity<>(membership, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/renew")
    public ResponseEntity<Membership> renewMembership(@PathVariable Long id) {
        Membership membership = membershipService.renewMembership(id);
        return new ResponseEntity<>(membership, HttpStatus.OK);
    }

    @PatchMapping("/{id}/upgrade")
    public ResponseEntity<Membership> upgradeMembership(@PathVariable Long id) {
        Membership membership = membershipService.upgradeMembership(id);
        return new ResponseEntity<>(membership, HttpStatus.OK);
    }

    @PatchMapping("/{id}/downGrade")
    public ResponseEntity<Membership> downgradeMembership(@PathVariable Long id) {
        Membership membership = membershipService.downgradeMembership(id);
        return new ResponseEntity<>(membership, HttpStatus.OK);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Membership> cancelMemberShip(@PathVariable Long id) {
        Membership membership = membershipService.cancelMemberShip(id);
        return new ResponseEntity<>(membership, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<Membership>> listMembership(SearchMembershipCriteria searchMembershipCriteria) {
        Page<Membership> memberships = membershipService.listMemberships(searchMembershipCriteria);
        return new ResponseEntity<>(memberships, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Membership> getMembership(@PathVariable Long id) {
        Membership membership = membershipService.getMembership(id);
        return new ResponseEntity<>(membership, HttpStatus.OK);
    }
}
