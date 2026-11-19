package com.Venkatesh.bus_reservation.config;

import com.Venkatesh.bus_reservation.entity.User;
import com.Venkatesh.bus_reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminSeeder {

    @Value("${app.admin.email:vpamudurti@gmail.com}")
    private String adminEmail;

    @Value("${app.admin.password:Venkatesh@123}")
    private String adminPassword;

    @Value("${app.admin.name:Admin Venkatesh}")
    private String adminName;

    @Bean
    public CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<User> existingUser = userRepository.findByEmail(adminEmail);

            if (existingUser.isPresent()) {
                log.info("Admin user already present: {}", adminEmail);
            } else {
                User admin = User.builder()
                        .name(adminName)
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role("ROLE_ADMIN")
                        .build();
                userRepository.save(admin);
                log.info("Admin user initialized successfully: {}", adminEmail);
            }
        };
    }
}
