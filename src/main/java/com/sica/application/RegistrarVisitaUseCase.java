package com.sica.application;

import com.sica.domain.Visita;

/**
 * Caso de uso: Registrar una visita.
 * Define el puerto de entrada para el registro de visitas.
 */
public interface RegistrarVisitaUseCase {

    /**
     * Registra el ingreso de una visita.
     *
     * @param personaId     ID de la persona que ingresa
     * @param funcionarioId ID del funcionario que autoriza la visita
     * @return la Visita registrada con ID asignado
     */
    Visita registrarIngreso(int personaId, int funcionarioId);

    /**
     * Registra la salida de una visita previamente registrada.
     *
     * @param visitaId ID de la visita a registrar salida
     */
    void registrarSalida(int visitaId);
}
