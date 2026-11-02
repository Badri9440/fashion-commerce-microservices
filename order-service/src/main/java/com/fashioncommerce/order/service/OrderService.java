
package com.fashioncommerce.order.service;

import com.fashioncommerce.order.dto.OrderRequestDTO;
import com.fashioncommerce.order.dto.OrderResponseDTO;
import com.fashioncommerce.order.entity.PaymentMethod;

import java.util.List;

public interface OrderService {

    OrderResponseDTO createOrder(OrderRequestDTO request);

    OrderResponseDTO getOrderById(Long orderId);

    OrderResponseDTO getOrderByNumber(String orderNumber);

    List<OrderResponseDTO> getOrdersByCustomer(Long customerId);

    List<OrderResponseDTO> getOrdersByWarehouse(Long warehouseId);

    OrderResponseDTO cancelOrder(Long orderId);

    OrderResponseDTO checkout(
            Long orderId,
            PaymentMethod paymentMethod,
            Boolean simulatePaymentSuccess
    );
}
