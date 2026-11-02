
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
import com.fashioncommerce.order.entity.PaymentMethod;
import com.fashioncommerce.order.entity.PaymentStatus;
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
        order.setOrderNumber("ORD-" + UUID.randomUUID());
        order.setCustomerId(request.customerId());
        order.setOrderType(request.orderType());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(request.deliveryAddress());
        order.setPincode(request.pincode());
        order.setWarehouseId(request.warehouseId());

        BigDecimal total = BigDecimal.ZERO;

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

            return toResponse(savedOrder);

        } catch (Exception ex) {

            boolean cleanupSuccessful = true;

            // Query inventory again in case a reservation was
            // created but its HTTP response was lost.
            try {
                List<ReservationClientDTO> current =
                        inventoryClient.getReservationsByOrder(
                                savedOrder.getId());

                if (current == null) {
                    cleanupSuccessful = false;
                } else {
                    for (ReservationClientDTO reservation : current) {
                        if ("ACTIVE".equals(reservation.status())) {
                            inventoryClient.cancelReservation(
                                    reservation.id());
                        } else if (!"CANCELLED".equals(
                                reservation.status())) {
                            cleanupSuccessful = false;
                        }
                    }
                }

            } catch (Exception cleanupEx) {
                cleanupSuccessful = false;
            }

            savedOrder.setStatus(
                    cleanupSuccessful
                            ? OrderStatus.CANCELLED
                            : OrderStatus.RECOVERY_REQUIRED
            );

            orderRepository.saveAndFlush(savedOrder);

            throw new ResponseStatusException(
                    cleanupSuccessful
                            ? SERVICE_UNAVAILABLE
                            : INTERNAL_SERVER_ERROR,
                    cleanupSuccessful
                            ? "Order creation failed; reservations released"
                            : "Order creation requires reconciliation",
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
    public OrderResponseDTO checkout(
            Long orderId,
            PaymentMethod paymentMethod,
            Boolean simulatePaymentSuccess
    ) {

        if (paymentMethod == null) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "Payment method is required");
        }

        if (paymentMethod == PaymentMethod.ONLINE
                && simulatePaymentSuccess == null) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Simulated payment result is required");
        }

        // Check existence first for a clear 404 response.
        findOrder(orderId);

        // Atomically claim the order.
        int claimed = orderRepository.updateStatusIfCurrent(
                orderId,
                OrderStatus.PENDING,
                OrderStatus.CHECKOUT_IN_PROGRESS
        );

        if (claimed != 1) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Order is not PENDING or checkout is already running"
            );
        }

        try {
            Order order = findOrder(orderId);

            List<ReservationClientDTO> reservations =
                    inventoryClient.getReservationsByOrder(orderId);

            if (reservations == null
                    || reservations.size() != order.getItems().size()
                    || reservations.stream().anyMatch(
                    r -> r == null
                            || r.id() == null
                            || !"ACTIVE".equals(r.status()))) {

                markRecoveryRequired(orderId);

                throw new ResponseStatusException(
                        CONFLICT,
                        "Reservations require reconciliation"
                );
            }

            // Simulated online payment failure:
            // no inventory confirmation has occurred.
            if (paymentMethod == PaymentMethod.ONLINE
                    && Boolean.FALSE.equals(simulatePaymentSuccess)) {

                order.setPaymentMethod(paymentMethod);
                order.setPaymentStatus(PaymentStatus.FAILED);

                // Persist payment failure before releasing claim.
                orderRepository.saveAndFlush(order);

                int released = orderRepository.updateStatusIfCurrent(
                        orderId,
                        OrderStatus.CHECKOUT_IN_PROGRESS,
                        OrderStatus.PENDING
                );

                if (released != 1) {
                    throw new IllegalStateException(
                            "Could not release checkout claim"
                    );
                }

                return toResponse(findOrder(orderId));
            }

            for (ReservationClientDTO reservation : reservations) {

                ReservationClientDTO confirmed =
                        inventoryClient.confirmReservation(
                                reservation.id());

                if (confirmed == null
                        || !"CONFIRMED".equals(confirmed.status())) {
                    throw new IllegalStateException(
                            "Reservation confirmation failed: "
                                    + reservation.id()
                    );
                }
            }

            // All inventory confirmations succeeded.
            order.setPaymentMethod(paymentMethod);

            order.setPaymentStatus(
                    paymentMethod == PaymentMethod.COD
                            ? PaymentStatus.NOT_REQUIRED
                            : PaymentStatus.SUCCESS
            );

            order.setStatus(OrderStatus.CONFIRMED);

            return toResponse(orderRepository.saveAndFlush(order));

        } catch (Exception ex) {

            // A database save might have succeeded even if
            // the response to the caller failed.
            try {
                Order latest = findOrder(orderId);

                if (latest.getStatus() == OrderStatus.CONFIRMED) {
                    return toResponse(latest);
                }
            } catch (Exception lookupEx) {
                ex.addSuppressed(lookupEx);
            }

            // Block checkout retries before attempting recovery.
            try {
                markRecoveryRequired(orderId);
            } catch (Exception statusEx) {
                ex.addSuppressed(statusEx);
            }

            boolean recoverySuccessful = true;

            try {
                List<ReservationClientDTO> current =
                        inventoryClient.getReservationsByOrder(orderId);

                if (current == null) {
                    recoverySuccessful = false;
                } else {
                    for (ReservationClientDTO reservation : current) {

                        if ("CONFIRMED".equals(reservation.status())) {

                            try {
                                ReservationClientDTO reversed =
                                        inventoryClient.reverseReservation(
                                                reservation.id());

                                if (reversed == null
                                        || !"REVERSED".equals(
                                        reversed.status())) {
                                    recoverySuccessful = false;
                                }

                            } catch (Exception recoveryEx) {
                                recoverySuccessful = false;
                            }
                        }
                    }
                }

            } catch (Exception recoveryEx) {
                recoverySuccessful = false;
            }

            // Do not return to PENDING:
            // reversed reservations cannot be reused.
            throw new ResponseStatusException(
                    recoverySuccessful
                            ? CONFLICT
                            : INTERNAL_SERVER_ERROR,
                    recoverySuccessful
                            ? "Checkout failed; order requires reconciliation before retry"
                            : "Checkout recovery incomplete; manual reconciliation required",
                    ex
            );
        }
    }

    @Override
    public OrderResponseDTO cancelOrder(Long orderId) {

        // Verify that the order exists.
        Order order = findOrder(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return toResponse(order);
        }

        // Use RECOVERY_REQUIRED as a temporary cancellation
        // claim to block checkout and competing cancellations.
        // A failure leaves the order blocked for reconciliation.
        int claimed = orderRepository.updateStatusIfCurrent(
                orderId,
                OrderStatus.PENDING,
                OrderStatus.RECOVERY_REQUIRED
        );

        if (claimed == 0) {
            claimed = orderRepository.updateStatusIfCurrent(
                    orderId,
                    OrderStatus.CONFIRMED,
                    OrderStatus.RECOVERY_REQUIRED
            );
        }

        if (claimed != 1) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Order cannot be cancelled in current status"
            );
        }

        try {
            List<ReservationClientDTO> reservations =
                    inventoryClient.getReservationsByOrder(orderId);

            if (reservations == null || reservations.isEmpty()) {
                throw new IllegalStateException(
                        "No inventory reservations found for order"
                );
            }

            for (ReservationClientDTO reservation : reservations) {

                if ("ACTIVE".equals(reservation.status())) {

                    inventoryClient.cancelReservation(
                            reservation.id());

                } else if ("CONFIRMED".equals(
                        reservation.status())) {

                    inventoryClient.reverseReservation(
                            reservation.id());

                } else if (!"CANCELLED".equals(
                        reservation.status())
                        && !"REVERSED".equals(
                        reservation.status())) {

                    throw new IllegalStateException(
                            "Unexpected reservation status: "
                                    + reservation.status()
                    );
                }
            }

            // Reload after the conditional status update.
            Order latest = findOrder(orderId);
            latest.setStatus(OrderStatus.CANCELLED);

            return toResponse(
                    orderRepository.saveAndFlush(latest));

        } catch (Exception ex) {

            // Leave the order in RECOVERY_REQUIRED.
            // Never silently mark it CANCELLED.
            throw new ResponseStatusException(
                    INTERNAL_SERVER_ERROR,
                    "Cancellation incomplete; reconciliation required",
                    ex
            );
        }
    }

    private void markRecoveryRequired(Long orderId) {

        int updated = orderRepository.updateStatusIfCurrent(
                orderId,
                OrderStatus.CHECKOUT_IN_PROGRESS,
                OrderStatus.RECOVERY_REQUIRED
        );

        if (updated == 0) {
            Order current = findOrder(orderId);

            if (current.getStatus()
                    != OrderStatus.RECOVERY_REQUIRED) {
                throw new IllegalStateException(
                        "Unable to mark order for recovery: "
                                + current.getStatus()
                );
            }
        }
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
                order.getUpdatedAt(),
                order.getPaymentMethod(),
                order.getPaymentStatus()
        );
    }
}
