package com.endava.endabank.dao;

import com.endava.endabank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    Optional<User> findByIdentifier(String identifier);
}
