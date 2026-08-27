package com.sica.infrastructure.persistence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.sica.domain.Incidente;
import com.sica.domain.port.IncidenteRepository;
import com.sica.infrastructure.config.DatabaseConfig;

/**
 * Adaptador JDBC para el repositorio de Incidentes.
 */
public class IncidenteRepositoryImpl implements IncidenteRepository {

    private final DatabaseConfig dbConfig;

    public IncidenteRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Incidente guardar(Incidente incidente) {
        String sql = "INSERT INTO incidentes (persona_id, descripcion, fecha, usuario_registro_id) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, incidente.getPersonaId());
            ps.setString(2, incidente.getDescripcion());
            ps.setDate(3, Date.valueOf(incidente.getFecha()));
            ps.setInt(4, incidente.getUsuarioRegistroId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    incidente.setId(keys.getInt(1));
                }
            }
            return incidente;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar incidente: " + e.getMessage(), e);
        }
    }

    @Override
    public Incidente findById(int id) {
        String sql = "SELECT id, persona_id, descripcion, fecha, usuario_registro_id "
                   + "FROM incidentes WHERE id = ?";
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
            throw new RuntimeException("Error al buscar incidente por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Incidente> findAll() {
        String sql = "SELECT id, persona_id, descripcion, fecha, usuario_registro_id "
                   + "FROM incidentes ORDER BY fecha DESC";
        List<Incidente> incidentes = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                incidentes.add(mapRow(rs));
            }
            return incidentes;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar incidentes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Incidente> findByPersonaId(int personaId) {
        String sql = "SELECT id, persona_id, descripcion, fecha, usuario_registro_id "
                   + "FROM incidentes WHERE persona_id = ? ORDER BY fecha DESC";
        List<Incidente> incidentes = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    incidentes.add(mapRow(rs));
                }
            }
            return incidentes;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar incidentes por persona: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Incidente> findByFecha(String fecha) {
        String sql = "SELECT id, persona_id, descripcion, fecha, usuario_registro_id "
                   + "FROM incidentes WHERE fecha = ? ORDER BY id";
        List<Incidente> incidentes = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    incidentes.add(mapRow(rs));
                }
            }
            return incidentes;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar incidentes por fecha: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Incidente incidente) {
        String sql = "UPDATE incidentes SET persona_id = ?, descripcion = ?, fecha = ?, "
                   + "usuario_registro_id = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, incidente.getPersonaId());
            ps.setString(2, incidente.getDescripcion());
            ps.setDate(3, Date.valueOf(incidente.getFecha()));
            ps.setInt(4, incidente.getUsuarioRegistroId());
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

    // ─── Mapper privado ─────────────────────────────────────

    private Incidente mapRow(ResultSet rs) throws SQLException {
        Incidente i = new Incidente();
        i.setId(rs.getInt("id"));
        i.setPersonaId(rs.getInt("persona_id"));
        i.setDescripcion(rs.getString("descripcion"));

        Date fecha = rs.getDate("fecha");
        i.setFecha(fecha != null ? fecha.toLocalDate() : null);

        i.setUsuarioRegistroId(rs.getInt("usuario_registro_id"));
        return i;
    }
}
