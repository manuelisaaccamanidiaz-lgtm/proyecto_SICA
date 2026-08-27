package com.sica.infrastructure.persistence;

import com.sica.domain.Usuario;
import com.sica.domain.port.UsuarioRepository;
import com.sica.infrastructure.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC para el repositorio de Usuarios.
 * Implementa el puerto UsuarioRepository definido en el dominio.
 */
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final DatabaseConfig dbConfig;

    public UsuarioRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (username, password_hash, rol_id) VALUES (?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setInt(3, usuario.getRolId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    usuario.setId(keys.getInt(1));
                }
            }
            return usuario;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public Usuario findById(int id) {
        String sql = "SELECT id, username, password_hash, rol_id FROM usuarios WHERE id = ?";
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
            throw new RuntimeException("Error al buscar usuario por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Usuario findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, rol_id FROM usuarios WHERE username = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por username: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Usuario> findAll() {
        String sql = "SELECT id, username, password_hash, rol_id FROM usuarios ORDER BY id";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapRow(rs));
            }
            return usuarios;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar usuarios: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET username = ?, password_hash = ?, rol_id = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setInt(3, usuario.getRolId());
            ps.setInt(4, usuario.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar usuario: " + e.getMessage(), e);
        }
    }

    // ─── Mapper privado ─────────────────────────────────────

    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRolId(rs.getInt("rol_id"));
        return u;
    }
}
