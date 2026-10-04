package org.demo.com.subscriptionsapp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;

import java.util.Date;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Membership extends BaseModel {

    private Long userId;

    @Setter
    private Long subscriptionId;

    @Setter
    private Date expireAt;

    @Setter
    private MembershipStatus membershipStatus;
}
