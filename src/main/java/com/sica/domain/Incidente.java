package com.sica.domain;

import java.time.LocalDate;

/**
 * Entidad de dominio: Incidente registrado en las instalaciones.
 * Registra un incidente asociado a una persona, con fecha y usuario que lo reporta.
 */
public class Incidente {

    private int id;
    private int personaId;
    private String descripcion;
    private LocalDate fecha;
    private int usuarioRegistroId;

    public Incidente() {}

    public Incidente(int id, int personaId, String descripcion, LocalDate fecha, int usuarioRegistroId) {
        this.id = id;
        this.personaId = personaId;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.usuarioRegistroId = usuarioRegistroId;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPersonaId() { return personaId; }
    public void setPersonaId(int personaId) { this.personaId = personaId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public int getUsuarioRegistroId() { return usuarioRegistroId; }
    public void setUsuarioRegistroId(int usuarioRegistroId) { this.usuarioRegistroId = usuarioRegistroId; }
}
