package com.fashioncommerce.inventory.service;

import com.fashioncommerce.inventory.dto.WarehouseRequestDTO;
import com.fashioncommerce.inventory.dto.WarehouseResponseDTO;

import java.util.List;

public interface WarehouseService {

    WarehouseResponseDTO createWarehouse(WarehouseRequestDTO request);

    List<WarehouseResponseDTO> getAllWarehouses();

    WarehouseResponseDTO getWarehouseById(Long id);

    WarehouseResponseDTO updateWarehouse(
            Long id,
            WarehouseRequestDTO request
    );

    void deactivateWarehouse(Long id);
}