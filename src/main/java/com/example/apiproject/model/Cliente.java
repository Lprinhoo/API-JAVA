package com.example.apiproject.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @ManyToMany(mappedBy = "clientes")
    @JsonIgnoreProperties("clientes")
    private Set<Oficina> oficinas = new HashSet<>();

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("cliente") // Evita recursão infinita ao serializar Veiculo
    private List<Veiculo> veiculos = new ArrayList<>();

    // Construtores
    public Cliente() {}

    public Cliente(String nome, String email, String googleId, String fotoPerfil) {
        this.nome = nome;
        this.email = email;
        this.googleId = googleId;
        this.fotoPerfil = fotoPerfil;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public Set<Oficina> getOficinas() {
        return oficinas;
    }

    public void setOficinas(Set<Oficina> oficinas) {
        this.oficinas = oficinas;
    }

    public List<Veiculo> getVeiculos() {
        return veiculos;
    }

    public void setVeiculos(List<Veiculo> veiculos) {
        this.veiculos = veiculos;
    }
}
