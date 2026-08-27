package com.sica.domain.port;

import com.sica.domain.Incidente;
import java.util.List;

/**
 * Puerto de salida: Repositorio de Incidentes.
 */
public interface IncidenteRepository {

    Incidente guardar(Incidente incidente);

    Incidente findById(int id);

    List<Incidente> findAll();

    List<Incidente> findByPersonaId(int personaId);

    List<Incidente> findByFecha(String fecha);

    void actualizar(Incidente incidente);

    void eliminar(int id);
}
