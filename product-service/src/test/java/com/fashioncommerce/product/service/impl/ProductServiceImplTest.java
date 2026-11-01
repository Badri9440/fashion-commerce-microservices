
package com.fashioncommerce.product.service.impl;

import com.fashioncommerce.product.dto.ProductRequestDTO;
import com.fashioncommerce.product.dto.ProductResponseDTO;
import com.fashioncommerce.product.entity.Product;
import com.fashioncommerce.product.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequestDTO request;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Cotton T-Shirt");
        product.setProductCode("TSH-001");
        product.setDescription("Premium cotton");
        product.setCategory("Men");
        product.setActive(true);

        request = new ProductRequestDTO();
        request.setName("Cotton T-Shirt");
        request.setProductCode("TSH-001");
        request.setDescription("Premium cotton");
        request.setCategory("Men");
        request.setActive(true);
    }

    @Test
    void createProduct_success() {
        when(productRepository.existsByProductCode("TSH-001"))
                .thenReturn(false);
        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        ProductResponseDTO response =
                productService.createProduct(request);

        assertNotNull(response);
        assertEquals("TSH-001", response.getProductCode());
        assertEquals("Cotton T-Shirt", response.getName());

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_duplicateCode_throwsException() {
        when(productRepository.existsByProductCode("TSH-001"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(request)
        );

        assertEquals("Product code already exists",
                exception.getMessage());

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void getProductById_success() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        ProductResponseDTO response =
                productService.getProductById(1L);

        assertEquals(1L, response.getId());
        assertEquals("TSH-001", response.getProductCode());
    }

    @Test
    void getProductById_notFound_throwsException() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.getProductById(99L)
        );

        assertEquals("Product not found with ID: 99",
                exception.getMessage());
    }

    @Test
    void getProductByCode_success() {
        when(productRepository.findByProductCode("TSH-001"))
                .thenReturn(Optional.of(product));

        ProductResponseDTO response =
                productService.getProductByCode("TSH-001");

        assertEquals(1L, response.getId());
    }

    @Test
    void getAllProducts_success() {
        when(productRepository.findAll())
                .thenReturn(List.of(product));

        List<ProductResponseDTO> responses =
                productService.getAllProducts();

        assertEquals(1, responses.size());
        assertEquals("TSH-001",
                responses.get(0).getProductCode());
    }

    @Test
    void updateProduct_success() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        when(productRepository.saveAndFlush(any(Product.class)))
                .thenReturn(product);

        request.setName("Updated Cotton T-Shirt");

        ProductResponseDTO response =
                productService.updateProduct(1L, request);

        assertEquals("Updated Cotton T-Shirt",
                response.getName());

        verify(productRepository)
                .saveAndFlush(any(Product.class));
    }

    @Test
    void deleteProduct_success() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }

}
