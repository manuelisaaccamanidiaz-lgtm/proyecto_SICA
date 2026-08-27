-- ============================================================
-- SICA - Sistema de Control de Acceso
-- Esquema de base de datos MySQL
-- ============================================================

CREATE DATABASE IF NOT EXISTS sica
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sica;

-- ============================================================
-- Tabla: roles
-- ============================================================
CREATE TABLE roles (
    id          INT             AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50)     NOT NULL UNIQUE
) ENGINE=InnoDB;

-- ============================================================
-- Tabla: permisos
-- ============================================================
CREATE TABLE permisos (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    nombre_permiso  VARCHAR(100)    NOT NULL UNIQUE,
    descripcion     TEXT
) ENGINE=InnoDB;

-- ============================================================
-- Tabla: rol_permisos (relación N:M entre roles y permisos)
-- ============================================================
CREATE TABLE rol_permisos (
    rol_id      INT NOT NULL,
    permiso_id  INT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_rolpermisos_rol
        FOREIGN KEY (rol_id)     REFERENCES roles(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rolpermisos_permiso
        FOREIGN KEY (permiso_id) REFERENCES permisos(id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Tabla: usuarios
-- ============================================================
CREATE TABLE usuarios (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    rol_id          INT             NOT NULL,
    CONSTRAINT fk_usuarios_rol
        FOREIGN KEY (rol_id) REFERENCES roles(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Tabla: empresas
-- ============================================================
CREATE TABLE empresas (
    id      INT             AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(150)    NOT NULL
) ENGINE=InnoDB;

-- ============================================================
-- Tabla: personas
-- ============================================================
CREATE TABLE personas (
    id          INT             AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(150)    NOT NULL,
    documento   VARCHAR(20)     NOT NULL UNIQUE,
    empresa_id  INT             NULL,
    CONSTRAINT fk_personas_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresas(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Tabla: visitas
-- ============================================================
CREATE TABLE visitas (
    id                  INT         AUTO_INCREMENT PRIMARY KEY,
    persona_id          INT         NOT NULL,
    funcionario_id      INT         NOT NULL,
    estado              VARCHAR(20) NOT NULL,
    fecha_hora_entrada  DATETIME    NOT NULL,
    fecha_hora_salida   DATETIME    NULL,
    CONSTRAINT fk_visitas_persona
        FOREIGN KEY (persona_id)     REFERENCES personas(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_visitas_funcionario
        FOREIGN KEY (funcionario_id) REFERENCES usuarios(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Tabla: incidentes
-- ============================================================
CREATE TABLE incidentes (
    id                      INT         AUTO_INCREMENT PRIMARY KEY,
    persona_id              INT         NOT NULL,
    descripcion             TEXT        NOT NULL,
    fecha                   DATE        NOT NULL,
    usuario_registro_id     INT         NOT NULL,
    CONSTRAINT fk_incidentes_persona
        FOREIGN KEY (persona_id)             REFERENCES personas(id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_incidentes_usuario
        FOREIGN KEY (usuario_registro_id)    REFERENCES usuarios(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Tabla: bitacora_auditoria
-- ============================================================
CREATE TABLE bitacora_auditoria (
    id          INT             AUTO_INCREMENT PRIMARY KEY,
    usuario_id  INT             NOT NULL,
    accion      VARCHAR(100)    NOT NULL,
    detalle     TEXT,
    fecha_hora  DATETIME        NOT NULL,
    CONSTRAINT fk_bitacora_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;
