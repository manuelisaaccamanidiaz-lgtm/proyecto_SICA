package com.sica.domain.exception;

/**
 * Se lanza durante el login cuando las credenciales (email, password)
 * son invalidas, el usuario esta desactivado, o la contrasena no coincide.
 */
public class CredencialesInvalidasException extends SicaException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
