package com.example.apiproject.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "oficina_users")
public class OficinaUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Pode ser o email do usuário

    @Column(nullable = false)
    @JsonIgnore // Para não expor a senha em respostas JSON
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id", nullable = false)
    private Oficina oficina;

    @Column(nullable = false)
    private String role = "ROLE_OFICINA"; // Papel padrão para usuários de oficina

    // Construtores
    public OficinaUser() {}

    public OficinaUser(String username, String password, Oficina oficina) {
        this.username = username;
        this.password = password;
        this.oficina = oficina;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Oficina getOficina() {
        return oficina;
    }

    public void setOficina(Oficina oficina) {
        this.oficina = oficina;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
