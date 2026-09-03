package com.sica.domain.port;

import com.sica.domain.Persona;
import java.util.List;

/**
 * Puerto de salida: Repositorio de Personas.
 */
public interface PersonaRepository {

    Persona guardar(Persona persona);

    Persona findById(int id);

    Persona findByDocumentoIdentidad(String documentoIdentidad);

    List<Persona> findAll();

    List<Persona> findByEmpresaId(int empresaId);

    void actualizar(Persona persona);

    void eliminar(int id);
}
