package com.sica.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio: Rol del sistema.
 * Corresponde a la tabla `roles` (columna `nombre_rol`).
 */
public class Rol {

    private int id;
    private String nombreRol;
    private List<Permiso> permisos;

    public Rol() {
        this.permisos = new ArrayList<>();
    }

    public Rol(int id, String nombreRol) {
        this.id = id;
        this.nombreRol = nombreRol;
        this.permisos = new ArrayList<>();
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    public List<Permiso> getPermisos() { return permisos; }
    public void setPermisos(List<Permiso> permisos) { this.permisos = permisos; }
}
