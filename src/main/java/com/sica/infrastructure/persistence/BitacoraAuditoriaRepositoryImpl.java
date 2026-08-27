package com.sica.infrastructure.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.sica.domain.BitacoraAuditoria;
import com.sica.domain.port.BitacoraAuditoriaRepository;
import com.sica.infrastructure.config.DatabaseConfig;

/**
 * Adaptador JDBC para el repositorio de Bitácora de Auditoría.
 * Solo permite INSERT y SELECT (no se edita ni elimina auditoría).
 */
public class BitacoraAuditoriaRepositoryImpl implements BitacoraAuditoriaRepository {

    private final DatabaseConfig dbConfig;

    public BitacoraAuditoriaRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public BitacoraAuditoria guardar(BitacoraAuditoria registro) {
        String sql = "INSERT INTO bitacora_auditoria (usuario_id, accion, detalle, fecha_hora) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, registro.getUsuarioId());
            ps.setString(2, registro.getAccion());
            ps.setString(3, registro.getDetalle());
            ps.setTimestamp(4, Timestamp.valueOf(registro.getFechaHora()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    registro.setId(keys.getInt(1));
                }
            }
            return registro;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar registro de auditoría: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BitacoraAuditoria> findAll() {
        String sql = "SELECT id, usuario_id, accion, detalle, fecha_hora "
                   + "FROM bitacora_auditoria ORDER BY fecha_hora DESC";
        List<BitacoraAuditoria> registros = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                registros.add(mapRow(rs));
            }
            return registros;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar auditoría: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BitacoraAuditoria> findByUsuarioId(int usuarioId) {
        String sql = "SELECT id, usuario_id, accion, detalle, fecha_hora "
                   + "FROM bitacora_auditoria WHERE usuario_id = ? ORDER BY fecha_hora DESC";
        List<BitacoraAuditoria> registros = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registros.add(mapRow(rs));
                }
            }
            return registros;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar auditoría por usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BitacoraAuditoria> findByFecha(String fecha) {
        String sql = "SELECT id, usuario_id, accion, detalle, fecha_hora "
                   + "FROM bitacora_auditoria WHERE DATE(fecha_hora) = ? ORDER BY fecha_hora";
        List<BitacoraAuditoria> registros = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registros.add(mapRow(rs));
                }
            }
            return registros;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar auditoría por fecha: " + e.getMessage(), e);
        }
    }

    // ─── Mapper privado ─────────────────────────────────────

    private BitacoraAuditoria mapRow(ResultSet rs) throws SQLException {
        BitacoraAuditoria b = new BitacoraAuditoria();
        b.setId(rs.getInt("id"));
        b.setUsuarioId(rs.getInt("usuario_id"));
        b.setAccion(rs.getString("accion"));
        b.setDetalle(rs.getString("detalle"));

        Timestamp ts = rs.getTimestamp("fecha_hora");
        b.setFechaHora(ts != null ? ts.toLocalDateTime() : null);

        return b;
    }
}
