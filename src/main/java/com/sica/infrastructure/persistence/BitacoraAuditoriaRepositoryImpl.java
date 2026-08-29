package com.sica.infrastructure.persistence;

import com.sica.domain.BitacoraAuditoria;
import com.sica.domain.port.BitacoraAuditoriaRepository;
import com.sica.infrastructure.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC para el repositorio de Bitácora de Auditoría.
 * Columnas: id (BIGINT), usuario_id, fecha_hora, accion_realizada,
 * tabla_afectada, registro_id_afectado, detalles.
 * Solo INSERT y SELECT (la auditoría no se edita ni elimina).
 */
public class BitacoraAuditoriaRepositoryImpl implements BitacoraAuditoriaRepository {

    private final DatabaseConfig dbConfig;

    public BitacoraAuditoriaRepositoryImpl() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public BitacoraAuditoria guardar(BitacoraAuditoria registro) {
        String sql = "INSERT INTO bitacora_auditoria (usuario_id, accion_realizada, tabla_afectada, "
                   + "registro_id_afectado, detalles, fecha_hora) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (registro.getUsuarioId() != null) {
                ps.setInt(1, registro.getUsuarioId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, registro.getAccionRealizada());
            ps.setString(3, registro.getTablaAfectada());
            if (registro.getRegistroIdAfectado() != null) {
                ps.setInt(4, registro.getRegistroIdAfectado());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, registro.getDetalles());
            if (registro.getFechaHora() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(registro.getFechaHora()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) registro.setId(keys.getLong(1));
            }
            return registro;
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar registro de auditoría: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BitacoraAuditoria> findAll() {
        String sql = "SELECT id, usuario_id, accion_realizada, tabla_afectada, "
                   + "registro_id_afectado, detalles, fecha_hora "
                   + "FROM bitacora_auditoria ORDER BY fecha_hora DESC";
        List<BitacoraAuditoria> registros = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) registros.add(mapRow(rs));
            return registros;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar auditoría: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BitacoraAuditoria> findByUsuarioId(int usuarioId) {
        String sql = "SELECT id, usuario_id, accion_realizada, tabla_afectada, "
                   + "registro_id_afectado, detalles, fecha_hora "
                   + "FROM bitacora_auditoria WHERE usuario_id = ? ORDER BY fecha_hora DESC";
        List<BitacoraAuditoria> registros = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) registros.add(mapRow(rs));
            }
            return registros;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar auditoría por usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BitacoraAuditoria> findByFecha(String fecha) {
        String sql = "SELECT id, usuario_id, accion_realizada, tabla_afectada, "
                   + "registro_id_afectado, detalles, fecha_hora "
                   + "FROM bitacora_auditoria WHERE DATE(fecha_hora) = ? ORDER BY fecha_hora";
        List<BitacoraAuditoria> registros = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) registros.add(mapRow(rs));
            }
            return registros;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar auditoría por fecha: " + e.getMessage(), e);
        }
    }

    private BitacoraAuditoria mapRow(ResultSet rs) throws SQLException {
        BitacoraAuditoria b = new BitacoraAuditoria();
        b.setId(rs.getLong("id"));
        int uid = rs.getInt("usuario_id");
        b.setUsuarioId(rs.wasNull() ? null : uid);
        b.setAccionRealizada(rs.getString("accion_realizada"));
        b.setTablaAfectada(rs.getString("tabla_afectada"));
        int rid = rs.getInt("registro_id_afectado");
        b.setRegistroIdAfectado(rs.wasNull() ? null : rid);
        b.setDetalles(rs.getString("detalles"));
        Timestamp ts = rs.getTimestamp("fecha_hora");
        b.setFechaHora(ts != null ? ts.toLocalDateTime() : null);
        return b;
    }
}
