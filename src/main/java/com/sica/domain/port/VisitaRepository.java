package com.sica.domain.port;

import com.sica.domain.Visita;
import java.util.List;

/**
 * Puerto de salida: Repositorio de Visitas.
 */
public interface VisitaRepository {

    Visita guardar(Visita visita);

    Visita findById(int id);

    List<Visita> findAll();

    List<Visita> findByPersonaId(int personaId);

    List<Visita> findEnCurso(); // visitas sin fecha_salida

    List<Visita> findByEstadoVisitaId(int estadoVisitaId);

    List<Visita> findByFechaEntrada(String fecha);

    void actualizar(Visita visita);

    void eliminar(int id);
}
