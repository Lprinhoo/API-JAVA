package com.example.apiproject.controller;

import com.example.apiproject.model.Message;
import com.example.apiproject.model.UserProfile;
import com.example.apiproject.model.Vehicle;
import com.example.apiproject.repository.MessageRepository;
import com.example.apiproject.repository.UserProfileRepository;
import com.example.apiproject.repository.VehicleRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Value("${google.client.id}")
    private String googleClientId;

    @GetMapping
    public Map<String, String> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "online");
        response.put("message", "API Spring Boot no Railway conectada ao PostgreSQL!");
        return response;
    }

    @GetMapping("api/messages")
    public List<Message> getMessages() {
        return messageRepository.findAll();
    }

    @PostMapping("api/messages")
    public Message addMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }

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

    @PostMapping("api/vehicles/{userId}")
    public ResponseEntity<Vehicle> createVehicle(@PathVariable Long userId, @RequestBody Vehicle vehicle) {
        return userProfileRepository.findById(userId).map(user -> {
            vehicle.setUserProfile(user);
            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedVehicle);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("api/vehicles/{userId}")
    public ResponseEntity<List<Vehicle>> getVehiclesByUser(@PathVariable Long userId) {
        List<Vehicle> vehicles = vehicleRepository.findByUserProfileId(userId);
        return ResponseEntity.ok(vehicles);
    }

    @DeleteMapping("api/vehicles/{vehicleId}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long vehicleId) {
        if (vehicleRepository.existsById(vehicleId)) {
            vehicleRepository.deleteById(vehicleId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("api/vehicles/{vehicleId}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long vehicleId, @RequestBody Vehicle vehicleDetails) {
        return vehicleRepository.findById(vehicleId).map(vehicle -> {
            vehicle.setMarca(vehicleDetails.getMarca());
            vehicle.setAno(vehicleDetails.getAno());
            vehicle.setPlaca(vehicleDetails.getPlaca());
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        }).orElse(ResponseEntity.notFound().build());
    }
}
