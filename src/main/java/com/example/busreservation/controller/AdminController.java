package com.example.busreservation.controller;

import com.example.busreservation.entity.Bus;
import com.example.busreservation.entity.Route;
import com.example.busreservation.entity.Schedule;
import com.example.busreservation.entity.User;
import com.example.busreservation.service.BusService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private BusService busService;

    // Helper method to check admin role
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session))
            return "redirect:/login";
        return "admin";
    }

    @GetMapping("/add-bus")
    public String addBusPage(HttpSession session, Model model) {
        if (!isAdmin(session))
            return "redirect:/login";
        model.addAttribute("bus", new Bus());
        return "addBus";
    }

    @PostMapping("/add-bus")
    public String addBus(@ModelAttribute Bus bus, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/login";
        busService.addBus(bus);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/add-route")
    public String addRoutePage(HttpSession session, Model model) {
        if (!isAdmin(session))
            return "redirect:/login";
        model.addAttribute("route", new Route());
        return "addRoute";
    }

    @PostMapping("/add-route")
    public String addRoute(@ModelAttribute Route route, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/login";
        busService.addRoute(route);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/add-schedule")
    public String addSchedulePage(HttpSession session, Model model) {
        if (!isAdmin(session))
            return "redirect:/login";
        model.addAttribute("schedule", new Schedule());
        model.addAttribute("buses", busService.getAllBuses());
        model.addAttribute("routes", busService.getAllRoutes());
        return "addSchedule";
    }

    @PostMapping("/add-schedule")
    public String addSchedule(@ModelAttribute Schedule schedule, HttpSession session) {
        if (!isAdmin(session))
            return "redirect:/login";
        // Manual binding for dates might be needed if @DateTimeFormat fails on object
        // binding
        // For simplicity, assuming standard binding works or using @InitBinder
        busService.addSchedule(schedule);
        return "redirect:/admin/dashboard";
    }
}
