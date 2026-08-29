package com.sica.application;

import com.sica.domain.Visita;

/**
 * Caso de uso: Registrar una visita.
 * Define el puerto de entrada para el registro de visitas.
 */
public interface RegistrarVisitaUseCase {

    /**
     * Registra el ingreso de una visita para una persona existente.
     *
     * @param personaId       ID de la persona que ingresa
     * @param usuarioId       ID del usuario (guarda/funcionario) que autoriza
     * @param vehiculoPlaca   placa del vehículo (puede ser null)
     * @return la Visita registrada con ID asignado y estado "Dentro"
     */
    Visita registrarIngreso(int personaId, int usuarioId, String vehiculoPlaca);

    /**
     * Registra el ingreso de un invitado pre-registrado (ya tiene estado en sistema).
     *
     * @param personaId       ID de la persona invitada
     * @param usuarioId       ID del usuario que autoriza
     * @param vehiculoPlaca   placa del vehículo (puede ser null)
     * @return la Visita registrada
     */
    Visita registrarIngresoInvitadoPreRegistrado(int personaId, int usuarioId, String vehiculoPlaca);

    /**
     * Registra la salida de una visita previamente registrada.
     *
     * @param visitaId ID de la visita a registrar salida
     */
    void registrarSalida(int visitaId);
}
