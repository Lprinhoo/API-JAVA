package com.example.apiproject.controller;

import com.example.apiproject.model.Cliente;
import com.example.apiproject.model.Oficina;
import com.example.apiproject.model.OficinaUser;
import com.example.apiproject.repository.ClienteRepository;
import com.example.apiproject.repository.OficinaRepository;
import com.example.apiproject.repository.OficinaUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/oficinas")
public class OficinaController {

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private OficinaUserRepository oficinaUserRepository;

    private OficinaUser getAuthenticatedOficinaUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return oficinaUserRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    private Cliente getAuthenticatedCliente() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return clienteRepository.findByEmail(username).orElse(null);
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<Oficina>> getAllOficinas() {
        OficinaUser oficinaUser = getAuthenticatedOficinaUser();
        if (oficinaUser != null) {
            // Se for usuário de oficina, retorna apenas a dele
            return ResponseEntity.ok(Collections.singletonList(oficinaUser.getOficina()));
        }
        // Clientes e outros podem ver todas
        return ResponseEntity.ok(oficinaRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Oficina> getOficinaById(@PathVariable Long id) {
        OficinaUser oficinaUser = getAuthenticatedOficinaUser();
        if (oficinaUser != null && !oficinaUser.getOficina().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Oficina> oficina = oficinaRepository.findById(id);
        return oficina.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{oficinaId}/clientes")
    public ResponseEntity<Set<Cliente>> getClientesByOficinaId(@PathVariable Long oficinaId) {
        OficinaUser oficinaUser = getAuthenticatedOficinaUser();
        if (oficinaUser == null || !oficinaUser.getOficina().getId().equals(oficinaId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Oficina> oficinaOpt = oficinaRepository.findById(oficinaId);
        return oficinaOpt.map(oficina -> ResponseEntity.ok(oficina.getClientes()))
                         .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Oficina> createOficina(@RequestBody Oficina oficina) {
        return ResponseEntity.status(HttpStatus.CREATED).body(oficinaRepository.save(oficina));
    }

    @PostMapping("/{oficinaId}/vincular-cliente/{clienteId}")
    public ResponseEntity<?> vincularCliente(@PathVariable Long oficinaId, @PathVariable Long clienteId) {
        OficinaUser oficinaUser = getAuthenticatedOficinaUser();
        if (oficinaUser == null || !oficinaUser.getOficina().getId().equals(oficinaId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Oficina> oficinaOpt = oficinaRepository.findById(oficinaId);
        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);

        if (oficinaOpt.isPresent() && clienteOpt.isPresent()) {
            Oficina oficina = oficinaOpt.get();
            Cliente cliente = clienteOpt.get();
            oficina.getClientes().add(cliente);
            oficinaRepository.save(oficina);
            return ResponseEntity.ok("Cliente vinculado com sucesso à oficina!");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{oficinaId}/desvincular-cliente/{clienteId}")
    public ResponseEntity<?> desvincularCliente(@PathVariable Long oficinaId, @PathVariable Long clienteId) {
        OficinaUser oficinaUser = getAuthenticatedOficinaUser();
        if (oficinaUser == null || !oficinaUser.getOficina().getId().equals(oficinaId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Oficina> oficinaOpt = oficinaRepository.findById(oficinaId);
        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);

        if (oficinaOpt.isPresent() && clienteOpt.isPresent()) {
            Oficina oficina = oficinaOpt.get();
            Cliente cliente = clienteOpt.get();
            if (oficina.getClientes().remove(cliente)) {
                oficinaRepository.save(oficina);
                return ResponseEntity.ok("Cliente desvinculado com sucesso da oficina!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não estava vinculado a esta oficina.");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Oficina> updateOficina(@PathVariable Long id, @RequestBody Oficina oficinaDetails) {
        OficinaUser oficinaUser = getAuthenticatedOficinaUser();
        if (oficinaUser == null || !oficinaUser.getOficina().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Oficina> oficinaOpt = oficinaRepository.findById(id);
        if (oficinaOpt.isPresent()) {
            Oficina existingOficina = oficinaOpt.get();
            existingOficina.setNome(oficinaDetails.getNome());
            existingOficina.setServicos(oficinaDetails.getServicos());
            existingOficina.setEndereco(oficinaDetails.getEndereco());
            existingOficina.setLatitude(oficinaDetails.getLatitude());
            existingOficina.setLongitude(oficinaDetails.getLongitude());
            existingOficina.setTelefone(oficinaDetails.getTelefone());
            existingOficina.setEmail(oficinaDetails.getEmail());
            existingOficina.setHorarioFuncionamento(oficinaDetails.getHorarioFuncionamento());
            existingOficina.setWebsite(oficinaDetails.getWebsite());
            existingOficina.setFormasPagamento(oficinaDetails.getFormasPagamento());
            existingOficina.setEspecialidades(oficinaDetails.getEspecialidades());
            return ResponseEntity.ok(oficinaRepository.save(existingOficina));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOficina(@PathVariable Long id) {
        OficinaUser oficinaUser = getAuthenticatedOficinaUser();
        if (oficinaUser == null || !oficinaUser.getOficina().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Oficina> oficina = oficinaRepository.findById(id);
        if (oficina.isPresent()) {
            oficinaRepository.delete(oficina.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
