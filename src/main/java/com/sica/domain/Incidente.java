package com.sica.domain;

import java.time.LocalDateTime;

/**
 * Entidad de dominio: Incidente de seguridad.
 * Corresponde a la tabla `incidentes` del esquema oficial.
 * Un incidente se asocia a una visita específica, no directamente a una persona.
 */
public class Incidente {

    private int id;
    private int visitaId;
    private int reportadoPorId;
    private LocalDateTime fecha;
    private String descripcion;

    public Incidente() {}

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVisitaId() { return visitaId; }
    public void setVisitaId(int visitaId) { this.visitaId = visitaId; }

    public int getReportadoPorId() { return reportadoPorId; }
    public void setReportadoPorId(int reportadoPorId) { this.reportadoPorId = reportadoPorId; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
