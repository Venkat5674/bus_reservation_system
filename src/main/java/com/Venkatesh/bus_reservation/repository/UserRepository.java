package com.Venkatesh.bus_reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Venkatesh.bus_reservation.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    java.util.Optional<User> findByEmail(String email);
}
