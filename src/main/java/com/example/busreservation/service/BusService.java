package com.example.busreservation.service;

import com.example.busreservation.entity.Bus;
import com.example.busreservation.entity.Route;
import com.example.busreservation.entity.Schedule;
import com.example.busreservation.repository.BusRepository;
import com.example.busreservation.repository.RouteRepository;
import com.example.busreservation.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BusService {

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    public Bus addBus(Bus bus) {
        return busRepository.save(bus);
    }

    public Route addRoute(Route route) {
        return routeRepository.save(route);
    }

    public Schedule addSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public List<Schedule> searchSchedules(String source, String destination, LocalDateTime date) {
        // Convert empty strings to null for the query
        String searchSource = (source != null && !source.trim().isEmpty()) ? source : null;
        String searchDestination = (destination != null && !destination.trim().isEmpty()) ? destination : null;
        // Date is already LocalDateTime or null

        return scheduleRepository.findSchedules(searchSource, searchDestination, date);
    }

    public Schedule getSchedule(Long id) {
        return scheduleRepository.findById(id).orElse(null);
    }
}
