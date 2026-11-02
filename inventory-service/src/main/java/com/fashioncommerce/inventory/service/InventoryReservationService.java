
package com.fashioncommerce.inventory.service;

import com.fashioncommerce.inventory.dto.*;

import java.util.List;

public interface InventoryReservationService {

    ReservationResponseDTO createReservation(
            CreateReservationRequestDTO request
    );

    ReservationResponseDTO confirmReservation(Long reservationId);

    ReservationResponseDTO cancelReservation(Long reservationId);

    ReservationResponseDTO reverseReservation(Long reservationId);

    List<ReservationResponseDTO> getReservationsByOrder(Long orderId);
}
