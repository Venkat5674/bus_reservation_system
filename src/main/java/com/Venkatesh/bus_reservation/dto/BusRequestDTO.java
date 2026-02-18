package com.Venkatesh.bus_reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusRequestDTO {

    @NotBlank(message = "Bus number is required")
    private String busNumber;

    @NotBlank(message = "Source is required")
    private String source;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    @Future(message = "Arrival time must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Total seats is required")
    private Integer totalSeats;

    @NotNull(message = "Price is required")
    private Double price;
}
