package com.endava.endabank.dao;

import com.endava.endabank.model.ForgotUserPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgotUserPasswordTokenDao extends JpaRepository<ForgotUserPasswordToken, Integer> {
    Optional<ForgotUserPasswordToken> findByUserId(Integer id);
}
