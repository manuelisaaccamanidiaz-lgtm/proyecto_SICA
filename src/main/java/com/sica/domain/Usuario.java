package com.sica.domain;

/**
 * Entidad de dominio: Usuario del sistema.
 * Corresponde a la tabla `usuarios` del esquema oficial.
 */
public class Usuario {

    private int id;
    private String nombre;
    private String email;
    private String passwordHash;   // columna 'password' en la BD (almacena hash BCrypt)
    private int rolId;
    private boolean estaActivo;

    public Usuario() {}

    public Usuario(int id, String nombre, String email, String passwordHash, int rolId, boolean estaActivo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rolId = rolId;
        this.estaActivo = estaActivo;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getRolId() { return rolId; }
    public void setRolId(int rolId) { this.rolId = rolId; }

    public boolean isEstaActivo() { return estaActivo; }
    public void setEstaActivo(boolean estaActivo) { this.estaActivo = estaActivo; }
}
