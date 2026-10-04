package org.demo.com.subscriptionsapp.repository;

import org.demo.com.subscriptionsapp.domain.entity.OrderEntity;
import org.demo.com.subscriptionsapp.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    long countByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus);

    @Query("""
            select coalesce(sum(o.amount), 0)
            from OrderEntity o
            where o.userId = :userId
              and o.orderStatus = :orderStatus
              and o.createdAt >= :start
              and o.createdAt < :end
            """)
    long sumAmountByUserIdAndOrderStatusAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("start") Date start,
            @Param("end") Date end);
}
