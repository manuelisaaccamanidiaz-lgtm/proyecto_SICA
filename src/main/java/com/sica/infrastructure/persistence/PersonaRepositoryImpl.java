package com.sica.infrastructure.persistence;

import com.sica.domain.Persona;
import com.sica.domain.TipoPersona;
import com.sica.domain.port.PersonaRepository;
import com.sica.infrastructure.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC para el repositorio de Personas.
 * Columnas: id, nombre, documento_identidad, empresa_id, tipo_persona, estado_acceso_id, url_foto.
 */
public class PersonaRepositoryImpl implements PersonaRepository {

    private final DatabaseConfig dbConfig;

    public PersonaRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Persona guardar(Persona persona) {
        String sql = "INSERT INTO personas (nombre, documento_identidad, empresa_id, tipo_persona, estado_acceso_id, url_foto) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, persona.getNombre());
            ps.setString(2, persona.getDocumentoIdentidad());
            if (persona.getEmpresaId() != null) {
                ps.setInt(3, persona.getEmpresaId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, persona.getTipoPersona().getValor());
            if (persona.getEstadoAccesoId() != null) {
                ps.setInt(5, persona.getEstadoAccesoId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, persona.getUrlFoto());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) persona.setId(keys.getInt(1));
            }
            return persona;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar persona: " + e.getMessage(), e);
        }
    }

    @Override
    public Persona findById(int id) {
        String sql = "SELECT id, nombre, documento_identidad, empresa_id, tipo_persona, estado_acceso_id, url_foto "
                   + "FROM personas WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar persona por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Persona findByDocumentoIdentidad(String documentoIdentidad) {
        String sql = "SELECT id, nombre, documento_identidad, empresa_id, tipo_persona, estado_acceso_id, url_foto "
                   + "FROM personas WHERE documento_identidad = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, documentoIdentidad);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar persona por documento: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Persona> findAll() {
        String sql = "SELECT id, nombre, documento_identidad, empresa_id, tipo_persona, estado_acceso_id, url_foto "
                   + "FROM personas ORDER BY id";
        List<Persona> personas = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) personas.add(mapRow(rs));
            return personas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar personas: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Persona> findByEmpresaId(int empresaId) {
        String sql = "SELECT id, nombre, documento_identidad, empresa_id, tipo_persona, estado_acceso_id, url_foto "
                   + "FROM personas WHERE empresa_id = ? ORDER BY nombre";
        List<Persona> personas = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empresaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) personas.add(mapRow(rs));
            }
            return personas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar personas por empresa: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Persona persona) {
        String sql = "UPDATE personas SET nombre=?, documento_identidad=?, empresa_id=?, "
                   + "tipo_persona=?, estado_acceso_id=?, url_foto=? WHERE id=?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, persona.getNombre());
            ps.setString(2, persona.getDocumentoIdentidad());
            if (persona.getEmpresaId() != null) {
                ps.setInt(3, persona.getEmpresaId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, persona.getTipoPersona().getValor());
            if (persona.getEstadoAccesoId() != null) {
                ps.setInt(5, persona.getEstadoAccesoId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, persona.getUrlFoto());
            ps.setInt(7, persona.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar persona: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM personas WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar persona: " + e.getMessage(), e);
        }
    }

    private Persona mapRow(ResultSet rs) throws SQLException {
        Persona p = new Persona();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDocumentoIdentidad(rs.getString("documento_identidad"));
        int empresaId = rs.getInt("empresa_id");
        p.setEmpresaId(rs.wasNull() ? null : empresaId);
        p.setTipoPersona(TipoPersona.fromValor(rs.getString("tipo_persona")));
        int estadoId = rs.getInt("estado_acceso_id");
        p.setEstadoAccesoId(rs.wasNull() ? null : estadoId);
        p.setUrlFoto(rs.getString("url_foto"));
        return p;
    }
}
