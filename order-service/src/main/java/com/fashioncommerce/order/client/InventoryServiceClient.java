
package com.fashioncommerce.order.client;

import com.fashioncommerce.order.client.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class InventoryServiceClient {

    private final RestClient restClient;

    public InventoryServiceClient(
            RestClient.Builder builder,
            @Value("${services.inventory.url}") String baseUrl
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public List<InventoryClientDTO> getWarehouseInventory(
            Long warehouseId
    ) {
        return restClient.get()
                .uri("/api/inventory/warehouse/{warehouseId}", warehouseId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public ReservationClientDTO createReservation(
            CreateReservationClientDTO request
    ) {
        return restClient.post()
                .uri("/api/reservations")
                .body(request)
                .retrieve()
                .body(ReservationClientDTO.class);
    }

    public ReservationClientDTO confirmReservation(
            Long reservationId
    ) {
        return restClient.post()
                .uri("/api/reservations/{id}/confirm", reservationId)
                .retrieve()
                .body(ReservationClientDTO.class);
    }

    public ReservationClientDTO cancelReservation(
            Long reservationId
    ) {
        return restClient.post()
                .uri("/api/reservations/{id}/cancel", reservationId)
                .retrieve()
                .body(ReservationClientDTO.class);
    }

    public List<ReservationClientDTO> getReservationsByOrder(
            Long orderId
    ) {
        return restClient.get()
                .uri("/api/reservations/order/{orderId}", orderId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
