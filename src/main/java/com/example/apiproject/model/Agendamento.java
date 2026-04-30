package com.example.apiproject.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "clientes"}) // Ignora clientes para evitar recursão e lazy loading issues
    private Oficina oficina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "oficinas", "veiculos"}) // Ignora oficinas e veiculos
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "cliente"}) // Ignora cliente
    private Veiculo veiculo;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(columnDefinition = "TEXT")
    private String servicosDescricao; // Descrição dos serviços solicitados

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgendamentoStatus status = AgendamentoStatus.PENDENTE; // Status inicial

    // Construtores
    public Agendamento() {}

    public Agendamento(Oficina oficina, Cliente cliente, Veiculo veiculo, LocalDateTime dataHora, String servicosDescricao) {
        this.oficina = oficina;
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.dataHora = dataHora;
        this.servicosDescricao = servicosDescricao;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Oficina getOficina() {
        return oficina;
    }

    public void setOficina(Oficina oficina) {
        this.oficina = oficina;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getServicosDescricao() {
        return servicosDescricao;
    }

    public void setServicosDescricao(String servicosDescricao) {
        this.servicosDescricao = servicosDescricao;
    }

    public AgendamentoStatus getStatus() {
        return status;
    }

    public void setStatus(AgendamentoStatus status) {
        this.status = status;
    }
}
