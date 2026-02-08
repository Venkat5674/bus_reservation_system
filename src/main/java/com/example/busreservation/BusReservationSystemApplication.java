package com.example.busreservation;

import com.example.busreservation.entity.User;
import com.example.busreservation.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BusReservationSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusReservationSystemApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(UserRepository repository) {
        return (args) -> {
            if (repository.findByEmail("admin@bus.com").isEmpty()) {
                User admin = new User();
                admin.setName("Administrator");
                admin.setEmail("admin@bus.com");
                admin.setPassword("admin"); // In a real app, you should encode this password!
                admin.setRole("ADMIN");
                repository.save(admin);
                System.out.println("Default admin user created: admin@bus.com / admin");
            }
        };
    }

}
