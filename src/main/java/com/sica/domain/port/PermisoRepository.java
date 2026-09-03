package com.sica.domain.port;

import com.sica.domain.Permiso;
import java.util.List;

/**
 * Puerto de salida: Repositorio de Permisos.
 * Consulta permisos asignados a roles a través de la tabla rol_permisos.
 */
public interface PermisoRepository {

    /**
     * Busca todos los permisos asignados a un rol específico.
     *
     * @param rolId ID del rol a consultar
     * @return lista de permisos del rol
     */
    List<Permiso> findByRolId(int rolId);

    /**
     * Verifica si un rol tiene un permiso con el nombre dado.
     *
     * @param rolId           ID del rol
     * @param nombrePermiso   nombre del permiso a buscar
     * @return true si el rol tiene ese permiso
     */
    boolean rolTienePermiso(int rolId, String nombrePermiso);

    /**
     * Busca un permiso por su nombre.
     *
     * @param nombrePermiso nombre único del permiso
     * @return el Permiso encontrado o null
     */
    Permiso findByNombrePermiso(String nombrePermiso);

    /**
     * Retorna todos los permisos del sistema.
     *
     * @return lista completa de permisos
     */
    List<Permiso> findAll();
}
