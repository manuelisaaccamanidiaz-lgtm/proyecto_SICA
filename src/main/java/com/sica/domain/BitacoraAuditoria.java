package com.sica.domain;

import java.time.LocalDateTime;

/**
 * Entidad de dominio: Bitácora de auditoría.
 * Corresponde a la tabla `bitacora_auditoria` del esquema oficial.
 * El id es BIGINT en la BD.
 */
public class BitacoraAuditoria {

    private long id;
    private Integer usuarioId;
    private LocalDateTime fechaHora;
    private String accionRealizada;
    private String tablaAfectada;
    private Integer registroIdAfectado;
    private String detalles;

    public BitacoraAuditoria() {}

    // Getters y setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getAccionRealizada() { return accionRealizada; }
    public void setAccionRealizada(String accionRealizada) { this.accionRealizada = accionRealizada; }

    public String getTablaAfectada() { return tablaAfectada; }
    public void setTablaAfectada(String tablaAfectada) { this.tablaAfectada = tablaAfectada; }

    public Integer getRegistroIdAfectado() { return registroIdAfectado; }
    public void setRegistroIdAfectado(Integer registroIdAfectado) { this.registroIdAfectado = registroIdAfectado; }

    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
}
