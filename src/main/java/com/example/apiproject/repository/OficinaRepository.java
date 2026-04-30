package com.example.apiproject.repository;

import com.example.apiproject.model.Oficina;
import com.example.apiproject.model.OficinaRegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OficinaRepository extends JpaRepository<Oficina, Long> {
    Optional<Oficina> findByRegistrationToken(String registrationToken);
    List<Oficina> findByRegistrationStatus(OficinaRegistrationStatus status);
}
