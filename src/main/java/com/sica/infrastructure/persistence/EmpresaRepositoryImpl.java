package com.sica.infrastructure.persistence;

import com.sica.domain.Empresa;
import com.sica.domain.port.EmpresaRepository;
import com.sica.infrastructure.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC para el repositorio de Empresas.
 */
public class EmpresaRepositoryImpl implements EmpresaRepository {

    private final DatabaseConfig dbConfig;

    public EmpresaRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Empresa guardar(Empresa empresa) {
        String sql = "INSERT INTO empresas (nombre) VALUES (?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, empresa.getNombre());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    empresa.setId(keys.getInt(1));
                }
            }
            return empresa;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar empresa: " + e.getMessage(), e);
        }
    }

    @Override
    public Empresa findById(int id) {
        String sql = "SELECT id, nombre FROM empresas WHERE id = ?";
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
            throw new RuntimeException("Error al buscar empresa por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Empresa> findAll() {
        String sql = "SELECT id, nombre FROM empresas ORDER BY nombre";
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empresas.add(mapRow(rs));
            }
            return empresas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar empresas: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Empresa empresa) {
        String sql = "UPDATE empresas SET nombre = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empresa.getNombre());
            ps.setInt(2, empresa.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar empresa: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM empresas WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar empresa: " + e.getMessage(), e);
        }
    }

    // ─── Mapper privado ─────────────────────────────────────

    private Empresa mapRow(ResultSet rs) throws SQLException {
        Empresa e = new Empresa();
        e.setId(rs.getInt("id"));
        e.setNombre(rs.getString("nombre"));
        return e;
    }
}
