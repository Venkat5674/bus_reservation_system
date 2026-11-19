package com.Venkatesh.bus_reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Venkatesh.bus_reservation.entity.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBusIdAndSeatNumber(Long busId, Integer seatNumber);

    Optional<Booking> findByBusIdAndSeatNumberAndStatus(Long busId, Integer seatNumber, String status);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByBusIdAndStatus(Long busId, String status);
}
