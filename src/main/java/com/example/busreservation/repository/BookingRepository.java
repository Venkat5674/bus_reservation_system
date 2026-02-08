package com.example.busreservation.repository;

import com.example.busreservation.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    List<Booking> findByScheduleId(Long scheduleId);

    boolean existsByScheduleIdAndSeatNumber(Long scheduleId, Integer seatNumber);
}
