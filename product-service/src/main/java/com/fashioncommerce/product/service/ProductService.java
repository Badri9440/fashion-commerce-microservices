package com.fashioncommerce.product.service;

import com.fashioncommerce.product.dto.ProductRequestDTO;
import com.fashioncommerce.product.dto.ProductResponseDTO;

import java.util.List;

public interface ProductService {

    ProductResponseDTO createProduct(ProductRequestDTO request);

    ProductResponseDTO getProductById(Long id);

    ProductResponseDTO getProductByCode(String productCode);

    List<ProductResponseDTO> getAllProducts();

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO request);

    void deleteProduct(Long id);
}