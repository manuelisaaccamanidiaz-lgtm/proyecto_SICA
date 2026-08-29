package com.sica.application;

import com.sica.domain.Usuario;

/**
 * Caso de uso: Inicio de sesión.
 * Puerto de entrada que define la operación de login.
 * El esquema oficial usa email (no username) para autenticación.
 */
public interface LoginUseCase {

    /**
     * Autentica un usuario con su email y contraseña.
     *
     * @param email         correo electrónico del usuario
     * @param plainPassword contraseña en texto plano
     * @return el Usuario autenticado si las credenciales son válidas
     * @throws RuntimeException si las credenciales son inválidas
     */
    Usuario login(String email, String plainPassword);
}
