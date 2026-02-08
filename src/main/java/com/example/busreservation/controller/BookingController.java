package com.example.busreservation.controller;

import com.example.busreservation.entity.Booking;
import com.example.busreservation.entity.Schedule;
import com.example.busreservation.entity.User;
import com.example.busreservation.service.BookingService;
import com.example.busreservation.service.BusService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BusService busService;

    @GetMapping("/select-seat")
    public String selectSeat(@RequestParam Long scheduleId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        Schedule schedule = busService.getSchedule(scheduleId);
        List<Booking> existingBookings = bookingService.getBookingsBySchedule(scheduleId);
        Set<Integer> bookedSeats = existingBookings.stream().map(Booking::getSeatNumber).collect(Collectors.toSet());

        model.addAttribute("schedule", schedule);
        model.addAttribute("bookedSeats", bookedSeats);
        return "seats";
    }

    @PostMapping("/book-ticket")
    public String bookTicket(@RequestParam Long scheduleId, @RequestParam Integer seatNumber, HttpSession session,
            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        try {
            Booking booking = bookingService.bookSeat(user.getId(), scheduleId, seatNumber);
            return "redirect:/ticket?bookingId=" + booking.getId(); // We need a way to show ticket, pass ID or object
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/select-seat?scheduleId=" + scheduleId;
        }
    }

    @GetMapping("/ticket")
    public String viewTicket(@RequestParam(required = false) Long bookingId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        if (bookingId != null) {
            // Fetch specific booking (not implemented in service yet but needed for
            // redirect above)
            // For now, let's just show history or valid confirmation
            // In real app, we fetch by ID
        }

        List<Booking> history = bookingService.getBookingsByUser(user.getId());
        model.addAttribute("bookings", history);
        return "ticket";
    }
}
