
package com.fashioncommerce.product.repository;

import com.fashioncommerce.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    List<ProductVariant> findByProductId(Long productId);

    boolean existsByProductIdAndSizeAndColor(
            Long productId,
            String size,
            String color
    );
}
