
package com.fashioncommerce.order.config;

import com.fashioncommerce.order.controller.OrderController;
import com.fashioncommerce.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@org.springframework.context.annotation.Import(SecurityConfig.class)
class OrderSecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OrderService service;

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mvc.perform(get("/api/orders/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotAccessAdminEndpoint() throws Exception {
        mvc.perform(get("/api/orders/customer/1")
                        .with(jwt().authorities(() -> "ROLE_CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessCustomerEndpoint() throws Exception {
        when(service.getOrdersByCustomer(1L))
                .thenReturn(List.of());

        mvc.perform(get("/api/orders/customer/1")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void warehouseCanAccessWarehouseEndpoint() throws Exception {
        when(service.getOrdersByWarehouse(1L))
                .thenReturn(List.of());

        mvc.perform(get("/api/orders/warehouse/1")
                        .with(jwt().authorities(() -> "ROLE_WAREHOUSE")))
                .andExpect(status().isOk());
    }

    @Test
    void customerCannotAccessWarehouseEndpoint() throws Exception {
        mvc.perform(get("/api/orders/warehouse/1")
                        .with(jwt().authorities(() -> "ROLE_CUSTOMER")))
                .andExpect(status().isForbidden());
    }
}
