package com.sica;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.sica.application.AprobacionVisitaUseCase;
import com.sica.application.AprobacionVisitaUseCaseImpl;
import com.sica.application.AuditoriaService;
import com.sica.application.AutorizacionService;
import com.sica.application.IncidenteUseCase;
import com.sica.application.IncidenteUseCaseImpl;
import com.sica.application.LoginUseCase;
import com.sica.application.LoginUseCaseImpl;
import com.sica.application.ReporteUseCase;
import com.sica.application.ReporteUseCaseImpl;
import com.sica.application.strategy.FlujoAccesoFactory;
import com.sica.application.strategy.FlujoAccesoStrategy;
import com.sica.application.strategy.InvitadoNoAnunciadoStrategy;
import com.sica.application.strategy.InvitadoPreRegistradoStrategy;
import com.sica.application.strategy.ResultadoAcceso;
import com.sica.application.strategy.SalidaOlvidadaStrategy;
import com.sica.application.strategy.SolicitudAcceso;
import com.sica.application.strategy.TrabajadorCarnetOlvidadoStrategy;
import com.sica.domain.Incidente;
import com.sica.domain.Persona;
import com.sica.domain.Usuario;
import com.sica.domain.Visita;
import com.sica.infrastructure.config.DatabaseConfig;
import com.sica.infrastructure.persistence.BitacoraAuditoriaRepositoryImpl;
import com.sica.infrastructure.persistence.IncidenteRepositoryImpl;
import com.sica.infrastructure.persistence.PermisoRepositoryImpl;
import com.sica.infrastructure.persistence.PersonaEstadoAccesoRepositoryImpl;
import com.sica.infrastructure.persistence.PersonaRepositoryImpl;
import com.sica.infrastructure.persistence.UsuarioRepositoryImpl;
import com.sica.infrastructure.persistence.VisitaEstadoRepositoryImpl;
import com.sica.infrastructure.persistence.VisitaRepositoryImpl;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static Usuario usuarioActual = null;

    private static final UsuarioRepositoryImpl usuarioRepo = new UsuarioRepositoryImpl();
    private static final PersonaRepositoryImpl personaRepo = new PersonaRepositoryImpl();
    private static final VisitaRepositoryImpl visitaRepo = new VisitaRepositoryImpl();
    private static final BitacoraAuditoriaRepositoryImpl auditoriaRepo = new BitacoraAuditoriaRepositoryImpl();
    private static final VisitaEstadoRepositoryImpl visitaEstadoRepo = new VisitaEstadoRepositoryImpl();
    private static final PersonaEstadoAccesoRepositoryImpl personaEstadoRepo = new PersonaEstadoAccesoRepositoryImpl();
    private static final PermisoRepositoryImpl permisoRepo = new PermisoRepositoryImpl();
    private static final IncidenteRepositoryImpl incidenteRepo = new IncidenteRepositoryImpl();

    private static final AuditoriaService auditoriaService = new AuditoriaService(auditoriaRepo);
    private static final AutorizacionService autorizacionService =
        new AutorizacionService(usuarioRepo, permisoRepo, auditoriaService);

    private static final LoginUseCase loginUseCase =
        new LoginUseCaseImpl(usuarioRepo, autorizacionService, auditoriaService);
    private static final IncidenteUseCase incidenteUseCase =
        new IncidenteUseCaseImpl(incidenteRepo, visitaRepo, personaRepo,
                                 personaEstadoRepo, auditoriaService, autorizacionService);
    private static final ReporteUseCase reporteUseCase =
        new ReporteUseCaseImpl(visitaRepo, personaRepo, incidenteRepo, visitaEstadoRepo);
    private static final AprobacionVisitaUseCase aprobacionVisitaUseCase =
        new AprobacionVisitaUseCaseImpl(visitaRepo, visitaEstadoRepo, personaRepo,
                                         auditoriaService, autorizacionService);

    private static final InvitadoPreRegistradoStrategy invitadoPreRegistrado =
        new InvitadoPreRegistradoStrategy(personaRepo, visitaRepo, visitaEstadoRepo,
                                          personaEstadoRepo, auditoriaService, autorizacionService);
    private static final InvitadoNoAnunciadoStrategy invitadoNoAnunciado =
        new InvitadoNoAnunciadoStrategy(personaRepo, visitaRepo, visitaEstadoRepo,
                                        personaEstadoRepo, auditoriaService, autorizacionService);
    private static final TrabajadorCarnetOlvidadoStrategy trabajadorCarnetOlvidado =
        new TrabajadorCarnetOlvidadoStrategy(personaRepo, visitaRepo, visitaEstadoRepo,
                                             personaEstadoRepo, auditoriaService, autorizacionService);
    private static final SalidaOlvidadaStrategy salidaOlvidada =
        new SalidaOlvidadaStrategy(personaRepo, visitaRepo, visitaEstadoRepo,
                                   auditoriaService, autorizacionService);

    private static final FlujoAccesoStrategy[] estrategias = {
        invitadoPreRegistrado, invitadoNoAnunciado, trabajadorCarnetOlvidado, salidaOlvidada
    };

    private static final FlujoAccesoFactory factory =
        new FlujoAccesoFactory(personaRepo, visitaRepo, visitaEstadoRepo,
                               invitadoPreRegistrado, invitadoNoAnunciado,
                               trabajadorCarnetOlvidado, salidaOlvidada);

    public static void main(String[] args) {
        System.out.println("=== SICA - Sistema de Control de Acceso ===\n");
        System.out.print("Probando conexion a BD... ");
        if (DatabaseConfig.getInstance().testConnection()) {
            System.out.println("Conectado.");
        } else {
            System.out.println("No se pudo conectar.");
            return;
        }
        if (!login()) return;
        boolean ejecutando = true;
        while (ejecutando) {
            mostrarMenuPrincipal();
            int opcion = leerEntero("Opcion: ");
            switch (opcion) {
                case 1 -> ejecutarFlujo(0);
                case 2 -> ejecutarFlujo(1);
                case 3 -> ejecutarFlujo(2);
                case 4 -> ejecutarFlujo(3);
                case 11 -> ejecutarFlujoAutomatico();
                case 5 -> login();
                case 6 -> probarAutorizacion();
                case 7 -> registrarIncidente();
                case 12 -> gestionarSolicitudesAcceso();
                case 8 -> verPersonasDentro();
                case 9 -> verIncidentesPorFecha();
                case 10 -> verVisitasPorEstado();
                case 0 -> { ejecutando = false; System.out.println("Hasta luego!"); }
                default -> System.out.println("Opcion no valida.");
            }
        }
    }

    private static boolean login() {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Contrasena: ");
        String password = scanner.nextLine().trim();
        try {
            usuarioActual = loginUseCase.login(email, password);
            System.out.println("Bienvenido, " + usuarioActual.getNombre() + " (ID: " + usuarioActual.getId() + ")");
            return true;
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private static void mostrarMenuPrincipal() {
        System.out.println("\n===========================================");
        System.out.println("  Usuario: " + usuarioActual.getNombre() + " (ID: " + usuarioActual.getId() + ")");
        System.out.println("===========================================");
        System.out.println("  --- Flujos de Acceso (Manual) ---");
        System.out.println("  1. Invitado Pre-Registrado");
        System.out.println("  2. Invitado No Anunciado");
        System.out.println("  3. Trabajador con Carnet Olvidado");
        System.out.println("  4. Salida Olvidada (Regularizacion)");
        System.out.println("  --- Deteccion Automatica ---");
        System.out.println("  11. Detectar flujo por documento");
        System.out.println("  --- Seguridad ---");
        System.out.println("  5. Cambiar usuario / Cerrar sesion");
        System.out.println("  6. Probar autorizacion (RBAC)");
        System.out.println("  7. Registrar incidente");
        System.out.println("  12. Solicitudes de Acceso (Aprobar/Rechazar)");
        System.out.println("  --- Reportes ---");
        System.out.println("  8. Personas dentro del complejo");
        System.out.println("  9. Incidentes por rango de fechas");
        System.out.println("  10. Visitas agrupadas por estado");
        System.out.println("  0. Salir");
        System.out.println("-------------------------------------------");
    }

    private static void ejecutarFlujo(int indice) {
        FlujoAccesoStrategy estrategia = estrategias[indice];
        System.out.println("\n> Flujo: " + estrategia.getNombreFlujo());
        SolicitudAcceso solicitud = new SolicitudAcceso().usuarioId(usuarioActual.getId());
        System.out.print("  Documento de identidad: ");
        String doc = scanner.nextLine().trim();
        if (!doc.isEmpty()) solicitud.documentoIdentidad(doc);
        if (indice == 1) {
            System.out.print("  Nombre del invitado: ");
            solicitud.nombreInvitado(scanner.nextLine().trim());
        }
        System.out.print("  Placa vehiculo (vacio si no aplica): ");
        String placa = scanner.nextLine().trim();
        if (!placa.isEmpty()) solicitud.vehiculoPlaca(placa);
        ResultadoAcceso resultado = estrategia.procesar(solicitud);
        System.out.println("\n" + resultado);
    }

    private static void ejecutarFlujoAutomatico() {
        System.out.println("\n> Deteccion automatica de flujo de acceso");
        System.out.print("  Documento de identidad: ");
        String doc = scanner.nextLine().trim();
        if (doc.isEmpty()) {
            System.out.println("Debe ingresar un documento.");
            return;
        }

        System.out.println("\n  Analizando documento: " + doc + "...");
        System.out.println("  " + factory.explicarDecision(doc));

        FlujoAccesoStrategy estrategia = factory.determinarFlujo(doc);
        if (estrategia == null) {
            System.out.println("\n  CASO AMBIGUO: La persona es un INVITADO sin condicion clara.");
            System.out.println("  Seleccione manualmente el flujo:");
            System.out.println("    1. Invitado Pre-Registrado");
            System.out.println("    2. Invitado No Anunciado");
            int opcion = leerEntero("  Opcion: ");
            if (opcion < 1 || opcion > 2) {
                System.out.println("Opcion invalida.");
                return;
            }
            estrategia = (opcion == 1) ? invitadoPreRegistrado : invitadoNoAnunciado;
        }

        System.out.println("\n  Flujo seleccionado: " + estrategia.getNombreFlujo());
        SolicitudAcceso solicitud = new SolicitudAcceso()
                .usuarioId(usuarioActual.getId())
                .documentoIdentidad(doc);

        if (estrategia == invitadoNoAnunciado) {
            System.out.print("  Nombre del invitado (si es nuevo): ");
            String nombre = scanner.nextLine().trim();
            if (!nombre.isEmpty()) solicitud.nombreInvitado(nombre);
        }
        System.out.print("  Placa vehiculo (vacio si no aplica): ");
        String placa = scanner.nextLine().trim();
        if (!placa.isEmpty()) solicitud.vehiculoPlaca(placa);

        ResultadoAcceso resultado = estrategia.procesar(solicitud);
        System.out.println("\n" + resultado);
    }

    private static void gestionarSolicitudesAcceso() {
        System.out.println("\n> Solicitudes de Acceso Pendientes");
        if (!autorizacionService.tienePermiso(usuarioActual.getId(), "aprobar_visita")) {
            System.out.println("ACCESO DENEGADO: No tiene permiso 'aprobar_visita'.");
            return;
        }

        List<Visita> pendientes = aprobacionVisitaUseCase.consultarSolicitudesPendientes();
        if (pendientes.isEmpty()) {
            System.out.println("  No hay solicitudes pendientes de aprobacion.");
            return;
        }

        System.out.println("  Solicitudes pendientes:");
        for (Visita v : pendientes) {
            Persona p = personaRepo.findById(v.getPersonaId());
            String nombre = p != null ? p.getNombre() : "ID:" + v.getPersonaId();
            System.out.println("  [" + v.getId() + "] " + nombre
                + " | Persona ID: " + v.getPersonaId()
                + " | Placa: " + (v.getVehiculoPlaca() != null ? v.getVehiculoPlaca() : "N/A"));
        }
        System.out.println("  Total: " + pendientes.size());

        System.out.print("\n  ID de la visita a procesar (0 para volver): ");
        int visitaId = leerEntero("");
        if (visitaId <= 0) return;

        System.out.println("  1. Aprobar");
        System.out.println("  2. Rechazar");
        int accion = leerEntero("  Opcion: ");

        try {
            if (accion == 1) {
                Visita aprobada = aprobacionVisitaUseCase.aprobarVisita(visitaId, usuarioActual.getId());
                System.out.println("  Visita ID " + aprobada.getId() + " APROBADA correctamente.");
            } else if (accion == 2) {
                Visita rechazada = aprobacionVisitaUseCase.rechazarVisita(visitaId, usuarioActual.getId());
                System.out.println("  Visita ID " + rechazada.getId() + " RECHAZADA.");
            } else {
                System.out.println("  Opcion invalida.");
            }
        } catch (RuntimeException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private static void probarAutorizacion() {
        System.out.println("\n> Prueba de autorizacion RBAC");
        System.out.print("  ID del usuario a verificar: ");
        int userId = leerEntero("");
        if (userId <= 0) { System.out.println("ID invalido."); return; }
        System.out.print("  Nombre del permiso a verificar: ");
        String permiso = scanner.nextLine().trim();
        if (permiso.isEmpty()) { System.out.println("Debe especificar un permiso."); return; }
        boolean tiene = autorizacionService.tienePermiso(userId, permiso);
        if (tiene) {
            System.out.println("El usuario " + userId + " TIENE el permiso '" + permiso + "'.");
        } else {
            System.out.println("El usuario " + userId + " NO tiene el permiso '" + permiso + "'.");
            System.out.println("   (Se registro intento fallido en bitacora de auditoria)");
        }
    }

    private static void registrarIncidente() {
        System.out.println("\n> Registrar incidente de seguridad");
        System.out.print("  ID de la visita: ");
        int visitaId = leerEntero("");
        if (visitaId <= 0) { System.out.println("ID de visita invalido."); return; }
        System.out.print("  Descripcion del incidente: ");
        String descripcion = scanner.nextLine().trim();
        if (descripcion.isEmpty()) { System.out.println("La descripcion es obligatoria."); return; }
        try {
            Incidente incidente = incidenteUseCase.registrarIncidente(visitaId, usuarioActual.getId(), descripcion);
            System.out.println("Incidente registrado. ID: " + incidente.getId());
            System.out.println("   La persona afectada ha sido bloqueada (Con Prohibicion de Ingreso).");
        } catch (RuntimeException e) { System.out.println(e.getMessage()); }
    }

    private static void verPersonasDentro() {
        System.out.println("\n> Personas actualmente dentro del complejo:");
        List<Persona> dentro = reporteUseCase.personasDentroDelComplejo();
        if (dentro.isEmpty()) { System.out.println("  (No hay personas dentro)"); return; }
        for (Persona p : dentro) {
            System.out.println("  * " + p.getNombre() + " | Doc: " + p.getDocumentoIdentidad() + " | Tipo: " + p.getTipoPersona());
        }
        System.out.println("  Total: " + dentro.size());
    }

    private static void verIncidentesPorFecha() {
        System.out.println("\n> Incidentes por rango de fechas");
        System.out.print("  Fecha inicio (YYYY-MM-DD): ");
        String inicio = scanner.nextLine().trim();
        System.out.print("  Fecha fin (YYYY-MM-DD): ");
        String fin = scanner.nextLine().trim();
        try {
            List<Incidente> incidentes = reporteUseCase.incidentesPorRangoFechas(inicio, fin);
            if (incidentes.isEmpty()) { System.out.println("  (No se encontraron incidentes)"); return; }
            for (Incidente i : incidentes) {
                System.out.println("  * ID: " + i.getId() + " | Fecha: " + i.getFecha() + " | Visita: " + i.getVisitaId() + " | Desc: " + i.getDescripcion());
            }
            System.out.println("  Total: " + incidentes.size());
        } catch (Exception e) { System.out.println("Formato de fecha invalido: " + e.getMessage()); }
    }

    private static void verVisitasPorEstado() {
        System.out.println("\n> Visitas agrupadas por estado:");
        Map<String, List<Visita>> agrupadas = reporteUseCase.visitasAgrupadasPorEstado();
        if (agrupadas.isEmpty()) { System.out.println("  (No hay visitas registradas)"); return; }
        for (Map.Entry<String, List<Visita>> entry : agrupadas.entrySet()) {
            System.out.println("  [" + entry.getKey() + "] - " + entry.getValue().size() + " visitas:");
            for (Visita v : entry.getValue()) {
                System.out.println("    * ID: " + v.getId() + " | Persona: " + v.getPersonaId() + " | Entrada: " + v.getFechaEntrada());
            }
        }
    }

    private static int leerEntero(String prompt) {
        System.out.print(prompt);
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}
