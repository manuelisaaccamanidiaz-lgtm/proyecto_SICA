package com.sica.application;

import com.sica.domain.Usuario;
import com.sica.domain.port.PermisoRepository;
import com.sica.domain.port.UsuarioRepository;

/**
 * Servicio de autorizacion RBAC (Role-Based Access Control).
 *
 * Verifica si un usuario tiene un permiso especifico consultando
 * los permisos asignados a su rol a traves de la tabla rol_permisos.
 *
 * Registra en bitacora de auditoria cada intento de autorizacion fallido
 * usando {@link AuditoriaService}.
 */
public class AutorizacionService {

    private final UsuarioRepository usuarioRepository;
    private final PermisoRepository permisoRepository;
    private final AuditoriaService auditoriaService;

    public AutorizacionService(UsuarioRepository usuarioRepository,
                                PermisoRepository permisoRepository,
                                AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.permisoRepository = permisoRepository;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Verifica si un usuario tiene el permiso especificado.
     *
     * Flujo:
     * 1. Busca el usuario por ID para obtener su rol_id
     * 2. Consulta si ese rol tiene el permiso dado (via rol_permisos)
     * 3. Si no tiene permiso, registra el intento fallido en auditoria
     *
     * @param usuarioId      ID del usuario a verificar
     * @param nombrePermiso  nombre del permiso requerido (ej. "registrar_visita")
     * @return true si el usuario tiene el permiso
     */
    public boolean tienePermiso(int usuarioId, String nombrePermiso) {
        Usuario usuario = usuarioRepository.findById(usuarioId);
        if (usuario == null) {
            auditoriaService.registrar(usuarioId, "AUTORIZACION_FALLIDA",
                    "usuarios", usuarioId,
                    "Usuario no encontrado: ID " + usuarioId
                    + " | Permiso requerido: " + nombrePermiso);
            return false;
        }

        boolean tienePermiso = permisoRepository.rolTienePermiso(usuario.getRolId(), nombrePermiso);

        if (!tienePermiso) {
            auditoriaService.registrar(usuarioId, "AUTORIZACION_FALLIDA",
                    "usuarios", usuarioId,
                    "Usuario '" + usuario.getNombre() + "' (rol_id=" + usuario.getRolId()
                    + ") no tiene permiso: " + nombrePermiso);
        }

        return tienePermiso;
    }
}
