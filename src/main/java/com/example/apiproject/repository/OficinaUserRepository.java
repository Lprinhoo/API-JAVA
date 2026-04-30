package com.example.apiproject.repository;

import com.example.apiproject.model.OficinaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OficinaUserRepository extends JpaRepository<OficinaUser, Long> {
    Optional<OficinaUser> findByUsername(String username);
}
