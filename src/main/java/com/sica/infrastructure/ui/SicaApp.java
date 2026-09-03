package com.sica.infrastructure.ui;

import com.sica.application.*;
import com.sica.application.strategy.*;
import com.sica.domain.Usuario;
import com.sica.infrastructure.persistence.*;

/**
 * Contenedor de dependencias y punto de entrada para la interfaz grafica.
 * Sigue la misma arquitectura hexagonal que Main.java (consola).
 */
public class SicaApp {

    // Repositorios
    private final UsuarioRepositoryImpl usuarioRepo = new UsuarioRepositoryImpl();
    private final PersonaRepositoryImpl personaRepo = new PersonaRepositoryImpl();
    private final VisitaRepositoryImpl visitaRepo = new VisitaRepositoryImpl();
    private final BitacoraAuditoriaRepositoryImpl auditoriaRepo = new BitacoraAuditoriaRepositoryImpl();
    private final VisitaEstadoRepositoryImpl visitaEstadoRepo = new VisitaEstadoRepositoryImpl();
    private final PersonaEstadoAccesoRepositoryImpl personaEstadoRepo = new PersonaEstadoAccesoRepositoryImpl();
    private final PermisoRepositoryImpl permisoRepo = new PermisoRepositoryImpl();
    private final IncidenteRepositoryImpl incidenteRepo = new IncidenteRepositoryImpl();

    // Servicios
    private final AuditoriaService auditoriaService = new AuditoriaService(auditoriaRepo);
    private final AutorizacionService autorizacionService =
        new AutorizacionService(usuarioRepo, permisoRepo, auditoriaService);

    // Casos de uso
    private final LoginUseCase loginUseCase =
        new LoginUseCaseImpl(usuarioRepo, autorizacionService, auditoriaService);
    private final IncidenteUseCase incidenteUseCase =
        new IncidenteUseCaseImpl(incidenteRepo, visitaRepo, personaRepo,
                                 personaEstadoRepo, auditoriaService, autorizacionService);
    private final ReporteUseCase reporteUseCase =
        new ReporteUseCaseImpl(visitaRepo, personaRepo, incidenteRepo, visitaEstadoRepo);
    private final AprobacionVisitaUseCase aprobacionVisitaUseCase =
        new AprobacionVisitaUseCaseImpl(visitaRepo, visitaEstadoRepo, personaRepo,
                                         auditoriaService, autorizacionService);

    // Estrategias
    private final InvitadoPreRegistradoStrategy invitadoPreRegistrado =
        new InvitadoPreRegistradoStrategy(personaRepo, visitaRepo, visitaEstadoRepo,
                                          personaEstadoRepo, auditoriaService, autorizacionService);
    private final InvitadoNoAnunciadoStrategy invitadoNoAnunciado =
        new InvitadoNoAnunciadoStrategy(personaRepo, visitaRepo, visitaEstadoRepo,
                                        personaEstadoRepo, auditoriaService, autorizacionService);
    private final TrabajadorCarnetOlvidadoStrategy trabajadorCarnetOlvidado =
        new TrabajadorCarnetOlvidadoStrategy(personaRepo, visitaRepo, visitaEstadoRepo,
                                             personaEstadoRepo, auditoriaService, autorizacionService);
    private final SalidaOlvidadaStrategy salidaOlvidada =
        new SalidaOlvidadaStrategy(personaRepo, visitaRepo, visitaEstadoRepo,
                                   auditoriaService, autorizacionService);

    private final FlujoAccesoStrategy[] estrategias = {
        invitadoPreRegistrado, invitadoNoAnunciado, trabajadorCarnetOlvidado, salidaOlvidada
    };

    private final FlujoAccesoFactory factory =
        new FlujoAccesoFactory(personaRepo, visitaRepo, visitaEstadoRepo,
                               invitadoPreRegistrado, invitadoNoAnunciado,
                               trabajadorCarnetOlvidado, salidaOlvidada);

    /** Abre la ventana de login. */
    public void iniciar() {
        LoginFrame loginFrame = new LoginFrame(loginUseCase, this);
        loginFrame.setVisible(true);
    }

    /** Abre el menu principal para el usuario autenticado. */
    public void abrirMenuPrincipal(Usuario usuario) {
        MenuPrincipalFrame menu = new MenuPrincipalFrame(
                usuario, auditoriaService, autorizacionService,
                incidenteUseCase, reporteUseCase, aprobacionVisitaUseCase,
                factory, estrategias, this, personaRepo);
        menu.setVisible(true);
    }
}
