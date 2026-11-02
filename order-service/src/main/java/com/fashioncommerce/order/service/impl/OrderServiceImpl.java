
package com.fashioncommerce.order.service.impl;

import com.fashioncommerce.order.client.InventoryServiceClient;
import com.fashioncommerce.order.client.ProductServiceClient;
import com.fashioncommerce.order.client.dto.CreateReservationClientDTO;
import com.fashioncommerce.order.client.dto.InventoryClientDTO;
import com.fashioncommerce.order.client.dto.ProductVariantClientDTO;
import com.fashioncommerce.order.client.dto.ReservationClientDTO;
import com.fashioncommerce.order.dto.*;
import com.fashioncommerce.order.entity.Order;
import com.fashioncommerce.order.entity.OrderItem;
import com.fashioncommerce.order.entity.OrderStatus;
import com.fashioncommerce.order.repository.OrderRepository;
import com.fashioncommerce.order.service.OrderService;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productClient;
    private final InventoryServiceClient inventoryClient;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            ProductServiceClient productClient,
            InventoryServiceClient inventoryClient
    ) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.inventoryClient = inventoryClient;
    }

    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO request) {

        if (request == null || request.items() == null
                || request.items().isEmpty()) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "Order items are required");
        }

        // Combine quantities for duplicate variants.
        Map<Long, Integer> quantities = new HashMap<>();

        for (OrderItemRequestDTO item : request.items()) {
            if (item == null || item.variantId() == null
                    || item.quantity() == null
                    || item.quantity() <= 0) {
                throw new ResponseStatusException(
                        BAD_REQUEST, "Invalid order item");
            }

            try {
                quantities.merge(
                        item.variantId(),
                        item.quantity(),
                        Math::addExact
                );
            } catch (ArithmeticException ex) {
                throw new ResponseStatusException(
                        BAD_REQUEST, "Quantity is too large");
            }
        }

        // Retrieve warehouse stock.
        List<InventoryClientDTO> stock =
                inventoryClient.getWarehouseInventory(
                        request.warehouseId());

        if (stock == null) {
            throw new ResponseStatusException(
                    BAD_GATEWAY, "Inventory response is empty");
        }

        Map<Long, InventoryClientDTO> stockByVariant =
                new HashMap<>();

        for (InventoryClientDTO inventory : stock) {
            stockByVariant.put(
                    inventory.productVariantId(), inventory);
        }

        Order order = new Order();
        order.setOrderNumber(
                "ORD-" + UUID.randomUUID());
        order.setCustomerId(request.customerId());
        order.setOrderType(request.orderType());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(request.deliveryAddress());
        order.setPincode(request.pincode());
        order.setWarehouseId(request.warehouseId());

        BigDecimal total = BigDecimal.ZERO;

        // Validate products and calculate prices.
        for (Map.Entry<Long, Integer> entry
                : quantities.entrySet()) {

            Long variantId = entry.getKey();
            Integer quantity = entry.getValue();

            ProductVariantClientDTO variant =
                    productClient.getVariant(variantId);

            if (variant == null
                    || !Boolean.TRUE.equals(variant.active())
                    || variant.price() == null
                    || variant.price().signum() < 0) {
                throw new ResponseStatusException(
                        BAD_REQUEST,
                        "Invalid or inactive variant: " + variantId);
            }

            InventoryClientDTO inventory =
                    stockByVariant.get(variantId);

            if (inventory == null
                    || inventory.availableQuantity() == null
                    || inventory.availableQuantity() < quantity) {
                throw new ResponseStatusException(
                        CONFLICT,
                        "Insufficient stock for variant: " + variantId);
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(variant.productId());
            orderItem.setVariantId(variantId);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(variant.price());

            BigDecimal subtotal = variant.price()
                    .multiply(BigDecimal.valueOf(quantity));

            orderItem.setSubtotal(subtotal);
            order.getItems().add(orderItem);

            total = total.add(subtotal);
        }

        order.setTotalAmount(total);

        // Save first to obtain the database order ID.
        Order savedOrder = orderRepository.saveAndFlush(order);

        List<Long> reservationIds = new ArrayList<>();

        try {
            for (OrderItem item : savedOrder.getItems()) {

                InventoryClientDTO inventory =
                        stockByVariant.get(item.getVariantId());

                CreateReservationClientDTO reservationRequest =
                        new CreateReservationClientDTO(
                                savedOrder.getId(),
                                inventory.id(),
                                item.getQuantity()
                        );

                ReservationClientDTO reservation =
                        inventoryClient.createReservation(
                                reservationRequest);

                if (reservation == null
                        || reservation.id() == null
                        || !"ACTIVE".equals(reservation.status())) {
                    throw new IllegalStateException(
                            "Inventory reservation failed");
                }

                reservationIds.add(reservation.id());
            }

            // Keep PENDING until checkout/payment handling
            // determines the next order status.
            return toResponse(savedOrder);

        } catch (Exception ex) {

            // Try to cancel reservations created before failure.
            boolean cleanupSuccessful = true;

            for (Long reservationId : reservationIds) {
                try {
                    inventoryClient.cancelReservation(reservationId);
                } catch (Exception cleanupException) {
                    cleanupSuccessful = false;
                }
            }

            savedOrder.setStatus(OrderStatus.CANCELLED);
            orderRepository.saveAndFlush(savedOrder);

            if (!cleanupSuccessful) {
                throw new ResponseStatusException(
                        INTERNAL_SERVER_ERROR,
                        "Reservation cleanup incomplete; manual reconciliation required",
                        ex
                );
            }

            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "Order could not be completed; reservation reconciliation may be required",
                    ex
            );
        }
    }

    @Override
    public OrderResponseDTO getOrderById(Long orderId) {
        return toResponse(findOrder(orderId));
    }

    @Override
    public OrderResponseDTO getOrderByNumber(String orderNumber) {
        Order order = orderRepository
                .findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Order not found"));

        return toResponse(order);
    }

    @Override
    public List<OrderResponseDTO> getOrdersByCustomer(
            Long customerId) {

        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<OrderResponseDTO> getOrdersByWarehouse(
            Long warehouseId) {

        return orderRepository.findByWarehouseId(warehouseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrderResponseDTO cancelOrder(Long orderId) {

        Order order = findOrder(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return toResponse(order);
        }

        if (order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Order cannot be cancelled in current status");
        }

        List<ReservationClientDTO> reservations =
                inventoryClient.getReservationsByOrder(orderId);

        if (reservations == null) {
            throw new ResponseStatusException(
                    BAD_GATEWAY, "Reservation response is empty");
        }

        for (ReservationClientDTO reservation : reservations) {
            if ("ACTIVE".equals(reservation.status())) {
                inventoryClient.cancelReservation(
                        reservation.id());
            } else if (!"CANCELLED".equals(reservation.status())) {
                throw new ResponseStatusException(
                        CONFLICT,
                        "Reservation cannot be cancelled: "
                                + reservation.id());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.saveAndFlush(order));
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "Order not found with ID: " + orderId));
    }

    private OrderResponseDTO toResponse(Order order) {

        List<OrderItemResponseDTO> items =
                order.getItems().stream()
                        .map(item -> new OrderItemResponseDTO(
                                item.getId(),
                                item.getProductId(),
                                item.getVariantId(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getSubtotal()
                        ))
                        .toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getOrderType(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getPincode(),
                order.getWarehouseId(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
