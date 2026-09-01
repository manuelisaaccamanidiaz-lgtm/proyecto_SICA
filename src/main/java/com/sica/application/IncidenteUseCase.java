package com.sica.application;

import com.sica.domain.Incidente;

/**
 * Caso de uso: Gestión de incidentes de seguridad.
 * Puerto de entrada para registrar incidentes y gestionar el acceso
 * de personas involucradas.
 */
public interface IncidenteUseCase {

    /**
     * Registra un incidente de seguridad asociado a una visita.
     * En el mismo flujo, actualiza el estado de acceso de la persona
     * afectada a "Con Prohibicion de Ingreso".
     *
     * @param visitaId          ID de la visita donde ocurrió el incidente
     * @param reportadoPorId    ID del usuario (Supervisor) que reporta
     * @param descripcion       descripción detallada del incidente
     * @return el Incidente registrado con ID asignado
     */
    Incidente registrarIncidente(int visitaId, int reportadoPorId, String descripcion);
}
