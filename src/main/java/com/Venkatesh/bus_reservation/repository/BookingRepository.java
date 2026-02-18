package com.Venkatesh.bus_reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Venkatesh.bus_reservation.entity.Booking;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBusIdAndSeatNumber(Long busId, Integer seatNumber);
}
