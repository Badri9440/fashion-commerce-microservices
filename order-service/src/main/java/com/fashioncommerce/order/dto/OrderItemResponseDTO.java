package com.fashioncommerce.order.dto;

import java.math.BigDecimal;

public record OrderItemResponseDTO(

        Long id,
        Long productId,
        Long variantId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal

) {}