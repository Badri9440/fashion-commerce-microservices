package com.fashioncommerce.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateReservationRequestDTO(

        @NotNull
        @Positive
        Long orderId,

        @NotNull
        @Positive
        Long inventoryId,

        @NotNull
        @Min(1)
        Integer quantity
) {
}