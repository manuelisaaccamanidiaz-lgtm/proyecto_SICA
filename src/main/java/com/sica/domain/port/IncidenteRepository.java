package com.sica.domain.port;

import com.sica.domain.Incidente;
import java.util.List;

/**
 * Puerto de salida: Repositorio de Incidentes.
 * Ahora se asocia a una visita_id en lugar de persona_id.
 */
public interface IncidenteRepository {

    Incidente guardar(Incidente incidente);

    Incidente findById(int id);

    List<Incidente> findAll();

    List<Incidente> findByVisitaId(int visitaId);

    List<Incidente> findByFecha(String fecha);

    void actualizar(Incidente incidente);

    void eliminar(int id);
}
