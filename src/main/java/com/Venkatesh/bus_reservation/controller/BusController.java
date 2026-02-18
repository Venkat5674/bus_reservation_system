package com.Venkatesh.bus_reservation.controller;

import com.Venkatesh.bus_reservation.dto.ApiResponse;
import com.Venkatesh.bus_reservation.dto.BusRequestDTO;
import com.Venkatesh.bus_reservation.dto.BusResponseDTO;
import com.Venkatesh.bus_reservation.dto.PageResponse;
import com.Venkatesh.bus_reservation.entity.Bus;
import com.Venkatesh.bus_reservation.exception.BadRequestException;
import com.Venkatesh.bus_reservation.repository.BusRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
public class BusController {

    private final BusRepository busRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BusResponseDTO>> createBus(@RequestBody @Valid BusRequestDTO request) {
        if (busRepository.existsByBusNumber(request.getBusNumber())) {
            throw new BadRequestException("Bus number " + request.getBusNumber() + " already exists");
        }

        Bus bus = Bus.builder()
                .busNumber(request.getBusNumber())
                .source(request.getSource())
                .destination(request.getDestination())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .price(request.getPrice())
                .build();

        Bus savedBus = busRepository.save(bus);

        return ResponseEntity.ok(ApiResponse.<BusResponseDTO>builder()
                .success(true)
                .message("Bus added successfully")
                .data(mapToDTO(savedBus))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BusResponseDTO>>> getAllBuses(Pageable pageable) {
        Page<Bus> busPage = busRepository.findAll(pageable);
        List<BusResponseDTO> content = busPage.getContent().stream()
                .map(this::mapToDTO)
                .toList();

        PageResponse<BusResponseDTO> pageResponse = PageResponse.<BusResponseDTO>builder()
                .content(content)
                .pageNumber(busPage.getNumber())
                .pageSize(busPage.getSize())
                .totalElements(busPage.getTotalElements())
                .totalPages(busPage.getTotalPages())
                .last(busPage.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.<PageResponse<BusResponseDTO>>builder()
                .success(true)
                .data(pageResponse)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BusResponseDTO>>> searchBus(
            @RequestParam String source,
            @RequestParam String destination) {

        List<BusResponseDTO> buses = busRepository
                .findBySourceAndDestination(source, destination)
                .stream()
                .map(this::mapToDTO)
                .toList();

        return ResponseEntity.ok(ApiResponse.<List<BusResponseDTO>>builder()
                .success(true)
                .data(buses)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private BusResponseDTO mapToDTO(Bus bus) {
        return BusResponseDTO.builder()
                .id(bus.getId())
                .busNumber(bus.getBusNumber())
                .source(bus.getSource())
                .destination(bus.getDestination())
                .departureTime(bus.getDepartureTime())
                .arrivalTime(bus.getArrivalTime())
                .totalSeats(bus.getTotalSeats())
                .availableSeats(bus.getAvailableSeats())
                .price(bus.getPrice())
                .build();
    }
}
