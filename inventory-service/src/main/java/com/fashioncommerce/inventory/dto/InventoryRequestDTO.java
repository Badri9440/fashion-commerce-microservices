package com.fashioncommerce.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryRequestDTO(

        @NotNull
        Long warehouseId,

        @NotNull
        Long productVariantId,

        @NotNull
        @Min(0)
        Integer quantity
) {
}