package com.example.apiproject.repository;

import com.example.apiproject.model.Oficina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OficinaRepository extends JpaRepository<Oficina, Long> {
}
