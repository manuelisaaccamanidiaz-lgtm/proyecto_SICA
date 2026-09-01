# SICA - Sistema de Control de Acceso

Sistema de control de acceso para complejos cerrados, construido con **arquitectura hexagonal** (puertos y adaptadores) + **Strategy pattern** + **Vertical Slice**.

## Stack

- **Java 17+** (sin frameworks, solo JDK estándar)
- **MySQL 8+** como motor de BD
- **JDBC** + **Apache Commons DBCP2** para pool de conexiones
- **jBCrypt** para hashing de contraseñas
- **Maven** como build tool

## Arquitectura

```
com.sica/
├── domain/                          ← Entidades puras, cero dependencias
│   ├── port/                        ← Interfaces (puertos de salida)
│   └── (entidades: Usuario, Persona, Visita, etc.)
├── application/                     ← Casos de uso (puertos de entrada)
│   └── strategy/                    ← Strategy pattern: 4 flujos de acceso
└── infrastructure/                  ← Adaptadores concretos
    ├── config/                      ← DatabaseConfig, PasswordUtil
    └── persistence/                 ← Repositorios JDBC
```

Los casos de uso **nunca** importan de `infrastructure`. Solo conocen los puertos de `domain/port`.

## Configuración de la Base de Datos

1. **Crear la BD en MySQL:**
   ```sql
   CREATE DATABASE sica CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Ejecutar el esquema:**
   ```bash
   mysql -u root -p sica < src/main/resources/sql/schema.sql
   ```

3. **Insertar datos iniciales** (catálogos + usuario admin):
   ```sql
   -- Roles
   INSERT INTO roles (nombre_rol) VALUES
   ('Superusuario'),
   ('Supervisor de Seguridad'),
   ('Guarda de Seguridad'),
   ('Funcionario de Empresa');

   -- Estados de acceso
   INSERT INTO persona_estados_acceso (nombre_estado) VALUES
   ('Activo'),
   ('Con Prohibicion de Ingreso');

   -- Estados de visita
   INSERT INTO visita_estados (nombre_estado) VALUES
   ('Pendiente de Aprobacion'),
   ('Aprobado'),
   ('Rechazado'),
   ('Dentro'),
   ('Fuera'),
   ('Expirado');

   -- Usuario admin (contraseña: admin123 → hash bcrypt)
   -- Genera el hash en Java o usa: https://bcrypt-generator.com/
   INSERT INTO usuarios (nombre, email, password, rol_id, esta_activo)
   VALUES ('Administrador', 'admin@sica.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, TRUE);
   ```

4. **Configurar credenciales:**
   Edita `src/main/resources/jdbc.properties` con tus datos:
   ```properties
   jdbc.url=jdbc:mysql://localhost:3306/sica
   jdbc.username=root
   jdbc.password=tu_password
   jdbc.driver=com.mysql.cj.jdbc.Driver
   ```

## Ejecutar

```bash
# Compilar
mvn clean compile

# Ejecutar
mvn exec:java -Dexec.mainClass="com.sica.Main"

# O ejecutar con Maven wrapper
mvn compile exec:java -Dexec.mainClass="com.sica.Main"
```

## Usuarios de Ejemplo por Rol

| Rol | Email | Contraseña | Descripción |
|-----|-------|------------|-------------|
| **Superusuario** | admin@sica.com | admin123 | Acceso total al sistema |
| **Supervisor de Seguridad** | supervisor@sica.com | super123 | Reporta incidentes, supervisa flujos |
| **Guarda de Seguridad** | guarda@sica.com | guarda123 | Registra ingresos/salidas de visitas |
| **Funcionario de Empresa** | funcionario@empresa.com | func123 | Registra visitas de su empresa |

> ⚠️ Cambia estas contraseñas en producción. Los hashes de ejemplo usan BCrypt.

## Flujos de Acceso (Strategy Pattern)

| # | Flujo | Descripción |
|---|-------|-------------|
| 1 | **Invitado Pre-Registrado** | Persona ya está en BD. Se busca, se verifica estado, se registra entrada. |
| 2 | **Invitado No Anunciado** | Persona no está en BD. Se crea al vuelo y se registra entrada. |
| 3 | **Trabajador con Carnet Olvidado** | Trabajador conocido sin carnet. Se verifica por documento y se ingresa. |
| 4 | **Salida Olvidada** | Persona dentro que olvidó salir. Se cierra la visita con hora actual. |

## Licencia

Proyecto interno de uso privado.
