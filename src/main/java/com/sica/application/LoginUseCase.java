package com.sica.application;

import com.sica.domain.Usuario;

/**
 * Caso de uso: Inicio de sesión.
 * Puerto de entrada que define la operación de login.
 */
public interface LoginUseCase {

    /**
     * Autentica un usuario con su username y contraseña.
     *
     * @param username nombre de usuario
     * @param plainPassword contraseña en texto plano
     * @return el Usuario autenticado si las credenciales son válidas
     * @throws RuntimeException si las credenciales son inválidas
     */
    Usuario login(String username, String plainPassword);
}
