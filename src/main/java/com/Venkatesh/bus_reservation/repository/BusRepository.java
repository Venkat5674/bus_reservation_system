package com.Venkatesh.bus_reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Venkatesh.bus_reservation.entity.Bus;

import java.util.List;

public interface BusRepository extends JpaRepository<Bus, Long> {

    List<Bus> findBySourceAndDestination(String source, String destination);

    boolean existsByBusNumber(String busNumber);
}
