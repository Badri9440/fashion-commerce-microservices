package com.fashioncommerce.product.service.impl;

import com.fashioncommerce.product.dto.ProductRequestDTO;
import com.fashioncommerce.product.dto.ProductResponseDTO;
import com.fashioncommerce.product.entity.Product;
import com.fashioncommerce.product.repository.ProductRepository;
import com.fashioncommerce.product.service.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO request) {

        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new IllegalArgumentException("Product code already exists");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setProductCode(request.getProductCode());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setActive(request.getActive());

        Product savedProduct = productRepository.save(product);

        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found with ID: " + id));

        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductByCode(String productCode) {

        Product product = productRepository.findByProductCode(productCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found with code: " + productCode));

        return mapToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found with ID: " + id));

        if (!product.getProductCode().equals(request.getProductCode())
                && productRepository.existsByProductCode(request.getProductCode())) {
            throw new IllegalArgumentException("Product code already exists");
        }

        product.setName(request.getName());
        product.setProductCode(request.getProductCode());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setActive(request.getActive());

        Product updatedProduct = productRepository.saveAndFlush(product);

        return mapToResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product not found with ID: " + id));

        productRepository.delete(product);
    }

    private ProductResponseDTO mapToResponse(Product product) {

        ProductResponseDTO response = new ProductResponseDTO();

        response.setId(product.getId());
        response.setName(product.getName());
        response.setProductCode(product.getProductCode());
        response.setDescription(product.getDescription());
        response.setCategory(product.getCategory());
        response.setActive(product.getActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        return response;
    }
}