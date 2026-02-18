package com.Venkatesh.bus_reservation.config;

import com.Venkatesh.bus_reservation.entity.User;
import com.Venkatesh.bus_reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class AdminSeeder {

    @Bean
    public CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "vpamudurti@gmail.com";
            String adminPassword = "Venkatesh@123";
            String adminName = "Admin Venkatesh";

            Optional<User> existingUser = userRepository.findByEmail(adminEmail);

            if (existingUser.isPresent()) {
                User user = existingUser.get();
                user.setPassword(passwordEncoder.encode(adminPassword));
                user.setRole("ROLE_ADMIN");
                userRepository.save(user);
                System.out.println("Admin user updated: " + adminEmail);
            } else {
                User admin = User.builder()
                        .name(adminName)
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role("ROLE_ADMIN")
                        .build();
                userRepository.save(admin);
                System.out.println("Admin user created: " + adminEmail);
            }
        };
    }
}
