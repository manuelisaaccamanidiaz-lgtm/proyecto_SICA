package com.sica.domain.port;

import com.sica.domain.Empresa;
import java.util.List;

/**
 * Puerto de salida: Repositorio de Empresas.
 */
public interface EmpresaRepository {

    Empresa guardar(Empresa empresa);

    Empresa findById(int id);

    List<Empresa> findAll();

    void actualizar(Empresa empresa);

    void eliminar(int id);
}
