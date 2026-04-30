package com.example.apiproject.controller;

import com.example.apiproject.model.Cliente;
import com.example.apiproject.model.Veiculo;
import com.example.apiproject.repository.ClienteRepository;
import com.example.apiproject.repository.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping
    public ResponseEntity<List<Veiculo>> getAllVeiculos() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Se for cliente, retorna apenas os veículos dele
        Optional<Cliente> clienteOpt = clienteRepository.findByEmail(username);
        if (clienteOpt.isPresent()) {
            return ResponseEntity.ok(veiculoRepository.findByClienteId(clienteOpt.get().getId()));
        }

        // Se for admin ou outro role, por enquanto permite ver todos (ajustar se necessário)
        return ResponseEntity.ok(veiculoRepository.findAll());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Veiculo>> getVeiculosByClienteId(@PathVariable Long clienteId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Verifica se o cliente logado está tentando acessar seus próprios veículos
        Optional<Cliente> clienteLogado = clienteRepository.findByEmail(username);
        if (clienteLogado.isPresent() && !clienteLogado.get().getId().equals(clienteId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(veiculoRepository.findByClienteId(clienteId));
    }

    @PostMapping
    public ResponseEntity<Veiculo> createVeiculo(@RequestBody Veiculo veiculo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<Cliente> clienteLogado = clienteRepository.findByEmail(username);
        
        // Se um cliente está logado, o veículo DEVE pertencer a ele
        if (clienteLogado.isPresent()) {
            veiculo.setCliente(clienteLogado.get());
        } else {
            // Caso seja um usuário de oficina cadastrando para um cliente
            if (veiculo.getCliente() == null || veiculo.getCliente().getId() == null) {
                return ResponseEntity.badRequest().build();
            }
            Optional<Cliente> clienteAlvo = clienteRepository.findById(veiculo.getCliente().getId());
            if (clienteAlvo.isEmpty()) return ResponseEntity.notFound().build();
            veiculo.setCliente(clienteAlvo.get());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(veiculoRepository.save(veiculo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVeiculo(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Optional<Veiculo> veiculoOpt = veiculoRepository.findById(id);
        if (veiculoOpt.isEmpty()) return ResponseEntity.notFound().build();

        // Só o dono do veículo pode deletar
        Optional<Cliente> clienteLogado = clienteRepository.findByEmail(username);
        if (clienteLogado.isPresent() && !veiculoOpt.get().getCliente().getId().equals(clienteLogado.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        veiculoRepository.delete(veiculoOpt.get());
        return ResponseEntity.noContent().build();
    }
}
