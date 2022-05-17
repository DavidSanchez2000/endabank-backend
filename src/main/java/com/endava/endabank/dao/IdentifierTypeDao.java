package com.endava.endabank.dao;

import com.endava.endabank.model.IdentifierType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdentifierTypeDao extends JpaRepository<IdentifierType, Integer> {
}
