package com.sica.application.strategy;

/**
 * Interfaz Strategy: define un flujo de acceso al complejo.
 *
 * Cada implementación concreta representa un escenario diferente:
 * 1. InvitadoPreRegistrado      → persona ya registrada en BD, se busca y se le registra entrada
 * 2. InvitadoNoAnunciado        → persona NO está en BD, se crea al vuelo y se registra entrada
 * 3. TrabajadorCarnetOlvidado   → trabajador sin carnet, se busca por doc y se le permite el paso
 * 4. SalidaOlvidada             → persona dentro pero olvidó registrar salida, se cierra la visita
 *
 * Los repositorios se inyectan en cada implementación concreta (constructor),
 * de modo que la lógica de acceso y persistencia están separadas.
 */
public interface FlujoAccesoStrategy {

    /**
     * Procesa el flujo de acceso con los datos de la solicitud.
     *
     * @param solicitud datos de entrada (persona, documento, placa, etc.)
     * @return resultado con éxito/fallo y detalles
     */
    ResultadoAcceso procesar(SolicitudAcceso solicitud);

    /**
     * Nombre descriptivo del flujo para mostrar en menús y auditoría.
     */
    String getNombreFlujo();
}
