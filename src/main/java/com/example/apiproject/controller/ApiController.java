package com.example.apiproject.controller;

import com.example.apiproject.model.UserProfile;
import com.example.apiproject.model.Vehicle;
import com.example.apiproject.repository.UserProfileRepository;
import com.example.apiproject.repository.VehicleRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/")
public class ApiController {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Value("${google.client.id}")
    private String googleClientId;

    // ─── Health Check ────────────────────────────────────────────────────────

    @GetMapping
    public Map<String, String> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "online");
        response.put("message", "API Spring Boot no Railway conectada ao PostgreSQL!");
        return response;
    }

    // ─── Auth Google ─────────────────────────────────────────────────────────

    @PostMapping("api/auth/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        try {
            String idToken = body.get("idToken");

            if (idToken == null || idToken.isEmpty()) {
                return ResponseEntity.badRequest().body("idToken é obrigatório");
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken token = verifier.verify(idToken);

            if (token == null) {
                return ResponseEntity.status(401).body("Token inválido ou expirado");
            }

            GoogleIdToken.Payload payload = token.getPayload();

            String googleId = payload.getSubject();
            String email    = payload.getEmail();
            String name     = (String) payload.get("name");
            String photoUrl = (String) payload.get("picture");

            UserProfile user = userProfileRepository
                    .findByGoogleId(googleId)
                    .orElse(new UserProfile());

            user.setGoogleId(googleId);
            user.setEmail(email);
            user.setName(name);
            user.setPhotoUrl(photoUrl);

            userProfileRepository.save(user);

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    // ─── Veículos ────────────────────────────────────────────────────────────

    @PostMapping("api/vehicles/{userId}")
    public ResponseEntity<?> addVehicle(@PathVariable Long userId,
                                         @RequestBody Vehicle vehicle) {
        return userProfileRepository.findById(userId)
                .map(user -> {
                    vehicle.setUserProfile(user);
                    Vehicle saved = vehicleRepository.save(vehicle);
                    return ResponseEntity.status(201).<Object>body(saved);
                })
                .orElse(ResponseEntity.status(404).body("Usuário não encontrado"));
    }

    @GetMapping("api/vehicles/{userId}")
    public ResponseEntity<List<Vehicle>> getVehicles(@PathVariable Long userId) {
        List<Vehicle> vehicles = vehicleRepository.findByUserProfileId(userId);
        return ResponseEntity.ok(vehicles);
    }

    @PutMapping("api/vehicles/{vehicleId}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long vehicleId,
                                            @RequestBody Vehicle vehicle) {
        return vehicleRepository.findById(vehicleId)
                .map(existing -> {
                    existing.setMarca(vehicle.getMarca());
                    existing.setAno(vehicle.getAno());
                    existing.setPlaca(vehicle.getPlaca());
                    Vehicle updated = vehicleRepository.save(existing);
                    return ResponseEntity.ok().<Object>body(updated);
                })
                .orElse(ResponseEntity.status(404).body("Veículo não encontrado"));
    }

    @DeleteMapping("api/vehicles/{vehicleId}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            return ResponseEntity.status(404).body("Veículo não encontrado");
        }
        vehicleRepository.deleteById(vehicleId);
        return ResponseEntity.noContent().build();
    }
}
