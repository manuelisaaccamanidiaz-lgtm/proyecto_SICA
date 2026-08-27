package com.sica.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio: Rol del sistema.
 * Un rol agrupa permisos que determinan qué puede hacer un usuario.
 */
public class Rol {

    private int id;
    private String nombre;
    private List<Permiso> permisos;

    public Rol() {
        this.permisos = new ArrayList<>();
    }

    public Rol(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.permisos = new ArrayList<>();
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Permiso> getPermisos() { return permisos; }
    public void setPermisos(List<Permiso> permisos) { this.permisos = permisos; }
}
