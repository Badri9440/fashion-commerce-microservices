
package com.fashioncommerce.order.client;

import com.fashioncommerce.order.client.dto.ProductVariantClientDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductServiceClient {

    private final RestClient restClient;

    public ProductServiceClient(
            RestClient.Builder builder,
            @Value("${services.product.url}") String baseUrl
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public ProductVariantClientDTO getVariant(Long variantId) {
        return restClient.get()
                .uri("/api/variants/{id}", variantId)
                .retrieve()
                .body(ProductVariantClientDTO.class);
    }
}
