package com.sica.domain.port;

import com.sica.domain.PersonaEstadoAcceso;
import java.util.List;

/**
 * Puerto de salida: Repositorio de estados de acceso de persona (catálogo).
 */
public interface PersonaEstadoAccesoRepository {

    /**
     * Busca un estado de acceso por su ID.
     *
     * @param id ID del estado
     * @return el PersonaEstadoAcceso o null
     */
    PersonaEstadoAcceso findById(int id);

    /**
     * Busca un estado de acceso por su nombre exacto.
     *
     * @param nombreEstado nombre del estado a buscar
     * @return el PersonaEstadoAcceso o null
     */
    PersonaEstadoAcceso findByNombreEstado(String nombreEstado);

    /**
     * Retorna todos los estados de acceso del catálogo.
     *
     * @return lista de todos los estados
     */
    List<PersonaEstadoAcceso> findAll();

    /**
     * Guarda un nuevo estado de acceso en el catálogo.
     *
     * @param estado el estado a guardar
     * @return el estado con ID asignado
     */
    PersonaEstadoAcceso guardar(PersonaEstadoAcceso estado);
}
