package com.sica.domain.port;

import com.sica.domain.Usuario;
import java.util.List;

/**
 * Puerto de salida: Repositorio de Usuarios.
 * La BD usa 'email' para login (no 'username').
 */
public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);

    Usuario findById(int id);

    Usuario findByEmail(String email);

    List<Usuario> findAll();

    void actualizar(Usuario usuario);

    void eliminar(int id);
}
