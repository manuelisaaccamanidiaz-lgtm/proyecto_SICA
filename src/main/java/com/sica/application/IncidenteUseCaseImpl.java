package com.sica.application;

import com.sica.domain.Incidente;
import com.sica.domain.Persona;
import com.sica.domain.PersonaEstadoAcceso;
import com.sica.domain.port.*;

/**
 * Implementacion del caso de uso: Registrar incidente de seguridad.
 *
 * Flujo:
 * 1. Verifica permiso "registrar_incidente" via AutorizacionService
 * 2. Valida que la visita exista
 * 3. Busca la persona asociada a la visita
 * 4. Registra el incidente en la BD
 * 5. Actualiza el estado_acceso_id de la persona a "Con Prohibicion de Ingreso"
 *    (inserta el estado en persona_estados_acceso si no existe)
 * 6. Registra la accion en bitacora de auditoria via AuditoriaService
 */
public class IncidenteUseCaseImpl implements IncidenteUseCase {

    private final IncidenteRepository incidenteRepository;
    private final VisitaRepository visitaRepository;
    private final PersonaRepository personaRepository;
    private final PersonaEstadoAccesoRepository personaEstadoAccesoRepository;
    private final AuditoriaService auditoriaService;
    private final AutorizacionService autorizacionService;

    public IncidenteUseCaseImpl(IncidenteRepository incidenteRepository,
                                 VisitaRepository visitaRepository,
                                 PersonaRepository personaRepository,
                                 PersonaEstadoAccesoRepository personaEstadoAccesoRepository,
                                 AuditoriaService auditoriaService,
                                 AutorizacionService autorizacionService) {
        this.incidenteRepository = incidenteRepository;
        this.visitaRepository = visitaRepository;
        this.personaRepository = personaRepository;
        this.personaEstadoAccesoRepository = personaEstadoAccesoRepository;
        this.auditoriaService = auditoriaService;
        this.autorizacionService = autorizacionService;
    }

    @Override
    public Incidente registrarIncidente(int visitaId, int reportadoPorId, String descripcion) {
        // 0. Verificar permiso RBAC
        if (!autorizacionService.tienePermiso(reportadoPorId, "registrar_incidente")) {
            throw new RuntimeException(
                "ACCESO DENEGADO: No tiene permiso 'registrar_incidente'. "
                + "Contacte al administrador.");
        }

        // 1. Validar que la visita exista
        var visita = visitaRepository.findById(visitaId);
        if (visita == null) {
            throw new IllegalArgumentException("La visita con ID " + visitaId + " no existe.");
        }

        // 2. Registrar el incidente
        Incidente incidente = new Incidente();
        incidente.setVisitaId(visitaId);
        incidente.setReportadoPorId(reportadoPorId);
        incidente.setFecha(java.time.LocalDateTime.now());
        incidente.setDescripcion(descripcion);

        Incidente guardado = incidenteRepository.guardar(incidente);

        // 3. Obtener la persona de la visita
        Persona persona = personaRepository.findById(visita.getPersonaId());

        // 4. Actualizar estado de acceso de la persona a "Con Prohibicion de Ingreso"
        if (persona != null) {
            PersonaEstadoAcceso estadoProhibicion =
                personaEstadoAccesoRepository.findByNombreEstado("Con Prohibicion de Ingreso");

            // Insertar el estado si no existe
            if (estadoProhibicion == null) {
                PersonaEstadoAcceso nuevoEstado = new PersonaEstadoAcceso();
                nuevoEstado.setNombreEstado("Con Prohibicion de Ingreso");
                estadoProhibicion = personaEstadoAccesoRepository.guardar(nuevoEstado);
            }

            persona.setEstadoAccesoId(estadoProhibicion.getId());
            personaRepository.actualizar(persona);
        }

        // 5. Auditar
        auditoriaService.registrar(reportadoPorId, "INCIDENTE_REGISTRADO",
                "incidentes", guardado.getId(),
                "Incidente ID: " + guardado.getId()
                + " | Visita ID: " + visitaId
                + " | Persona: " + (persona != null ? persona.getNombre() : "desconocida")
                + " | Descripcion: " + descripcion);

        return guardado;
    }
}
