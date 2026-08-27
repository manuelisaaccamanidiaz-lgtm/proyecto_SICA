package com.sica.domain;

import java.time.LocalDateTime;

/**
 * Entidad de dominio: Visita registrada en el sistema.
 * Registra la entrada y salida de una persona visitante.
 */
public class Visita {

    private int id;
    private int personaId;
    private int funcionarioId;
    private String estado;
    private LocalDateTime fechaHoraEntrada;
    private LocalDateTime fechaHoraSalida; // nullable: puede estar en curso

    public Visita() {}

    public Visita(int id, int personaId, int funcionarioId, String estado,
                  LocalDateTime fechaHoraEntrada, LocalDateTime fechaHoraSalida) {
        this.id = id;
        this.personaId = personaId;
        this.funcionarioId = funcionarioId;
        this.estado = estado;
        this.fechaHoraEntrada = fechaHoraEntrada;
        this.fechaHoraSalida = fechaHoraSalida;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPersonaId() { return personaId; }
    public void setPersonaId(int personaId) { this.personaId = personaId; }

    public int getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(int funcionarioId) { this.funcionarioId = funcionarioId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaHoraEntrada() { return fechaHoraEntrada; }
    public void setFechaHoraEntrada(LocalDateTime fechaHoraEntrada) { this.fechaHoraEntrada = fechaHoraEntrada; }

    public LocalDateTime getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }
}
