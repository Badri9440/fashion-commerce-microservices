
package com.fashioncommerce.product.controller;

import com.fashioncommerce.product.dto.ProductVariantRequestDTO;
import com.fashioncommerce.product.dto.ProductVariantResponseDTO;
import com.fashioncommerce.product.service.ProductVariantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService variantService;

    @PostMapping
    public ResponseEntity<ProductVariantResponseDTO> createVariant(
            @Valid @RequestBody ProductVariantRequestDTO request) {

        ProductVariantResponseDTO response =
                variantService.createVariant(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductVariantResponseDTO> getVariantById(
            @PathVariable Long id) {

        return ResponseEntity.ok(variantService.getVariantById(id));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductVariantResponseDTO> getVariantBySku(
            @PathVariable String sku) {

        return ResponseEntity.ok(variantService.getVariantBySku(sku));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductVariantResponseDTO>> getVariantsByProduct(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                variantService.getVariantsByProductId(productId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductVariantResponseDTO> updateVariant(
            @PathVariable Long id,
            @Valid @RequestBody ProductVariantRequestDTO request) {

        return ResponseEntity.ok(
                variantService.updateVariant(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {

        variantService.deleteVariant(id);

        return ResponseEntity.noContent().build();
    }
}
