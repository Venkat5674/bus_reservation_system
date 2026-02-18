package com.Venkatesh.bus_reservation.controller;

import com.Venkatesh.bus_reservation.dto.ApiResponse;
import com.Venkatesh.bus_reservation.dto.AuthResponse;
import com.Venkatesh.bus_reservation.dto.LoginRequest;
import com.Venkatesh.bus_reservation.dto.RegisterRequest;
import com.Venkatesh.bus_reservation.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody @Valid RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
