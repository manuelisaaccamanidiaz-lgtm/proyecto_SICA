package com.sica.infrastructure.ui;

import com.sica.application.*;
import com.sica.application.strategy.*;
import com.sica.domain.*;
import com.sica.domain.port.PersonaRepository;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MenuPrincipalFrame extends JFrame {
    private final Usuario usuarioActual;
    private final AuditoriaService auditoriaService;
    private final AutorizacionService autorizacionService;
    private final IncidenteUseCase incidenteUseCase;
    private final ReporteUseCase reporteUseCase;
    private final AprobacionVisitaUseCase aprobacionVisitaUseCase;
    private final FlujoAccesoFactory factory;
    private final FlujoAccesoStrategy[] estrategias;
    private final PersonaRepository personaRepository;
    private final SicaApp sicaApp;

    public MenuPrincipalFrame(Usuario usuario, AuditoriaService auditoriaService,
            AutorizacionService autorizacionService, IncidenteUseCase incidenteUseCase,
            ReporteUseCase reporteUseCase, AprobacionVisitaUseCase aprobacionVisitaUseCase,
            FlujoAccesoFactory factory, FlujoAccesoStrategy[] estrategias,
            SicaApp sicaApp,
            PersonaRepository personaRepository) {
        this.usuarioActual = usuario;
        this.auditoriaService = auditoriaService;
        this.autorizacionService = autorizacionService;
        this.incidenteUseCase = incidenteUseCase;
        this.reporteUseCase = reporteUseCase;
        this.aprobacionVisitaUseCase = aprobacionVisitaUseCase;
        this.factory = factory;
        this.estrategias = estrategias;
        this.personaRepository = personaRepository;
        this.sicaApp = sicaApp;
        setTitle("SICA - Menu Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel header = new JLabel("Usuario: " + usuario.getNombre() + " (ID: " + usuario.getId() + ")");
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        mainPanel.add(header, BorderLayout.NORTH);
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        addSectionLabel(buttonsPanel, "Flujos de Acceso");
        addButton(buttonsPanel, "Registrar Acceso (Auto-detect)", e -> registrarAccesoAutomatico());
        addButton(buttonsPanel, "Invitado Pre-Registrado", e -> ejecutarFlujo(0));
        addButton(buttonsPanel, "Invitado No Anunciado", e -> ejecutarFlujo(1));
        addButton(buttonsPanel, "Trabajador Carnet Olvidado", e -> ejecutarFlujo(2));
        addButton(buttonsPanel, "Salida Olvidada", e -> ejecutarFlujo(3));
        addSectionLabel(buttonsPanel, "Seguridad");
        addButton(buttonsPanel, "Registrar Incidente", e -> registrarIncidente());
        addButton(buttonsPanel, "Probar Autorizacion (RBAC)", e -> probarAutorizacion());
        addSectionLabel(buttonsPanel, "Aprobacion de Accesos");
        addButton(buttonsPanel, "Solicitudes de Acceso (Aprobar/Rechazar)", e -> gestionarSolicitudesAcceso());
        addSectionLabel(buttonsPanel, "Reportes");
        addButton(buttonsPanel, "Personas Dentro del Complejo", e -> verPersonasDentro());
        addButton(buttonsPanel, "Incidentes por Rango de Fechas", e -> verIncidentesPorFecha());
        addButton(buttonsPanel, "Visitas por Estado", e -> verVisitasPorEstado());
        JScrollPane scrollPane = new JScrollPane(buttonsPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        JButton logoutBtn = new JButton("Cerrar Sesion");
        logoutBtn.addActionListener(e -> { dispose(); sicaApp.iniciar(); });
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.add(logoutBtn);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }
    private void addSectionLabel(JPanel panel, String text) {
        JLabel label = new JLabel("  " + text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(new Color(80, 80, 80));
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
    }
    private void addButton(JPanel panel, String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.addActionListener(action);
        panel.add(btn);
        panel.add(Box.createVerticalStrut(3));
    }
    private void registrarAccesoAutomatico() {
        String doc = JOptionPane.showInputDialog(this, "Documento de identidad:", "Registrar Acceso (Auto)", JOptionPane.QUESTION_MESSAGE);
        if (doc == null || doc.trim().isEmpty()) return;
        FlujoAccesoStrategy estrategia = factory.determinarFlujo(doc.trim());
        String explicacion = factory.explicarDecision(doc.trim());
        if (estrategia == null) {
            String[] opciones = {"Invitado Pre-Registrado", "Invitado No Anunciado"};
            int eleccion = JOptionPane.showOptionDialog(this, explicacion + "\n\nCaso ambiguo: seleccione manualmente.", "Seleccion Manual", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, opciones, opciones[0]);
            if (eleccion < 0) return;
            estrategia = (eleccion == 0) ? estrategias[0] : estrategias[1];
        } else {
            JOptionPane.showMessageDialog(this, explicacion, "Flujo Detectado", JOptionPane.INFORMATION_MESSAGE);
        }
        ejecutarEstrategia(estrategia, doc.trim());
    }
    private void ejecutarFlujo(int indice) {
        FlujoAccesoStrategy estrategia = estrategias[indice];
        String doc = JOptionPane.showInputDialog(this, "Documento de identidad:", estrategia.getNombreFlujo(), JOptionPane.QUESTION_MESSAGE);
        ejecutarEstrategia(estrategia, doc);
    }
    private void ejecutarEstrategia(FlujoAccesoStrategy estrategia, String doc) {
        if (doc == null) return;
        doc = doc.trim();
        SolicitudAcceso solicitud = new SolicitudAcceso().usuarioId(usuarioActual.getId()).documentoIdentidad(doc.isEmpty() ? null : doc);
        if (estrategia == estrategias[1]) {
            String nombre = JOptionPane.showInputDialog(this, "Nombre del invitado:", "Datos del Invitado", JOptionPane.QUESTION_MESSAGE);
            if (nombre == null || nombre.trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Se requiere el nombre.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            solicitud.nombreInvitado(nombre.trim());
        }
        String placa = JOptionPane.showInputDialog(this, "Placa vehicular (vacio si no aplica):", "Vehiculo", JOptionPane.QUESTION_MESSAGE);
        if (placa != null && !placa.trim().isEmpty()) solicitud.vehiculoPlaca(placa.trim());
        ResultadoAcceso resultado = estrategia.procesar(solicitud);
        int tipo = resultado.isExitoso() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
        JOptionPane.showMessageDialog(this, resultado.toString(), estrategia.getNombreFlujo(), tipo);
    }
    private void gestionarSolicitudesAcceso() {
        if (!autorizacionService.tienePermiso(usuarioActual.getId(), "aprobar_visita")) {
            JOptionPane.showMessageDialog(this, "ACCESO DENEGADO: No tiene permiso 'aprobar_visita'.", "Denegado", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Visita> pendientes = aprobacionVisitaUseCase.consultarSolicitudesPendientes();
        if (pendientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay solicitudes pendientes de aprobacion.", "Solicitudes", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder("Solicitudes pendientes:\n\n");
        String[] opcionesVisitas = new String[pendientes.size()];
        for (int i = 0; i < pendientes.size(); i++) {
            Visita v = pendientes.get(i);
            Persona p = personaRepository.findById(v.getPersonaId());
            String nombre = p != null ? p.getNombre() : "ID:" + v.getPersonaId();
            String placa = v.getVehiculoPlaca() != null ? v.getVehiculoPlaca() : "N/A";
            String item = "[" + v.getId() + "] " + nombre + " | Placa: " + placa;
            opcionesVisitas[i] = item;
            sb.append(item).append("\n");
        }
        String seleccion = (String) JOptionPane.showInputDialog(this, sb.toString(), "Seleccionar Solicitud", JOptionPane.QUESTION_MESSAGE, null, opcionesVisitas, opcionesVisitas[0]);
        if (seleccion == null) return;
        int visitaId;
        try {
            String idStr = seleccion.substring(seleccion.indexOf('[') + 1, seleccion.indexOf(']'));
            visitaId = Integer.parseInt(idStr.trim());
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error al interpretar la seleccion.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        String[] acciones = {"Aprobar", "Rechazar"};
        int eleccion = JOptionPane.showOptionDialog(this, "Visita ID: " + visitaId + "\nSeleccione una accion:", "Aprobar / Rechazar", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, acciones, acciones[0]);
        if (eleccion < 0) return;
        try {
            if (eleccion == 0) {
                Visita aprobada = aprobacionVisitaUseCase.aprobarVisita(visitaId, usuarioActual.getId());
                JOptionPane.showMessageDialog(this, "Visita ID " + aprobada.getId() + " APROBADA correctamente.", "Aprobada", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Visita rechazada = aprobacionVisitaUseCase.rechazarVisita(visitaId, usuarioActual.getId());
                JOptionPane.showMessageDialog(this, "Visita ID " + rechazada.getId() + " RECHAZADA.", "Rechazada", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }
    private void registrarIncidente() {
        String visitaIdStr = JOptionPane.showInputDialog(this, "ID de la visita:", "Registrar Incidente", JOptionPane.QUESTION_MESSAGE);
        if (visitaIdStr == null) return;
        int visitaId;
        try { visitaId = Integer.parseInt(visitaIdStr.trim()); } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "ID invalido.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        String descripcion = JOptionPane.showInputDialog(this, "Descripcion del incidente:", "Registrar Incidente", JOptionPane.WARNING_MESSAGE);
        if (descripcion == null || descripcion.trim().isEmpty()) { JOptionPane.showMessageDialog(this, "La descripcion es obligatoria.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        try {
            Incidente incidente = incidenteUseCase.registrarIncidente(visitaId, usuarioActual.getId(), descripcion.trim());
            JOptionPane.showMessageDialog(this, "Incidente registrado. ID: " + incidente.getId() + "\nLa persona afectada ha sido bloqueada.", "Incidente Registrado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }
    private void probarAutorizacion() {
        String userIdStr = JOptionPane.showInputDialog(this, "ID del usuario a verificar:", "Probar Autorizacion", JOptionPane.QUESTION_MESSAGE);
        if (userIdStr == null) return;
        int userId;
        try { userId = Integer.parseInt(userIdStr.trim()); } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "ID invalido.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        String permiso = JOptionPane.showInputDialog(this, "Nombre del permiso:", "Probar Autorizacion", JOptionPane.QUESTION_MESSAGE);
        if (permiso == null || permiso.trim().isEmpty()) return;
        boolean tiene = autorizacionService.tienePermiso(userId, permiso.trim());
        if (tiene) {
            JOptionPane.showMessageDialog(this, "El usuario " + userId + " TIENE el permiso '" + permiso + "'.", "Autorizado", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "El usuario " + userId + " NO tiene el permiso '" + permiso + "'.", "Denegado", JOptionPane.WARNING_MESSAGE);
        }
    }
    private void verPersonasDentro() {
        String empresaIdString = JOptionPane.showInputDialog(this, "Id de la empresa", "Buscar personas por empresa", JOptionPane.QUESTION_MESSAGE);
        int empresaId = Integer.parseInt(empresaIdString.trim());
        List<Persona> dentro = reporteUseCase.personasDentroDelComplejo(empresaId);
        if (dentro.isEmpty()) { JOptionPane.showMessageDialog(this, "No hay personas dentro del complejo.", "Personas Dentro", JOptionPane.INFORMATION_MESSAGE); return; }
        StringBuilder sb = new StringBuilder("Personas dentro del complejo:\n\n");
        for (Persona p : dentro) { sb.append("- ").append(p.getNombre()).append(" | Doc: ").append(p.getDocumentoIdentidad()).append(" | Tipo: ").append(p.getTipoPersona()).append("|Fecha y hora de entrada:").append("\n"); }
        sb.append("\nTotal: ").append(dentro.size());
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Personas Dentro (" + dentro.size() + ")", JOptionPane.INFORMATION_MESSAGE);
    }
    private void verIncidentesPorFecha() {
        String inicio = JOptionPane.showInputDialog(this, "Fecha inicio (YYYY-MM-DD):", "Incidentes por Fecha", JOptionPane.QUESTION_MESSAGE);
        if (inicio == null) return;
        String fin = JOptionPane.showInputDialog(this, "Fecha fin (YYYY-MM-DD):", "Incidentes por Fecha", JOptionPane.QUESTION_MESSAGE);
        if (fin == null) return;
        try {
            List<Incidente> incidentes = reporteUseCase.incidentesPorRangoFechas(inicio.trim(), fin.trim());
            if (incidentes.isEmpty()) { JOptionPane.showMessageDialog(this, "No se encontraron incidentes.", "Incidentes", JOptionPane.INFORMATION_MESSAGE); return; }
            StringBuilder sb = new StringBuilder("Incidentes en el rango:\n\n");
            for (Incidente i : incidentes) { sb.append("- ID: ").append(i.getId()).append(" | Fecha: ").append(i.getFecha()).append(" | Visita: ").append(i.getVisitaId()).append("\n  Desc: ").append(i.getDescripcion()).append("\n"); }
            sb.append("\nTotal: ").append(incidentes.size());
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Incidentes (" + incidentes.size() + ")", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Formato de fecha invalido: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }
    private void verVisitasPorEstado() {
        Map<String, List<Visita>> agrupadas = reporteUseCase.visitasAgrupadasPorEstado();
        if (agrupadas.isEmpty()) { JOptionPane.showMessageDialog(this, "No hay visitas registradas.", "Visitas por Estado", JOptionPane.INFORMATION_MESSAGE); return; }
        StringBuilder sb = new StringBuilder("Visitas agrupadas por estado:\n\n");
        for (Map.Entry<String, List<Visita>> entry : agrupadas.entrySet()) {
            sb.append("[").append(entry.getKey()).append("] - ").append(entry.getValue().size()).append(" visitas:\n");
            for (Visita v : entry.getValue()) { sb.append("  * ID: ").append(v.getId()).append(" | Persona: ").append(v.getPersonaId()).append(" | Entrada: ").append(v.getFechaEntrada()).append("\n"); }
            sb.append("\n");
        }
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Visitas por Estado", JOptionPane.INFORMATION_MESSAGE);
    }
}
