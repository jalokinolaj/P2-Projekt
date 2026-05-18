package com.example.Repositories;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
