/* ============================================================================
   SCRIPT SQL MAESTRO - ECOSISTEMA SOA "I.E.P. SENOR DE LOS MILAGROS"
   Motor: MySQL 8+ / MariaDB (XAMPP phpMyAdmin - localhost:3306, user: root)
   ============================================================================ */

/* 1. BASE DE DATOS: colegio_auth_db (Puerto 8085 - auth-service) */
CREATE DATABASE IF NOT EXISTS colegio_auth_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE colegio_auth_db;

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario CHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL COMMENT 'Hash BCrypt',
    nombre_completo VARCHAR(120) NOT NULL,
    rol ENUM('SECRETARIA', 'TESORERIA', 'DOCENTE', 'DIRECCION', 'APODERADO') NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

/* Contrasena en texto plano para todos los usuarios = "123456" */
INSERT INTO usuarios (id_usuario, username, password, nombre_completo, rol, activo) VALUES
('a0000000-0000-0000-0000-000000000001', 'secretaria', '$2a$10$Fwvd6PTXhVIYssBZwZ.iHed.cXOYhwf5yR5yOmfbGU3PTvVlt3Fg6', 'María Elena Gómez (Secretaría)', 'SECRETARIA', TRUE),
('a0000000-0000-0000-0000-000000000002', 'tesoreria',  '$2a$10$Fwvd6PTXhVIYssBZwZ.iHed.cXOYhwf5yR5yOmfbGU3PTvVlt3Fg6', 'Carlos Alberto Ruiz (Tesorería)', 'TESORERIA', TRUE),
('a0000000-0000-0000-0000-000000000003', 'docente',    '$2a$10$Fwvd6PTXhVIYssBZwZ.iHed.cXOYhwf5yR5yOmfbGU3PTvVlt3Fg6', 'Lucía Fernanda Torres (Docente)', 'DOCENTE', TRUE),
('a0000000-0000-0000-0000-000000000004', 'direccion',  '$2a$10$Fwvd6PTXhVIYssBZwZ.iHed.cXOYhwf5yR5yOmfbGU3PTvVlt3Fg6', 'Mg. Roberto Chávez (Dirección)', 'DIRECCION', TRUE),
('a0000000-0000-0000-0000-000000000005', 'apoderado',  '$2a$10$Fwvd6PTXhVIYssBZwZ.iHed.cXOYhwf5yR5yOmfbGU3PTvVlt3Fg6', 'Carlos García (Apoderado)', 'APODERADO', TRUE)
ON DUPLICATE KEY UPDATE nombre_completo = VALUES(nombre_completo), password = VALUES(password);


/* 2. BASE DE DATOS: colegio_estudiante_db (Puerto 8082 - estudiante-service) */
CREATE DATABASE IF NOT EXISTS colegio_estudiante_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE colegio_estudiante_db;

CREATE TABLE IF NOT EXISTS apoderados (
    id_apoderado CHAR(36) NOT NULL PRIMARY KEY,
    dni VARCHAR(8) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    celular VARCHAR(15) NOT NULL,
    correo VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS estudiantes (
    id_estudiante CHAR(36) NOT NULL PRIMARY KEY,
    codigo_estudiante VARCHAR(20) NOT NULL UNIQUE,
    dni VARCHAR(8) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    sexo CHAR(1) NOT NULL,
    direccion VARCHAR(200),
    id_apoderado CHAR(36) NOT NULL,
    parentesco VARCHAR(30) NOT NULL,
    CONSTRAINT fk_est_apoderado FOREIGN KEY (id_apoderado) REFERENCES apoderados(id_apoderado)
);

INSERT INTO apoderados (id_apoderado, dni, nombres, apellidos, celular, correo) VALUES
('b0000000-0000-0000-0000-000000000001', '45874589', 'Carlos', 'García Mendoza', '987654321', 'cgarcia@gmail.com'),
('b0000000-0000-0000-0000-000000000002', '41236547', 'Rosa', 'Ramos Vega', '965432187', 'rramos@gmail.com'),
('b0000000-0000-0000-0000-000000000003', '43987123', 'Jorge', 'Morales Díaz', '945612378', 'jmorales@gmail.com')
ON DUPLICATE KEY UPDATE correo = VALUES(correo);

INSERT INTO estudiantes (id_estudiante, codigo_estudiante, dni, nombres, apellidos, fecha_nacimiento, sexo, direccion, id_apoderado, parentesco) VALUES
('550e8400-e29b-41d4-a716-446655440001', '2026-0001', '73215489', 'María Fernanda', 'García López', '2016-08-14', 'F', 'Av. Grau 145, Sechura', 'b0000000-0000-0000-0000-000000000001', 'Padre'),
('550e8400-e29b-41d4-a716-446655440002', '2026-0002', '74581290', 'Juan Carlos', 'Pérez Ramos', '2015-03-22', 'M', 'Calle Bolívar 310, Sechura', 'b0000000-0000-0000-0000-000000000002', 'Madre'),
('550e8400-e29b-41d4-a716-446655440003', '2026-0003', '76123498', 'Andrea Nicole', 'Silva Morales', '2014-11-05', 'F', 'Jr. San Martín 208, Sechura', 'b0000000-0000-0000-0000-000000000003', 'Padre')
ON DUPLICATE KEY UPDATE nombres = VALUES(nombres);


/* 3. BASE DE DATOS: colegio_tesoreria_db (Puerto 8081 - tesoreria-service) */
CREATE DATABASE IF NOT EXISTS colegio_tesoreria_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE colegio_tesoreria_db;

CREATE TABLE IF NOT EXISTS deudas (
    id_deuda CHAR(36) NOT NULL PRIMARY KEY,
    id_estudiante CHAR(36) NOT NULL,
    concepto VARCHAR(120) NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    deuda_vencida BOOLEAN NOT NULL DEFAULT FALSE,
    pagada BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS cargos (
    id_cargo CHAR(36) NOT NULL PRIMARY KEY,
    id_estudiante CHAR(36) NOT NULL,
    concepto VARCHAR(120) NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado ENUM('PENDIENTE', 'PAGADO', 'ANULADO_COMPENSACION') NOT NULL DEFAULT 'PENDIENTE',
    motivo_anulacion VARCHAR(255) NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

/* Estudiante ...0003 tiene deuda_vencida = TRUE para probar rechazo RN-01 */
INSERT INTO deudas (id_deuda, id_estudiante, concepto, monto, fecha_vencimiento, deuda_vencida, pagada) VALUES
('d0000000-0000-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440001', 'Pensión Diciembre 2025', 250.00, '2025-12-30', FALSE, TRUE),
('d0000000-0000-0000-0000-000000000002', '550e8400-e29b-41d4-a716-446655440002', 'Pensión Diciembre 2025', 250.00, '2025-12-30', FALSE, TRUE),
('d0000000-0000-0000-0000-000000000003', '550e8400-e29b-41d4-a716-446655440003', 'Pensión Noviembre 2025 (Vencida)', 280.00, '2025-11-30', TRUE, FALSE)
ON DUPLICATE KEY UPDATE deuda_vencida = VALUES(deuda_vencida);

INSERT INTO cargos (id_cargo, id_estudiante, concepto, monto, fecha_vencimiento, estado) VALUES
('c0000000-0000-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440001', 'Matrícula 2026', 300.00, '2026-03-30', 'PAGADO'),
('c0000000-0000-0000-0000-000000000002', '550e8400-e29b-41d4-a716-446655440002', 'Matrícula 2026', 300.00, '2026-03-30', 'PENDIENTE')
ON DUPLICATE KEY UPDATE estado = VALUES(estado);


/* 4. BASE DE DATOS: colegio_matricula_db (Puerto 8080 - matricula-service) */
CREATE DATABASE IF NOT EXISTS colegio_matricula_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE colegio_matricula_db;

CREATE TABLE IF NOT EXISTS aulas (
    id_aula CHAR(36) NOT NULL PRIMARY KEY,
    periodo VARCHAR(10) NOT NULL,
    grado VARCHAR(30) NOT NULL,
    seccion VARCHAR(10) NOT NULL,
    capacidad_maxima INT NOT NULL,
    vacantes INT NOT NULL
);

CREATE TABLE IF NOT EXISTS matricula (
    id_matricula CHAR(36) NOT NULL PRIMARY KEY,
    id_estudiante CHAR(36) NOT NULL,
    id_cargo CHAR(36) NOT NULL,
    id_aula CHAR(36) NOT NULL,
    fecha_matricula DATE NOT NULL,
    estado ENUM('REGISTRADA', 'ANULADA') NOT NULL DEFAULT 'REGISTRADA',
    UNIQUE KEY uk_estudiante_aula_estado (id_estudiante, id_aula, estado)
);

INSERT INTO aulas (id_aula, periodo, grado, seccion, capacidad_maxima, vacantes) VALUES
('550e8400-e29b-41d4-a716-446655440011', '2026', '3ro Primaria', 'A', 25, 1),
('550e8400-e29b-41d4-a716-446655440012', '2026', '4to Primaria', 'B', 25, 0),
('550e8400-e29b-41d4-a716-446655440013', '2026', '5to Primaria', 'A', 30, 10)
ON DUPLICATE KEY UPDATE vacantes = VALUES(vacantes);


/* 5. BASE DE DATOS: colegio_academico_db (Puerto 8083 - academico-service) */
CREATE DATABASE IF NOT EXISTS colegio_academico_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE colegio_academico_db;

CREATE TABLE IF NOT EXISTS notas (
    id_nota CHAR(36) NOT NULL PRIMARY KEY,
    id_estudiante CHAR(36) NOT NULL,
    id_aula CHAR(36) NOT NULL,
    curso VARCHAR(80) NOT NULL,
    competencia VARCHAR(120) NOT NULL,
    bimestre INT NOT NULL,
    calificacion ENUM('AD', 'A', 'B', 'C') NOT NULL,
    acta_cerrada BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS asistencias (
    id_asistencia CHAR(36) NOT NULL PRIMARY KEY,
    id_estudiante CHAR(36) NOT NULL,
    id_aula CHAR(36) NOT NULL,
    fecha DATE NOT NULL,
    estado ENUM('P', 'T', 'F') NOT NULL COMMENT 'P=Presente, T=Tardanza, F=Falta',
    justificacion VARCHAR(255) NULL
);

INSERT INTO notas (id_nota, id_estudiante, id_aula, curso, competencia, bimestre, calificacion, acta_cerrada) VALUES
('e0000000-0000-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440013', 'Matemática', 'Resuelve problemas de cantidad', 1, 'AD', FALSE)
ON DUPLICATE KEY UPDATE calificacion = VALUES(calificacion);

INSERT INTO asistencias (id_asistencia, id_estudiante, id_aula, fecha, estado, justificacion) VALUES
('f0000000-0000-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440013', '2026-03-10', 'P', NULL)
ON DUPLICATE KEY UPDATE estado = VALUES(estado);
