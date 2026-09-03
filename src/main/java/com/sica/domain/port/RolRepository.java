package com.sica.domain.port;

import com.sica.domain.Rol;
import java.util.List;

/**
 * Puerto de salida: Repositorio de Roles.
 */
public interface RolRepository {

    Rol guardar(Rol rol);

    Rol findById(int id);

    Rol findByNombreRol(String nombreRol);

    List<Rol> findAll();

    void actualizar(Rol rol);

    void eliminar(int id);
}
