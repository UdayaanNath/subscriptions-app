package org.demo.com.subscriptionsapp.api.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;

@Getter
@Builder
@AllArgsConstructor
public class UpdateUser {

    @NotNull(message = "User account status is required")
    private UserAccountStatus userAccountStatus;
}
