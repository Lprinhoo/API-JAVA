package com.example.apiproject.controller;

import com.example.apiproject.model.*;
import com.example.apiproject.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

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

    // Listar todos os agendamentos (com autorização)
    @GetMapping
    public ResponseEntity<List<Agendamento>> getAllAgendamentos() {
        OficinaUser oficinaUser = getAuthenticatedOficinaUser();
        if (oficinaUser != null) {
            // Usuário de oficina vê apenas os agendamentos da sua oficina
            return ResponseEntity.ok(agendamentoRepository.findByOficinaId(oficinaUser.getOficina().getId()));
        }

        Cliente cliente = getAuthenticatedCliente();
        if (cliente != null) {
            // Cliente vê apenas os seus agendamentos
            return ResponseEntity.ok(agendamentoRepository.findByClienteId(cliente.getId()));
        }

        // Se não for nenhum dos dois (ex: admin ou sem autenticação), pode-se decidir o que fazer
        // Por enquanto, retorna FORBIDDEN se não for nem oficina nem cliente autenticado
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Obter agendamento por ID (com autorização)
    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> getAgendamentoById(@PathVariable Long id) {
        Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);
        if (agendamentoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Agendamento agendamento = agendamentoOpt.get();
        OficinaUser oficinaUser = getAuthenticatedOficinaUser();
        Cliente cliente = getAuthenticatedCliente();

        if (oficinaUser != null) {
            // Usuário de oficina só pode ver agendamentos da sua oficina
            if (!agendamento.getOficina().getId().equals(oficinaUser.getOficina().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else if (cliente != null) {
            // Cliente só pode ver seus próprios agendamentos
            if (!agendamento.getCliente().getId().equals(cliente.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Não autenticado
        }

        return ResponseEntity.ok(agendamento);
    }

    // Criar um novo agendamento (apenas cliente)
    @PostMapping
    public ResponseEntity<?> createAgendamento(@RequestBody Agendamento agendamentoRequest) {
        Cliente cliente = getAuthenticatedCliente();
        if (cliente == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas clientes podem criar agendamentos.");
        }

        // Verifica se a oficina existe
        Optional<Oficina> oficinaOpt = oficinaRepository.findById(agendamentoRequest.getOficina().getId());
        if (oficinaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Oficina não encontrada.");
        }

        // Verifica se o veículo existe e pertence ao cliente autenticado
        Optional<Veiculo> veiculoOpt = veiculoRepository.findById(agendamentoRequest.getVeiculo().getId());
        if (veiculoOpt.isEmpty() || !veiculoOpt.get().getCliente().getId().equals(cliente.getId())) {
            return ResponseEntity.badRequest().body("Veículo não encontrado ou não pertence ao cliente autenticado.");
        }

        // Preenche o agendamento com os objetos gerenciados pelo JPA
        agendamentoRequest.setOficina(oficinaOpt.get());
        agendamentoRequest.setCliente(cliente); // Garante que o agendamento é do cliente logado
        agendamentoRequest.setVeiculo(veiculoOpt.get());
        agendamentoRequest.setStatus(AgendamentoStatus.PENDENTE); // Status inicial

        Agendamento novoAgendamento = agendamentoRepository.save(agendamentoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoAgendamento);
    }

    // Atualizar um agendamento (com autorização)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAgendamento(@PathVariable Long id, @RequestBody Agendamento agendamentoDetails) {
        Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);
        if (agendamentoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Agendamento existingAgendamento = agendamentoOpt.get();
        OficinaUser oficinaUser = getAuthenticatedOficinaUser();
        Cliente cliente = getAuthenticatedCliente();

        if (oficinaUser != null) {
            // Usuário de oficina pode atualizar agendamentos da sua oficina
            if (!existingAgendamento.getOficina().getId().equals(oficinaUser.getOficina().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            // Oficina pode alterar data/hora, descrição e status
            existingAgendamento.setDataHora(agendamentoDetails.getDataHora());
            existingAgendamento.setServicosDescricao(agendamentoDetails.getServicosDescricao());
            existingAgendamento.setStatus(agendamentoDetails.getStatus()); // Oficina pode mudar o status
        } else if (cliente != null) {
            // Cliente só pode alterar a descrição ou cancelar (se status for PENDENTE)
            if (!existingAgendamento.getCliente().getId().equals(cliente.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            existingAgendamento.setServicosDescricao(agendamentoDetails.getServicosDescricao());
            // Cliente só pode cancelar se o status atual for PENDENTE
            if (agendamentoDetails.getStatus() == AgendamentoStatus.CANCELADO && existingAgendamento.getStatus() == AgendamentoStatus.PENDENTE) {
                existingAgendamento.setStatus(AgendamentoStatus.CANCELADO);
            } else if (agendamentoDetails.getStatus() != existingAgendamento.getStatus()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cliente não pode alterar o status do agendamento para este valor.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Não autenticado
        }

        Agendamento updatedAgendamento = agendamentoRepository.save(existingAgendamento);
        return ResponseEntity.ok(updatedAgendamento);
    }

    // Deletar um agendamento (com autorização)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgendamento(@PathVariable Long id) {
        Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);
        if (agendamentoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Agendamento agendamento = agendamentoOpt.get();
        OficinaUser oficinaUser = getAuthenticatedOficinaUser();
        Cliente cliente = getAuthenticatedCliente();

        if (oficinaUser != null) {
            // Usuário de oficina só pode deletar agendamentos da sua oficina
            if (!agendamento.getOficina().getId().equals(oficinaUser.getOficina().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else if (cliente != null) {
            // Cliente só pode deletar seus próprios agendamentos (e se estiverem PENDENTE)
            if (!agendamento.getCliente().getId().equals(cliente.getId()) || agendamento.getStatus() != AgendamentoStatus.PENDENTE) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Não autenticado
        }

        agendamentoRepository.delete(agendamento);
        return ResponseEntity.noContent().build();
    }
}
