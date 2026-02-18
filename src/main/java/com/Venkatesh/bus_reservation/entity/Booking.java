package com.Venkatesh.bus_reservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"bus_id", "seatNumber"})
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @Column(nullable = false)
    private Integer seatNumber;

    private LocalDateTime bookingTime;

    @Column(nullable = false)
    private String status; // CONFIRMED, CANCELLED
}
