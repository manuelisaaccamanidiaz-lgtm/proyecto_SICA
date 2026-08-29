package com.sica.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sica.domain.PersonaEstadoAcceso;
import com.sica.domain.port.PersonaEstadoAccesoRepository;
import com.sica.infrastructure.config.DatabaseConfig;

/**
 * Adaptador JDBC para el catálogo de estados de acceso de persona.
 */
public class PersonaEstadoAccesoRepositoryImpl implements PersonaEstadoAccesoRepository {

    private final DatabaseConfig dbConfig;

    public PersonaEstadoAccesoRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public PersonaEstadoAcceso findById(int id) {
        String sql = "SELECT id, nombre_estado FROM persona_estados_acceso WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar estado de acceso: " + e.getMessage(), e);
        }
    }

    @Override
    public PersonaEstadoAcceso findByNombreEstado(String nombreEstado) {
        String sql = "SELECT id, nombre_estado FROM persona_estados_acceso WHERE nombre_estado = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreEstado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar estado de acceso por nombre: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PersonaEstadoAcceso> findAll() {
        String sql = "SELECT id, nombre_estado FROM persona_estados_acceso ORDER BY id";
        List<PersonaEstadoAcceso> estados = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                estados.add(mapRow(rs));
            }
            return estados;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar estados de acceso: " + e.getMessage(), e);
        }
    }

    private PersonaEstadoAcceso mapRow(ResultSet rs) throws SQLException {
        return new PersonaEstadoAcceso(rs.getInt("id"), rs.getString("nombre_estado"));
    }
}
