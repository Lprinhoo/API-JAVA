package com.example.apiproject.controller;

import com.example.apiproject.model.Cliente;
import com.example.apiproject.repository.ClienteRepository;
import com.example.apiproject.security.JwtUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${google.client.id}")
    private String googleClientId;

    @GetMapping
    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        return cliente.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> data) {
        String idTokenString = data.get("idToken");

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String googleId = payload.getSubject();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");

                Cliente cliente = clienteRepository.findByGoogleId(googleId)
                        .orElseGet(() -> clienteRepository.save(new Cliente(name, email, googleId, pictureUrl)));

                Map<String, Object> claims = new HashMap<>();
                claims.put("clienteId", cliente.getId());
                claims.put("userType", "CLIENTE");

                String jwt = jwtUtils.generateJwtToken(cliente.getEmail(), claims);

                Map<String, Object> response = new HashMap<>();
                response.put("token", jwt);
                response.put("clienteId", cliente.getId()); // Adiciona clienteId na resposta
                response.put("username", cliente.getEmail()); // Adiciona username na resposta
                response.put("userType", "CLIENTE"); // Adiciona userType na resposta

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
            }
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao validar token");
        }
    }

    @PostMapping
    public Cliente createCliente(@RequestBody Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> updateCliente(@PathVariable Long id, @RequestBody Cliente clienteDetails) {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        if (cliente.isPresent()) {
            Cliente existingCliente = cliente.get();
            existingCliente.setNome(clienteDetails.getNome());
            existingCliente.setEmail(clienteDetails.getEmail());
            existingCliente.setGoogleId(clienteDetails.getGoogleId());
            existingCliente.setFotoPerfil(clienteDetails.getFotoPerfil());
            return ResponseEntity.ok(clienteRepository.save(existingCliente));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        if (cliente.isPresent()) {
            clienteRepository.delete(cliente.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
