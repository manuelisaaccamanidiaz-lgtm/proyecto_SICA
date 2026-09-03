package com.sica.application.strategy;

import com.sica.application.AuditoriaService;
import com.sica.application.AutorizacionService;
import com.sica.domain.*;
import com.sica.domain.port.*;

/**
 * Flujo 2: Invitado No Anunciado.
 * La persona NO esta pre-registrada en la BD.
 * El guarda de seguridad la registra al vuelo con datos basicos,
 * se crea la persona tipo INVITADO y se crea una solicitud de visita
 * con estado "Pendiente de Aprobacion". La fecha de entrada NO se registra
 * y el campo visita_aprobada_por queda NULL.
 * Un funcionario debe aprobar o rechazar la solicitud antes de permitir el ingreso.
 *
 * Requiere permiso: registrar_visita
 */
public class InvitadoNoAnunciadoStrategy implements FlujoAccesoStrategy {

    private final PersonaRepository personaRepository;
    private final VisitaRepository visitaRepository;
    private final VisitaEstadoRepository visitaEstadoRepository;
    private final PersonaEstadoAccesoRepository personaEstadoAccesoRepository;
    private final AuditoriaService auditoriaService;
    private final AutorizacionService autorizacionService;

    public InvitadoNoAnunciadoStrategy(PersonaRepository personaRepository,
                                        VisitaRepository visitaRepository,
                                        VisitaEstadoRepository visitaEstadoRepository,
                                        PersonaEstadoAccesoRepository personaEstadoAccesoRepository,
                                        AuditoriaService auditoriaService,
                                        AutorizacionService autorizacionService) {
        this.personaRepository = personaRepository;
        this.visitaRepository = visitaRepository;
        this.visitaEstadoRepository = visitaEstadoRepository;
        this.personaEstadoAccesoRepository = personaEstadoAccesoRepository;
        this.auditoriaService = auditoriaService;
        this.autorizacionService = autorizacionService;
    }

    @Override
    public String getNombreFlujo() {
        return "Invitado No Anunciado";
    }

    @Override
    public ResultadoAcceso procesar(SolicitudAcceso solicitud) {
        try {
            // 0. Verificar permiso RBAC
            if (!autorizacionService.tienePermiso(solicitud.getUsuarioId(), "registrar_visita")) {
                return ResultadoAcceso.fallo(
                    "ACCESO DENEGADO: No tiene permiso 'registrar_visita'. "
                    + "Contacte al administrador.");
            }

            // 1. Verificar que se provea documento de identidad
            if (solicitud.getDocumentoIdentidad() == null || solicitud.getDocumentoIdentidad().isEmpty()) {
                return ResultadoAcceso.fallo("Se requiere numero de documento de identidad.");
            }
            if (solicitud.getNombreInvitado() == null || solicitud.getNombreInvitado().isEmpty()) {
                return ResultadoAcceso.fallo("Se requiere el nombre del invitado.");
            }

            // 2. Verificar si ya existe en la BD (puede haber visitado antes)
            Persona persona = personaRepository.findByDocumentoIdentidad(solicitud.getDocumentoIdentidad());

            if (persona == null) {
                // 2a. Crear nueva persona tipo INVITADO
                persona = new Persona();
                persona.setNombre(solicitud.getNombreInvitado());
                persona.setDocumentoIdentidad(solicitud.getDocumentoIdentidad());
                persona.setEmpresaId(null);
                persona.setTipoPersona(TipoPersona.INVITADO);
                persona.setUrlFoto(null);

                var estadoActivo = personaEstadoAccesoRepository.findByNombreEstado("Activo");
                if (estadoActivo != null) {
                    persona.setEstadoAccesoId(estadoActivo.getId());
                }

                persona = personaRepository.guardar(persona);
            }

            // 3. Verificar estado de acceso
            if (persona.getEstadoAccesoId() != null) {
                var estado = personaEstadoAccesoRepository.findById(persona.getEstadoAccesoId());
                if (estado != null && estado.getNombreEstado().contains("Prohibicion")) {
                    return ResultadoAcceso.fallo(
                        "ACCESO DENEGADO: " + persona.getNombre()
                        + " tiene estado '" + estado.getNombreEstado() + "'.");
                }
            }

            // 4. Verificar que no tenga visita en curso o pendiente
            var enCurso = visitaRepository.findEnCurso();
            for (Visita v : enCurso) {
                if (v.getPersonaId() == persona.getId()) {
                    return ResultadoAcceso.fallo(
                        "La persona '" + persona.getNombre()
                        + "' ya tiene una visita en curso (ID: " + v.getId() + ").");
                }
            }

            // Verificar que no tenga visita pendiente de aprobacion
            var pendientes = visitaEstadoRepository.findByNombreEstado("Pendiente de Aprobacion");
            if (pendientes != null) {
                var visitasPendientes = visitaRepository.findByEstadoVisitaId(pendientes.getId());
                for (Visita v : visitasPendientes) {
                    if (v.getPersonaId() == persona.getId()) {
                        return ResultadoAcceso.fallo(
                            "La persona '" + persona.getNombre()
                            + "' ya tiene una solicitud pendiente de aprobacion (Visita ID: " + v.getId() + ").");
                    }
                }
            }

            // 5. Obtener estado "Pendiente de Aprobacion" del catalogo
            var estadoPendiente = visitaEstadoRepository.findByNombreEstado("Pendiente de Aprobacion");
            if (estadoPendiente == null) {
                return ResultadoAcceso.fallo("Error: Estado 'Pendiente de Aprobacion' no encontrado en catalogo.");
            }

            // 6. Crear la visita pendiente de aprobacion (sin fecha_entrada, sin visita_aprobada_por)
            Visita visita = new Visita();
            visita.setPersonaId(persona.getId());
            visita.setFechaEntrada(null);        // NO se registra aun
            visita.setFechaSalida(null);
            visita.setEstadoVisitaId(estadoPendiente.getId());
            visita.setVehiculoPlaca(solicitud.getVehiculoPlaca());
            visita.setVisitaAprobadaPor(null);    // NULL hasta que funcione apruebe

            Visita guardada = visitaRepository.guardar(visita);

            // 7. Auditar
            auditoriaService.registrar(solicitud.getUsuarioId(),
                    "ACCESO_INVITADO_NO_ANUNCIADO_PENDIENTE", "visitas", guardada.getId(),
                    "Solicitud de acceso creada para invitado no anunciado: " + persona.getNombre()
                    + " | Doc: " + persona.getDocumentoIdentidad()
                    + " | Visita ID: " + guardada.getId()
                    + " | Estado: Pendiente de Aprobacion");

            String mensaje = "Solicitud de acceso creada para '" + persona.getNombre()
                + "'. Visita ID: " + guardada.getId()
                + ". Estado: Pendiente de Aprobacion."
                + " Un funcionario debe aprobar o rechazar la solicitud.";

            return ResultadoAcceso.exito(mensaje)
                .visitaId(guardada.getId())
                .personaId(persona.getId());

        } catch (Exception e) {
            return ResultadoAcceso.fallo("Error al procesar invitado no anunciado: " + e.getMessage());
        }
    }
}
