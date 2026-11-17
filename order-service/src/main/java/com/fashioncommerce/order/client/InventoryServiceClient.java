
package com.fashioncommerce.order.client;

import com.fashioncommerce.order.client.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class InventoryServiceClient {

    private final RestClient restClient;
    private final RestClient authClient = RestClient.create();
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;

    private String accessToken;
    private long expiresAt;

    public InventoryServiceClient(
            RestClient.Builder builder,
            @Value("${services.inventory.url}") String baseUrl,
            @Value("${services.auth.token-url}") String tokenUrl,
            @Value("${services.auth.client-id}") String clientId,
            @Value("${services.auth.client-secret}") String clientSecret
    ) {
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setBearerAuth(getToken());
                    return execution.execute(request, body);
                })
                .build();
    }

    private synchronized String getToken() {
        if (accessToken != null && System.currentTimeMillis() < expiresAt) {
            return accessToken;
        }

        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        Map<String, Object> response = authClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null || !(response.get("access_token") instanceof String token)) {
            throw new IllegalStateException("Keycloak did not return an access token");
        }

        accessToken = token;
        long seconds = ((Number) response.get("expires_in")).longValue();
        expiresAt = System.currentTimeMillis() + Math.max(0, seconds - 30) * 1000;

        return accessToken;
    }

    public List<InventoryClientDTO> getWarehouseInventory(Long warehouseId) {
        return restClient.get()
                .uri("/api/inventory/warehouse/{warehouseId}", warehouseId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public ReservationClientDTO createReservation(CreateReservationClientDTO request) {
        return restClient.post()
                .uri("/api/reservations")
                .body(request)
                .retrieve()
                .body(ReservationClientDTO.class);
    }

    public ReservationClientDTO confirmReservation(Long reservationId) {
        return restClient.post()
                .uri("/api/reservations/{id}/confirm", reservationId)
                .retrieve()
                .body(ReservationClientDTO.class);
    }

    public ReservationClientDTO cancelReservation(Long reservationId) {
        return restClient.post()
                .uri("/api/reservations/{id}/cancel", reservationId)
                .retrieve()
                .body(ReservationClientDTO.class);
    }

    public ReservationClientDTO reverseReservation(Long reservationId) {
        return restClient.post()
                .uri("/api/reservations/{id}/reverse", reservationId)
                .retrieve()
                .body(ReservationClientDTO.class);
    }

    public List<ReservationClientDTO> getReservationsByOrder(Long orderId) {
        return restClient.get()
                .uri("/api/reservations/order/{orderId}", orderId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
