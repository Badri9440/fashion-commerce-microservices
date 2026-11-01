package com.fashioncommerce.inventory.dto;

public record InventoryResponseDTO(
        Long id,
        Long warehouseId,
        Long productVariantId,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity
) {
}