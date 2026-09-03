package com.sica.application.strategy;

/**
 * DTO de entrada: datos necesarios para procesar cualquier flujo de acceso.
 * Se construye con la información que el guarda de seguridad ingresa en consola.
 */
public class SolicitudAcceso {

    private int personaId;
    private int usuarioId;          // quien autoriza (guarda/funcionario)
    private String documentoIdentidad; // para búsqueda por documento
    private String vehiculoPlaca;   // opcional
    private String descripcion;     // para incidentes o regularizaciones
    private String nombreInvitado;  // para invitados no anunciados (se crea en el momento)

    public SolicitudAcceso() {}

    // Builder-like setters (fluid)
    public SolicitudAcceso personaId(int personaId) {
        this.personaId = personaId;
        return this;
    }

    public SolicitudAcceso usuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
        return this;
    }

    public SolicitudAcceso documentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
        return this;
    }

    public SolicitudAcceso vehiculoPlaca(String vehiculoPlaca) {
        this.vehiculoPlaca = vehiculoPlaca;
        return this;
    }

    public SolicitudAcceso descripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    public SolicitudAcceso nombreInvitado(String nombreInvitado) {
        this.nombreInvitado = nombreInvitado;
        return this;
    }

    // Getters
    public int getPersonaId() { return personaId; }
    public int getUsuarioId() { return usuarioId; }
    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public String getVehiculoPlaca() { return vehiculoPlaca; }
    public String getDescripcion() { return descripcion; }
    public String getNombreInvitado() { return nombreInvitado; }
}
