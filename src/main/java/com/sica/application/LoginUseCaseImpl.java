package com.sica.application;

import com.sica.domain.Usuario;
import com.sica.domain.port.BitacoraAuditoriaRepository;
import com.sica.domain.port.UsuarioRepository;
import com.sica.infrastructure.config.PasswordUtil;

import java.time.LocalDateTime;

/**
 * Implementación del caso de uso de inicio de sesión.
 * 
 * Flujo:
 * 1. Busca el usuario por username
 * 2. Verifica la contraseña con BCrypt
 * 3. Registra el intento en la bitácora de auditoría
 * 4. Retorna el usuario o lanza excepción
 */
public class LoginUseCaseImpl implements LoginUseCase {

    private final UsuarioRepository usuarioRepository;
    private final BitacoraAuditoriaRepository auditoriaRepository;

    public LoginUseCaseImpl(UsuarioRepository usuarioRepository,
                            BitacoraAuditoriaRepository auditoriaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Usuario login(String username, String plainPassword) {
        // 1. Buscar usuario
        Usuario usuario = usuarioRepository.findByUsername(username);

        if (usuario == null) {
            auditar(null, "LOGIN_FALLIDO", "Usuario no encontrado: " + username);
            throw new RuntimeException("Credenciales inválidas.");
        }

        // 2. Verificar contraseña
        if (!PasswordUtil.verify(plainPassword, usuario.getPasswordHash())) {
            auditar(usuario.getId(), "LOGIN_FALLIDO", "Contraseña incorrecta para usuario: " + username);
            throw new RuntimeException("Credenciales inválidas.");
        }

        // 3. Login exitoso
        auditar(usuario.getId(), "LOGIN_EXITOSO", "Sesión iniciada: " + username);

        return usuario;
    }

    private void auditar(Integer usuarioId, String accion, String detalle) {
        try {
            com.sica.domain.BitacoraAuditoria registro = new com.sica.domain.BitacoraAuditoria();
            registro.setUsuarioId(usuarioId != null ? usuarioId : 0);
            registro.setAccion(accion);
            registro.setDetalle(detalle);
            registro.setFechaHora(LocalDateTime.now());
            auditoriaRepository.guardar(registro);
        } catch (Exception e) {
            // La auditoría no debe bloquear el login
            System.err.println("[SICA] Warning: No se pudo registrar auditoría: " + e.getMessage());
        }
    }
}
