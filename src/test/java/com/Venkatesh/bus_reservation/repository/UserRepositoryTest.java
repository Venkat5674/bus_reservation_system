package com.Venkatesh.bus_reservation.repository;

import com.Venkatesh.bus_reservation.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindByEmail() {
        User user = User.builder()
                .name("Alice")
                .email("alice@test.com")
                .password("secret123")
                .role("ROLE_USER")
                .build();

        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("alice@test.com");
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
        assertTrue(userRepository.existsByEmail("alice@test.com"));
        assertFalse(userRepository.existsByEmail("bob@test.com"));
    }
}
