package com.sica.infrastructure.persistence;

import com.sica.domain.Usuario;
import com.sica.domain.port.UsuarioRepository;
import com.sica.infrastructure.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC para el repositorio de Usuarios.
 * Columnas: id, nombre, email, password, rol_id, esta_activo.
 */
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final DatabaseConfig dbConfig;

    public UsuarioRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, email, password, rol_id, esta_activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getPasswordHash());
            ps.setInt(4, usuario.getRolId());
            ps.setBoolean(5, usuario.isEstaActivo());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) usuario.setId(keys.getInt(1));
            }
            return usuario;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public Usuario findById(int id) {
        String sql = "SELECT id, nombre, email, password, rol_id, esta_activo FROM usuarios WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Usuario findByEmail(String email) {
        String sql = "SELECT id, nombre, email, password, rol_id, esta_activo FROM usuarios WHERE email = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por email: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Usuario> findAll() {
        String sql = "SELECT id, nombre, email, password, rol_id, esta_activo FROM usuarios ORDER BY id";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) usuarios.add(mapRow(rs));
            return usuarios;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar usuarios: " + e.getMessage(), e);
        }
    }

    @Override
    public int findIdEmpresa() {
        String sql = "SELECT em.id FROM usuarios u JOIN visitas vi ON vi.visita_aprobada_por = u.id JOIN personas ORDER BY id";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) usuarios.add(mapRow(rs));
            return usuarios;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar usuarios: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre=?, email=?, password=?, rol_id=?, esta_activo=? WHERE id=?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getPasswordHash());
            ps.setInt(4, usuario.getRolId());
            ps.setBoolean(5, usuario.isEstaActivo());
            ps.setInt(6, usuario.getId());
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

    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password"));
        u.setRolId(rs.getInt("rol_id"));
        u.setEstaActivo(rs.getBoolean("esta_activo"));
        return u;
    }
}
