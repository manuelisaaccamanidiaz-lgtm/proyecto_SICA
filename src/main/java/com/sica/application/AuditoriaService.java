package com.sica.application;

import com.sica.domain.BitacoraAuditoria;
import com.sica.domain.port.BitacoraAuditoriaRepository;

import java.time.LocalDateTime;

/**
 * Servicio centralizado de auditoria.
 *
 * Envuelve {@link BitacoraAuditoriaRepository} y ofrece un metodo simple
 * para registrar acciones en la bitacora. Reemplaza los metodos privados
 * {@code auditar()} que estaban duplicados en cada clase de application.
 *
 * Los errores de auditoria nunca bloquean la operacion principal.
 */
public class AuditoriaService {

    private final BitacoraAuditoriaRepository auditoriaRepository;

    public AuditoriaService(BitacoraAuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * Registra una accion en la bitacora de auditoria.
     *
     * @param usuarioId   ID del usuario que realiza la accion (puede ser null en casos anonymous)
     * @param accion      nombre de la accion realizada (ej. "LOGIN_EXITOSO")
     * @param tabla       tabla afectada (ej. "visitas")
     * @param registroId  ID del registro afectado (0 si no aplica)
     * @param detalle     descripcion detallada de la accion
     */
    public void registrar(Integer usuarioId, String accion, String tabla,
                          int registroId, String detalle) {
        try {
            BitacoraAuditoria registro = new BitacoraAuditoria();
            registro.setUsuarioId(usuarioId);
            registro.setAccionRealizada(accion);
            registro.setTablaAfectada(tabla);
            registro.setRegistroIdAfectado(registroId);
            registro.setDetalles(detalle);
            registro.setFechaHora(LocalDateTime.now());
            auditoriaRepository.guardar(registro);
        } catch (Exception e) {
            System.err.println("[SICA] Warning: No se pudo registrar auditoria: " + e.getMessage());
        }
    }
}
