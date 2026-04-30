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

import java.util.*;

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

    @GetMapping
    public ResponseEntity<List<Oficina>> getAllOficinas() {
        OficinaUser oficinaUser = getAuthenticatedOficinaUser();
        if (oficinaUser != null) {
            return ResponseEntity.ok(Collections.singletonList(oficinaUser.getOficina()));
        }
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
            return ResponseEntity.ok("Cliente vinculado com sucesso!");
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
                return ResponseEntity.ok("Cliente desvinculado com sucesso!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vínculo não encontrado.");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Oficina> updateOficina(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        OficinaUser oficinaUser = getAuthenticatedOficinaUser();
        if (oficinaUser == null || !oficinaUser.getOficina().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Oficina> oficinaOpt = oficinaRepository.findById(id);
        if (oficinaOpt.isPresent()) {
            Oficina existingOficina = oficinaOpt.get();
            
            if (updates.containsKey("nome")) existingOficina.setNome((String) updates.get("nome"));
            if (updates.containsKey("servicos")) existingOficina.setServicos((String) updates.get("servicos"));
            if (updates.containsKey("endereco")) existingOficina.setEndereco((String) updates.get("endereco"));
            if (updates.containsKey("latitude")) existingOficina.setLatitude(Double.valueOf(updates.get("latitude").toString()));
            if (updates.containsKey("longitude")) existingOficina.setLongitude(Double.valueOf(updates.get("longitude").toString()));
            if (updates.containsKey("telefone")) existingOficina.setTelefone((String) updates.get("telefone"));
            if (updates.containsKey("email")) existingOficina.setEmail((String) updates.get("email"));
            
            // Mapeamento específico do campo 'horarios' do cliente desktop para 'horarioFuncionamento' no banco
            if (updates.containsKey("horarios")) {
                existingOficina.setHorarioFuncionamento((String) updates.get("horarios"));
            } else if (updates.containsKey("horarioFuncionamento")) {
                existingOficina.setHorarioFuncionamento((String) updates.get("horarioFuncionamento"));
            }
            
            if (updates.containsKey("website")) existingOficina.setWebsite((String) updates.get("website"));
            if (updates.containsKey("formasPagamento")) existingOficina.setFormasPagamento((String) updates.get("formasPagamento"));
            if (updates.containsKey("especialidades")) existingOficina.setEspecialidades((String) updates.get("especialidades"));

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
