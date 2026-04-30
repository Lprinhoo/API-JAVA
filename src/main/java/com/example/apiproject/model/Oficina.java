package com.example.apiproject.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "oficinas")
public class Oficina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String servicos;

    private String endereco;

    private Double latitude;

    private Double longitude;

    private String telefone;

    private String email;

    @Column(name = "horario_funcionamento")
    private String horarioFuncionamento;

    private String website;

    @Column(name = "formas_pagamento")
    private String formasPagamento;

    private String especialidades;

    @ManyToMany
    @JoinTable(
        name = "oficina_clientes",
        joinColumns = @JoinColumn(name = "oficina_id"),
        inverseJoinColumns = @JoinColumn(name = "cliente_id")
    )
    @JsonIgnoreProperties("oficinas")
    private Set<Cliente> clientes = new HashSet<>();

    // Construtores
    public Oficina() {}

    public Oficina(String nome, String servicos, String endereco, Double latitude, Double longitude, 
                   String telefone, String email, String horarioFuncionamento, String website, 
                   String formasPagamento, String especialidades) {
        this.nome = nome;
        this.servicos = servicos;
        this.endereco = endereco;
        this.latitude = latitude;
        this.longitude = longitude;
        this.telefone = telefone;
        this.email = email;
        this.horarioFuncionamento = horarioFuncionamento;
        this.website = website;
        this.formasPagamento = formasPagamento;
        this.especialidades = especialidades;
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

    public String getServicos() {
        return servicos;
    }

    public void setServicos(String servicos) {
        this.servicos = servicos;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
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

    public String getHorarioFuncionamento() {
        return horarioFuncionamento;
    }

    public void setHorarioFuncionamento(String horarioFuncionamento) {
        this.horarioFuncionamento = horarioFuncionamento;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getFormasPagamento() {
        return formasPagamento;
    }

    public void setFormasPagamento(String formasPagamento) {
        this.formasPagamento = formasPagamento;
    }

    public String getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(String especialidades) {
        this.especialidades = especialidades;
    }

    public Set<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(Set<Cliente> clientes) {
        this.clientes = clientes;
    }
}
