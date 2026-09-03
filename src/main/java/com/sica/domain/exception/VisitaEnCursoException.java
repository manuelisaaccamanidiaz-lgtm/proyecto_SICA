package com.sica.domain.exception;

/**
 * Se lanza cuando una persona ya tiene una visita activa (estado "Dentro")
 * y se intenta registrar una nueva entrada.
 */
public class VisitaEnCursoException extends SicaException {

    private final int visitaId;

    public VisitaEnCursoException(String mensaje, int visitaId) {
        super(mensaje);
        this.visitaId = visitaId;
    }

    public int getVisitaId() {
        return visitaId;
    }
}
