
package com.fashioncommerce.product.service.impl;

import com.fashioncommerce.product.dto.ProductVariantRequestDTO;
import com.fashioncommerce.product.dto.ProductVariantResponseDTO;
import com.fashioncommerce.product.entity.Product;
import com.fashioncommerce.product.entity.ProductVariant;
import com.fashioncommerce.product.repository.ProductRepository;
import com.fashioncommerce.product.repository.ProductVariantRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductVariantServiceImplTest {

    @Mock
    private ProductVariantRepository variantRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductVariantServiceImpl variantService;

    private Product product;
    private ProductVariant variant;
    private ProductVariantRequestDTO request;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Cotton T-Shirt");
        product.setProductCode("TSH-001");

        variant = new ProductVariant();
        variant.setId(10L);
        variant.setProduct(product);
        variant.setSku("TSH-001-BLK-M");
        variant.setSize("M");
        variant.setColor("Black");
        variant.setPrice(new BigDecimal("599.00"));
        variant.setActive(true);

        request = new ProductVariantRequestDTO();
        request.setProductId(1L);
        request.setSku("TSH-001-BLK-M");
        request.setSize("M");
        request.setColor("Black");
        request.setPrice(new BigDecimal("599.00"));
        request.setActive(true);
    }

    @Test
    void createVariant_success() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(variantRepository.existsByProductIdAndSizeAndColor(
                1L, "M", "Black")).thenReturn(false);

        when(variantRepository.saveAndFlush(any(ProductVariant.class)))
                .thenReturn(variant);

        ProductVariantResponseDTO response =
                variantService.createVariant(request);

        assertEquals(10L, response.getId());
        assertEquals("TSH-001-BLK-M", response.getSku());
        assertEquals(0,
                new BigDecimal("599.00").compareTo(response.getPrice()));

        verify(variantRepository).saveAndFlush(any(ProductVariant.class));
    }

    @Test
    void createVariant_productNotFound() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> variantService.createVariant(request));

        assertEquals("Product not found with ID: 1",
                exception.getMessage());

        verify(variantRepository, never())
                .saveAndFlush(any(ProductVariant.class));
    }

    @Test
    void createVariant_duplicateSizeColor() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(variantRepository.existsByProductIdAndSizeAndColor(
                1L, "M", "Black")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> variantService.createVariant(request));

        assertEquals(
                "Variant with this size and color already exists for this product",
                exception.getMessage());

        verify(variantRepository, never())
                .saveAndFlush(any(ProductVariant.class));
    }

    @Test
    void getVariantById_success() {
        when(variantRepository.findById(10L))
                .thenReturn(Optional.of(variant));

        ProductVariantResponseDTO response =
                variantService.getVariantById(10L);

        assertEquals("TSH-001-BLK-M", response.getSku());
    }

    @Test
    void getVariantById_notFound() {
        when(variantRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> variantService.getVariantById(99L));
    }

    @Test
    void getVariantBySku_success() {
        when(variantRepository.findBySku("TSH-001-BLK-M"))
                .thenReturn(Optional.of(variant));

        ProductVariantResponseDTO response =
                variantService.getVariantBySku("TSH-001-BLK-M");

        assertEquals(10L, response.getId());
    }

    @Test
    void getVariantsByProductId_success() {
        when(productRepository.existsById(1L))
                .thenReturn(true);

        when(variantRepository.findByProductId(1L))
                .thenReturn(List.of(variant));

        List<ProductVariantResponseDTO> responses =
                variantService.getVariantsByProductId(1L);

        assertEquals(1, responses.size());
    }

    @Test
    void getVariantsByProductId_productNotFound() {
        when(productRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> variantService.getVariantsByProductId(99L));
    }

    @Test
    void updateVariant_success() {
        when(variantRepository.findById(10L))
                .thenReturn(Optional.of(variant));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(variantRepository.saveAndFlush(any(ProductVariant.class)))
                .thenReturn(variant);

        request.setPrice(new BigDecimal("649.00"));

        ProductVariantResponseDTO response =
                variantService.updateVariant(10L, request);

        assertEquals(0,
                new BigDecimal("649.00").compareTo(response.getPrice()));

        verify(variantRepository).saveAndFlush(variant);
    }

    @Test
    void updateVariant_duplicateSku() {
        when(variantRepository.findById(10L))
                .thenReturn(Optional.of(variant));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        request.setSku("EXISTING-SKU");

        when(variantRepository.existsBySku("EXISTING-SKU"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> variantService.updateVariant(10L, request));

        assertEquals("Variant SKU already exists",
                exception.getMessage());
    }

    @Test
    void deleteVariant_success() {
        when(variantRepository.findById(10L))
                .thenReturn(Optional.of(variant));

        variantService.deleteVariant(10L);

        verify(variantRepository).delete(variant);
    }

    @Test
    void deleteVariant_notFound() {
        when(variantRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> variantService.deleteVariant(99L));

        verify(variantRepository, never())
                .delete(any(ProductVariant.class));
    }
    @Test
    void createVariant_duplicateSku_throwsException() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(variantRepository.existsBySku("TSH-001-BLK-M"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> variantService.createVariant(request)
        );

        assertEquals("Variant SKU already exists",
                exception.getMessage());

        verify(variantRepository, never())
                .saveAndFlush(any(ProductVariant.class));
    }
}
