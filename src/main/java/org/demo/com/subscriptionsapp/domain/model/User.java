package org.demo.com.subscriptionsapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.demo.com.subscriptionsapp.domain.enums.UserAccountStatus;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseModel {

    private String username;

    @Setter
    private UserAccountStatus userAccountStatus;
}
