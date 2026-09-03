package com.sica.infrastructure.persistence;

import com.sica.domain.Visita;
import com.sica.domain.port.VisitaRepository;
import com.sica.infrastructure.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC para el repositorio de Visitas.
 * Columnas del esquema oficial: id, persona_id, fecha_entrada, fecha_salida,
 * estado_visita_id, vehiculo_placa, visita_aprobada_por.
 */
public class VisitaRepositoryImpl implements VisitaRepository {

    private final DatabaseConfig dbConfig;

    public VisitaRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Visita guardar(Visita visita) {
        String sql = "INSERT INTO visitas (persona_id, fecha_entrada, fecha_salida, "
                   + "estado_visita_id, vehiculo_placa, visita_aprobada_por) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, visita.getPersonaId());
            if (visita.getFechaEntrada() != null) {
                ps.setTimestamp(2, Timestamp.valueOf(visita.getFechaEntrada()));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            if (visita.getFechaSalida() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(visita.getFechaSalida()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.setInt(4, visita.getEstadoVisitaId());
            ps.setString(5, visita.getVehiculoPlaca());
            if (visita.getVisitaAprobadaPor() != null) {
                ps.setInt(6, visita.getVisitaAprobadaPor());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) visita.setId(keys.getInt(1));
            }
            return visita;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar visita: " + e.getMessage(), e);
        }
    }

    @Override
    public Visita findById(int id) {
        String sql = "SELECT id, persona_id, fecha_entrada, fecha_salida, "
                   + "estado_visita_id, vehiculo_placa, visita_aprobada_por "
                   + "FROM visitas WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visita por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Visita> findAll() {
        String sql = "SELECT id, persona_id, fecha_entrada, fecha_salida, "
                   + "estado_visita_id, vehiculo_placa, visita_aprobada_por "
                   + "FROM visitas ORDER BY fecha_entrada DESC";
        List<Visita> visitas = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) visitas.add(mapRow(rs));
            return visitas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar visitas: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Visita> findByPersonaId(int personaId) {
        String sql = "SELECT id, persona_id, fecha_entrada, fecha_salida, "
                   + "estado_visita_id, vehiculo_placa, visita_aprobada_por "
                   + "FROM visitas WHERE persona_id = ? ORDER BY fecha_entrada DESC";
        List<Visita> visitas = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) visitas.add(mapRow(rs));
            }
            return visitas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visitas por persona: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Visita> findEnCurso() {
        String sql = "SELECT id, persona_id, fecha_entrada, fecha_salida, "
                   + "estado_visita_id, vehiculo_placa, visita_aprobada_por "
                   + "FROM visitas WHERE fecha_salida IS NULL ORDER BY fecha_entrada DESC";
        List<Visita> visitas = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) visitas.add(mapRow(rs));
            return visitas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visitas en curso: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Visita> findByEstadoVisitaId(int estadoVisitaId) {
        String sql = "SELECT id, persona_id, fecha_entrada, fecha_salida, "
                   + "estado_visita_id, vehiculo_placa, visita_aprobada_por "
                   + "FROM visitas WHERE estado_visita_id = ? ORDER BY fecha_entrada DESC";
        List<Visita> visitas = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, estadoVisitaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) visitas.add(mapRow(rs));
            }
            return visitas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visitas por estado: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Visita> findByFechaEntrada(String fecha) {
        String sql = "SELECT id, persona_id, fecha_entrada, fecha_salida, "
                   + "estado_visita_id, vehiculo_placa, visita_aprobada_por "
                   + "FROM visitas WHERE DATE(fecha_entrada) = ? ORDER BY fecha_entrada";
        List<Visita> visitas = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) visitas.add(mapRow(rs));
            }
            return visitas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar visitas por fecha: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Visita visita) {
        String sql = "UPDATE visitas SET persona_id=?, fecha_entrada=?, fecha_salida=?, "
                   + "estado_visita_id=?, vehiculo_placa=?, visita_aprobada_por=? WHERE id=?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, visita.getPersonaId());
            if (visita.getFechaEntrada() != null) {
                ps.setTimestamp(2, Timestamp.valueOf(visita.getFechaEntrada()));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            if (visita.getFechaSalida() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(visita.getFechaSalida()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.setInt(4, visita.getEstadoVisitaId());
            ps.setString(5, visita.getVehiculoPlaca());
            if (visita.getVisitaAprobadaPor() != null) {
                ps.setInt(6, visita.getVisitaAprobadaPor());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setInt(7, visita.getId());
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

    private Visita mapRow(ResultSet rs) throws SQLException {
        Visita v = new Visita();
        v.setId(rs.getInt("id"));
        v.setPersonaId(rs.getInt("persona_id"));
        Timestamp entrada = rs.getTimestamp("fecha_entrada");
        v.setFechaEntrada(entrada != null ? entrada.toLocalDateTime() : null);
        Timestamp salida = rs.getTimestamp("fecha_salida");
        v.setFechaSalida(salida != null ? salida.toLocalDateTime() : null);
        v.setEstadoVisitaId(rs.getInt("estado_visita_id"));
        v.setVehiculoPlaca(rs.getString("vehiculo_placa"));
        int apr = rs.getInt("visita_aprobada_por");
        v.setVisitaAprobadaPor(rs.wasNull() ? null : apr);
        return v;
    }
}
