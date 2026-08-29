package com.sica.domain;

/**
 * Entidad de dominio: Catálogo de estados de visita.
 * Corresponde a la tabla `visita_estados`.
 * Valores típicos: 'Dentro', 'Fuera', 'Pendiente de Aprobacion', 'Aprobado', 'Rechazado', 'Expirado'.
 */
public class VisitaEstado {

    private int id;
    private String nombreEstado;

    public VisitaEstado() {}

    public VisitaEstado(int id, String nombreEstado) {
        this.id = id;
        this.nombreEstado = nombreEstado;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombreEstado() { return nombreEstado; }
    public void setNombreEstado(String nombreEstado) { this.nombreEstado = nombreEstado; }

    @Override
    public String toString() {
        return nombreEstado;
    }
}
