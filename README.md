# Sistema de Matrículas SOA - I.E.P. Señor de los Milagros

Backend modular basado en Arquitectura Orientada a Servicios (SOA) desarrollado en Java 17 y Spring Boot 3.3.5 para la gestión de matrículas, control financiero y autenticación por roles.

---

## Requisitos previos

- **JDK 17** instalado (configurar `JAVA_HOME` apuntando al JDK 17).
- **XAMPP** (MySQL / MariaDB corriendo en el puerto `3306`, usuario `root` sin contraseña).
- **RabbitMQ** (opcional en local, puerto `5672`; si no está activo, el registro de matrícula continúa y deja la advertencia en consola).

---

## Servicios incluidos y puertos

| Servicio | Carpeta | Puerto | Base de datos |
| :--- | :--- | :---: | :--- |
| `auth-service` | `/auth-service` | `8085` | `colegio_auth_db` |
| `tesoreria-service` | `/tesoreria-service` | `8081` | `colegio_tesoreria_db` |
| `matricula-service` | `/` (raíz) | `8080` | `colegio_matricula_db` |

---

## 1. Preparar la base de datos (XAMPP)

1. Abrir el panel de control de **XAMPP** e iniciar **Apache** y **MySQL**.
2. Entrar a `http://localhost/phpmyadmin`.
3. Ir a la pestaña **Importar** (o **SQL**) y ejecutar el archivo:
   ```text
   sql/init_soa_xampp.sql
   ```
   Esto crea las 5 bases de datos del esquema (`colegio_auth_db`, `colegio_estudiante_db`, `colegio_tesoreria_db`, `colegio_matricula_db`, `colegio_academico_db`) junto con los usuarios, alumnos, deudas y aulas de prueba.

---

## 2. Cómo levantar los servicios

Se recomienda abrir **3 terminales** (PowerShell o CMD) en la raíz del proyecto y ejecutar los servicios en este orden:

### Terminal 1 — `auth-service` (Puerto 8085)
Encargado del login y emisión de tokens JWT.
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd -f auth-service\pom.xml spring-boot:run
```

### Terminal 2 — `tesoreria-service` (Puerto 8081)
Encargado de validar deudas vencidas (RN-01), generar cargos y ejecutar compensación Saga.
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd -f tesoreria-service\pom.xml spring-boot:run
```

### Terminal 3 — `matricula-service` (Puerto 8080)
Orquestador principal (`ProcesoMatricula`) y frontend web integrado.
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd spring-boot:run
```

> **Nota:** Si PowerShell ya tiene `JAVA_HOME` configurado en las variables del sistema, no hace falta escribir la primera línea.

---

## 3. Accesos al sistema

Una vez levantados los 3 servicios:

- **Sistema Web (Frontend):** `http://localhost:8080`
- **Swagger Matrícula:** `http://localhost:8080/swagger-ui.html`
- **Swagger Tesorería:** `http://localhost:8081/swagger-ui.html`
- **Swagger Auth:** `http://localhost:8085/swagger-ui.html`

---

## 4. Credenciales y datos de prueba

### Usuarios (`auth-service`)
Todos los usuarios semilla tienen la contraseña **`123456`**:

- `secretaria` (Rol: `SECRETARIA`)
- `tesoreria` (Rol: `TESORERIA`)
- `docente` (Rol: `DOCENTE`)
- `direccion` (Rol: `DIRECCION`)
- `apoderado` (Rol: `APODERADO`)

### Escenarios de prueba en el módulo de Matrícula
- **Matrícula exitosa (HTTP 201):** Estudiante `550e8400-e29b-41d4-a716-446655440001` o `...0002` (sin deuda) + Aula `550e8400-e29b-41d4-a716-446655440013` (10 vacantes).
- **Rechazo por deuda vencida RN-01 (HTTP 422):** Estudiante `550e8400-e29b-41d4-a716-446655440003` (Andrea Silva).
- **Rechazo por falta de vacantes RN-02 (HTTP 409):** Aula `550e8400-e29b-41d4-a716-446655440012` (0 vacantes).
- **Rechazo por matrícula duplicada (HTTP 409):** Intentar matricular dos veces al mismo estudiante en la misma aula con estado `REGISTRADA`.
