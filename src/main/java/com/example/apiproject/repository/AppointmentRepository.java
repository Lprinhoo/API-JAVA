package com.example.apiproject.repository;

import com.example.apiproject.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserProfileId(Long userId);
    List<Appointment> findByOficinaId(Long oficinaId);
}
