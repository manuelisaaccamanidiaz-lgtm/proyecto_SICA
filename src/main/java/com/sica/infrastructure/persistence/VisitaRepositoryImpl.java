package com.sica.infrastructure.persistence;

import com.sica.domain.Visita;
import com.sica.domain.port.VisitaRepository;
import com.sica.infrastructure.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC para el repositorio de Visitas.
 */
public class VisitaRepositoryImpl implements VisitaRepository {

    private final DatabaseConfig dbConfig;

    public VisitaRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Visita guardar(Visita visita) {
        String sql = "INSERT INTO visitas (persona_id, funcionario_id, estado, fecha_hora_entrada, fecha_hora_salida) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, visita.getPersonaId());
            ps.setInt(2, visita.getFuncionarioId());
            ps.setString(3, visita.getEstado());
            ps.setTimestamp(4, Timestamp.valueOf(visita.getFechaHoraEntrada()));
            if (visita.getFechaHoraSalida() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(visita.getFechaHoraSalida()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    visita.setId(keys.getInt(1));
                }
            }
            return visita;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar visita: " + e.getMessage(), e);
        }
    }

    @Override
    public Visita findById(int id) {
        String sql = "SELECT id, persona_id, funcionario_id, estado, fecha_hora_entrada, fecha_hora_salida "
                   + "FROM visitas WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visita por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Visita> findAll() {
        String sql = "SELECT id, persona_id, funcionario_id, estado, fecha_hora_entrada, fecha_hora_salida "
                   + "FROM visitas ORDER BY fecha_hora_entrada DESC";
        List<Visita> visitas = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                visitas.add(mapRow(rs));
            }
            return visitas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar visitas: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Visita> findByPersonaId(int personaId) {
        String sql = "SELECT id, persona_id, funcionario_id, estado, fecha_hora_entrada, fecha_hora_salida "
                   + "FROM visitas WHERE persona_id = ? ORDER BY fecha_hora_entrada DESC";
        List<Visita> visitas = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapRow(rs));
                }
            }
            return visitas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visitas por persona: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Visita> findEnCurso() {
        String sql = "SELECT id, persona_id, funcionario_id, estado, fecha_hora_entrada, fecha_hora_salida "
                   + "FROM visitas WHERE fecha_hora_salida IS NULL ORDER BY fecha_hora_entrada DESC";
        List<Visita> visitas = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                visitas.add(mapRow(rs));
            }
            return visitas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visitas en curso: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Visita> findByFechaEntrada(String fecha) {
        String sql = "SELECT id, persona_id, funcionario_id, estado, fecha_hora_entrada, fecha_hora_salida "
                   + "FROM visitas WHERE DATE(fecha_hora_entrada) = ? ORDER BY fecha_hora_entrada";
        List<Visita> visitas = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapRow(rs));
                }
            }
            return visitas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visitas por fecha: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Visita visita) {
        String sql = "UPDATE visitas SET persona_id = ?, funcionario_id = ?, estado = ?, "
                   + "fecha_hora_entrada = ?, fecha_hora_salida = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, visita.getPersonaId());
            ps.setInt(2, visita.getFuncionarioId());
            ps.setString(3, visita.getEstado());
            ps.setTimestamp(4, Timestamp.valueOf(visita.getFechaHoraEntrada()));
            if (visita.getFechaHoraSalida() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(visita.getFechaHoraSalida()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            ps.setInt(6, visita.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar visita: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM visitas WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar visita: " + e.getMessage(), e);
        }
    }

    // ─── Mapper privado ─────────────────────────────────────

    private Visita mapRow(ResultSet rs) throws SQLException {
        Visita v = new Visita();
        v.setId(rs.getInt("id"));
        v.setPersonaId(rs.getInt("persona_id"));
        v.setFuncionarioId(rs.getInt("funcionario_id"));
        v.setEstado(rs.getString("estado"));

        Timestamp entrada = rs.getTimestamp("fecha_hora_entrada");
        v.setFechaHoraEntrada(entrada != null ? entrada.toLocalDateTime() : null);

        Timestamp salida = rs.getTimestamp("fecha_hora_salida");
        v.setFechaHoraSalida(salida != null ? salida.toLocalDateTime() : null);

        return v;
    }
}
