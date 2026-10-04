package org.demo.com.subscriptionsapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;

import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    private String username;

    private Date createdAt;

    @Setter
    private Date updatedAt;

    @Setter
    private UserAccountStatus userAccountStatus;
}
