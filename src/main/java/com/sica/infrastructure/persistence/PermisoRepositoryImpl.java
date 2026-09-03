package com.sica.infrastructure.persistence;

import com.sica.domain.Permiso;
import com.sica.domain.port.PermisoRepository;
import com.sica.infrastructure.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC para el repositorio de Permisos.
 * Usa la tabla intermedia rol_permisos para consultar permisos por rol.
 */
public class PermisoRepositoryImpl implements PermisoRepository {

    private final DatabaseConfig dbConfig;

    public PermisoRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public List<Permiso> findByRolId(int rolId) {
        String sql = "SELECT p.id, p.nombre_permiso, p.descripcion "
                   + "FROM permisos p "
                   + "INNER JOIN rol_permisos rp ON rp.permiso_id = p.id "
                   + "WHERE rp.rol_id = ? ORDER BY p.nombre_permiso";
        List<Permiso> permisos = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rolId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) permisos.add(mapRow(rs));
            }
            return permisos;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar permisos por rol: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean rolTienePermiso(int rolId, String nombrePermiso) {
        String sql = "SELECT COUNT(*) FROM rol_permisos rp "
                   + "INNER JOIN permisos p ON rp.permiso_id = p.id "
                   + "WHERE rp.rol_id = ? AND p.nombre_permiso = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rolId);
            ps.setString(2, nombrePermiso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar permiso: " + e.getMessage(), e);
        }
    }

    @Override
    public Permiso findByNombrePermiso(String nombrePermiso) {
        String sql = "SELECT id, nombre_permiso, descripcion FROM permisos WHERE nombre_permiso = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombrePermiso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar permiso por nombre: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Permiso> findAll() {
        String sql = "SELECT id, nombre_permiso, descripcion FROM permisos ORDER BY nombre_permiso";
        List<Permiso> permisos = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) permisos.add(mapRow(rs));
            return permisos;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar permisos: " + e.getMessage(), e);
        }
    }

    private Permiso mapRow(ResultSet rs) throws SQLException {
        Permiso p = new Permiso();
        p.setId(rs.getInt("id"));
        p.setNombrePermiso(rs.getString("nombre_permiso"));
        p.setDescripcion(rs.getString("descripcion"));
        return p;
    }
}
