package com.sica.application.strategy;

import com.sica.application.AuditoriaService;
import com.sica.application.AutorizacionService;
import com.sica.domain.*;
import com.sica.domain.port.*;

import java.time.LocalDateTime;

/**
 * Flujo 3: Trabajador con Carnet Olvidado.
 * El trabajador es conocido en el sistema (tipo TRABAJADOR) pero no presenta carnet.
 * Se verifica su identidad por documento, se confirma que este activo,
 * y se le permite el paso.
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

            // 5. Verificar que no tenga visita en curso
            var enCurso = visitaRepository.findEnCurso();
            for (Visita v : enCurso) {
                if (v.getPersonaId() == persona.getId()) {
                    return ResultadoAcceso.fallo(
                        "El trabajador '" + persona.getNombre()
                        + "' ya tiene una visita en curso (ID: " + v.getId() + ").");
                }
            }

            // 6. Obtener estado "Dentro"
            var estadoDentro = visitaEstadoRepository.findByNombreEstado("Dentro");
            if (estadoDentro == null) {
                return ResultadoAcceso.fallo("Error: Estado 'Dentro' no encontrado en catalogo.");
            }

            // 7. Registrar la visita (sin placa, sin carnet, solo documento verificado)
            Visita visita = new Visita();
            visita.setPersonaId(persona.getId());
            visita.setFechaEntrada(LocalDateTime.now());
            visita.setFechaSalida(null);
            visita.setEstadoVisitaId(estadoDentro.getId());
            visita.setVehiculoPlaca(solicitud.getVehiculoPlaca());
            visita.setVisitaAprobadaPor(solicitud.getUsuarioId());

            Visita guardada = visitaRepository.guardar(visita);

            // 8. Auditar
            auditoriaService.registrar(solicitud.getUsuarioId(),
                    "ACCESO_TRABAJADOR_CARNET_OLVIDADO", "visitas", guardada.getId(),
                    "Trabajador sin carnet verificado por documento: " + persona.getNombre()
                    + " | Doc: " + persona.getDocumentoIdentidad()
                    + " | Visita ID: " + guardada.getId());

            return ResultadoAcceso.exito(
                "Trabajador '" + persona.getNombre()
                + "' verificado por documento y ingresado. Visita ID: " + guardada.getId())
                .visitaId(guardada.getId())
                .personaId(persona.getId());

        } catch (Exception e) {
            return ResultadoAcceso.fallo("Error al procesar trabajador: " + e.getMessage());
        }
    }
}
