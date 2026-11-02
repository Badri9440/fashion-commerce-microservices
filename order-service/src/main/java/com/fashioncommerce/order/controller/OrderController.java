
package com.fashioncommerce.order.controller;

import com.fashioncommerce.order.dto.OrderRequestDTO;
import com.fashioncommerce.order.dto.OrderResponseDTO;
import com.fashioncommerce.order.entity.PaymentMethod;
import com.fashioncommerce.order.service.OrderService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody OrderRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                orderService.getOrderById(id));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponseDTO> getOrderByNumber(
            @PathVariable String orderNumber
    ) {
        return ResponseEntity.ok(
                orderService.getOrderByNumber(orderNumber));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDTO>> getCustomerOrders(
            @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(
                orderService.getOrdersByCustomer(customerId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<OrderResponseDTO>> getWarehouseOrders(
            @PathVariable Long warehouseId
    ) {
        return ResponseEntity.ok(
                orderService.getOrdersByWarehouse(warehouseId));
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<OrderResponseDTO> checkout(
            @PathVariable Long id,
            @RequestParam PaymentMethod paymentMethod,
            @RequestParam(required = false)
            Boolean simulatePaymentSuccess
    ) {
        return ResponseEntity.ok(
                orderService.checkout(
                        id,
                        paymentMethod,
                        simulatePaymentSuccess
                )
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                orderService.cancelOrder(id));
    }
}
