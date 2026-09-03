package com.sica.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sica.domain.VisitaEstado;
import com.sica.domain.port.VisitaEstadoRepository;
import com.sica.infrastructure.config.DatabaseConfig;

/**
 * Adaptador JDBC para el catálogo de estados de visita.
 */
public class VisitaEstadoRepositoryImpl implements VisitaEstadoRepository {

    private final DatabaseConfig dbConfig;

    public VisitaEstadoRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public VisitaEstado findById(int id) {
        String sql = "SELECT id, nombre_estado FROM visita_estados WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar estado de visita: " + e.getMessage(), e);
        }
    }

    @Override
    public VisitaEstado findByNombreEstado(String nombreEstado) {
        String sql = "SELECT id, nombre_estado FROM visita_estados WHERE nombre_estado = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreEstado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar estado de visita por nombre: " + e.getMessage(), e);
        }
    }

    @Override
    public List<VisitaEstado> findAll() {
        String sql = "SELECT id, nombre_estado FROM visita_estados ORDER BY id";
        List<VisitaEstado> estados = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                estados.add(mapRow(rs));
            }
            return estados;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar estados de visita: " + e.getMessage(), e);
        }
    }

    private VisitaEstado mapRow(ResultSet rs) throws SQLException {
        return new VisitaEstado(rs.getInt("id"), rs.getString("nombre_estado"));
    }
}
