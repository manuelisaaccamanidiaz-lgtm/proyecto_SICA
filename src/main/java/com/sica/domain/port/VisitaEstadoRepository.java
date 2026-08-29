package com.sica.domain.port;

import com.sica.domain.VisitaEstado;
import java.util.List;

/**
 * Puerto de salida: Repositorio de estados de visita (catálogo).
 */
public interface VisitaEstadoRepository {

    VisitaEstado findById(int id);

    VisitaEstado findByNombreEstado(String nombreEstado);

    List<VisitaEstado> findAll();
}
