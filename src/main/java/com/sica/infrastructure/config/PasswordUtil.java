package com.sica.infrastructure.config;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para hashing y verificación de contraseñas usando BCrypt.
 * Proporciona un wrapper sobre jBCrypt para uso en la capa de infraestructura.
 */
public final class PasswordUtil {

    private static final int LOG_ROUNDS = 12; // Factor de coste BCrypt

    private PasswordUtil() {
        // Clase utilitaria, no instanciar
    }

    /**
     * Genera un hash BCrypt de la contraseña en texto plano.
     *
     * @param plainPassword contraseña sin procesar
     * @return hash BCrypt (60 caracteres)
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifica que una contraseña en texto plano coincida con un hash BCrypt.
     *
     * @param plainPassword contraseña sin procesar
     * @param hashedPassword hash almacenado en la BD
     * @return true si la contraseña coincide
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
