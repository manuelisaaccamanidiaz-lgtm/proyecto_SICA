package com.sica.application.strategy;

import com.sica.domain.*;
import com.sica.domain.port.*;

import java.time.LocalDateTime;

/**
 * Flujo 4: Salida Olvidada (Regularización).
 * Una persona que está "Dentro" olvidó registrar su salida.
 * El guarda busca la visita en curso por documento de la persona
 * y la cierra con la fecha/hora actual.
 */
public class SalidaOlvidadaStrategy implements FlujoAccesoStrategy {

    private final PersonaRepository personaRepository;
    private final VisitaRepository visitaRepository;
    private final VisitaEstadoRepository visitaEstadoRepository;
    private final BitacoraAuditoriaRepository auditoriaRepository;

    public SalidaOlvidadaStrategy(PersonaRepository personaRepository,
                                   VisitaRepository visitaRepository,
                                   VisitaEstadoRepository visitaEstadoRepository,
                                   BitacoraAuditoriaRepository auditoriaRepository) {
        this.personaRepository = personaRepository;
        this.visitaRepository = visitaRepository;
        this.visitaEstadoRepository = visitaEstadoRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public String getNombreFlujo() {
        return "Salida Olvidada (Regularización)";
    }

    @Override
    public ResultadoAcceso procesar(SolicitudAcceso solicitud) {
        try {
            // 1. Se requiere documento o ID de la persona
            Persona persona = buscarPersona(solicitud);
            if (persona == null) {
                return ResultadoAcceso.fallo("No se encontró la persona con los datos proporcionados.");
            }

            // 2. Buscar la visita en curso de esa persona
            Visita visitaEnCurso = null;
            var enCurso = visitaRepository.findEnCurso();
            for (Visita v : enCurso) {
                if (v.getPersonaId() == persona.getId()) {
                    visitaEnCurso = v;
                    break;
                }
            }

            if (visitaEnCurso == null) {
                return ResultadoAcceso.fallo(
                    "No se encontró visita en curso para '" + persona.getNombre()
                    + "'. La persona puede no haber registrado entrada.");
            }

            // 3. Obtener estado "Fuera"
            var estadoFuera = visitaEstadoRepository.findByNombreEstado("Fuera");
            if (estadoFuera == null) {
                return ResultadoAcceso.fallo("Error: Estado 'Fuera' no encontrado en catálogo.");
            }

            // 4. Cerrar la visita (regularizar la salida)
            LocalDateTime ahora = LocalDateTime.now();
            visitaEnCurso.setFechaSalida(ahora);
            visitaEnCurso.setEstadoVisitaId(estadoFuera.getId());
            visitaRepository.actualizar(visitaEnCurso);

            // 5. Calcular tiempo que estuvo dentro (para informativo)
            String duracion = "";
            if (visitaEnCurso.getFechaEntrada() != null) {
                long minutos = java.time.Duration.between(visitaEnCurso.getFechaEntrada(), ahora).toMinutes();
                duracion = " (estuvo " + minutos + " min dentro)";
            }

            // 6. Auditar
            int userId = solicitud.getUsuarioId();
            Integer regUserId = visitaEnCurso.getVisitaAprobadaPor();
            auditar(userId > 0 ? userId : (regUserId != null ? regUserId : 0),
                    "SALIDA_OLVIDADA_REGULARIZACION", "visitas", visitaEnCurso.getId(),
                    "Salida olvidada regularizada para: " + persona.getNombre()
                    + " | Visita ID: " + visitaEnCurso.getId()
                    + " | Salida registrada: " + ahora
                    + duracion);

            return ResultadoAcceso.exito(
                "Salida regularizada para '" + persona.getNombre()
                + "'. Visita ID: " + visitaEnCurso.getId()
                + ". Salida: " + ahora
                + duracion)
                .visitaId(visitaEnCurso.getId())
                .personaId(persona.getId());

        } catch (Exception e) {
            return ResultadoAcceso.fallo("Error al regularizar salida: " + e.getMessage());
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
