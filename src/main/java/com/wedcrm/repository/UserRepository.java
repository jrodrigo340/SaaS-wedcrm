package com.wedcrm.repository;

import com.wedcrm.entity.User;
import com.wedcrm.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByRefreshToken(String token);

    List<User> findAllByRole(Role role);

    boolean existsByEmail(String email);

    List<User> findAllByActiveTrue();

}
