package com.sica.infrastructure.persistence;

import com.sica.domain.Rol;
import com.sica.domain.port.RolRepository;
import com.sica.infrastructure.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC para el repositorio de Roles.
 * Columnas: id, nombre_rol.
 */
public class RolRepositoryImpl implements RolRepository {

    private final DatabaseConfig dbConfig;

    public RolRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Rol guardar(Rol rol) {
        String sql = "INSERT INTO roles (nombre_rol) VALUES (?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, rol.getNombreRol());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) rol.setId(keys.getInt(1));
            }
            return rol;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar rol: " + e.getMessage(), e);
        }
    }

    @Override
    public Rol findById(int id) {
        String sql = "SELECT id, nombre_rol FROM roles WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar rol por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Rol findByNombreRol(String nombreRol) {
        String sql = "SELECT id, nombre_rol FROM roles WHERE nombre_rol = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreRol);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar rol por nombre: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Rol> findAll() {
        String sql = "SELECT id, nombre_rol FROM roles ORDER BY id";
        List<Rol> roles = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) roles.add(mapRow(rs));
            return roles;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar roles: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Rol rol) {
        String sql = "UPDATE roles SET nombre_rol = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rol.getNombreRol());
            ps.setInt(2, rol.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar rol: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM roles WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar rol: " + e.getMessage(), e);
        }
    }

    private Rol mapRow(ResultSet rs) throws SQLException {
        Rol r = new Rol();
        r.setId(rs.getInt("id"));
        r.setNombreRol(rs.getString("nombre_rol"));
        return r;
    }
}
