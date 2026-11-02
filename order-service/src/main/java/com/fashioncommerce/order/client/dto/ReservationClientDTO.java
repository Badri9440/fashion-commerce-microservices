package com.fashioncommerce.order.client.dto;

public record ReservationClientDTO(
        Long id,
        Long orderId,
        Long inventoryId,
        Integer quantity,
        String status
) {}