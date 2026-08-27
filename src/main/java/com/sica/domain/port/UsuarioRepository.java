package com.sica.domain.port;

import com.sica.domain.Usuario;
import java.util.List;

/**
 * Puerto de salida: Repositorio de Usuarios.
 * Define las operaciones de persistencia que la capa de dominio necesita.
 * La implementación concreta está en infrastructure/persistence.
 */
public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);

    Usuario findById(int id);

    Usuario findByUsername(String username);

    List<Usuario> findAll();

    void actualizar(Usuario usuario);

    void eliminar(int id);
}
