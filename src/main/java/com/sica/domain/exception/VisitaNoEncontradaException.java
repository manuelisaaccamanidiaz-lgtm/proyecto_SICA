package com.sica.domain.exception;

/**
 * Se lanza cuando se busca una visita por ID y no existe en la BD.
 */
public class VisitaNoEncontradaException extends SicaException {

    public VisitaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
