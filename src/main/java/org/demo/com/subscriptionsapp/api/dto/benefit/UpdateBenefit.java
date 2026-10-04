package org.demo.com.subscriptionsapp.api.dto.benefit;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.demo.com.subscriptionsapp.domain.enums.BenefitType;

@Getter
@Builder
@AllArgsConstructor
public class UpdateBenefit {

    private BenefitType benefitType;

    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @Min(value = 0, message = "Discount percent must be zero or a positive number")
    @Max(value = 100, message = "Discount percent cannot be greater than 100")
    private Integer discountPercent;
}
