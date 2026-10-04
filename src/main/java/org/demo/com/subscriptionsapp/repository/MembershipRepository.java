package org.demo.com.subscriptionsapp.repository;

import org.demo.com.subscriptionsapp.domain.entity.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipRepository extends JpaRepository<MembershipEntity, Long>, JpaSpecificationExecutor<MembershipEntity> {

    List<MembershipEntity> findByUserId(Long userId);
}
