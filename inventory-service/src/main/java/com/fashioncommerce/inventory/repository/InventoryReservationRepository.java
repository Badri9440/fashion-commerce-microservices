package com.fashioncommerce.inventory.repository;

import com.fashioncommerce.inventory.entity.InventoryReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, Long> {

    Optional<InventoryReservation> findByOrderIdAndInventoryId(
            Long orderId,
            Long inventoryId
    );

    List<InventoryReservation> findByOrderId(Long orderId);
}