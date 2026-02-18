package com.Venkatesh.bus_reservation.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponseDTO {

    private Long id;
    private UserResponseDTO user;
    private BusResponseDTO bus;
    private Integer seatNumber;
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingTime;
    private String status;
}
