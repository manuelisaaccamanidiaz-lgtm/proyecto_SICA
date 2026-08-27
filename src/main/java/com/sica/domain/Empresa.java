package com.sica.domain;

/**
 * Entidad de dominio: Empresa.
 * Representa una empresa asociada a las personas que visitan las instalaciones.
 */
public class Empresa {

    private int id;
    private String nombre;

    public Empresa() {}

    public Empresa(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
