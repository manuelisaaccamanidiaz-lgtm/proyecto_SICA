package com.sica.domain;

/**
 * Entidad de dominio: Persona (trabajador o invitado).
 * Corresponde a la tabla `personas` del esquema oficial.
 */
public class Persona {

    private int id;
    private String nombre;
    private String documentoIdentidad;
    private Integer empresaId;
    private TipoPersona tipoPersona;
    private Integer estadoAccesoId;
    private String urlFoto;

    public Persona() {}

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDocumentoIdentidad() { return documentoIdentidad; }
    public void setDocumentoIdentidad(String documentoIdentidad) { this.documentoIdentidad = documentoIdentidad; }

    public Integer getEmpresaId() { return empresaId; }
    public void setEmpresaId(Integer empresaId) { this.empresaId = empresaId; }

    public TipoPersona getTipoPersona() { return tipoPersona; }
    public void setTipoPersona(TipoPersona tipoPersona) { this.tipoPersona = tipoPersona; }

    public Integer getEstadoAccesoId() { return estadoAccesoId; }
    public void setEstadoAccesoId(Integer estadoAccesoId) { this.estadoAccesoId = estadoAccesoId; }

    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }
}
