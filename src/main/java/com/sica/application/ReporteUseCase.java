package com.sica.application;

import com.sica.domain.Incidente;
import com.sica.domain.Persona;
import com.sica.domain.Visita;

import java.util.List;
import java.util.Map;

/**
 * Caso de uso: Generación de reportes.
 * Puerto de entrada para consultas y reportes del sistema.
 * Usa Stream API obligatoriamente para el procesamiento de datos.
 */
public interface ReporteUseCase {

    /**
     * Retorna las personas actualmente dentro del complejo.
     * Filtra visitas con fecha_salida NULL y resuelve la persona asociada.
     *
     * @return lista de personas dentro del complejo
     */
    List<Persona> personasDentroDelComplejo(int empresaId);

    /**
     * Retorna incidentes ocurridos en un rango de fechas.
     * Formato de fechas: "YYYY-MM-DD".
     *
     * @param fechaInicio fecha de inicio (inclusive)
     * @param fechaFin    fecha de fin (inclusive)
     * @return lista de incidentes en el rango
     */
    List<Incidente> incidentesPorRangoFechas(String fechaInicio, String fechaFin);

    /**
     * Retorna visitas agrupadas por su estado (nombre del estado).
     *
     * @return mapa con clave = nombre del estado, valor = lista de visitas
     */
    Map<String, List<Visita>> visitasAgrupadasPorEstado();
}
