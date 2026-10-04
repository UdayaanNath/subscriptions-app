package org.demo.com.subscriptionsapp.api.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;

import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
public class User {

    private Long id;

    @NotBlank(message = "Username cannot be blank")
    private String username;

    private Date createdAt;

    private Date updatedAt;

    @NotNull(message = "User account status is required")
    private UserAccountStatus userAccountStatus;
}
