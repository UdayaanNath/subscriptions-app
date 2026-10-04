package org.demo.com.subscriptionsapp.repository;

import org.demo.com.subscriptionsapp.domain.entity.SubscriptionEntity;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionStatus;
import org.demo.com.subscriptionsapp.domain.enums.SubscriptionTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long>, JpaSpecificationExecutor<SubscriptionEntity> {

    Optional<SubscriptionEntity> findFirstBySubscriptionTierAndSubscriptionStatus(
            SubscriptionTier subscriptionTier,
            SubscriptionStatus subscriptionStatus);
}
