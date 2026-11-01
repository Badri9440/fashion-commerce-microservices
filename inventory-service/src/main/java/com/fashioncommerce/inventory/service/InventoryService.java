
package com.fashioncommerce.inventory.service;

import com.fashioncommerce.inventory.dto.InventoryRequestDTO;
import com.fashioncommerce.inventory.dto.InventoryResponseDTO;
import com.fashioncommerce.inventory.dto.StockQuantityRequestDTO;

import java.util.List;

public interface InventoryService {

    InventoryResponseDTO createInventory(InventoryRequestDTO request);

    InventoryResponseDTO getInventoryById(Long id);

    List<InventoryResponseDTO> getInventoryByWarehouse(Long warehouseId);

    InventoryResponseDTO addStock(
            Long inventoryId,
            StockQuantityRequestDTO request
    );
}
