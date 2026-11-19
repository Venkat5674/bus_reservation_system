package com.Venkatesh.bus_reservation.service;

import com.Venkatesh.bus_reservation.entity.Booking;
import com.Venkatesh.bus_reservation.entity.Bus;
import com.Venkatesh.bus_reservation.entity.User;
import com.Venkatesh.bus_reservation.exception.BadRequestException;
import com.Venkatesh.bus_reservation.repository.BookingRepository;
import com.Venkatesh.bus_reservation.repository.BusRepository;
import com.Venkatesh.bus_reservation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusRepository busRepository;

    @InjectMocks
    private BookingService bookingService;

    private User sampleUser;
    private Bus sampleBus;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("Passenger")
                .email("passenger@example.com")
                .role("ROLE_USER")
                .build();

        sampleBus = Bus.builder()
                .id(10L)
                .busNumber("BUS-101")
                .source("City A")
                .destination("City B")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(4))
                .totalSeats(40)
                .availableSeats(40)
                .price(25.0)
                .build();
    }

    @Test
    void bookSeat_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(busRepository.findById(10L)).thenReturn(Optional.of(sampleBus));
        when(bookingRepository.findByBusIdAndSeatNumberAndStatus(10L, 5, "CONFIRMED")).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.bookSeat(1L, 10L, 5);

        assertNotNull(booking);
        assertEquals(5, booking.getSeatNumber());
        assertEquals("CONFIRMED", booking.getStatus());
        assertEquals(39, sampleBus.getAvailableSeats());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void bookSeat_InvalidSeatNumber_ThrowsBadRequestException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(busRepository.findById(10L)).thenReturn(Optional.of(sampleBus));

        assertThrows(BadRequestException.class, () -> bookingService.bookSeat(1L, 10L, 50));
        assertThrows(BadRequestException.class, () -> bookingService.bookSeat(1L, 10L, 0));
        assertThrows(BadRequestException.class, () -> bookingService.bookSeat(1L, 10L, null));
    }

    @Test
    void bookSeat_SeatAlreadyBooked_ThrowsBadRequestException() {
        Booking existing = Booking.builder()
                .id(99L)
                .seatNumber(5)
                .status("CONFIRMED")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(busRepository.findById(10L)).thenReturn(Optional.of(sampleBus));
        when(bookingRepository.findByBusIdAndSeatNumberAndStatus(10L, 5, "CONFIRMED")).thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class, () -> bookingService.bookSeat(1L, 10L, 5));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_Success() {
        sampleBus.setAvailableSeats(39);
        Booking booking = Booking.builder()
                .id(100L)
                .bus(sampleBus)
                .user(sampleUser)
                .seatNumber(5)
                .status("CONFIRMED")
                .build();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        String result = bookingService.cancelBooking(100L);

        assertEquals("Booking cancelled successfully", result);
        assertEquals("CANCELLED", booking.getStatus());
        assertEquals(40, sampleBus.getAvailableSeats());
    }

    @Test
    void cancelBooking_AlreadyCancelled_ThrowsBadRequestException() {
        Booking booking = Booking.builder()
                .id(100L)
                .bus(sampleBus)
                .seatNumber(5)
                .status("CANCELLED")
                .build();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThrows(BadRequestException.class, () -> bookingService.cancelBooking(100L));
    }
}
