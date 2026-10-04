package org.demo.com.subscriptionsapp.api.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateUser {

    @NotBlank(message = "Username cannot be blank")
    private String username;
}
