
package com.fashioncommerce.inventory.service.impl;

import com.fashioncommerce.inventory.dto.InventoryRequestDTO;
import com.fashioncommerce.inventory.dto.InventoryResponseDTO;
import com.fashioncommerce.inventory.dto.StockQuantityRequestDTO;
import com.fashioncommerce.inventory.entity.Inventory;
import com.fashioncommerce.inventory.entity.Warehouse;
import com.fashioncommerce.inventory.repository.InventoryRepository;
import com.fashioncommerce.inventory.repository.WarehouseRepository;
import com.fashioncommerce.inventory.service.InventoryService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            WarehouseRepository warehouseRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public InventoryResponseDTO createInventory(
            InventoryRequestDTO request
    ) {
        Warehouse warehouse = warehouseRepository
                .findById(request.warehouseId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Warehouse not found with ID: " + request.warehouseId()
                ));

        if (!Boolean.TRUE.equals(warehouse.getActive())) {
            throw new IllegalStateException("Warehouse is inactive");
        }

        if (inventoryRepository.existsByWarehouseIdAndProductVariantId(
                request.warehouseId(),
                request.productVariantId()
        )) {
            throw new IllegalStateException(
                    "Inventory already exists for this warehouse and variant"
            );
        }

        Inventory inventory = new Inventory();
        inventory.setWarehouse(warehouse);
        inventory.setProductVariantId(request.productVariantId());
        inventory.setQuantity(request.quantity());
        inventory.setReservedQuantity(0);

        return mapToResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDTO getInventoryById(Long id) {
        return mapToResponse(findInventory(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> getInventoryByWarehouse(Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new NoSuchElementException(
                    "Warehouse not found with ID: " + warehouseId
            );
        }

        return inventoryRepository.findByWarehouseId(warehouseId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public InventoryResponseDTO addStock(
            Long inventoryId,
            StockQuantityRequestDTO request
    ) {
        Inventory inventory = findInventory(inventoryId);

        int updatedQuantity = Math.addExact(
                inventory.getQuantity(),
                request.quantity()
        );

        inventory.setQuantity(updatedQuantity);

        return mapToResponse(inventoryRepository.saveAndFlush(inventory));
    }

    private Inventory findInventory(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Inventory not found with ID: " + id
                ));
    }

    private InventoryResponseDTO mapToResponse(Inventory inventory) {
        int available = inventory.getQuantity()
                - inventory.getReservedQuantity();

        return new InventoryResponseDTO(
                inventory.getId(),
                inventory.getWarehouse().getId(),
                inventory.getProductVariantId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                available
        );
    }
}
