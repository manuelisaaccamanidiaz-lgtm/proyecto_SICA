package com.sica.application;

import com.sica.domain.BitacoraAuditoria;
import com.sica.domain.Usuario;
import com.sica.domain.port.BitacoraAuditoriaRepository;
import com.sica.domain.port.UsuarioRepository;
import com.sica.infrastructure.config.PasswordUtil;

import java.time.LocalDateTime;

/**
 * Implementación del caso de uso de inicio de sesión.
 *
 * Flujo:
 * 1. Busca el usuario por email
 * 2. Verifica que esté activo
 * 3. Verifica la contraseña con BCrypt
 * 4. Registra el intento en la bitácora de auditoría
 * 5. Retorna el usuario o lanza excepción
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
    public Usuario login(String email, String plainPassword) {
        // 1. Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null) {
            auditar(null, "LOGIN_FALLIDO", "Usuario no encontrado: " + email,
                    "usuarios", null);
            throw new RuntimeException("Credenciales inválidas.");
        }

        // 2. Verificar que esté activo
        if (!usuario.isEstaActivo()) {
            auditar(usuario.getId(), "LOGIN_FALLIDO",
                    "Usuario desactivado: " + email, "usuarios", usuario.getId());
            throw new RuntimeException("Credenciales inválidas.");
        }

        // 3. Verificar contraseña
        if (!PasswordUtil.verify(plainPassword, usuario.getPasswordHash())) {
            auditar(usuario.getId(), "LOGIN_FALLIDO",
                    "Contraseña incorrecta para: " + email, "usuarios", usuario.getId());
            throw new RuntimeException("Credenciales inválidas.");
        }

        // 4. Login exitoso
        auditar(usuario.getId(), "LOGIN_EXITOSO",
                "Sesión iniciada: " + email, "usuarios", usuario.getId());

        return usuario;
    }

    private void auditar(Integer usuarioId, String accion, String detalles,
                         String tabla, Integer registroId) {
        try {
            BitacoraAuditoria registro = new BitacoraAuditoria();
            registro.setUsuarioId(usuarioId);
            registro.setAccionRealizada(accion);
            registro.setTablaAfectada(tabla);
            registro.setRegistroIdAfectado(registroId);
            registro.setDetalles(detalles);
            registro.setFechaHora(LocalDateTime.now());
            auditoriaRepository.guardar(registro);
        } catch (Exception e) {
            // La auditoría no debe bloquear el login
            System.err.println("[SICA] Warning: No se pudo registrar auditoría: " + e.getMessage());
        }
    }
}
