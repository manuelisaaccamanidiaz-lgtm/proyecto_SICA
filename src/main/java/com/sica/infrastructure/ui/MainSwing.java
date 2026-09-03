package com.sica.infrastructure.ui;

import com.sica.infrastructure.config.DatabaseConfig;

import javax.swing.*;

/**
 * Punto de entrada para la interfaz grafica de SICA.
 * Arranca LoginFrame en el EDT de Swing.
 * Main.java (consola) se mantiene intacto como respaldo.
 */
public class MainSwing {

    public static void main(String[] args) {
        // Verificar conexion a BD antes de abrir UI
        System.out.println("SICA GUI - Probando conexion a BD...");
        if (!DatabaseConfig.getInstance().testConnection()) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar a la base de datos.\n"
                    + "Verifique jdbc.properties y que MySQL este corriendo.",
                    "SICA - Error de Conexion", JOptionPane.ERROR_MESSAGE);
            return;
        }
        System.out.println("SICA GUI - Conexion OK. Abriendo interfaz grafica...");

        SwingUtilities.invokeLater(() -> {
            SicaApp app = new SicaApp();
            app.iniciar();
        });
    }
}
