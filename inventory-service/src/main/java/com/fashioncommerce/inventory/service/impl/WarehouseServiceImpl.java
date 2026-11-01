package com.fashioncommerce.inventory.service.impl;

import com.fashioncommerce.inventory.dto.WarehouseRequestDTO;
import com.fashioncommerce.inventory.dto.WarehouseResponseDTO;
import com.fashioncommerce.inventory.entity.Warehouse;
import com.fashioncommerce.inventory.repository.WarehouseRepository;
import com.fashioncommerce.inventory.service.WarehouseService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseServiceImpl(
            WarehouseRepository warehouseRepository
    ) {
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public WarehouseResponseDTO createWarehouse(
            WarehouseRequestDTO request
    ) {

        if (warehouseRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException(
                    "Warehouse code already exists"
            );
        }

        Warehouse warehouse = new Warehouse();

        warehouse.setName(request.name());
        warehouse.setCode(request.code());
        warehouse.setLocation(request.location());
        warehouse.setActive(true);

        Warehouse saved = warehouseRepository.save(warehouse);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponseDTO> getAllWarehouses() {

        return warehouseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponseDTO getWarehouseById(Long id) {

        Warehouse warehouse = findWarehouse(id);

        return mapToResponse(warehouse);
    }

    @Override
    public WarehouseResponseDTO updateWarehouse(
            Long id,
            WarehouseRequestDTO request
    ) {

        Warehouse warehouse = findWarehouse(id);

        if (!Boolean.TRUE.equals(warehouse.getActive())) {
            throw new IllegalStateException(
                    "Cannot update a deactivated warehouse"
            );
        }

        if (warehouseRepository.existsByCodeAndIdNot(
                request.code(), id
        )) {
            throw new IllegalArgumentException(
                    "Warehouse code already exists"
            );
        }

        warehouse.setName(request.name());
        warehouse.setCode(request.code());
        warehouse.setLocation(request.location());

        Warehouse updated = warehouseRepository.save(warehouse);

        return mapToResponse(updated);
    }

    @Override
    public void deactivateWarehouse(Long id) {

        Warehouse warehouse = findWarehouse(id);

        warehouse.setActive(false);

        warehouseRepository.save(warehouse);
    }

    private Warehouse findWarehouse(Long id) {

        return warehouseRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Warehouse not found with ID: " + id
                        )
                );
    }

    private WarehouseResponseDTO mapToResponse(
            Warehouse warehouse
    ) {

        return new WarehouseResponseDTO(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getCode(),
                warehouse.getLocation(),
                warehouse.getActive()
        );
    }
}