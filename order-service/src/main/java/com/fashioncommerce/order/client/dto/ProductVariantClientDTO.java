package com.fashioncommerce.order.client.dto;

import java.math.BigDecimal;

public record ProductVariantClientDTO(
        Long id,
        Long productId,
        String sku,
        String size,
        String color,
        BigDecimal price,
        Boolean active
) {}