package com.sica.application.strategy;

import com.sica.domain.*;
import com.sica.domain.port.*;

import java.time.LocalDateTime;

/**
 * Flujo 2: Invitado No Anunciado.
 * La persona NO está pre-registrada en la BD.
 * El guarda de seguridad la registra al vuelo con datos básicos,
 * se crea la persona tipo INVITADO y se le registra la entrada.
 * Requiere aprobación de un funcionario (usuarioId).
 */
public class InvitadoNoAnunciadoStrategy implements FlujoAccesoStrategy {

    private final PersonaRepository personaRepository;
    private final VisitaRepository visitaRepository;
    private final VisitaEstadoRepository visitaEstadoRepository;
    private final PersonaEstadoAccesoRepository personaEstadoAccesoRepository;
    private final BitacoraAuditoriaRepository auditoriaRepository;

    public InvitadoNoAnunciadoStrategy(PersonaRepository personaRepository,
                                        VisitaRepository visitaRepository,
                                        VisitaEstadoRepository visitaEstadoRepository,
                                        PersonaEstadoAccesoRepository personaEstadoAccesoRepository,
                                        BitacoraAuditoriaRepository auditoriaRepository) {
        this.personaRepository = personaRepository;
        this.visitaRepository = visitaRepository;
        this.visitaEstadoRepository = visitaEstadoRepository;
        this.personaEstadoAccesoRepository = personaEstadoAccesoRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public String getNombreFlujo() {
        return "Invitado No Anunciado";
    }

    @Override
    public ResultadoAcceso procesar(SolicitudAcceso solicitud) {
        try {
            // 1. Verificar que se provea documento de identidad
            if (solicitud.getDocumentoIdentidad() == null || solicitud.getDocumentoIdentidad().isEmpty()) {
                return ResultadoAcceso.fallo("Se requiere número de documento de identidad.");
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
                persona.setEmpresaId(null); // sin empresa
                persona.setTipoPersona(TipoPersona.INVITADO);
                persona.setUrlFoto(null);

                // Buscar estado "Activo" por defecto
                var estadoActivo = personaEstadoAccesoRepository.findByNombreEstado("Activo");
                if (estadoActivo != null) {
                    persona.setEstadoAccesoId(estadoActivo.getId());
                }

                persona = personaRepository.guardar(persona);
            }

            // 3. Verificar estado de acceso (puede haber sido prohibido)
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

            // 5. Obtener estado "Dentro"
            var estadoDentro = visitaEstadoRepository.findByNombreEstado("Dentro");
            if (estadoDentro == null) {
                return ResultadoAcceso.fallo("Error: Estado 'Dentro' no encontrado en catálogo.");
            }

            // 6. Crear la visita (con aprobación del funcionario)
            Visita visita = new Visita();
            visita.setPersonaId(persona.getId());
            visita.setFechaEntrada(LocalDateTime.now());
            visita.setFechaSalida(null);
            visita.setEstadoVisitaId(estadoDentro.getId());
            visita.setVehiculoPlaca(solicitud.getVehiculoPlaca());
            visita.setVisitaAprobadaPor(solicitud.getUsuarioId());

            Visita guardada = visitaRepository.guardar(visita);

            // 7. Auditar
            auditar(solicitud.getUsuarioId(), "ACCESO_INVITADO_NO_ANUNCIADO", "visitas", guardada.getId(),
                    "Invitado no anunciado registrado: " + persona.getNombre()
                    + " | Doc: " + persona.getDocumentoIdentidad()
                    + " | Visita ID: " + guardada.getId());

            String mensaje = persona.getId() > 0 && solicitud.getPersonaId() == 0
                ? "Nuevo invitado creado e ingresado. Visita ID: " + guardada.getId()
                : "Invitado no anunciado '" + persona.getNombre() + "' ingresado. Visita ID: " + guardada.getId();

            return ResultadoAcceso.exito(mensaje)
                .visitaId(guardada.getId())
                .personaId(persona.getId());

        } catch (Exception e) {
            return ResultadoAcceso.fallo("Error al procesar invitado no anunciado: " + e.getMessage());
        }
    }

    private void auditar(int userId, String accion, String tabla, int registroId, String detalle) {
        try {
            BitacoraAuditoria reg = new BitacoraAuditoria();
            reg.setUsuarioId(userId);
            reg.setAccionRealizada(accion);
            reg.setTablaAfectada(tabla);
            reg.setRegistroIdAfectado(registroId);
            reg.setDetalles(detalle);
            reg.setFechaHora(LocalDateTime.now());
            auditoriaRepository.guardar(reg);
        } catch (Exception e) {
            System.err.println("[SICA] Warning: auditoría falló: " + e.getMessage());
        }
    }
}
