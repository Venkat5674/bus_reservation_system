package com.Venkatesh.bus_reservation.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String role;
}
