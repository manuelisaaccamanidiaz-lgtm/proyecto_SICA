package com.sica.domain.exception;

/**
 * Se lanza cuando un usuario no tiene el permiso RBAC requerido
 * para realizar una operacion.
 */
public class PermisoDenegadoException extends SicaException {

    private final int usuarioId;
    private final String permisoRequerido;

    public PermisoDenegadoException(String mensaje, int usuarioId, String permisoRequerido) {
        super(mensaje);
        this.usuarioId = usuarioId;
        this.permisoRequerido = permisoRequerido;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public String getPermisoRequerido() {
        return permisoRequerido;
    }
}
