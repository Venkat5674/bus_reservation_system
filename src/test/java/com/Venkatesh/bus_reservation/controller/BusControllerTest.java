package com.Venkatesh.bus_reservation.controller;

import com.Venkatesh.bus_reservation.dto.BusRequestDTO;
import com.Venkatesh.bus_reservation.entity.Bus;
import com.Venkatesh.bus_reservation.repository.BusRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BusRepository busRepository;

    @Test
    void getAllBuses_PublicAccess_ReturnsOk() throws Exception {
        Bus bus = Bus.builder()
                .id(1L)
                .busNumber("BUS-001")
                .source("A")
                .destination("B")
                .totalSeats(40)
                .availableSeats(40)
                .price(20.0)
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .build();

        when(busRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(bus)));

        mockMvc.perform(get("/api/buses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].busNumber").value("BUS-001"));
    }

    @Test
    void searchBus_PublicAccess_ReturnsOk() throws Exception {
        Bus bus = Bus.builder()
                .id(1L)
                .busNumber("BUS-002")
                .source("CityA")
                .destination("CityB")
                .totalSeats(50)
                .availableSeats(50)
                .price(30.0)
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(3))
                .build();

        when(busRepository.findBySourceAndDestination("CityA", "CityB")).thenReturn(List.of(bus));

        mockMvc.perform(get("/api/buses/search")
                        .param("source", "CityA")
                        .param("destination", "CityB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].busNumber").value("BUS-002"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBus_AdminRole_ReturnsOk() throws Exception {
        BusRequestDTO request = BusRequestDTO.builder()
                .busNumber("BUS-NEW-100")
                .source("Alpha")
                .destination("Beta")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(3))
                .totalSeats(40)
                .price(35.0)
                .build();

        Bus savedBus = Bus.builder()
                .id(100L)
                .busNumber(request.getBusNumber())
                .source(request.getSource())
                .destination(request.getDestination())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .totalSeats(40)
                .availableSeats(40)
                .price(35.0)
                .build();

        when(busRepository.existsByBusNumber(request.getBusNumber())).thenReturn(false);
        when(busRepository.save(any(Bus.class))).thenReturn(savedBus);

        mockMvc.perform(post("/api/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.busNumber").value("BUS-NEW-100"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createBus_UserRole_ReturnsForbidden() throws Exception {
        BusRequestDTO request = BusRequestDTO.builder()
                .busNumber("BUS-NEW-100")
                .source("Alpha")
                .destination("Beta")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(3))
                .totalSeats(40)
                .price(35.0)
                .build();

        mockMvc.perform(post("/api/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBus_Anonymous_ReturnsUnauthorized() throws Exception {
        BusRequestDTO request = BusRequestDTO.builder()
                .busNumber("BUS-NEW-100")
                .source("Alpha")
                .destination("Beta")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(3))
                .totalSeats(40)
                .price(35.0)
                .build();

        mockMvc.perform(post("/api/buses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
