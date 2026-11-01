package com.fashioncommerce.inventory.controller;

import com.fashioncommerce.inventory.dto.WarehouseRequestDTO;
import com.fashioncommerce.inventory.dto.WarehouseResponseDTO;
import com.fashioncommerce.inventory.service.WarehouseService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(
            WarehouseService warehouseService
    ) {
        this.warehouseService = warehouseService;
    }

    @PostMapping
    public ResponseEntity<WarehouseResponseDTO> createWarehouse(
            @Valid @RequestBody WarehouseRequestDTO request
    ) {

        WarehouseResponseDTO response =
                warehouseService.createWarehouse(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<WarehouseResponseDTO>>
    getAllWarehouses() {

        return ResponseEntity.ok(
                warehouseService.getAllWarehouses()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponseDTO> getWarehouseById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                warehouseService.getWarehouseById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponseDTO> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseRequestDTO request
    ) {

        return ResponseEntity.ok(
                warehouseService.updateWarehouse(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateWarehouse(
            @PathVariable Long id
    ) {

        warehouseService.deactivateWarehouse(id);

        return ResponseEntity.noContent().build();
    }
}