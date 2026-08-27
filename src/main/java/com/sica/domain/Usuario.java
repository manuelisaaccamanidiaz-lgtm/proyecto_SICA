package com.sica.domain;

/**
 * Entidad de dominio: Usuario del sistema.
 * Representa un usuario con credenciales y un rol asignado.
 */
public class Usuario {

    private int id;
    private String username;
    private String passwordHash;
    private int rolId;

    public Usuario() {}

    public Usuario(int id, String username, String passwordHash, int rolId) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rolId = rolId;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getRolId() { return rolId; }
    public void setRolId(int rolId) { this.rolId = rolId; }
}
