package com.fashioncommerce.inventory.dto;

public record WarehouseResponseDTO(
        Long id,
        String name,
        String code,
        String location,
        Boolean active
) {
}