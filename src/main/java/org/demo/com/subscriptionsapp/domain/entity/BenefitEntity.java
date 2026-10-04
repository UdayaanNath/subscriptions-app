package org.demo.com.subscriptionsapp.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.demo.com.subscriptionsapp.domain.enums.BenefitType;

@Entity
@Table(name = "benefits", indexes = {
        @Index(name = "idx_benefits_subscription_id", columnList = "subscription_id")
})
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitEntity extends BaseEntity {

    @NotNull(message = "Subscription ID is required")
    @Positive
    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Setter
    @NotNull(message = "Benefit type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_type", nullable = false)
    private BenefitType benefitType;

    @Setter
    @NotBlank(message = "Benefit name cannot be blank")
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Setter
    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Setter
    @Min(value = 0, message = "Discount percent must be zero or a positive number")
    @Max(value = 100, message = "Discount percent cannot be greater than 100")
    @Column(name = "discount_percent")
    private Integer discountPercent;
}
