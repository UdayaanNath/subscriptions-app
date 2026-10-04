package org.demo.com.subscriptionsapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.demo.com.subscriptionsapp.domain.enums.MembershipStatus;

import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Membership {

    private Long id;

    private Long userId;

    @Setter
    private Long subscriptionId;

    private Date createdAt;

    @Setter
    private Date expireAt;

    @Setter
    private Date updatedAt;

    @Setter
    private MembershipStatus membershipStatus;
}
