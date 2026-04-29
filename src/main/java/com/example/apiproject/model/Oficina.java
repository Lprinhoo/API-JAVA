package com.example.apiproject.model;

import jakarta.persistence.*;
import java.util.List; // Import List
import java.util.UUID;

@Entity
@Table(name = "oficinas")
public class Oficina {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String nome;
    private String endereco;
    @Column(unique = true)
    private String telefone;
    private String email; // Added email field
    private Double latitude;
    private Double longitude;

    @ElementCollection // For collections of basic types or embeddable classes
    @CollectionTable(name = "oficina_servicos", joinColumns = @JoinColumn(name = "oficina_id"))
    @Column(name = "servico")
    private List<String> servicos; // Changed to List<String>

    public Oficina() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<String> getServicos() { // Changed return type
        return servicos;
    }

    public void setServicos(List<String> servicos) { // Changed parameter type
        this.servicos = servicos;
    }
}
