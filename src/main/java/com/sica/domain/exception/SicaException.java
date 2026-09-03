package com.sica.domain.exception;

/**
 * Excepcion base del dominio SICA.
 * Todas las excepciones de negocio deben extender esta clase
 * para permitir un manejo uniforme en las capas superiores.
 */
public abstract class SicaException extends RuntimeException {

    public SicaException(String mensaje) {
        super(mensaje);
    }

    public SicaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
