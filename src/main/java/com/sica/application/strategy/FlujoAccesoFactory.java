package com.sica.application.strategy;

import com.sica.domain.Persona;
import com.sica.domain.TipoPersona;
import com.sica.domain.Visita;
import com.sica.domain.VisitaEstado;
import com.sica.domain.port.PersonaRepository;
import com.sica.domain.port.VisitaEstadoRepository;
import com.sica.domain.port.VisitaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Factory que determina automaticamente que {@link FlujoAccesoStrategy} aplicar
 * a partir del documento de identidad de una persona.
 *
 * Logica de decision (en orden de prioridad):
 * <ol>
 *   <li>Si la persona NO existe por documento &rarr; {@code InvitadoNoAnunciado}</li>
 *   <li>Si existe y tiene una visita con estado "Dentro" &rarr; {@code SalidaOlvidada}</li>
 *   <li>Si existe y tiene una visita "Aprobado" (pendiente de ingreso) &rarr; {@code InvitadoPreRegistrado}</li>
 *   <li>Si existe, es tipo TRABAJADOR y no cae en los casos anteriores &rarr; {@code TrabajadorCarnetOlvidado}</li>
 *   <li>Si existe, es tipo INVITADO y no cae en los casos anteriores &rarr; {@code null} (caso ambiguo)</li>
 * </ol>
 *
 * La Factory no reemplaza las estrategias existentes, solo las selecciona.
 */
public class FlujoAccesoFactory {

    private final PersonaRepository personaRepository;
    private final VisitaRepository visitaRepository;
    private final VisitaEstadoRepository visitaEstadoRepository;

    private final InvitadoPreRegistradoStrategy invitadoPreRegistrado;
    private final InvitadoNoAnunciadoStrategy invitadoNoAnunciado;
    private final TrabajadorCarnetOlvidadoStrategy trabajadorCarnetOlvidado;
    private final SalidaOlvidadaStrategy salidaOlvidada;

    /**
     * Construye la factory con todos los repositorios y las 4 estrategias concretas.
     */
    public FlujoAccesoFactory(PersonaRepository personaRepository,
                               VisitaRepository visitaRepository,
                               VisitaEstadoRepository visitaEstadoRepository,
                               InvitadoPreRegistradoStrategy invitadoPreRegistrado,
                               InvitadoNoAnunciadoStrategy invitadoNoAnunciado,
                               TrabajadorCarnetOlvidadoStrategy trabajadorCarnetOlvidado,
                               SalidaOlvidadaStrategy salidaOlvidada) {
        this.personaRepository = personaRepository;
        this.visitaRepository = visitaRepository;
        this.visitaEstadoRepository = visitaEstadoRepository;
        this.invitadoPreRegistrado = invitadoPreRegistrado;
        this.invitadoNoAnunciado = invitadoNoAnunciado;
        this.trabajadorCarnetOlvidado = trabajadorCarnetOlvidado;
        this.salidaOlvidada = salidaOlvidada;
    }

    /**
     * Determina el flujo de acceso mas apropiado segun el documento de identidad.
     *
     * @param documentoIdentidad documento de la persona a evaluar
     * @return la estrategia sugerida, o {@code null} si el caso es ambiguo
     *         (persona INVITADO sin condicion clara)
     */
    public FlujoAccesoStrategy determinarFlujo(String documentoIdentidad) {
        if (documentoIdentidad == null || documentoIdentidad.isBlank()) {
            return null;
        }

        // 1. Buscar la persona por documento
        Persona persona = personaRepository.findByDocumentoIdentidad(documentoIdentidad.trim());

        // 1a. No existe -> InvitadoNoAnunciado
        if (persona == null) {
            return invitadoNoAnunciado;
        }

        // 2. Verificar si tiene visita en curso con estado "Dentro" -> SalidaOlvidada
        VisitaEstado estadoDentro = visitaEstadoRepository.findByNombreEstado("Dentro");
        if (estadoDentro != null) {
            List<Visita> visitas = visitaRepository.findByPersonaId(persona.getId());
            boolean tieneDentro = visitas.stream()
                    .anyMatch(v -> v.getEstadoVisitaId() == estadoDentro.getId());
            if (tieneDentro) {
                return salidaOlvidada;
            }
        }

        // 3. Verificar si tiene una visita "Aprobado" (pendiente de ingreso) -> InvitadoPreRegistrado
        VisitaEstado estadoAprobado = visitaEstadoRepository.findByNombreEstado("Aprobado");
        if (estadoAprobado != null) {
            List<Visita> visitas = visitaRepository.findByPersonaId(persona.getId());
            boolean tieneAprobado = visitas.stream()
                    .anyMatch(v -> v.getEstadoVisitaId() == estadoAprobado.getId());
            if (tieneAprobado) {
                return invitadoPreRegistrado;
            }
        }

        // 4. Es TRABAJADOR y no cae en casos anteriores -> TrabajadorCarnetOlvidado
        if (persona.getTipoPersona() == TipoPersona.TRABAJADOR) {
            return trabajadorCarnetOlvidado;
        }

        // 5. Es INVITADO y no cae en ningun caso -> caso ambiguo
        return null;
    }

    /**
     * Obtiene un mensaje descriptivo del motivo por el que se sugiere un flujo.
     *
     * @param documentoIdentidad documento evaluado
     * @return descripcion legible del razonamiento de la factory
     */
    public String explicarDecision(String documentoIdentidad) {
        if (documentoIdentidad == null || documentoIdentidad.isBlank()) {
            return "No se proporciono documento de identidad.";
        }

        Persona persona = personaRepository.findByDocumentoIdentidad(documentoIdentidad.trim());

        if (persona == null) {
            return "Persona NO encontrada con documento '" + documentoIdentidad
                    + "' -> Invitado No Anunciado (se creara al vuelo).";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Persona encontrada: '").append(persona.getNombre()).append("'")
          .append(" (").append(persona.getTipoPersona()).append("). ");

        // Verificar estado Dentro
        VisitaEstado estadoDentro = visitaEstadoRepository.findByNombreEstado("Dentro");
        if (estadoDentro != null) {
            List<Visita> visitas = visitaRepository.findByPersonaId(persona.getId());
            boolean tieneDentro = visitas.stream()
                    .anyMatch(v -> v.getEstadoVisitaId() == estadoDentro.getId());
            if (tieneDentro) {
                sb.append("Tiene visita EN CURSO (estado 'Dentro') -> Salida Olvidada.");
                return sb.toString();
            }
        }

        // Verificar estado Aprobado (pendiente de ingreso)
        VisitaEstado estadoAprobado = visitaEstadoRepository.findByNombreEstado("Aprobado");
        if (estadoAprobado != null) {
            List<Visita> visitas = visitaRepository.findByPersonaId(persona.getId());
            boolean tieneAprobado = visitas.stream()
                    .anyMatch(v -> v.getEstadoVisitaId() == estadoAprobado.getId());
            if (tieneAprobado) {
                sb.append("Tiene visita APROBADA (pendiente de ingreso) -> Invitado Pre-Registrado.");
                return sb.toString();
            }
        }

        if (persona.getTipoPersona() == TipoPersona.TRABAJADOR) {
            sb.append("Es TRABAJADOR sin visita en curso ni aprobada -> Trabajador con Carnet Olvidado.");
            return sb.toString();
        }

        sb.append("Es INVITADO sin condicion clara -> Caso ambiguo (seleccion manual requerida).");
        return sb.toString();
    }
}
