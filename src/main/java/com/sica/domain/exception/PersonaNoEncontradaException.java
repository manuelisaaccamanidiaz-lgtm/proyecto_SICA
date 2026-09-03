package com.sica.domain.exception;

/**
 * Se lanza cuando no se encuentra una persona en el sistema
 * por su ID o documento de identidad.
 */
public class PersonaNoEncontradaException extends SicaException {

    public PersonaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
