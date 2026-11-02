package com.fashioncommerce.order.client.dto;

public record InventoryClientDTO(
        Long id,
        Long warehouseId,
        Long productVariantId,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity
) {}