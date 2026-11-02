package com.fashioncommerce.order.client.dto;

public record CreateReservationClientDTO(
        Long orderId,
        Long inventoryId,
        Integer quantity
) {}