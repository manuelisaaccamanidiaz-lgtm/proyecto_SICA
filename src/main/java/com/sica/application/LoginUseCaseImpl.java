package com.sica.application;

import com.sica.domain.Usuario;
import com.sica.domain.exception.CredencialesInvalidasException;
import com.sica.domain.exception.PermisoDenegadoException;
import com.sica.domain.port.UsuarioRepository;
import com.sica.infrastructure.config.PasswordUtil;

/**
 * Implementacion del caso de uso de inicio de sesion.
 *
 * Flujo:
 * 1. Busca el usuario por email
 * 2. Verifica que este activo
 * 3. Verifica la contrasena con BCrypt
 * 4. Verifica permiso RBAC "acceder_sistema"
 * 5. Registra el intento en la bitacora de auditoria via AuditoriaService
 * 6. Retorna el usuario o lanza excepcion
 */
public class LoginUseCaseImpl implements LoginUseCase {

    private final UsuarioRepository usuarioRepository;
    private final AutorizacionService autorizacionService;
    private final AuditoriaService auditoriaService;

    public LoginUseCaseImpl(UsuarioRepository usuarioRepository,
                            AutorizacionService autorizacionService,
                            AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.autorizacionService = autorizacionService;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public Usuario login(String email, String plainPassword) {
        // 1. Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null) {
            auditoriaService.registrar(null, "LOGIN_FALLIDO", "usuarios", 0,
                    "Usuario no encontrado: " + email);
            throw new CredencialesInvalidasException("Credenciales invalidas.");
        }

        // 2. Verificar que este activo
        if (!usuario.isEstaActivo()) {
            auditoriaService.registrar(usuario.getId(), "LOGIN_FALLIDO",
                    "usuarios", usuario.getId(),
                    "Usuario desactivado: " + email);
            throw new CredencialesInvalidasException("Credenciales invalidas.");
        }

        // 3. Verificar contrasena
        if (!PasswordUtil.verify(plainPassword, usuario.getPasswordHash())) {
            auditoriaService.registrar(usuario.getId(), "LOGIN_FALLIDO",
                    "usuarios", usuario.getId(),
                    "Contrasena incorrecta para: " + email);
            throw new CredencialesInvalidasException("Credenciales invalidas.");
        }

        // 4. Verificar permiso RBAC "acceder_sistema"
        if (!autorizacionService.tienePermiso(usuario.getId(), "acceder_sistema")) {
            auditoriaService.registrar(usuario.getId(), "LOGIN_FALLIDO",
                    "usuarios", usuario.getId(),
                    "Sin permiso 'acceder_sistema': " + email);
            throw new PermisoDenegadoException(
                "No tiene permiso para acceder al sistema.",
                usuario.getId(), "acceder_sistema");
        }

        // 5. Login exitoso
        auditoriaService.registrar(usuario.getId(), "LOGIN_EXITOSO",
                "usuarios", usuario.getId(),
                "Sesion iniciada: " + email);

        return usuario;
    }
}
