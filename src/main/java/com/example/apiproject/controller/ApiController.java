package com.example.apiproject.controller;

import com.example.apiproject.model.*;
import com.example.apiproject.repository.AppointmentRepository;
import com.example.apiproject.repository.OficinaRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/")
public class ApiController {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private OficinaRepository oficinaRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

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

    // ─── Oficinas ────────────────────────────────────────────────────────────

    @GetMapping("api/oficinas")
    public List<Oficina> getOficinas() {
        return oficinaRepository.findAll();
    }

    @GetMapping("api/oficinas/{id}")
    public ResponseEntity<Oficina> getOficinaById(@PathVariable Long id) {
        return oficinaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("api/oficinas")
    public ResponseEntity<Oficina> createOficina(@RequestBody Oficina oficina) {
        Oficina saved = oficinaRepository.save(oficina);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("api/oficinas/{id}")
    public ResponseEntity<?> updateOficina(@PathVariable Long id, @RequestBody Oficina details) {
        return oficinaRepository.findById(id)
                .map(oficina -> {
                    oficina.setNome(details.getNome());
                    oficina.setEndereco(details.getEndereco());
                    oficina.setTelefone(details.getTelefone());
                    oficina.setLatitude(details.getLatitude());
                    oficina.setLongitude(details.getLongitude());
                    oficina.setServicos(details.getServicos());
                    Oficina updated = oficinaRepository.save(oficina);
                    return ResponseEntity.ok().<Object>body(updated);
                })
                .orElse(ResponseEntity.status(404).body("Oficina não encontrada"));
    }

    @DeleteMapping("api/oficinas/{id}")
    public ResponseEntity<Void> deleteOficina(@PathVariable Long id) {
        if (!oficinaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        oficinaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Agendamentos (Appointments) ─────────────────────────────────────────

    @PostMapping("api/appointments/{userId}/{oficinaId}")
    public ResponseEntity<?> createAppointment(@PathVariable Long userId,
                                               @PathVariable Long oficinaId,
                                               @RequestBody Appointment appointment) {
        Optional<UserProfile> userOpt = userProfileRepository.findById(userId);
        if (userOpt.isEmpty()) return ResponseEntity.status(404).body("Usuário não encontrado");

        Optional<Oficina> oficinaOpt = oficinaRepository.findById(oficinaId);
        if (oficinaOpt.isEmpty()) return ResponseEntity.status(404).body("Oficina não encontrada");

        appointment.setUserProfile(userOpt.get());
        appointment.setOficina(oficinaOpt.get());

        if (appointment.getStatus() == null || appointment.getStatus().isEmpty()) {
            appointment.setStatus("PENDENTE");
        }

        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("api/appointments/user/{userId}")
    public List<AppointmentDTO> getAppointmentsByUser(@PathVariable Long userId) {
        List<Appointment> appointments = appointmentRepository.findByUserProfileId(userId);
        return appointments.stream()
                .map(AppointmentDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("api/appointments/oficina/{oficinaId}")
    public List<Appointment> getAppointmentsByOficina(@PathVariable Long oficinaId) {
        return appointmentRepository.findByOficinaId(oficinaId);
    }

    @PatchMapping("api/appointments/{appointmentId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long appointmentId,
                                          @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        List<String> validStatus = Arrays.asList("PENDENTE", "CONFIRMADO", "CANCELADO", "CONCLUIDO");

        if (newStatus == null || !validStatus.contains(newStatus.toUpperCase())) {
            return ResponseEntity.badRequest().body("Status inválido");
        }

        return appointmentRepository.findById(appointmentId)
                .map(appointment -> {
                    appointment.setStatus(newStatus.toUpperCase());
                    appointmentRepository.save(appointment);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("api/appointments/{appointmentId}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            return ResponseEntity.notFound().build();
        }
        appointmentRepository.deleteById(appointmentId);
        return ResponseEntity.noContent().build();
    }
}
