package com.sica.domain;

/**
 * Entidad de dominio: Persona que visita la empresa.
 * Puede estar asociada a una empresa (opcional).
 */
public class Persona {

    private int id;
    private String nombre;
    private String documento;
    private Integer empresaId; // nullable: puede no estar asociada a una empresa

    public Persona() {}

    public Persona(int id, String nombre, String documento, Integer empresaId) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.empresaId = empresaId;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public Integer getEmpresaId() { return empresaId; }
    public void setEmpresaId(Integer empresaId) { this.empresaId = empresaId; }
}
