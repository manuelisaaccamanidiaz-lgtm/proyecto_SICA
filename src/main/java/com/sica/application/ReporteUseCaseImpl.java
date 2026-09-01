package com.sica.application;

import com.sica.domain.Incidente;
import com.sica.domain.Persona;
import com.sica.domain.Visita;
import com.sica.domain.VisitaEstado;
import com.sica.domain.port.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementacion del caso de uso: Generacion de reportes.
 *
 * Todas las consultas traen la lista completa del repositorio y la procesan
 * con Stream API (filter, map, collect) como requisito del disenio.
 * Esto permite flexibilidad de filtrado en memoria sin depender del motor SQL.
 */
public class ReporteUseCaseImpl implements ReporteUseCase {

    private final VisitaRepository visitaRepository;
    private final PersonaRepository personaRepository;
    private final IncidenteRepository incidenteRepository;
    private final VisitaEstadoRepository visitaEstadoRepository;

    public ReporteUseCaseImpl(VisitaRepository visitaRepository,
                               PersonaRepository personaRepository,
                               IncidenteRepository incidenteRepository,
                               VisitaEstadoRepository visitaEstadoRepository) {
        this.visitaRepository = visitaRepository;
        this.personaRepository = personaRepository;
        this.incidenteRepository = incidenteRepository;
        this.visitaEstadoRepository = visitaEstadoRepository;
    }

    @Override
    public List<Persona> personasDentroDelComplejo() {
        // 1. Trae todas las visitas en curso
        List<Visita> enCurso = visitaRepository.findEnCurso();

        // 2. Extrae los IDs unicos de personas, una sola vez
        Set<Integer> personaIds = enCurso.stream()
                .map(Visita::getPersonaId)
                .collect(Collectors.toSet());

        // 3. Trae todas las personas necesarias en una sola consulta (batch)
        List<Persona> todasLasPersonas = personaRepository.findAll();
        Map<Integer, Persona> personasPorId = todasLasPersonas.stream()
                .collect(Collectors.toMap(Persona::getId, p -> p));

        // 4. Stream solo para combinar/filtrar en memoria (sin queries)
        return enCurso.stream()
                .map(Visita::getPersonaId)
                .distinct()
                .map(personasPorId::get)
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Incidente> incidentesPorRangoFechas(String fechaInicio, String fechaFin) {
        // Trae todos los incidentes y filtra por rango de fechas en memoria.
        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = LocalDate.parse(fechaFin);

        List<Incidente> todos = incidenteRepository.findAll();

        return todos.stream()
                .filter(i -> i.getFecha() != null)
                .filter(i -> {
                    LocalDate fechaIncidente = i.getFecha().toLocalDate();
                    return !fechaIncidente.isBefore(inicio) && !fechaIncidente.isAfter(fin);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<Visita>> visitasAgrupadasPorEstado() {
        // 1. Trae todas las visitas
        List<Visita> todas = visitaRepository.findAll();

        // 2. Batch load de estados del catalogo (evita N+1)
        List<VisitaEstado> todosLosEstados = visitaEstadoRepository.findAll();
        Map<Integer, String> estadosPorId = todosLosEstados.stream()
                .collect(Collectors.toMap(VisitaEstado::getId, e -> e.getNombreEstado()));

        // 3. Stream para agrupar en memoria
        return todas.stream()
                .collect(Collectors.groupingBy(v ->
                    estadosPorId.getOrDefault(v.getEstadoVisitaId(), "Desconocido")
                ));
    }
}
