package com.sica.domain.exception;

/**
 * Se lanza cuando falta un estado del catalogo (visita_estados
 * o persona_estados_acceso) que es requerido para procesar la operacion.
 */
public class EstadoNoEncontradoException extends SicaException {

    public EstadoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
