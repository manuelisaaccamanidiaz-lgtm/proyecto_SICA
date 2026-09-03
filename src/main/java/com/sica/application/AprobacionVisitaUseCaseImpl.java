package com.sica.application;

import com.sica.domain.Visita;
import com.sica.domain.port.PersonaRepository;
import com.sica.domain.port.VisitaEstadoRepository;
import com.sica.domain.port.VisitaRepository;

/**
 * Implementacion del caso de uso de aprobacion de visitas.
 *
 * Flujo:
 * <ol>
 *   <li>Verifica permiso 'aprobar_visita' via {@link AutorizacionService}</li>
 *   <li>Busca la visita por ID</li>
 *   <li>Valida que este en estado "Pendiente de Aprobacion"</li>
 *   <li>Cambia el estado a "Aprobado" o "Rechazado"</li>
 *   <li>Registra visita_aprobada_por = ID del funcionario</li>
 *   <li>Registra la accion en bitacora de auditoria</li>
 * </ol>
 */
public class AprobacionVisitaUseCaseImpl implements AprobacionVisitaUseCase {

    private final VisitaRepository visitaRepository;
    private final VisitaEstadoRepository visitaEstadoRepository;
    private final PersonaRepository personaRepository;
    private final AuditoriaService auditoriaService;
    private final AutorizacionService autorizacionService;

    public AprobacionVisitaUseCaseImpl(VisitaRepository visitaRepository,
                                        VisitaEstadoRepository visitaEstadoRepository,
                                        PersonaRepository personaRepository,
                                        AuditoriaService auditoriaService,
                                        AutorizacionService autorizacionService) {
        this.visitaRepository = visitaRepository;
        this.visitaEstadoRepository = visitaEstadoRepository;
        this.personaRepository = personaRepository;
        this.auditoriaService = auditoriaService;
        this.autorizacionService = autorizacionService;
    }

    @Override
    public java.util.List<Visita> consultarSolicitudesPendientes() {
        var estadoPendiente = visitaEstadoRepository.findByNombreEstado("Pendiente de Aprobacion");
        if (estadoPendiente == null) {
            return java.util.Collections.emptyList();
        }
        return visitaRepository.findByEstadoVisitaId(estadoPendiente.getId());
    }

    @Override
    public Visita aprobarVisita(int visitaId, int funcionarioId) {
        // 1. Verificar permiso
        if (!autorizacionService.tienePermiso(funcionarioId, "aprobar_visita")) {
            throw new com.sica.domain.exception.PermisoDenegadoException(
                "El usuario no tiene permiso para aprobar visitas", funcionarioId, "aprobar_visita");
        }

        // 2. Buscar la visita
        Visita visita = visitaRepository.findById(visitaId);
        if (visita == null) {
            throw new com.sica.domain.exception.VisitaNoEncontradaException("La visita ID " + visitaId + " no existe");
        }

        // 3. Verificar que este pendiente
        var estadoPendiente = visitaEstadoRepository.findByNombreEstado("Pendiente de Aprobacion");
        if (estadoPendiente == null) {
            throw new com.sica.domain.exception.EstadoNoEncontradoException("Pendiente de Aprobacion");
        }
        if (visita.getEstadoVisitaId() != estadoPendiente.getId()) {
            String estadoActual = obtenerNombreEstado(visita.getEstadoVisitaId());
            throw new IllegalStateException(
                "La visita ID " + visitaId + " no esta pendiente de aprobacion. "
                + "Estado actual: " + estadoActual);
        }

        // 4. Obtener estado "Aprobado"
        var estadoAprobado = visitaEstadoRepository.findByNombreEstado("Aprobado");
        if (estadoAprobado == null) {
            throw new com.sica.domain.exception.EstadoNoEncontradoException("Aprobado");
        }

        // 5. Actualizar la visita
        visita.setEstadoVisitaId(estadoAprobado.getId());
        visita.setVisitaAprobadaPor(funcionarioId);
        // fecha_entrada permanece NULL - se registra cuando el invitado ingrese
        visitaRepository.actualizar(visita);

        // 6. Auditar
        String nombrePersona = obtenerNombrePersona(visita.getPersonaId());
        auditoriaService.registrar(funcionarioId,
                "APROBACION_VISITA", "visitas", visitaId,
                "Visita ID " + visitaId + " APROBADA"
                + " | Persona: " + nombrePersona
                + " | Aprobada por usuario ID: " + funcionarioId);

        return visita;
    }

    @Override
    public Visita rechazarVisita(int visitaId, int funcionarioId) {
        // 1. Verificar permiso
        if (!autorizacionService.tienePermiso(funcionarioId, "aprobar_visita")) {
            throw new com.sica.domain.exception.PermisoDenegadoException(
                "El usuario no tiene permiso para aprobar visitas", funcionarioId, "aprobar_visita");
        }

        // 2. Buscar la visita
        Visita visita = visitaRepository.findById(visitaId);
        if (visita == null) {
            throw new com.sica.domain.exception.VisitaNoEncontradaException("La visita ID " + visitaId + " no existe");
        }

        // 3. Verificar que este pendiente
        var estadoPendiente = visitaEstadoRepository.findByNombreEstado("Pendiente de Aprobacion");
        if (estadoPendiente == null) {
            throw new com.sica.domain.exception.EstadoNoEncontradoException("Pendiente de Aprobacion");
        }
        if (visita.getEstadoVisitaId() != estadoPendiente.getId()) {
            String estadoActual = obtenerNombreEstado(visita.getEstadoVisitaId());
            throw new IllegalStateException(
                "La visita ID " + visitaId + " no esta pendiente de aprobacion. "
                + "Estado actual: " + estadoActual);
        }

        // 4. Obtener estado "Rechazado"
        var estadoRechazado = visitaEstadoRepository.findByNombreEstado("Rechazado");
        if (estadoRechazado == null) {
            throw new com.sica.domain.exception.EstadoNoEncontradoException("Rechazado");
        }

        // 5. Actualizar la visita
        visita.setEstadoVisitaId(estadoRechazado.getId());
        visita.setVisitaAprobadaPor(funcionarioId);
        visitaRepository.actualizar(visita);

        // 6. Auditar
        String nombrePersona = obtenerNombrePersona(visita.getPersonaId());
        auditoriaService.registrar(funcionarioId,
                "RECHAZO_VISITA", "visitas", visitaId,
                "Visita ID " + visitaId + " RECHAZADA"
                + " | Persona: " + nombrePersona
                + " | Rechazada por usuario ID: " + funcionarioId);

        return visita;
    }

    /**
     * Obtiene el nombre de un estado de visita por su ID.
     */
    private String obtenerNombreEstado(int estadoId) {
        var estado = visitaEstadoRepository.findById(estadoId);
        return estado != null ? estado.getNombreEstado() : "Desconocido (ID:" + estadoId + ")";
    }

    /**
     * Obtiene el nombre de una persona por su ID.
     */
    private String obtenerNombrePersona(int personaId) {
        var persona = personaRepository.findById(personaId);
        return persona != null ? persona.getNombre() : "Desconocida (ID:" + personaId + ")";
    }
}
