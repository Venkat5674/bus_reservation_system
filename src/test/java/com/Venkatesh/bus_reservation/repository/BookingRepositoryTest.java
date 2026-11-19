package com.Venkatesh.bus_reservation.repository;

import com.Venkatesh.bus_reservation.entity.Booking;
import com.Venkatesh.bus_reservation.entity.Bus;
import com.Venkatesh.bus_reservation.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusRepository busRepository;

    @Test
    void testSaveAndFindByBusIdAndSeatNumberAndStatus() {
        User user = userRepository.save(User.builder()
                .name("Bob")
                .email("bob@test.com")
                .password("pass123")
                .role("ROLE_USER")
                .build());

        Bus bus = busRepository.save(Bus.builder()
                .busNumber("BUS-REPO-1")
                .source("Origin")
                .destination("Dest")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .totalSeats(30)
                .availableSeats(29)
                .price(15.0)
                .build());

        Booking booking = bookingRepository.save(Booking.builder()
                .user(user)
                .bus(bus)
                .seatNumber(7)
                .bookingTime(LocalDateTime.now())
                .status("CONFIRMED")
                .build());

        Optional<Booking> found = bookingRepository.findByBusIdAndSeatNumberAndStatus(bus.getId(), 7, "CONFIRMED");
        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getUser().getId());

        Optional<Booking> notFound = bookingRepository.findByBusIdAndSeatNumberAndStatus(bus.getId(), 7, "CANCELLED");
        assertFalse(notFound.isPresent());
    }
}
