package com.example.apiproject.model;

import java.util.UUID;

public class AppointmentDTO {
    private Long id;
    private String servico;
    private String dataHora;
    private String status;
    private UUID oficinaId;
    private String oficinaNome;
    private String oficinaEndereco;
    private Long userId;
    private String userName;
    private String userEmail;

    public AppointmentDTO(Appointment a) {
        this.id = a.getId();
        this.servico = a.getServico();
        this.dataHora = a.getDataHora();
        this.status = a.getStatus();
        if (a.getOficina() != null) {
            this.oficinaId = a.getOficina().getId();
            this.oficinaNome = a.getOficina().getNome();
            this.oficinaEndereco = a.getOficina().getEndereco();
        }
        if (a.getUserProfile() != null) {
            this.userId = a.getUserProfile().getId();
            this.userName = a.getUserProfile().getName();
            this.userEmail = a.getUserProfile().getEmail();
        }
    }

    public Long getId() { return id; }
    public String getServico() { return servico; }
    public String getDataHora() { return dataHora; }
    public String getStatus() { return status; }
    public UUID getOficinaId() { return oficinaId; }
    public String getOficinaNome() { return oficinaNome; }
    public String getOficinaEndereco() { return oficinaEndereco; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
}
