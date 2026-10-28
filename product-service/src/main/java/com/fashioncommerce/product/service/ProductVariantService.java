package com.fashioncommerce.product.service;

import com.fashioncommerce.product.dto.ProductVariantRequestDTO;
import com.fashioncommerce.product.dto.ProductVariantResponseDTO;

import java.util.List;

public interface ProductVariantService {

    ProductVariantResponseDTO createVariant(
            ProductVariantRequestDTO request);

    ProductVariantResponseDTO getVariantById(Long id);

    ProductVariantResponseDTO getVariantBySku(String sku);

    List<ProductVariantResponseDTO> getVariantsByProductId(
            Long productId);

    ProductVariantResponseDTO updateVariant(
            Long id, ProductVariantRequestDTO request);

    void deleteVariant(Long id);
}