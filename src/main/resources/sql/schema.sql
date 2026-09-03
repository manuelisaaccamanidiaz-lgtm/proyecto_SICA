-- =========== TABLAS DE AUTENTICACIÓN Y AUTORIZACIÓN (RBAC) ===========

CREATE DATABASE IF NOT EXISTS SICA;
USE SICA;

-- Define los roles disponibles en el sistema
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) UNIQUE NOT NULL -- 'Superusuario', 'Supervisor de Seguridad', 'Guarda de Seguridad', 'Funcionario de Empresa'
);

-- Define cada acción granular que se puede realizar en el sistema
CREATE TABLE permisos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_permiso VARCHAR(100) UNIQUE NOT NULL, -- ej. 'crear_usuario', 'registrar_visita', 'generar_reporte_auditoria'
    descripcion TEXT
);

-- Tabla de unión que asigna permisos a los roles
CREATE TABLE rol_permisos (
    rol_id INT,
    permiso_id INT,
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id) REFERENCES roles(id),
    FOREIGN KEY (permiso_id) REFERENCES permisos(id)
);

-- Almacena los usuarios y su rol asignado
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- En una aplicación real, esto debería ser un hash
    rol_id INT,
    esta_activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (rol_id) REFERENCES roles(id)
);


-- =========== TABLAS DE CONSULTA (LOOKUP TABLES) PARA ESTADOS ===========

-- Define los posibles estados de acceso de una persona
CREATE TABLE persona_estados_acceso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(50) UNIQUE NOT NULL -- 'Activo', 'Con Prohibicion de Ingreso'
);

-- Define los posibles estados de una visita
CREATE TABLE visita_estados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(50) UNIQUE NOT NULL -- 'Dentro', 'Fuera', 'Pendiente de Aprobacion', 'Aprobado', 'Rechazado', 'Expirado', etc.
);


-- =========== TABLAS OPERACIONALES DEL NEGOCIO ===========

-- Almacena las empresas dentro del complejo
CREATE TABLE empresas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    contacto_principal VARCHAR(100)
);

-- Almacena a todas las personas (trabajadores e invitados)
CREATE TABLE personas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    documento_identidad VARCHAR(20) UNIQUE NOT NULL,
    empresa_id INT,
    tipo_persona ENUM('Trabajador', 'Invitado') NOT NULL,
    estado_acceso_id INT,
    url_foto VARCHAR(255),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    FOREIGN KEY (estado_acceso_id) REFERENCES persona_estados_acceso(id)
);

-- Registro atómico de cada evento de entrada y salida
CREATE TABLE visitas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    persona_id INT,
    fecha_entrada DATETIME,
    fecha_salida DATETIME,
    estado_visita_id INT,
    vehiculo_placa VARCHAR(10),
    visita_aprobada_por INT, -- ID del usuario (Funcionario) que aprueba el ingreso no anunciado
    FOREIGN KEY (persona_id) REFERENCES personas(id),
    FOREIGN KEY (estado_visita_id) REFERENCES visita_estados(id),
    FOREIGN KEY (visita_aprobada_por) REFERENCES usuarios(id)
);

-- Registro de incidentes de seguridad
CREATE TABLE incidentes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    visita_id INT, -- Un incidente ocurre durante una visita específica
    reportado_por_id INT, -- ID del usuario (Supervisor) que reporta
    fecha DATETIME NOT NULL,
    descripcion TEXT NOT NULL,
    FOREIGN KEY (visita_id) REFERENCES visitas(id),
    FOREIGN KEY (reportado_por_id) REFERENCES usuarios(id)
);


-- =========== TABLA DE AUDITORÍA ===========

-- Bitácora para registrar todas las acciones importantes del sistema
CREATE TABLE bitacora_auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accion_realizada VARCHAR(255) NOT NULL, -- ej. 'LOGIN_EXITOSO', 'CREACION_PERSONA', 'CAMBIO_ESTADO_VISITA'
    tabla_afectada VARCHAR(100),
    registro_id_afectado INT,
    detalles TEXT, -- Puede usarse para almacenar un resumen del cambio o datos en formato JSON
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);