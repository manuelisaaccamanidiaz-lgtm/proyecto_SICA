package com.sica.domain;

/**
 * Entidad de dominio: Empresa.
 * Corresponde a la tabla `empresas` del esquema oficial.
 */
public class Empresa {

    private int id;
    private String nombre;
    private String contactoPrincipal;

    public Empresa() {}

    public Empresa(int id, String nombre, String contactoPrincipal) {
        this.id = id;
        this.nombre = nombre;
        this.contactoPrincipal = contactoPrincipal;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getContactoPrincipal() { return contactoPrincipal; }
    public void setContactoPrincipal(String contactoPrincipal) { this.contactoPrincipal = contactoPrincipal; }
}
