package com.Venkatesh.bus_reservation.controller;

import com.Venkatesh.bus_reservation.dto.AuthResponse;
import com.Venkatesh.bus_reservation.dto.LoginRequest;
import com.Venkatesh.bus_reservation.dto.RegisterRequest;
import com.Venkatesh.bus_reservation.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void register_ValidRequest_ReturnsOk() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice Smith")
                .email("alice.smith@example.com")
                .password("Secur3Passw0rd")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token-xyz")
                .role("ROLE_USER")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token-xyz"))
                .andExpect(jsonPath("$.data.role").value("ROLE_USER"));
    }

    @Test
    void register_InvalidEmail_ReturnsBadRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice Smith")
                .email("invalid-email-format")
                .password("Secur3Passw0rd")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    void login_ValidRequest_ReturnsOk() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("alice.smith@example.com")
                .password("Secur3Passw0rd")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token-xyz")
                .role("ROLE_USER")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token-xyz"));
    }
}
