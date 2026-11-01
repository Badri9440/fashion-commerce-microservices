package com.fashioncommerce.inventory.service.impl;

import com.fashioncommerce.inventory.dto.*;
import com.fashioncommerce.inventory.entity.*;
import com.fashioncommerce.inventory.repository.*;
import com.fashioncommerce.inventory.service.InventoryReservationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class InventoryReservationServiceImpl
        implements InventoryReservationService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;

    public InventoryReservationServiceImpl(
            InventoryRepository inventoryRepository,
            InventoryReservationRepository reservationRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public ReservationResponseDTO createReservation(
            CreateReservationRequestDTO request
    ) {
        var existing = reservationRepository.findByOrderIdAndInventoryId(
                request.orderId(),
                request.inventoryId()
        );

        if (existing.isPresent()) {
            InventoryReservation reservation = existing.get();

            if (!reservation.getQuantity().equals(request.quantity())) {
                throw new IllegalStateException(
                        "Reservation already exists with a different quantity"
                );
            }

            return mapToResponse(reservation);
        }

        Inventory inventory = findInventory(request.inventoryId());

        if (!Boolean.TRUE.equals(inventory.getWarehouse().getActive())) {
            throw new IllegalStateException("Warehouse is inactive");
        }

        int available = inventory.getQuantity()
                - inventory.getReservedQuantity();

        if (request.quantity() > available) {
            throw new IllegalStateException(
                    "Insufficient available stock. Available: " + available
            );
        }

        inventory.setReservedQuantity(
                Math.addExact(
                        inventory.getReservedQuantity(),
                        request.quantity()
                )
        );

        inventoryRepository.saveAndFlush(inventory);

        InventoryReservation reservation = new InventoryReservation();
        reservation.setOrderId(request.orderId());
        reservation.setInventory(inventory);
        reservation.setQuantity(request.quantity());
        reservation.setStatus(ReservationStatus.ACTIVE);

        return mapToResponse(
                reservationRepository.saveAndFlush(reservation)
        );
    }

    @Override
    public ReservationResponseDTO confirmReservation(Long reservationId) {
        InventoryReservation reservation = findReservation(reservationId);

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return mapToResponse(reservation);
        }

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only ACTIVE reservations can be confirmed"
            );
        }

        Inventory inventory = reservation.getInventory();
        int quantity = reservation.getQuantity();

        if (inventory.getQuantity() < quantity
                || inventory.getReservedQuantity() < quantity) {
            throw new IllegalStateException(
                    "Inventory quantity is inconsistent"
            );
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity
        );

        reservation.setStatus(ReservationStatus.CONFIRMED);

        inventoryRepository.saveAndFlush(inventory);
        reservationRepository.saveAndFlush(reservation);

        return mapToResponse(reservation);
    }

    @Override
    public ReservationResponseDTO cancelReservation(Long reservationId) {
        InventoryReservation reservation = findReservation(reservationId);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return mapToResponse(reservation);
        }

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only ACTIVE reservations can be cancelled"
            );
        }

        Inventory inventory = reservation.getInventory();
        int quantity = reservation.getQuantity();

        if (inventory.getReservedQuantity() < quantity) {
            throw new IllegalStateException(
                    "Reserved quantity is inconsistent"
            );
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity
        );

        reservation.setStatus(ReservationStatus.CANCELLED);

        inventoryRepository.saveAndFlush(inventory);
        reservationRepository.saveAndFlush(reservation);

        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> getReservationsByOrder(Long orderId) {
        return reservationRepository.findByOrderId(orderId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Inventory findInventory(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Inventory not found with ID: " + id
                ));
    }

    private InventoryReservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Reservation not found with ID: " + id
                ));
    }

    private ReservationResponseDTO mapToResponse(
            InventoryReservation reservation
    ) {
        return new ReservationResponseDTO(
                reservation.getId(),
                reservation.getOrderId(),
                reservation.getInventory().getId(),
                reservation.getQuantity(),
                reservation.getStatus()
        );
    }
}