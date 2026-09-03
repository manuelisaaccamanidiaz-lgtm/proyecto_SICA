package com.sica.domain;

import java.time.LocalDateTime;

/**
 * Entidad de dominio: Visita registrada.
 * Corresponde a la tabla `visitas` del esquema oficial.
 */
public class Visita {

    private int id;
    private int personaId;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaSalida;
    private int estadoVisitaId;      // FK → visita_estados
    private String vehiculoPlaca;
    private Integer visitaAprobadaPor; // FK → usuarios (quien aprueba ingreso no anunciado)

    public Visita() {}

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPersonaId() { return personaId; }
    public void setPersonaId(int personaId) { this.personaId = personaId; }

    public LocalDateTime getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(LocalDateTime fechaEntrada) { this.fechaEntrada = fechaEntrada; }

    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDateTime fechaSalida) { this.fechaSalida = fechaSalida; }

    public int getEstadoVisitaId() { return estadoVisitaId; }
    public void setEstadoVisitaId(int estadoVisitaId) { this.estadoVisitaId = estadoVisitaId; }

    public String getVehiculoPlaca() { return vehiculoPlaca; }
    public void setVehiculoPlaca(String vehiculoPlaca) { this.vehiculoPlaca = vehiculoPlaca; }

    public Integer getVisitaAprobadaPor() { return visitaAprobadaPor; }
    public void setVisitaAprobadaPor(Integer visitaAprobadaPor) { this.visitaAprobadaPor = visitaAprobadaPor; }
}
