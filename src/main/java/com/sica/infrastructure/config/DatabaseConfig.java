package com.sica.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Configuración de conexión a la base de datos MySQL.
 * Lee las credenciales desde src/main/resources/jdbc.properties.
 * Utiliza Apache Commons DBCP2 para el pool de conexiones.
 */
public class DatabaseConfig {

    private static DatabaseConfig instance;
    private final BasicDataSource dataSource;

    private DatabaseConfig() {
        Properties props = loadProperties();
        this.dataSource = createDataSource(props);
    }

    /**
     * Singleton: retorna la única instancia de DatabaseConfig.
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /**
     * Retorna una conexión del pool.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Prueba que la conexión a la base de datos funciona correctamente.
     *
     * @return true si la conexión fue exitosa, false en caso contrario
     */
    public boolean testConnection() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1");
            System.out.println("[SICA] ✅ Conexión a la base de datos exitosa.");
            return true;
        } catch (SQLException e) {
            System.err.println("[SICA] ❌ Error al conectar a la base de datos: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cierra el pool de conexiones. Llamar al terminar la aplicación.
     */
    public void close() {
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                System.out.println("[SICA] Pool de conexiones cerrado.");
            }
        } catch (SQLException e) {
            System.err.println("[SICA] Error al cerrar el pool: " + e.getMessage());
        }
    }

    // ─── Métodos privados ───────────────────────────────────

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("jdbc.properties")) {
            if (input == null) {
                throw new RuntimeException("No se encontró jdbc.properties en el classpath.");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer jdbc.properties: " + e.getMessage(), e);
        }
        return props;
    }

    private BasicDataSource createDataSource(Properties props) {
        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(props.getProperty("db.url"));
        ds.setUsername(props.getProperty("db.username"));
        ds.setPassword(props.getProperty("db.password"));

        ds.setInitialSize(Integer.parseInt(props.getProperty("db.pool.initialSize", "5")));
        ds.setMaxTotal(Integer.parseInt(props.getProperty("db.pool.maxTotal", "20")));
        ds.setMaxIdle(Integer.parseInt(props.getProperty("db.pool.maxIdle", "10")));
        ds.setMinIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "5")));
        ds.setMaxWait(Duration.ofMillis(Long.parseLong(props.getProperty("db.pool.maxWaitMillis", "10000"))));
        return ds;
    }
}
