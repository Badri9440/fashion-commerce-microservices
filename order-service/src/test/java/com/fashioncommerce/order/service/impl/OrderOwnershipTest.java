
package com.fashioncommerce.order.service.impl;

import com.fashioncommerce.order.client.InventoryServiceClient;
import com.fashioncommerce.order.client.ProductServiceClient;
import com.fashioncommerce.order.entity.Order;
import com.fashioncommerce.order.repository.OrderRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderOwnershipTest {

    private final OrderRepository repository =
            mock(OrderRepository.class);

    private final OrderServiceImpl service = new OrderServiceImpl(
            repository,
            mock(ProductServiceClient.class),
            mock(InventoryServiceClient.class)
    );

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void customerCanAccessOwnOrder() {
        authenticate("customer-A", "CUSTOMER");

        Order order = createOrder("customer-A");
        when(repository.findById(1L))
                .thenReturn(Optional.of(order));

        assertDoesNotThrow(() -> service.getOrderById(1L));
    }

    @Test
    void customerCannotAccessAnotherCustomersOrder() {
        authenticate("customer-B", "CUSTOMER");

        Order order = createOrder("customer-A");
        when(repository.findById(1L))
                .thenReturn(Optional.of(order));

        assertThrows(
                AccessDeniedException.class,
                () -> service.getOrderById(1L)
        );
    }

    @Test
    void adminCanAccessAnyOrder() {
        authenticate("admin-user", "ADMIN");

        Order order = createOrder("customer-A");
        when(repository.findById(1L))
                .thenReturn(Optional.of(order));

        assertDoesNotThrow(() -> service.getOrderById(1L));
    }

    private Order createOrder(String owner) {
        Order order = new Order();
        order.setId(1L);
        order.setKeycloakUserId(owner);
        order.setItems(new java.util.ArrayList<>());
        return order;
    }

    private void authenticate(String userId, String role) {
        Jwt jwt = new Jwt(
                "test-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                java.util.Map.of("alg", "RS256"),
                java.util.Map.of("sub", userId)
        );

        var authentication = new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}
