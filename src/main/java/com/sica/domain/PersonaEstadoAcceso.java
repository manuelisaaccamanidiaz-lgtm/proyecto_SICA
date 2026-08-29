package com.sica.domain;

/**
 * Entidad de dominio: Catálogo de estados de acceso de persona.
 * Corresponde a la tabla `persona_estados_acceso`.
 * Valores típicos: 'Activo', 'Con Prohibicion de Ingreso'.
 */
public class PersonaEstadoAcceso {

    private int id;
    private String nombreEstado;

    public PersonaEstadoAcceso() {}

    public PersonaEstadoAcceso(int id, String nombreEstado) {
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
