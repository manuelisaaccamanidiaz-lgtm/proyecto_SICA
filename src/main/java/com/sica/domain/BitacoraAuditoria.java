package com.sica.domain;

import java.time.LocalDateTime;

/**
 * Entidad de dominio: Bitácora de auditoría.
 * Registra cada acción relevante realizada por un usuario en el sistema,
 * permitiendo trazabilidad y auditoría de operaciones.
 */
public class BitacoraAuditoria {

    private int id;
    private int usuarioId;
    private String accion;
    private String detalle;
    private LocalDateTime fechaHora;

    public BitacoraAuditoria() {}

    public BitacoraAuditoria(int id, int usuarioId, String accion, String detalle, LocalDateTime fechaHora) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.detalle = detalle;
        this.fechaHora = fechaHora;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
