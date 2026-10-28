
package com.fashioncommerce.product.service.impl;

import com.fashioncommerce.product.dto.ProductVariantRequestDTO;
import com.fashioncommerce.product.dto.ProductVariantResponseDTO;
import com.fashioncommerce.product.entity.Product;
import com.fashioncommerce.product.entity.ProductVariant;
import com.fashioncommerce.product.repository.ProductRepository;
import com.fashioncommerce.product.repository.ProductVariantRepository;
import com.fashioncommerce.product.service.ProductVariantService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    @Override
    public ProductVariantResponseDTO createVariant(
            ProductVariantRequestDTO request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found with ID: " + request.getProductId()));

        if (variantRepository.existsByProductIdAndSizeAndColor(
                request.getProductId(),
                request.getSize(),
                request.getColor())) {

            throw new IllegalArgumentException(
                    "Variant with this size and color already exists for this product");
        }

        ProductVariant variant = new ProductVariant();

        variant.setProduct(product);
        variant.setSku(request.getSku());
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setPrice(request.getPrice());
        variant.setActive(request.getActive());

        return mapToResponse(variantRepository.saveAndFlush(variant));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantResponseDTO getVariantById(Long id) {

        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Variant not found with ID: " + id));

        return mapToResponse(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantResponseDTO getVariantBySku(String sku) {

        ProductVariant variant = variantRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Variant not found with SKU: " + sku));

        return mapToResponse(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantResponseDTO> getVariantsByProductId(
            Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException(
                    "Product not found with ID: " + productId);
        }

        return variantRepository.findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Override
    public ProductVariantResponseDTO updateVariant(
            Long id, ProductVariantRequestDTO request) {

        // Step 1: Check whether the variant exists
        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Variant not found with ID: " + id));

        // Step 2: Check whether the product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found with ID: " + request.getProductId()));

        // Step 3: Validate SKU uniqueness
        if (!variant.getSku().equals(request.getSku())
                && variantRepository.existsBySku(request.getSku())) {

            throw new IllegalArgumentException(
                    "Variant SKU already exists");
        }

        // Step 4: Check whether product, size, or color changed
        boolean combinationChanged =
                !variant.getProduct().getId().equals(request.getProductId())
                        || !variant.getSize().equals(request.getSize())
                        || !variant.getColor().equals(request.getColor());

        // Step 5: Prevent duplicate size/color combinations
        if (combinationChanged
                && variantRepository.existsByProductIdAndSizeAndColor(
                request.getProductId(),
                request.getSize(),
                request.getColor())) {

            throw new IllegalArgumentException(
                    "Variant with this size and color already exists for this product");
        }

        // Step 6: Update variant fields
        variant.setProduct(product);
        variant.setSku(request.getSku());
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setPrice(request.getPrice());
        variant.setActive(request.getActive());

        // Step 7: Save changes and return updated response
        ProductVariant updatedVariant =
                variantRepository.saveAndFlush(variant);

        return mapToResponse(updatedVariant);
    }


    @Override
    public void deleteVariant(Long id) {

        ProductVariant variant = variantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Variant not found with ID: " + id));

        variantRepository.delete(variant);
    }

    private ProductVariantResponseDTO mapToResponse(ProductVariant variant) {

        ProductVariantResponseDTO response = new ProductVariantResponseDTO();

        response.setId(variant.getId());
        response.setProductId(variant.getProduct().getId());
        response.setSku(variant.getSku());
        response.setSize(variant.getSize());
        response.setColor(variant.getColor());
        response.setPrice(variant.getPrice());
        response.setActive(variant.getActive());
        response.setCreatedAt(variant.getCreatedAt());
        response.setUpdatedAt(variant.getUpdatedAt());

        return response;
    }
}
