package com.fashioncommerce.order.dto;

import com.fashioncommerce.order.entity.OrderStatus;
import com.fashioncommerce.order.entity.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(

        Long id,
        String orderNumber,
        Long customerId,
        OrderType orderType,
        OrderStatus status,
        BigDecimal totalAmount,
        String deliveryAddress,
        String pincode,
        Long warehouseId,
        List<OrderItemResponseDTO> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}