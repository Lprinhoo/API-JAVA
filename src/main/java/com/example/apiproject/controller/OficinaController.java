package com.example.apiproject.controller;

import com.example.apiproject.model.Cliente;
import com.example.apiproject.model.Oficina;
import com.example.apiproject.model.OficinaRegistrationStatus;
import com.example.apiproject.model.OficinaUser;
import com.example.apiproject.repository.ClienteRepository;
import com.example.apiproject.repository.OficinaRepository;
import com.example.apiproject.repository.OficinaUserRepository;
import com.example.apiproject.dto.OficinaRegistrationCompleteRequest; // Importar o DTO
import com.example.apiproject.service.OficinaService; // Importar o serviço
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

    @Autowired
    private OficinaService oficinaService; // Injetar o serviço

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
        // Retorna apenas oficinas ativas para usuários não autenticados ou outros casos
        return ResponseEntity.ok(oficinaRepository.findByRegistrationStatus(OficinaRegistrationStatus.ACTIVE));
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

    // Endpoint para iniciar o registro da oficina após o pagamento
    // Este endpoint agora chama o serviço para gerar o token e criar a oficina pendente
    @PostMapping("/initiate-registration") // Corrigido para o endpoint correto
    public ResponseEntity<Map<String, String>> initiateOficinaRegistration() { // Nome do método corrigido
        // Aqui você integraria com um gateway de pagamento real.
        // Por enquanto, vamos simular um pagamento bem-sucedido.

        // Após a confirmação do pagamento pelo gateway:
        Map<String, String> registrationInfo = oficinaService.initiateOficinaRegistration();
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationInfo);
    }

    // Endpoint para completar o registro da oficina
    @PostMapping("/complete-registration")
    public ResponseEntity<?> completeOficinaRegistration(@RequestBody OficinaRegistrationCompleteRequest request) {
        Optional<Oficina> oficinaOpt = oficinaRepository.findByRegistrationToken(request.getRegistrationToken());

        if (oficinaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token de registro inválido ou não encontrado.");
        }

        Oficina oficina = oficinaOpt.get();

        // Verifica se a oficina já foi ativada
        if (oficina.getRegistrationStatus() == OficinaRegistrationStatus.ACTIVE) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Esta oficina já foi registrada e está ativa.");
        }
        
        // Atualiza os detalhes da oficina
        oficina.setNome(request.getNome());
        oficina.setServicos(request.getServicos());
        oficina.setEndereco(request.getEndereco());
        oficina.setLatitude(request.getLatitude());
        oficina.setLongitude(request.getLongitude());
        oficina.setTelefone(request.getTelefone());
        oficina.setEmail(request.getEmail());
        oficina.setHorarioFuncionamento(request.getHorarioFuncionamento());
        oficina.setWebsite(request.getWebsite());
        oficina.setFormasPagamento(request.getFormasPagamento());
        oficina.setEspecialidades(request.getEspecialidades());
        oficina.setRegistrationStatus(OficinaRegistrationStatus.ACTIVE); // Ativa a oficina

        oficinaRepository.save(oficina);

        return ResponseEntity.ok("Registro da oficina concluído com sucesso! ID da Oficina: " + oficina.getId());
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
            oficina.getClientes().add(clienteOpt.get());
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
