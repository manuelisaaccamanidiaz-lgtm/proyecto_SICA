package com.sica.application.strategy;

import com.sica.application.AuditoriaService;
import com.sica.application.AutorizacionService;
import com.sica.domain.*;
import com.sica.domain.port.*;

/**
 * Flujo 3: Trabajador con Carnet Olvidado.
 * El trabajador es conocido en el sistema (tipo TRABAJADOR) pero no presenta carnet.
 * Se verifica su identidad por documento, se confirma que este activo,
 * y se crea una solicitud de visita con estado "Pendiente de Aprobacion".
 * La fecha de entrada NO se registra y visita_aprobada_por queda NULL.
 * Un funcionario debe aprobar o rechazar la solicitud.
 *
 * Requiere permiso: registrar_visita
 */
public class TrabajadorCarnetOlvidadoStrategy implements FlujoAccesoStrategy {

    private final PersonaRepository personaRepository;
    private final VisitaRepository visitaRepository;
    private final VisitaEstadoRepository visitaEstadoRepository;
    private final PersonaEstadoAccesoRepository personaEstadoAccesoRepository;
    private final AuditoriaService auditoriaService;
    private final AutorizacionService autorizacionService;

    public TrabajadorCarnetOlvidadoStrategy(PersonaRepository personaRepository,
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
        return "Trabajador con Carnet Olvidado";
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

            // 1. Se requiere documento de identidad para verificar al trabajador
            if (solicitud.getDocumentoIdentidad() == null || solicitud.getDocumentoIdentidad().isEmpty()) {
                return ResultadoAcceso.fallo("Se requiere el numero de documento para verificar al trabajador.");
            }

            // 2. Buscar la persona por documento
            Persona persona = personaRepository.findByDocumentoIdentidad(solicitud.getDocumentoIdentidad());
            if (persona == null) {
                return ResultadoAcceso.fallo(
                    "No se encontro ninguna persona con el documento: " + solicitud.getDocumentoIdentidad());
            }

            // 3. Verificar que sea TRABAJADOR
            if (persona.getTipoPersona() != TipoPersona.TRABAJADOR) {
                return ResultadoAcceso.fallo(
                    "La persona '" + persona.getNombre()
                    + "' no es un trabajador. Use el flujo correspondiente para invitados.");
            }

            // 4. Verificar estado de acceso
            if (persona.getEstadoAccesoId() != null) {
                var estado = personaEstadoAccesoRepository.findById(persona.getEstadoAccesoId());
                if (estado != null && estado.getNombreEstado().contains("Prohibicion")) {
                    return ResultadoAcceso.fallo(
                        "ACCESO DENEGADO: " + persona.getNombre()
                        + " tiene estado '" + estado.getNombreEstado() + "'.");
                }
            }

            // 5. Verificar que no tenga visita en curso o pendiente
            var enCurso = visitaRepository.findEnCurso();
            for (Visita v : enCurso) {
                if (v.getPersonaId() == persona.getId()) {
                    return ResultadoAcceso.fallo(
                        "El trabajador '" + persona.getNombre()
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
                            "El trabajador '" + persona.getNombre()
                            + "' ya tiene una solicitud pendiente de aprobacion (Visita ID: " + v.getId() + ").");
                    }
                }
            }

            // 6. Obtener estado "Pendiente de Aprobacion"
            var estadoPendiente = visitaEstadoRepository.findByNombreEstado("Pendiente de Aprobacion");
            if (estadoPendiente == null) {
                return ResultadoAcceso.fallo("Error: Estado 'Pendiente de Aprobacion' no encontrado en catalogo.");
            }

            // 7. Crear la solicitud de visita pendiente (sin fecha_entrada, sin aprobador)
            Visita visita = new Visita();
            visita.setPersonaId(persona.getId());
            visita.setFechaEntrada(null);        // NO se registra aun
            visita.setFechaSalida(null);
            visita.setEstadoVisitaId(estadoPendiente.getId());
            visita.setVehiculoPlaca(solicitud.getVehiculoPlaca());
            visita.setVisitaAprobadaPor(null);    // NULL hasta que funcione apruebe

            Visita guardada = visitaRepository.guardar(visita);

            // 8. Auditar
            auditoriaService.registrar(solicitud.getUsuarioId(),
                    "ACCESO_TRABAJADOR_CARNET_OLVIDADO_PENDIENTE", "visitas", guardada.getId(),
                    "Solicitud de acceso creada para trabajador sin carnet: " + persona.getNombre()
                    + " | Doc: " + persona.getDocumentoIdentidad()
                    + " | Visita ID: " + guardada.getId()
                    + " | Estado: Pendiente de Aprobacion");

            return ResultadoAcceso.exito(
                "Solicitud de acceso creada para trabajador '" + persona.getNombre()
                + "'. Visita ID: " + guardada.getId()
                + ". Estado: Pendiente de Aprobacion."
                + " Un funcionario debe aprobar o rechazar la solicitud.")
                .visitaId(guardada.getId())
                .personaId(persona.getId());

        } catch (Exception e) {
            return ResultadoAcceso.fallo("Error al procesar trabajador: " + e.getMessage());
        }
    }
}
