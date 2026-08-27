package com.sica.application;

import com.sica.domain.BitacoraAuditoria;
import com.sica.domain.Persona;
import com.sica.domain.Visita;
import com.sica.domain.port.BitacoraAuditoriaRepository;
import com.sica.domain.port.PersonaRepository;
import com.sica.domain.port.VisitaRepository;

import java.time.LocalDateTime;

/**
 * Implementación del caso de uso: Registrar una visita.
 * 
 * Flujo de registrarIngreso:
 * 1. Valida que la persona exista en el sistema
 * 2. Verifica que la persona no tenga ya una visita en curso
 * 3. Crea la visita con estado "EN_CURSO"
 * 4. Registra la acción en la bitácora de auditoría
 * 
 * Flujo de registrarSalida:
 * 1. Valida que la visita exista y esté en curso
 * 2. Registra la fecha/hora de salida
 * 3. Actualiza el estado a "FINALIZADA"
 * 4. Registra la acción en la bitácora de auditoría
 */
public class RegistrarVisitaUseCaseImpl implements RegistrarVisitaUseCase {

    private final PersonaRepository personaRepository;
    private final VisitaRepository visitaRepository;
    private final BitacoraAuditoriaRepository auditoriaRepository;

    public RegistrarVisitaUseCaseImpl(PersonaRepository personaRepository,
                                       VisitaRepository visitaRepository,
                                       BitacoraAuditoriaRepository auditoriaRepository) {
        this.personaRepository = personaRepository;
        this.visitaRepository = visitaRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Visita registrarIngreso(int personaId, int funcionarioId) {
        // 1. Validar que la persona exista
        Persona persona = personaRepository.findById(personaId);
        if (persona == null) {
            throw new IllegalArgumentException("La persona con ID " + personaId + " no existe.");
        }

        // 2. Verificar que no tenga visita en curso
        var visitasEnCurso = visitaRepository.findByPersonaId(personaId);
        for (Visita v : visitasEnCurso) {
            if (v.getFechaHoraSalida() == null) {
                throw new IllegalStateException(
                    "La persona " + persona.getNombre() + " ya tiene una visita en curso (ID: " + v.getId() + ").");
            }
        }

        // 3. Crear la visita
        Visita visita = new Visita();
        visita.setPersonaId(personaId);
        visita.setFuncionarioId(funcionarioId);
        visita.setEstado("EN_CURSO");
        visita.setFechaHoraEntrada(LocalDateTime.now());
        visita.setFechaHoraSalida(null);

        Visita visitaGuardada = visitaRepository.guardar(visita);

        // 4. Registrar en auditoría
        auditar(funcionarioId, "VISITA_INGRESO",
                "Visita ID: " + visitaGuardada.getId()
                + " | Persona: " + persona.getNombre() + " (" + persona.getDocumento() + ")");

        return visitaGuardada;
    }

    @Override
    public void registrarSalida(int visitaId) {
        // 1. Validar que la visita exista
        Visita visita = visitaRepository.findById(visitaId);
        if (visita == null) {
            throw new IllegalArgumentException("La visita con ID " + visitaId + " no existe.");
        }

        // 2. Verificar que esté en curso
        if (visita.getFechaHoraSalida() != null) {
            throw new IllegalStateException(
                "La visita ID " + visitaId + " ya fue finalizada el " + visita.getFechaHoraSalida() + ".");
        }

        // 3. Registrar salida
        visita.setFechaHoraSalida(LocalDateTime.now());
        visita.setEstado("FINALIZADA");
        visitaRepository.actualizar(visita);

        // 4. Registrar en auditoría
        auditar(visita.getFuncionarioId(), "VISITA_SALIDA",
                "Visita ID: " + visitaId + " finalizada.");
    }

    private void auditar(int usuarioId, String accion, String detalle) {
        try {
            BitacoraAuditoria registro = new BitacoraAuditoria();
            registro.setUsuarioId(usuarioId);
            registro.setAccion(accion);
            registro.setDetalle(detalle);
            registro.setFechaHora(LocalDateTime.now());
            auditoriaRepository.guardar(registro);
        } catch (Exception e) {
            System.err.println("[SICA] Warning: No se pudo registrar auditoría: " + e.getMessage());
        }
    }
}
