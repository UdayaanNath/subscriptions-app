package org.demo.com.subscriptionsapp.repository;

import org.demo.com.subscriptionsapp.domain.entity.BenefitEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface BenefitRepository extends JpaRepository<BenefitEntity, Long> {

    Page<BenefitEntity> findBySubscriptionIdIn(Collection<Long> subscriptionIds, Pageable pageable);
}
