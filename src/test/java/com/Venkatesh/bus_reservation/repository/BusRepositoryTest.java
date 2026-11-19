package com.Venkatesh.bus_reservation.repository;

import com.Venkatesh.bus_reservation.entity.Bus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BusRepositoryTest {

    @Autowired
    private BusRepository busRepository;

    @Test
    void testSaveAndFindBySourceAndDestination() {
        Bus bus = Bus.builder()
                .busNumber("BUS-XYZ-1")
                .source("New York")
                .destination("Boston")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(5))
                .totalSeats(50)
                .availableSeats(50)
                .price(45.0)
                .build();

        busRepository.save(bus);

        List<Bus> found = busRepository.findBySourceAndDestination("New York", "Boston");
        assertFalse(found.isEmpty());
        assertEquals("BUS-XYZ-1", found.get(0).getBusNumber());
    }

    @Test
    void testExistsByBusNumber() {
        Bus bus = Bus.builder()
                .busNumber("BUS-UNIQUE-01")
                .source("City X")
                .destination("City Y")
                .departureTime(LocalDateTime.now().plusDays(2))
                .arrivalTime(LocalDateTime.now().plusDays(2).plusHours(3))
                .totalSeats(30)
                .availableSeats(30)
                .price(20.0)
                .build();

        busRepository.save(bus);

        assertTrue(busRepository.existsByBusNumber("BUS-UNIQUE-01"));
        assertFalse(busRepository.existsByBusNumber("NON-EXISTENT"));
    }
}
