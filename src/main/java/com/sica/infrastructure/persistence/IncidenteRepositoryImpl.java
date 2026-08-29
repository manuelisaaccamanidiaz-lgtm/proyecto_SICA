package com.sica.infrastructure.persistence;

import com.sica.domain.Incidente;
import com.sica.domain.port.IncidenteRepository;
import com.sica.infrastructure.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC para el repositorio de Incidentes.
 * Columnas: id, visita_id, reportado_por_id, fecha (DATETIME), descripcion.
 */
public class IncidenteRepositoryImpl implements IncidenteRepository {

    private final DatabaseConfig dbConfig;

    public IncidenteRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Incidente guardar(Incidente incidente) {
        String sql = "INSERT INTO incidentes (visita_id, reportado_por_id, fecha, descripcion) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, incidente.getVisitaId());
            ps.setInt(2, incidente.getReportadoPorId());
            ps.setTimestamp(3, Timestamp.valueOf(incidente.getFecha()));
            ps.setString(4, incidente.getDescripcion());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) incidente.setId(keys.getInt(1));
            }
            return incidente;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar incidente: " + e.getMessage(), e);
        }
    }

    @Override
    public Incidente findById(int id) {
        String sql = "SELECT id, visita_id, reportado_por_id, fecha, descripcion "
                   + "FROM incidentes WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar incidente por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Incidente> findAll() {
        String sql = "SELECT id, visita_id, reportado_por_id, fecha, descripcion "
                   + "FROM incidentes ORDER BY fecha DESC";
        List<Incidente> incidentes = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) incidentes.add(mapRow(rs));
            return incidentes;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar incidentes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Incidente> findByVisitaId(int visitaId) {
        String sql = "SELECT id, visita_id, reportado_por_id, fecha, descripcion "
                   + "FROM incidentes WHERE visita_id = ? ORDER BY fecha DESC";
        List<Incidente> incidentes = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, visitaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) incidentes.add(mapRow(rs));
            }
            return incidentes;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar incidentes por visita: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Incidente> findByFecha(String fecha) {
        String sql = "SELECT id, visita_id, reportado_por_id, fecha, descripcion "
                   + "FROM incidentes WHERE DATE(fecha) = ? ORDER BY id";
        List<Incidente> incidentes = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) incidentes.add(mapRow(rs));
            }
            return incidentes;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar incidentes por fecha: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Incidente incidente) {
        String sql = "UPDATE incidentes SET visita_id=?, reportado_por_id=?, "
                   + "fecha=?, descripcion=? WHERE id=?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, incidente.getVisitaId());
            ps.setInt(2, incidente.getReportadoPorId());
            ps.setTimestamp(3, Timestamp.valueOf(incidente.getFecha()));
            ps.setString(4, incidente.getDescripcion());
            ps.setInt(5, incidente.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar incidente: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM incidentes WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar incidente: " + e.getMessage(), e);
        }
    }

    private Incidente mapRow(ResultSet rs) throws SQLException {
        Incidente i = new Incidente();
        i.setId(rs.getInt("id"));
        i.setVisitaId(rs.getInt("visita_id"));
        i.setReportadoPorId(rs.getInt("reportado_por_id"));
        Timestamp ts = rs.getTimestamp("fecha");
        i.setFecha(ts != null ? ts.toLocalDateTime() : null);
        i.setDescripcion(rs.getString("descripcion"));
        return i;
    }
}
