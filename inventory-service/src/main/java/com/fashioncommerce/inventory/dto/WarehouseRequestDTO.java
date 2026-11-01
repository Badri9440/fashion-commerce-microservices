package com.fashioncommerce.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WarehouseRequestDTO(

        @NotBlank(message = "Warehouse name is required")
        @Size(max = 150)
        String name,

        @NotBlank(message = "Warehouse code is required")
        @Size(max = 50)
        String code,

        @NotBlank(message = "Warehouse location is required")
        @Size(max = 255)
        String location
) {
}