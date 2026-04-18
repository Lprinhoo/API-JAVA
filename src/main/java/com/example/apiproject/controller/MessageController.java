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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<?> authenticateGoogleUser(@RequestBody Map<String, String> payload) {
        try {
            String idTokenString = payload.get("idToken");
            
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
            }

            GoogleIdToken.Payload googlePayload = idToken.getPayload();
            String googleId = googlePayload.getSubject();
            String email = googlePayload.getEmail();
            String name = (String) googlePayload.get("name");
            String pictureUrl = (String) googlePayload.get("picture");

            UserProfile user = userProfileRepository.findByGoogleId(googleId)
                    .orElse(new UserProfile());

            user.setGoogleId(googleId);
            user.setEmail(email);
            user.setName(name);
            user.setPhotoUrl(pictureUrl);

            userProfileRepository.save(user);

            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro na autenticação: " + e.getMessage());
        }
    }
}
