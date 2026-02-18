package com.Venkatesh.bus_reservation.controller;

import com.Venkatesh.bus_reservation.dto.ApiResponse;
import com.Venkatesh.bus_reservation.dto.PageResponse;
import com.Venkatesh.bus_reservation.dto.UserResponseDTO;
import com.Venkatesh.bus_reservation.entity.User;
import com.Venkatesh.bus_reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDTO>>> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserResponseDTO> content = userPage.getContent().stream()
                .map(this::mapToDTO)
                .toList();

        PageResponse<UserResponseDTO> pageResponse = PageResponse.<UserResponseDTO>builder()
                .content(content)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponseDTO>>builder()
                .success(true)
                .data(pageResponse)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(ApiResponse.<UserResponseDTO>builder()
                .success(true)
                .data(mapToDTO(user))
                .timestamp(LocalDateTime.now())
                .build());
    }

    // Creating users is now handled via AuthController register

    private UserResponseDTO mapToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
