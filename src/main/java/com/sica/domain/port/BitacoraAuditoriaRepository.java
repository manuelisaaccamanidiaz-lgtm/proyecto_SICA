package com.sica.domain.port;

import com.sica.domain.BitacoraAuditoria;
import java.util.List;

/**
 * Puerto de salida: Repositorio de Bitácora de Auditoría.
 * Solo INSERT y SELECT (la auditoría no se edita ni elimina).
 */
public interface BitacoraAuditoriaRepository {

    BitacoraAuditoria guardar(BitacoraAuditoria registro);

    List<BitacoraAuditoria> findAll();

    List<BitacoraAuditoria> findByUsuarioId(int usuarioId);

    List<BitacoraAuditoria> findByFecha(String fecha);
}
