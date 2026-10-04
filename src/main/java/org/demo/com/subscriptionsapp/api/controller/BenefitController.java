package org.demo.com.subscriptionsapp.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.com.subscriptionsapp.api.dto.benefit.Benefit;
import org.demo.com.subscriptionsapp.api.dto.benefit.CreateBenefit;
import org.demo.com.subscriptionsapp.api.dto.benefit.SearchBenefitCriteria;
import org.demo.com.subscriptionsapp.api.dto.benefit.UpdateBenefit;
import org.demo.com.subscriptionsapp.service.BenefitService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/benefits")
@RequiredArgsConstructor
public class BenefitController {

    private final BenefitService benefitService;

    @PostMapping
    public ResponseEntity<Benefit> createBenefit(@Valid @RequestBody CreateBenefit createBenefit) {
        Benefit benefit = benefitService.createBenefit(createBenefit);
        return new ResponseEntity<>(benefit, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Benefit> updateBenefit(@PathVariable Long id, @Valid @RequestBody UpdateBenefit updateBenefit) {
        Benefit benefit = benefitService.updateBenefit(id, updateBenefit);
        return new ResponseEntity<>(benefit, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBenefit(@PathVariable Long id) {
        benefitService.deleteBenefit(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<Page<Benefit>> listBenefits(@Valid SearchBenefitCriteria searchBenefitCriteria) {
        Page<Benefit> benefits = benefitService.listBenefits(searchBenefitCriteria);
        return new ResponseEntity<>(benefits, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Benefit> getBenefit(@PathVariable Long id) {
        Benefit benefit = benefitService.getBenefit(id);
        return new ResponseEntity<>(benefit, HttpStatus.OK);
    }
}
