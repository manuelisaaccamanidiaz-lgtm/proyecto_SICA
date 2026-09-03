package com.sica.infrastructure.ui;

import com.sica.application.LoginUseCase;
import com.sica.domain.Usuario;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana de inicio de sesion.
 * Adaptador de entrada grafico que usa LoginUseCase.
 */
public class LoginFrame extends JFrame {

    private final LoginUseCase loginUseCase;
    private final SicaApp sicaApp;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JLabel statusLabel;

    public LoginFrame(LoginUseCase loginUseCase, SicaApp sicaApp) {
        this.loginUseCase = loginUseCase;
        this.sicaApp = sicaApp;

        setTitle("SICA - Inicio de Sesion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 260);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("SICA - Sistema de Control de Acceso");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        // Email
        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        mainPanel.add(new JLabel("Email:"), gbc);
        emailField = new JTextField(20);
        gbc.gridx = 1;
        mainPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Contrasena:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);

        // Button
        loginButton = new JButton("Ingresar");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 5, 5);
        mainPanel.add(loginButton, gbc);

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        mainPanel.add(statusLabel, gbc);

        add(mainPanel);

        // Actions
        loginButton.addActionListener(e -> intentarLogin());
        passwordField.addActionListener(e -> intentarLogin());
    }

    private void intentarLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Ingrese email y contrasena.");
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("Autenticando...");

        SwingWorker<Usuario, Void> worker = new SwingWorker<>() {
            @Override
            protected Usuario doInBackground() throws Exception {
                return loginUseCase.login(email, password);
            }

            @Override
            protected void done() {
                try {
                    Usuario usuario = get();
                    statusLabel.setForeground(new Color(0, 128, 0));
                    statusLabel.setText("Bienvenido, " + usuario.getNombre());
                    dispose();
                    sicaApp.abrirMenuPrincipal(usuario);
                } catch (Exception ex) {
                    statusLabel.setForeground(Color.RED);
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setText(cause.getMessage());
                    loginButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
