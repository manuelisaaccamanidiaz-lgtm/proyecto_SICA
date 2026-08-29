package com.sica.application;

import com.sica.domain.BitacoraAuditoria;
import com.sica.domain.Persona;
import com.sica.domain.Visita;
import com.sica.domain.port.BitacoraAuditoriaRepository;
import com.sica.domain.port.PersonaRepository;
import com.sica.domain.port.VisitaRepository;
import com.sica.domain.port.VisitaEstadoRepository;

import java.time.LocalDateTime;

/**
 * Implementación del caso de uso: Registrar una visita.
 *
 * Ahora usa el catálogo de visita_estados (FK) en lugar de texto libre.
 *
 * Flujo de registrarIngreso:
 * 1. Valida que la persona exista en el sistema
 * 2. Verifica que la persona no tenga ya una visita en curso (estado = "Dentro")
 * 3. Crea la visita con estado "Dentro"
 * 4. Registra la acción en la bitácora de auditoría
 *
 * Flujo de registrarSalida:
 * 1. Valida que la visita exista y esté "Dentro"
 * 2. Registra la fecha/hora de salida
 * 3. Actualiza el estado a "Fuera"
 * 4. Registra la acción en la bitácora de auditoría
 */
public class RegistrarVisitaUseCaseImpl implements RegistrarVisitaUseCase {

    private final PersonaRepository personaRepository;
    private final VisitaRepository visitaRepository;
    private final VisitaEstadoRepository visitaEstadoRepository;
    private final BitacoraAuditoriaRepository auditoriaRepository;

    public RegistrarVisitaUseCaseImpl(PersonaRepository personaRepository,
                                       VisitaRepository visitaRepository,
                                       VisitaEstadoRepository visitaEstadoRepository,
                                       BitacoraAuditoriaRepository auditoriaRepository) {
        this.personaRepository = personaRepository;
        this.visitaRepository = visitaRepository;
        this.visitaEstadoRepository = visitaEstadoRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Visita registrarIngreso(int personaId, int usuarioId, String vehiculoPlaca) {
        // 1. Validar que la persona exista
        Persona persona = personaRepository.findById(personaId);
        if (persona == null) {
            throw new IllegalArgumentException("La persona con ID " + personaId + " no existe.");
        }

        // 2. Verificar que no tenga visita en curso
        var visitasEnCurso = visitaRepository.findEnCurso();
        for (Visita v : visitasEnCurso) {
            if (v.getPersonaId() == personaId) {
                throw new IllegalStateException(
                    "La persona " + persona.getNombre() + " ya tiene una visita en curso (ID: " + v.getId() + ").");
            }
        }

        // 3. Obtener estado "Dentro" del catálogo
        var estadoDentro = visitaEstadoRepository.findByNombreEstado("Dentro");
        if (estadoDentro == null) {
            throw new IllegalStateException("El estado 'Dentro' no existe en el catálogo visita_estados.");
        }

        // 4. Crear la visita
        Visita visita = new Visita();
        visita.setPersonaId(personaId);
        visita.setFechaEntrada(LocalDateTime.now());
        visita.setFechaSalida(null);
        visita.setEstadoVisitaId(estadoDentro.getId());
        visita.setVehiculoPlaca(vehiculoPlaca);
        visita.setVisitaAprobadaPor(usuarioId);

        Visita visitaGuardada = visitaRepository.guardar(visita);

        // 5. Registrar en auditoría
        auditar(usuarioId, "VISITA_INGRESO", "visitas", visitaGuardada.getId(),
                "Visita ID: " + visitaGuardada.getId()
                + " | Persona: " + persona.getNombre()
                + " (" + persona.getDocumentoIdentidad() + ")");

        return visitaGuardada;
    }

    @Override
    public Visita registrarIngresoInvitadoPreRegistrado(int personaId, int usuarioId, String vehiculoPlaca) {
        // 1. Validar que la persona exista y sea invitado
        Persona persona = personaRepository.findById(personaId);
        if (persona == null) {
            throw new IllegalArgumentException("La persona con ID " + personaId + " no existe.");
        }
        if (persona.getTipoPersona() != com.sica.domain.TipoPersona.INVITADO) {
            throw new IllegalArgumentException(
                "La persona " + persona.getNombre() + " no es un invitado. Use registrarIngreso para trabajadores.");
        }

        // 2. Verificar que no tenga visita en curso
        var visitasEnCurso = visitaRepository.findEnCurso();
        for (Visita v : visitasEnCurso) {
            if (v.getPersonaId() == personaId) {
                throw new IllegalStateException(
                    "La persona " + persona.getNombre() + " ya tiene una visita en curso (ID: " + v.getId() + ").");
            }
        }

        // 3. Obtener estado "Dentro"
        var estadoDentro = visitaEstadoRepository.findByNombreEstado("Dentro");
        if (estadoDentro == null) {
            throw new IllegalStateException("El estado 'Dentro' no existe en el catálogo visita_estados.");
        }

        // 4. Crear la visita
        Visita visita = new Visita();
        visita.setPersonaId(personaId);
        visita.setFechaEntrada(LocalDateTime.now());
        visita.setFechaSalida(null);
        visita.setEstadoVisitaId(estadoDentro.getId());
        visita.setVehiculoPlaca(vehiculoPlaca);
        visita.setVisitaAprobadaPor(usuarioId);

        Visita visitaGuardada = visitaRepository.guardar(visita);

        // 5. Registrar en auditoría
        auditar(usuarioId, "VISITA_INGRESO_INVITADO", "visitas", visitaGuardada.getId(),
                "Invitado pre-registrado ID: " + personaId
                + " | Nombre: " + persona.getNombre()
                + " | Doc: " + persona.getDocumentoIdentidad());

        return visitaGuardada;
    }

    @Override
    public void registrarSalida(int visitaId) {
        // 1. Validar que la visita exista
        Visita visita = visitaRepository.findById(visitaId);
        if (visita == null) {
            throw new IllegalArgumentException("La visita con ID " + visitaId + " no existe.");
        }

        // 2. Verificar que esté "Dentro" (no tenga salida)
        if (visita.getFechaSalida() != null) {
            throw new IllegalStateException(
                "La visita ID " + visitaId + " ya fue finalizada el " + visita.getFechaSalida() + ".");
        }

        // 3. Obtener estado "Fuera"
        var estadoFuera = visitaEstadoRepository.findByNombreEstado("Fuera");
        if (estadoFuera == null) {
            throw new IllegalStateException("El estado 'Fuera' no existe en el catálogo visita_estados.");
        }

        // 4. Registrar salida
        visita.setFechaSalida(LocalDateTime.now());
        visita.setEstadoVisitaId(estadoFuera.getId());
        visitaRepository.actualizar(visita);

        // 5. Registrar en auditoría
        Integer userId = visita.getVisitaAprobadaPor();
        auditar(userId != null ? userId : 0, "VISITA_SALIDA", "visitas", visitaId,
                "Visita ID: " + visitaId + " finalizada.");
    }

    private void auditar(int usuarioId, String accion, String tabla, int registroId, String detalles) {
        try {
            BitacoraAuditoria registro = new BitacoraAuditoria();
            registro.setUsuarioId(usuarioId);
            registro.setAccionRealizada(accion);
            registro.setTablaAfectada(tabla);
            registro.setRegistroIdAfectado(registroId);
            registro.setDetalles(detalles);
            registro.setFechaHora(LocalDateTime.now());
            auditoriaRepository.guardar(registro);
        } catch (Exception e) {
            System.err.println("[SICA] Warning: No se pudo registrar auditoría: " + e.getMessage());
        }
    }
}
