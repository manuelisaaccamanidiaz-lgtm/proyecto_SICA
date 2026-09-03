package com.sica.application;

import com.sica.domain.Visita;
import java.util.List;

/**
 * Caso de uso: Aprobacion de solicitudes de acceso pendientes.
 *
 * Permite a un funcionario con permiso 'aprobar_visita' consultar,
 * aprobar o rechazar solicitudes de visita que estan en estado
 * "Pendiente de Aprobacion".
 */
public interface AprobacionVisitaUseCase {

    /**
     * Obtiene todas las visitas pendientes de aprobacion.
     *
     * @return lista de visitas con estado "Pendiente de Aprobacion"
     */
    List<Visita> consultarSolicitudesPendientes();

    /**
     * Aprueba una solicitud de acceso pendiente.
     *
     * @param visitaId       ID de la visita a aprobar
     * @param funcionarioId  ID del funcionario que aprueba
     * @return la visita actualizada
     * @throws com.sica.domain.exception.PermisoDenegadoException   si el usuario no tiene permiso
     * @throws com.sica.domain.exception.VisitaNoEncontradaException si la visita no existe
     * @throws com.sica.domain.exception.EstadoNoEncontradoException si el estado no esta en catalogo
     */
    Visita aprobarVisita(int visitaId, int funcionarioId);

    /**
     * Rechaza una solicitud de acceso pendiente.
     *
     * @param visitaId       ID de la visita a rechazar
     * @param funcionarioId  ID del funcionario que rechaza
     * @return la visita actualizada
     * @throws com.sica.domain.exception.PermisoDenegadoException   si el usuario no tiene permiso
     * @throws com.sica.domain.exception.VisitaNoEncontradaException si la visita no existe
     * @throws com.sica.domain.exception.EstadoNoEncontradoException si el estado no esta en catalogo
     */
    Visita rechazarVisita(int visitaId, int funcionarioId);
}
