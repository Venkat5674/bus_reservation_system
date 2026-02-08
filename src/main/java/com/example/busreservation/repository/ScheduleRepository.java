package com.example.busreservation.repository;

import com.example.busreservation.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // Custom query to search schedules
    @Query("SELECT s FROM Schedule s WHERE " +
            "(:source IS NULL OR s.route.source LIKE %:source%) AND " +
            "(:destination IS NULL OR s.route.destination LIKE %:destination%) AND " +
            "(:date IS NULL OR s.departureTime >= :date)")
    List<Schedule> findSchedules(@Param("source") String source,
            @Param("destination") String destination,
            @Param("date") LocalDateTime date);
}
