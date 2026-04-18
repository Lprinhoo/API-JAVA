package com.example.apiproject.controller;

import com.example.apiproject.model.Message;
import com.example.apiproject.model.UserProfile;
import com.example.apiproject.repository.MessageRepository;
import com.example.apiproject.repository.UserProfileRepository;
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
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

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
}
