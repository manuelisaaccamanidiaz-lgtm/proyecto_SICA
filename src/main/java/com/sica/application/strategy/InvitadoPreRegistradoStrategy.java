package com.sica.application.strategy;

import com.sica.domain.BitacoraAuditoria;
import com.sica.domain.Persona;
import com.sica.domain.Visita;
import com.sica.domain.TipoPersona;
import com.sica.domain.port.*;

import java.time.LocalDateTime;

/**
 * Flujo 1: Invitado Pre-Registrado.
 * La persona ya existe en la BD como tipo INVITADO.
 * Se verifica su estado de acceso, se busca si ya tiene visita en curso,
 * y se le registra la entrada si todo está en orden.
 */
public class InvitadoPreRegistradoStrategy implements FlujoAccesoStrategy {

    private final PersonaRepository personaRepository;
    private final VisitaRepository visitaRepository;
    private final VisitaEstadoRepository visitaEstadoRepository;
    private final PersonaEstadoAccesoRepository personaEstadoAccesoRepository;
    private final BitacoraAuditoriaRepository auditoriaRepository;

    public InvitadoPreRegistradoStrategy(PersonaRepository personaRepository,
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
        return "Invitado Pre-Registrado";
    }

    @Override
    public ResultadoAcceso procesar(SolicitudAcceso solicitud) {
        try {
            // 1. Buscar la persona por ID o documento
            Persona persona = buscarPersona(solicitud);
            if (persona == null) {
                return ResultadoAcceso.fallo("No se encontró la persona con los datos proporcionados.");
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

            // 5. Obtener estado "Dentro" del catálogo
            var estadoDentro = visitaEstadoRepository.findByNombreEstado("Dentro");
            if (estadoDentro == null) {
                return ResultadoAcceso.fallo("Error: Estado 'Dentro' no encontrado en catálogo.");
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
            auditar(solicitud.getUsuarioId(), "ACCESO_INVITADO_PRE_REGISTRADO", "visitas", guardada.getId(),
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
