package com.Venkatesh.bus_reservation.service;

import com.Venkatesh.bus_reservation.dto.AuthResponse;
import com.Venkatesh.bus_reservation.dto.LoginRequest;
import com.Venkatesh.bus_reservation.dto.RegisterRequest;
import com.Venkatesh.bus_reservation.entity.User;
import com.Venkatesh.bus_reservation.exception.BadRequestException;
import com.Venkatesh.bus_reservation.repository.UserRepository;
import com.Venkatesh.bus_reservation.security.CustomUserDetailsService;
import com.Venkatesh.bus_reservation.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER") // Default role
                .build();

        userRepository.save(user);

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole())
                .build();
    }
}
