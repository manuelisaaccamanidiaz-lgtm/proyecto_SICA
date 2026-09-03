package com.sica.application.strategy;

/**
 * DTO de salida: resultado del procesamiento de un flujo de acceso.
 * Contiene si fue exitoso, un mensaje descriptivo, y datos relevantes.
 */
public class ResultadoAcceso {

    private boolean exitoso;
    private String mensaje;
    private Integer visitaId;    // ID de la visita creada/modificada (si aplica)
    private Integer personaId;   // ID de la persona involucrada

    public ResultadoAcceso() {}

    public ResultadoAcceso(boolean exitoso, String mensaje) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
    }

    // Builder-like setters
    public ResultadoAcceso visitaId(Integer visitaId) {
        this.visitaId = visitaId;
        return this;
    }

    public ResultadoAcceso personaId(Integer personaId) {
        this.personaId = personaId;
        return this;
    }

    // Getters
    public boolean isExitoso() { return exitoso; }
    public String getMensaje() { return mensaje; }
    public Integer getVisitaId() { return visitaId; }
    public Integer getPersonaId() { return personaId; }

    // Factory methods
    public static ResultadoAcceso exito(String mensaje) {
        return new ResultadoAcceso(true, mensaje);
    }

    public static ResultadoAcceso fallo(String mensaje) {
        return new ResultadoAcceso(false, mensaje);
    }

    @Override
    public String toString() {
        return (exitoso ? "✅" : "❌") + " " + mensaje;
    }
}
