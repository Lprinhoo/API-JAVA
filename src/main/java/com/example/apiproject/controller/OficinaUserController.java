package com.example.apiproject.controller;

import com.example.apiproject.model.Oficina;
import com.example.apiproject.model.OficinaUser;
import com.example.apiproject.repository.OficinaRepository;
import com.example.apiproject.repository.OficinaUserRepository;
import com.example.apiproject.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/oficina-users")
public class OficinaUserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private OficinaUserRepository oficinaUserRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String password = data.get("password");
        Long oficinaId = Long.parseLong(data.get("oficinaId"));

        if (oficinaUserRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Erro: Username já existe!");
        }

        Optional<Oficina> oficinaOpt = oficinaRepository.findById(oficinaId);
        if (oficinaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: Oficina não encontrada!");
        }

        // Criar nova conta de usuário
        OficinaUser user = new OficinaUser(username, encoder.encode(password), oficinaOpt.get());
        oficinaUserRepository.save(user);

        return ResponseEntity.ok("Usuário de oficina registrado com sucesso!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String password = data.get("password");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        OficinaUser user = oficinaUserRepository.findByUsername(username).get();

        Map<String, Object> claims = new HashMap<>();
        claims.put("oficinaId", user.getOficina().getId());
        claims.put("userType", "OFICINA"); // Usa userType em vez de role

        String jwt = jwtUtils.generateJwtToken(username, claims);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("oficinaId", user.getOficina().getId());
        response.put("userType", "OFICINA"); // Adiciona userType na resposta

        return ResponseEntity.ok(response);
    }
}
