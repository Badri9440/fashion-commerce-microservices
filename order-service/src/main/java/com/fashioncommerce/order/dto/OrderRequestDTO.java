package com.fashioncommerce.order.dto;

import com.fashioncommerce.order.entity.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record OrderRequestDTO(

        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotNull(message = "Order type is required")
        OrderType orderType,

        @NotBlank(message = "Delivery address is required")
        String deliveryAddress,

        @NotBlank(message = "Pincode is required")
        @Pattern(
                regexp = "^[1-9][0-9]{5}$",
                message = "Enter a valid 6-digit Indian pincode"
        )
        String pincode,

        @NotNull(message = "Warehouse ID is required")
        Long warehouseId,

        @NotEmpty(message = "Order must contain at least one item")
        List<@NotNull @Valid OrderItemRequestDTO> items

) {}