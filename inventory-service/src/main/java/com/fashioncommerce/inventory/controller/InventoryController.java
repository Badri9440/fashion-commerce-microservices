package com.fashioncommerce.inventory.controller;

import com.fashioncommerce.inventory.dto.*;
import com.fashioncommerce.inventory.service.InventoryService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryResponseDTO> createInventory(
            @Valid @RequestBody InventoryRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createInventory(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponseDTO> getInventoryById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                inventoryService.getInventoryById(id)
        );
    }

    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryResponseDTO>> getByWarehouse(
            @PathVariable Long warehouseId
    ) {
        return ResponseEntity.ok(
                inventoryService.getInventoryByWarehouse(warehouseId)
        );
    }

    @PostMapping("/{id}/add-stock")
    public ResponseEntity<InventoryResponseDTO> addStock(
            @PathVariable Long id,
            @Valid @RequestBody StockQuantityRequestDTO request
    ) {
        return ResponseEntity.ok(
                inventoryService.addStock(id, request)
        );
    }
}