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
            seedOrUpdateUser(userRepository, passwordEncoder, adminEmail, adminPassword, adminName);
            if (!adminEmail.equalsIgnoreCase("vpamudurti@gmail.com")) {
                seedOrUpdateUser(userRepository, passwordEncoder, "vpamudurti@gmail.com", "Venkatesh@123", "Admin Venkatesh");
            }
        };
    }

    private void seedOrUpdateUser(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                  String email, String password, String name) {
        if (email == null || email.isBlank()) return;
        String cleanEmail = email.trim().toLowerCase();
        Optional<User> existingUser = userRepository.findByEmail(cleanEmail);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setPassword(passwordEncoder.encode(password));
            user.setRole("ROLE_ADMIN");
            userRepository.save(user);
            log.info("Admin user credentials updated and verified: {}", cleanEmail);
        } else {
            User admin = User.builder()
                    .name(name)
                    .email(cleanEmail)
                    .password(passwordEncoder.encode(password))
                    .role("ROLE_ADMIN")
                    .build();
            userRepository.save(admin);
            log.info("Admin user created successfully: {}", cleanEmail);
        }
    }
}
