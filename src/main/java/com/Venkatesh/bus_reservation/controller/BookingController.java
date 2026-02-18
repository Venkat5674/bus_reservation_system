package com.Venkatesh.bus_reservation.controller;

import com.Venkatesh.bus_reservation.dto.*;
import com.Venkatesh.bus_reservation.entity.Booking;
import com.Venkatesh.bus_reservation.entity.Bus;
import com.Venkatesh.bus_reservation.entity.User;
import com.Venkatesh.bus_reservation.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponseDTO>> bookSeat(
            @RequestParam Long userId,
            @RequestParam Long busId,
            @RequestParam Integer seatNumber) {

        Booking booking = bookingService.bookSeat(userId, busId, seatNumber);

        return ResponseEntity.ok(ApiResponse.<BookingResponseDTO>builder()
                .success(true)
                .data(mapToDTO(booking))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse<String>> cancelBooking(@PathVariable Long id) {
        String message = bookingService.cancelBooking(id);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookingResponseDTO>>> getAllBookings(Pageable pageable) {
        Page<Booking> bookingPage = bookingService.getAllBookings(pageable); // Need to add this method to Service
        List<BookingResponseDTO> content = bookingPage.getContent().stream()
                .map(this::mapToDTO)
                .toList();

        PageResponse<BookingResponseDTO> pageResponse = PageResponse.<BookingResponseDTO>builder()
                .content(content)
                .pageNumber(bookingPage.getNumber())
                .pageSize(bookingPage.getSize())
                .totalElements(bookingPage.getTotalElements())
                .totalPages(bookingPage.getTotalPages())
                .last(bookingPage.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.<PageResponse<BookingResponseDTO>>builder()
                .success(true)
                .data(pageResponse)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private BookingResponseDTO mapToDTO(Booking booking) {

        User user = booking.getUser();
        Bus bus = booking.getBus();

        UserResponseDTO userDTO = UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        BusResponseDTO busDTO = BusResponseDTO.builder()
                .id(bus.getId())
                .busNumber(bus.getBusNumber())
                .source(bus.getSource())
                .destination(bus.getDestination())
                .departureTime(bus.getDepartureTime())
                .arrivalTime(bus.getArrivalTime())
                .totalSeats(bus.getTotalSeats())
                .availableSeats(bus.getAvailableSeats())
                .price(bus.getPrice())
                .build();

        return BookingResponseDTO.builder()
                .id(booking.getId())
                .user(userDTO)
                .bus(busDTO)
                .seatNumber(booking.getSeatNumber())
                .bookingTime(booking.getBookingTime())
                .status(booking.getStatus())
                .build();
    }
}
