-- ============================================================
-- SICA - Datos iniciales (roles, permisos, estados, usuarios)
-- ============================================================
-- Los hashes BCrypt de ejemplo deben generarse por separado.
-- Se incluyen contraseñas en texto plano como comentario.

USE SICA;

-- ============ ROLES ============
INSERT INTO roles (nombre_rol) VALUES
    ('Superusuario'),
    ('Supervisor de Seguridad'),
    ('Guarda de Seguridad'),
    ('Funcionario de Empresa');

-- ============ PERMISOS GRANULARES ============
INSERT INTO permisos (nombre_permiso, descripcion) VALUES
    ('acceder_sistema',           'Acceder al sistema de control de acceso'),
    ('registrar_visita',          'Registrar entrada de personas al complejo'),
    ('regularizar_salida',        'Regularizar salidas olvidadas'),
    ('registrar_incidente',       'Registrar incidentes de seguridad'),
    ('bloquear_persona',          'Bloquear/Banear persona del complejo'),
    ('ver_reportes',              'Consultar reportes del sistema'),
    ('ver_auditoria',             'Consultar bitacora de auditoria'),
    ('gestionar_usuarios',        'Crear, editar y eliminar usuarios del sistema'),
    ('gestionar_roles',           'Administrar roles y sus permisos'),
    ('gestionar_permisos',        'Administrar permisos del sistema'),
    ('gestionar_empresas',        'Crear, editar y eliminar empresas'),
    ('gestionar_personas',        'Crear, editar y eliminar personas registradas'),
    ('aprobar_visita',            'Aprobar o rechazar solicitudes de acceso pendientes');

-- ============ ROL_PERMISOS ============
-- Superusuario: TODOS los permisos
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT 1, id FROM permisos;

-- Supervisor de Seguridad: seguridad, reportes, auditoria
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT 2, id FROM permisos WHERE nombre_permiso IN (
    'acceder_sistema', 'registrar_visita', 'regularizar_salida',
    'registrar_incidente', 'bloquear_persona', 'ver_reportes',
    'ver_auditoria', 'gestionar_personas'
);

-- Guarda de Seguridad: operaciones basicas de acceso
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT 3, id FROM permisos WHERE nombre_permiso IN (
    'acceder_sistema', 'registrar_visita', 'regularizar_salida'
);

-- Funcionario de Empresa: solo registrar visitas propias
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT 4, id FROM permisos WHERE nombre_permiso IN (
    'acceder_sistema', 'registrar_visita', 'aprobar_visita'
);

-- ============ ESTADOS DE ACCESO DE PERSONA ============
INSERT INTO persona_estados_acceso (nombre_estado) VALUES
    ('Activo'),
    ('Con Prohibicion de Ingreso');

-- ============ ESTADOS DE VISITA ============
INSERT INTO visita_estados (nombre_estado) VALUES
    ('Dentro'),
    ('Fuera'),
    ('Pendiente de Aprobacion'),
    ('Aprobado'),
    ('Rechazado'),
    ('Expirado');

-- ============ USUARIOS DE EJEMPLO ============
-- Contraseña en texto plano entre parentesis para referencia.
-- Generar hash real: PasswordUtil.hash("password") o BCrypt online.
-- Todos los usuarios estan activos y con rol asignado.

-- Superusuario (rol_id=1)
-- password: admin123
INSERT INTO usuarios (nombre, email, password, rol_id, esta_activo) VALUES
    ('Carlos Mendoza', 'admin@sica.com', '$2a$12$9.RVyFQG/E3WVSoGWgZomOwfBLXcXgizuEFWJlFdRddNRUJlLGn5i', 1, TRUE);

-- Supervisor de Seguridad (rol_id=2)
-- password: supervisor123
INSERT INTO usuarios (nombre, email, password, rol_id, esta_activo) VALUES
    ('Maria Torres', 'supervisor@sica.com', '$2a$12$6NzDFaa7nytH8Rf3RkqsnOYFKgU1.32BNbun3/M4OJJzBYT7u9IyW', 2, TRUE);

-- Guarda de Seguridad (rol_id=3)
-- password: guarda123
INSERT INTO usuarios (nombre, email, password, rol_id, esta_activo) VALUES
    ('Juan Perez', 'guarda@sica.com', '$2a$12$ip72yIWo2VsQfyOf1CtrgONGmZow0J7qXF2zxQC2q1x.PiDv/prZu', 3, TRUE);

-- Funcionario de Empresa (rol_id=4)
-- password: funcionario123
INSERT INTO usuarios (nombre, email, password, rol_id, esta_activo) VALUES
    ('Ana Ramirez', 'funcionario@sica.com', '$2a$12$EzOdSHBtSqRLS6PfbxUWle0oJqFuxV66/3n1yMDxnt09aPGNi4fJq', 4, TRUE);
