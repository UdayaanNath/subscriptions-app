package org.demo.com.subscriptionsapp.api.dto.benefit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.demo.com.subscriptionsapp.domain.enums.BenefitType;

import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
public class Benefit {

    private Long id;

    private Long subscriptionId;

    private BenefitType benefitType;

    private String name;

    private String description;

    private Integer discountPercent;

    private Date createdAt;

    private Date updatedAt;
}
