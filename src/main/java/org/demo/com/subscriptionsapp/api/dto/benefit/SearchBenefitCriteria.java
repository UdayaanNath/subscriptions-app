package org.demo.com.subscriptionsapp.api.dto.benefit;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.demo.com.subscriptionsapp.api.dto.searchCriteria.BaseSearchCriteria;

@Getter
@Setter
public class SearchBenefitCriteria extends BaseSearchCriteria {

    @NotNull(message = "User ID is required")
    @Positive
    private Long userId;
}
