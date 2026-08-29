package com.sica;

import java.util.Scanner;

import com.sica.application.LoginUseCase;
import com.sica.application.LoginUseCaseImpl;
import com.sica.application.strategy.FlujoAccesoStrategy;
import com.sica.application.strategy.InvitadoNoAnunciadoStrategy;
import com.sica.application.strategy.InvitadoPreRegistradoStrategy;
import com.sica.application.strategy.ResultadoAcceso;
import com.sica.application.strategy.SalidaOlvidadaStrategy;
import com.sica.application.strategy.SolicitudAcceso;
import com.sica.application.strategy.TrabajadorCarnetOlvidadoStrategy;
import com.sica.domain.Usuario;
import com.sica.infrastructure.config.DatabaseConfig;
import com.sica.infrastructure.persistence.BitacoraAuditoriaRepositoryImpl;
import com.sica.infrastructure.persistence.PersonaEstadoAccesoRepositoryImpl;
import com.sica.infrastructure.persistence.PersonaRepositoryImpl;
import com.sica.infrastructure.persistence.UsuarioRepositoryImpl;
import com.sica.infrastructure.persistence.VisitaEstadoRepositoryImpl;
import com.sica.infrastructure.persistence.VisitaRepositoryImpl;

/**
 * Main: menú de consola para probar login + 4 flujos de acceso.
 * Wiring manual de dependencias (sin framework DI).
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static Usuario usuarioActual = null;

    // Repositorios (singletons via DatabaseConfig)
    private static final UsuarioRepositoryImpl usuarioRepo = new UsuarioRepositoryImpl();
    private static final PersonaRepositoryImpl personaRepo = new PersonaRepositoryImpl();
    private static final VisitaRepositoryImpl visitaRepo = new VisitaRepositoryImpl();
    private static final BitacoraAuditoriaRepositoryImpl auditoriaRepo = new BitacoraAuditoriaRepositoryImpl();
    private static final VisitaEstadoRepositoryImpl visitaEstadoRepo = new VisitaEstadoRepositoryImpl();
    private static final PersonaEstadoAccesoRepositoryImpl personaEstadoRepo = new PersonaEstadoAccesoRepositoryImpl();

    // Casos de uso
    private static final LoginUseCase loginUseCase = new LoginUseCaseImpl(usuarioRepo, auditoriaRepo);

    // Estrategias de acceso
    private static final FlujoAccesoStrategy invitadoPreRegistrado =
        new InvitadoPreRegistradoStrategy(personaRepo, visitaRepo, visitaEstadoRepo, personaEstadoRepo, auditoriaRepo);
    private static final FlujoAccesoStrategy invitadoNoAnunciado =
        new InvitadoNoAnunciadoStrategy(personaRepo, visitaRepo, visitaEstadoRepo, personaEstadoRepo, auditoriaRepo);
    private static final FlujoAccesoStrategy trabajadorCarnetOlvidado =
        new TrabajadorCarnetOlvidadoStrategy(personaRepo, visitaRepo, visitaEstadoRepo, personaEstadoRepo, auditoriaRepo);
    private static final FlujoAccesoStrategy salidaOlvidada =
        new SalidaOlvidadaStrategy(personaRepo, visitaRepo, visitaEstadoRepo, auditoriaRepo);

    private static final FlujoAccesoStrategy[] estrategias = {
        invitadoPreRegistrado, invitadoNoAnunciado, trabajadorCarnetOlvidado, salidaOlvidada
    };

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════╗");
        System.out.println("║        SICA - Sistema de Control de Acceso    ║");
        System.out.println("║        Arquitectura Hexagonal + Strategy      ║");
        System.out.println("╚═══════════════════════════════════════════════╝");
        System.out.println();

        // Verificar conexión a BD
        System.out.print("Probando conexión a BD... ");
        if (DatabaseConfig.getInstance().testConnection()) {
            System.out.println("✅ Conectado.");
        } else {
            System.out.println("❌ No se pudo conectar. Verifique jdbc.properties.");
            return;
        }
        System.out.println();

        // Login
        if (!login()) return;

        // Menú principal
        boolean ejecutando = true;
        while (ejecutando) {
            mostrarMenuPrincipal();
            int opcion = leerEntero("Opción: ");

            switch (opcion) {
                case 1 -> ejecutarFlujo(0);
                // Invitado Pre-Registrado
                case 2 -> ejecutarFlujo(1);
                // Invitado No Anunciado
                case 3 -> ejecutarFlujo(2);
                // Trabajador Carnet Olvidado
                case 4 -> ejecutarFlujo(3);
                // Salida Olvidada
                case 5 -> login();
                // Cambiar de usuario
                case 0 -> {
                    ejecutando = false;
                    System.out.println("\n👋 Hasta luego, " + usuarioActual.getNombre() + "!");
                }
                default -> System.out.println("⚠️  Opción no válida.");
            }
        }
    }

    private static boolean login() {
        System.out.print("📧 Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("🔑 Contraseña: ");
        String password = scanner.nextLine().trim();

        try {
            usuarioActual = loginUseCase.login(email, password);
            System.out.println("✅ Bienvenido, " + usuarioActual.getNombre()
                + " (ID: " + usuarioActual.getId() + ")");
            return true;
        } catch (RuntimeException e) {
            System.out.println("❌ " + e.getMessage());
            return false;
        }
    }

    private static void mostrarMenuPrincipal() {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Usuario: " + usuarioActual.getNombre()
            + " (ID: " + usuarioActual.getId() + ")");
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  1. Invitado Pre-Registrado");
        System.out.println("  2. Invitado No Anunciado");
        System.out.println("  3. Trabajador con Carnet Olvidado");
        System.out.println("  4. Salida Olvidada (Regularización)");
        System.out.println("  5. Cambiar de usuario");
        System.out.println("  0. Salir");
        System.out.println("───────────────────────────────────────────────");
    }

    private static void ejecutarFlujo(int indice) {
        FlujoAccesoStrategy estrategia = estrategias[indice];
        System.out.println("\n▶ Flujo: " + estrategia.getNombreFlujo());

        SolicitudAcceso solicitud = new SolicitudAcceso()
            .usuarioId(usuarioActual.getId());

        System.out.print("  Documento de identidad: ");
        String doc = scanner.nextLine().trim();
        if (!doc.isEmpty()) {
            solicitud.documentoIdentidad(doc);
        }

        System.out.print("  ID de persona (0 si no sabe): ");
        int personaId = leerEntero("");
        if (personaId > 0) {
            solicitud.personaId(personaId);
        }

        if (indice == 1) { // Invitado no anunciado
            System.out.print("  Nombre del invitado: ");
            solicitud.nombreInvitado(scanner.nextLine().trim());
        }

        System.out.print("  Placa vehículo (vacío si no aplica): ");
        String placa = scanner.nextLine().trim();
        if (!placa.isEmpty()) {
            solicitud.vehiculoPlaca(placa);
        }

        if (indice == 3) { // Salida olvidada — no necesita placa
            System.out.println("  (Se buscará la visita en curso de esta persona)");
        }

        // Procesar
        ResultadoAcceso resultado = estrategia.procesar(solicitud);
        System.out.println();
        System.out.println(resultado);
    }

    private static int leerEntero(String prompt) {
        System.out.print(prompt);
        try {
            String linea = scanner.nextLine().trim();
            return Integer.parseInt(linea);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
