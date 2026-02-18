package com.Venkatesh.bus_reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Venkatesh.bus_reservation.entity.*;
import com.Venkatesh.bus_reservation.repository.*;
import com.Venkatesh.bus_reservation.exception.BadRequestException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BusRepository busRepository;

    @Transactional
    public Booking bookSeat(Long userId, Long busId, Integer seatNumber) {

        // 1️⃣ Validate User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // 2️⃣ Validate Bus
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new BadRequestException("Bus not found"));

        // 3️⃣ Validate Seat Number Range
        if (seatNumber == null || seatNumber <= 0 || seatNumber > bus.getTotalSeats()) {
            throw new BadRequestException("Invalid seat number");
        }

        // 4️⃣ Check If Seat Already Booked
        bookingRepository.findByBusIdAndSeatNumber(busId, seatNumber)
                .ifPresent(existingBooking -> {
                    throw new BadRequestException("Seat already booked");
                });

        // 5️⃣ Check Availability
        if (bus.getAvailableSeats() <= 0) {
            throw new BadRequestException("No seats available");
        }

        // 6️⃣ Reduce Available Seats
        bus.setAvailableSeats(bus.getAvailableSeats() - 1);

        // 7️⃣ Create Booking
        Booking booking = Booking.builder()
                .user(user)
                .bus(bus)
                .seatNumber(seatNumber)
                .bookingTime(LocalDateTime.now())
                .status("CONFIRMED")
                .build();

        return bookingRepository.save(booking);
    }

    @Transactional
    public String cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BadRequestException("Booking not found"));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BadRequestException("Booking already cancelled");
        }

        // Restore seat
        Bus bus = booking.getBus();
        bus.setAvailableSeats(bus.getAvailableSeats() + 1);

        booking.setStatus("CANCELLED");

        return "Booking cancelled successfully";
    }

    public org.springframework.data.domain.Page<Booking> getAllBookings(
            org.springframework.data.domain.Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }
}
