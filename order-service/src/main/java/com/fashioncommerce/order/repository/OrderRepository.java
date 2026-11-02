
package com.fashioncommerce.order.repository;

import com.fashioncommerce.order.entity.Order;
import com.fashioncommerce.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByWarehouseId(Long warehouseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            UPDATE Order o
            SET o.status = :newStatus,
                o.version = o.version + 1
            WHERE o.id = :orderId
              AND o.status = :expectedStatus
            """)
    int updateStatusIfCurrent(
            @Param("orderId") Long orderId,
            @Param("expectedStatus") OrderStatus expectedStatus,
            @Param("newStatus") OrderStatus newStatus
    );
}
