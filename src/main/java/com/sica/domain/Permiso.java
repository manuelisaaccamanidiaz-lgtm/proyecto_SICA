package com.sica.domain;

/**
 * Entidad de dominio: Permiso del sistema.
 * Representa una acción específica que un usuario puede realizar.
 * Ejemplo: "VISITA_REGISTRAR", "USUARIO_CREAR", "INCIDENTE_VER".
 */
public class Permiso {

    private int id;
    private String nombrePermiso;
    private String descripcion;

    public Permiso() {}

    public Permiso(int id, String nombrePermiso, String descripcion) {
        this.id = id;
        this.nombrePermiso = nombrePermiso;
        this.descripcion = descripcion;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombrePermiso() { return nombrePermiso; }
    public void setNombrePermiso(String nombrePermiso) { this.nombrePermiso = nombrePermiso; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
