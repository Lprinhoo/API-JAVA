package com.example.apiproject.repository;

import com.example.apiproject.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    List<Agendamento> findByOficinaId(Long oficinaId);
    List<Agendamento> findByClienteId(Long clienteId);
    List<Agendamento> findByOficinaIdAndClienteId(Long oficinaId, Long clienteId);
}
