package com.fashioncommerce.inventory.dto;

import com.fashioncommerce.inventory.entity.ReservationStatus;

public record ReservationResponseDTO(
        Long id,
        Long orderId,
        Long inventoryId,
        Integer quantity,
        ReservationStatus status
) {
}