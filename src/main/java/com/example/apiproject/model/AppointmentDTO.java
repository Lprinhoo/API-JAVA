package com.example.apiproject.model;

public class AppointmentDTO {
    private Long id;
    private String servico;
    private String dataHora;
    private String status;
    private Long oficinaId;
    private String oficinaNome;
    private String oficinaEndereco;

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
    }

    public Long getId() {
        return id;
    }

    public String getServico() {
        return servico;
    }

    public String getDataHora() {
        return dataHora;
    }

    public String getStatus() {
        return status;
    }

    public Long getOficinaId() {
        return oficinaId;
    }

    public String getOficinaNome() {
        return oficinaNome;
    }

    public String getOficinaEndereco() {
        return oficinaEndereco;
    }
}
