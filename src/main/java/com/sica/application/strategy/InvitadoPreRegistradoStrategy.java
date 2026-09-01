package com.sica.application.strategy;

import com.sica.application.AuditoriaService;
import com.sica.application.AutorizacionService;
import com.sica.domain.Persona;
import com.sica.domain.Visita;
import com.sica.domain.TipoPersona;
import com.sica.domain.port.*;

import java.time.LocalDateTime;

/**
 * Flujo 1: Invitado Pre-Registrado.
 * La persona ya existe en la BD como tipo INVITADO.
 * Se verifica su estado de acceso, se busca si ya tiene visita en curso,
 * y se le registra la entrada si todo esta en orden.
 *
 * Requiere permiso: registrar_visita
 */
public class InvitadoPreRegistradoStrategy implements FlujoAccesoStrategy {

    private final PersonaRepository personaRepository;
    private final VisitaRepository visitaRepository;
    private final VisitaEstadoRepository visitaEstadoRepository;
    private final PersonaEstadoAccesoRepository personaEstadoAccesoRepository;
    private final AuditoriaService auditoriaService;
    private final AutorizacionService autorizacionService;

    public InvitadoPreRegistradoStrategy(PersonaRepository personaRepository,
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
        return "Invitado Pre-Registrado";
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

            // 1. Buscar la persona por ID o documento
            Persona persona = buscarPersona(solicitud);
            if (persona == null) {
                return ResultadoAcceso.fallo("No se encontro la persona con los datos proporcionados.");
            }

            // 2. Verificar que sea tipo INVITADO
            if (persona.getTipoPersona() != TipoPersona.INVITADO) {
                return ResultadoAcceso.fallo(
                    "La persona '" + persona.getNombre() + "' no es un invitado. "
                    + "Use el flujo correspondiente para trabajadores.");
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

            // 4. Verificar que no tenga visita en curso
            var enCurso = visitaRepository.findEnCurso();
            for (Visita v : enCurso) {
                if (v.getPersonaId() == persona.getId()) {
                    return ResultadoAcceso.fallo(
                        "La persona '" + persona.getNombre()
                        + "' ya tiene una visita en curso (ID: " + v.getId() + ").");
                }
            }

            // 5. Obtener estado "Dentro" del catalogo
            var estadoDentro = visitaEstadoRepository.findByNombreEstado("Dentro");
            if (estadoDentro == null) {
                return ResultadoAcceso.fallo("Error: Estado 'Dentro' no encontrado en catalogo.");
            }

            // 6. Crear la visita
            Visita visita = new Visita();
            visita.setPersonaId(persona.getId());
            visita.setFechaEntrada(LocalDateTime.now());
            visita.setFechaSalida(null);
            visita.setEstadoVisitaId(estadoDentro.getId());
            visita.setVehiculoPlaca(solicitud.getVehiculoPlaca());
            visita.setVisitaAprobadaPor(solicitud.getUsuarioId());

            Visita guardada = visitaRepository.guardar(visita);

            // 7. Auditar
            auditoriaService.registrar(solicitud.getUsuarioId(),
                    "ACCESO_INVITADO_PRE_REGISTRADO", "visitas", guardada.getId(),
                    "Invitado pre-registrado: " + persona.getNombre()
                    + " | Doc: " + persona.getDocumentoIdentidad()
                    + " | Visita ID: " + guardada.getId());

            return ResultadoAcceso.exito(
                "Invitado pre-registrado '" + persona.getNombre()
                + "' ingresado correctamente. Visita ID: " + guardada.getId())
                .visitaId(guardada.getId())
                .personaId(persona.getId());

        } catch (Exception e) {
            return ResultadoAcceso.fallo("Error al procesar acceso: " + e.getMessage());
        }
    }

    private Persona buscarPersona(SolicitudAcceso s) {
        if (s.getPersonaId() > 0) {
            return personaRepository.findById(s.getPersonaId());
        }
        if (s.getDocumentoIdentidad() != null && !s.getDocumentoIdentidad().isEmpty()) {
            return personaRepository.findByDocumentoIdentidad(s.getDocumentoIdentidad());
        }
        return null;
    }
}
