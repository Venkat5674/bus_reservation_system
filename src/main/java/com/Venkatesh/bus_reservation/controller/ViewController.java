package com.Venkatesh.bus_reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin-login")
    public String adminLogin() {
        return "admin-login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/user-dashboard")
    public String userDashboard() {
        return "user-dashboard";
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/bus-search")
    public String busSearch() {
        return "bus-search";
    }
}
