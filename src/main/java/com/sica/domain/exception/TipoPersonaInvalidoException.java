package com.sica.domain.exception;

/**
 * Se lanza cuando se intenta usar un flujo de acceso incompatible
 * con el tipo de persona (ej. flujo de invitado con un Trabajador).
 */
public class TipoPersonaInvalidoException extends SicaException {

    public TipoPersonaInvalidoException(String mensaje) {
        super(mensaje);
    }
}
