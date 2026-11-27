# Configuraciones `application.properties` — Ecosistema SOA (6 Microservicios)

Todos los servicios están configurados para **XAMPP MySQL** (`localhost:3306`, usuario `root`, sin contraseña), **RabbitMQ** (`localhost:5672`) y **seguridad JWT transversal** con la misma clave secreta HMAC-SHA256.

---

## 1. `auth-service` (Puerto `8085`) — `colegio_auth_db`
```properties
spring.application.name=auth-service
server.port=8085

spring.datasource.url=jdbc:mysql://localhost:3306/colegio_auth_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=America/Lima
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

app.security.jwt.secret=colegio-secret-key-apf3-2026-muy-larga-256bits
app.security.jwt.expiration-ms=86400000

springdoc.swagger-ui.path=/swagger-ui.html
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [CorrID:%X{correlationId}] %logger{36} - %msg%n
```

---

## 2. `tesoreria-service` (Puerto `8081`) — `colegio_tesoreria_db`
```properties
spring.application.name=tesoreria-service
server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/colegio_tesoreria_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=America/Lima
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

app.security.jwt.enabled=true
app.security.jwt.secret=colegio-secret-key-apf3-2026-muy-larga-256bits

springdoc.swagger-ui.path=/swagger-ui.html
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [CorrID:%X{correlationId}] %logger{36} - %msg%n
```

---

## 3. `estudiante-service` (Puerto `8082`) — `colegio_estudiante_db`
```properties
spring.application.name=estudiante-service
server.port=8082

spring.datasource.url=jdbc:mysql://localhost:3306/colegio_estudiante_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=America/Lima
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

app.security.jwt.enabled=true
app.security.jwt.secret=colegio-secret-key-apf3-2026-muy-larga-256bits

springdoc.swagger-ui.path=/swagger-ui.html
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [CorrID:%X{correlationId}] %logger{36} - %msg%n
```

---

## 4. `academico-service` (Puerto `8083`) — `colegio_academico_db`
*(Agrupa NotasService y AsistenciaService)*
```properties
spring.application.name=academico-service
server.port=8083

spring.datasource.url=jdbc:mysql://localhost:3306/colegio_academico_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=America/Lima
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

app.security.jwt.enabled=true
app.security.jwt.secret=colegio-secret-key-apf3-2026-muy-larga-256bits

springdoc.swagger-ui.path=/swagger-ui.html
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [CorrID:%X{correlationId}] %logger{36} - %msg%n
```

---

## 5. `utilitarios-service` (Puerto `8084`) — RabbitMQ + Reportes
*(Agrupa NotificacionService vía AMQP y ReporteService)*
```properties
spring.application.name=utilitarios-service
server.port=8084

# RabbitMQ (AMQP)
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/

rabbitmq.exchange.matriculas=exchange.matriculas
rabbitmq.queue.matricula-registrada=matricula.registrada
rabbitmq.routingkey.matricula-registrada=rk.matricula.registrada

app.security.jwt.enabled=true
app.security.jwt.secret=colegio-secret-key-apf3-2026-muy-larga-256bits

springdoc.swagger-ui.path=/swagger-ui.html
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [CorrID:%X{correlationId}] %logger{36} - %msg%n
```

---

## 6. `matricula-service` (Puerto `8080`) — `colegio_matricula_db` (Orquestador Principal)
```properties
spring.application.name=microservice-colegio
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/colegio_matricula_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=America/Lima
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/
rabbitmq.exchange.matriculas=exchange.matriculas
rabbitmq.routingkey.matricula-registrada=rk.matricula.registrada

# Seguridad JWT (false para pruebas rápidas sin login; true para validar token de auth-service)
app.security.jwt.enabled=false
app.security.jwt.secret=colegio-secret-key-apf3-2026-muy-larga-256bits

# URLs de servicios SOA consumidos por el orquestador
servicios.tesoreria.url=http://localhost:8081
servicios.aula.url=http://localhost:8080
servicios.estudiante.url=http://localhost:8082

springdoc.swagger-ui.path=/swagger-ui.html
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [CorrID:%X{correlationId}] %logger{36} - %msg%n
```
