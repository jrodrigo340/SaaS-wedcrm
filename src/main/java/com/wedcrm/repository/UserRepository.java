package com.wedcrm.repository;

import com.wedcrm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User,UUID> {

}
