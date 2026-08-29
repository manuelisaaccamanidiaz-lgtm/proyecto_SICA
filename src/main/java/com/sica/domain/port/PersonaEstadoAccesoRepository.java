package com.sica.domain.port;

import com.sica.domain.PersonaEstadoAcceso;
import java.util.List;

/**
 * Puerto de salida: Repositorio de estados de acceso de persona (catálogo).
 */
public interface PersonaEstadoAccesoRepository {

    PersonaEstadoAcceso findById(int id);

    PersonaEstadoAcceso findByNombreEstado(String nombreEstado);

    List<PersonaEstadoAcceso> findAll();
}
