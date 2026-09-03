package com.sica.domain;

import com.sica.domain.exception.TipoPersonaInvalidoException;

/**
 * Enum que define los tipos de persona en el sistema.
 * Corresponde al ENUM('Trabajador', 'Invitado') en la tabla personas.
 */
public enum TipoPersona {
    TRABAJADOR("Trabajador"),
    INVITADO("Invitado");

    private final String valor;

    TipoPersona(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    /**
     * Convierte una cadena del ENUM de MySQL a la constante Java.
     * Acepta tanto el valor del ENUM como el nombre de la constante.
     */
    public static TipoPersona fromValor(String valor) {
        if (valor == null) throw new TipoPersonaInvalidoException("TipoPersona no puede ser null");
        for (TipoPersona tp : values()) {
            if (tp.valor.equalsIgnoreCase(valor) || tp.name().equalsIgnoreCase(valor)) {
                return tp;
            }
        }
        throw new TipoPersonaInvalidoException("TipoPersona desconocida: " + valor);
    }
}
