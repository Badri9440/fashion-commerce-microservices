package com.fashioncommerce.inventory.repository;

import com.fashioncommerce.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByWarehouseIdAndProductVariantId(
            Long warehouseId,
            Long productVariantId
    );

    boolean existsByWarehouseIdAndProductVariantId(
            Long warehouseId,
            Long productVariantId
    );

    List<Inventory> findByWarehouseId(Long warehouseId);
}