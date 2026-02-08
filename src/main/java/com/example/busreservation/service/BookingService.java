package com.example.busreservation.service;

import com.example.busreservation.entity.Booking;
import com.example.busreservation.entity.Schedule;
import com.example.busreservation.entity.User;
import com.example.busreservation.repository.BookingRepository;
import com.example.busreservation.repository.ScheduleRepository;
import com.example.busreservation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Booking bookSeat(Long userId, Long scheduleId, Integer seatNumber) {
        // Check if seat is already booked
        if (bookingRepository.existsByScheduleIdAndSeatNumber(scheduleId, seatNumber)) {
            throw new RuntimeException("Seat " + seatNumber + " is already booked for this schedule.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSchedule(schedule);
        booking.setSeatNumber(seatNumber);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("CONFIRMED");

        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> getBookingsBySchedule(Long scheduleId) {
        return bookingRepository.findByScheduleId(scheduleId);
    }
}
