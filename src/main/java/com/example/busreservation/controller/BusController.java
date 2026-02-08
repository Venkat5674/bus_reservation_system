package com.example.busreservation.controller;

import com.example.busreservation.service.BusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class BusController {

    @Autowired
    private BusService busService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/search")
    public String searchBuses(@RequestParam(required = false) String source,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        LocalDateTime startOfDay = (date != null) ? date.atStartOfDay() : null;

        // Pass to service
        model.addAttribute("schedules", busService.searchSchedules(source, destination, startOfDay));

        // Pass criteria back to view to re-populate form
        model.addAttribute("searchSource", source);
        model.addAttribute("searchDestination", destination);
        model.addAttribute("searchDate", date);

        return "buses";
    }
}
