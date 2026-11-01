package com.fashioncommerce.inventory.controller;

import com.fashioncommerce.inventory.dto.*;
import com.fashioncommerce.inventory.service.InventoryReservationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class InventoryReservationController {

    private final InventoryReservationService reservationService;

    public InventoryReservationController(
            InventoryReservationService reservationService
    ) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDTO> create(
            @Valid @RequestBody CreateReservationRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(request));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ReservationResponseDTO> confirm(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                reservationService.confirmReservation(id)
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponseDTO> cancel(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                reservationService.cancelReservation(id)
        );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ReservationResponseDTO>> getByOrder(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                reservationService.getReservationsByOrder(orderId)
        );
    }
}