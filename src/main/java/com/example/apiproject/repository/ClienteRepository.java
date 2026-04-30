package com.example.apiproject.repository;

import com.example.apiproject.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByEmail(String email);
    Optional<Cliente> findByGoogleId(String googleId);

    // Adicionado para buscar clientes por ID da oficina
    List<Cliente> findByOficinas_Id(Long oficinaId);
}
