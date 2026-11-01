package com.fashioncommerce.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockQuantityRequestDTO(

        @NotNull
        @Min(1)
        Integer quantity
) {
}