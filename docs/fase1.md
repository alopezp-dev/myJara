# MyJara — Documentación Fase 1

> Sistema de información sanitaria regional para Extremadura  
> Stack: Java 17 · Spring Boot 3.5.15 · PostgreSQL 16 · Flyway · Docker

---

## Índice

1. [Descripción general](#1-descripción-general)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Estructura del proyecto](#3-estructura-del-proyecto)
4. [Infraestructura](#4-infraestructura)
5. [Base de datos](#5-base-de-datos)
6. [Módulo 1 — Gestión de pacientes](#6-módulo-1--gestión-de-pacientes)
7. [API REST](#7-api-rest)
8. [Configuración](#8-configuración)
9. [Cómo arrancar el proyecto](#9-cómo-arrancar-el-proyecto)

---

## 1. Descripción general

MyJara es una réplica del sistema JARA (sistema de información sanitaria del Servicio Extremeño de Salud). El objetivo es construir un HIS (Hospital Information System) completo que gestione pacientes, citas, historia clínica electrónica, prescripción electrónica, urgencias y hospitalización a escala regional.

La Fase 1 implementa la infraestructura base del proyecto y el primer módulo funcional: gestión de pacientes con una API REST completa.

---

## 2. Stack tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 17 |
| Framework backend | Spring Boot | 3.5.15 |
| Persistencia | Spring Data JPA + Hibernate | 6.6.x |
| Base de datos | PostgreSQL | 16 |
| Migraciones SQL | Flyway | 11.7.x |
| Contenedores | Docker + Docker Compose | — |
| Seguridad | Spring Security | 6.5.x |
| Reducción de boilerplate | Lombok | 1.18.x |
| Build | Maven | 3.x |

---

## 3. Estructura del proyecto

```
myJara/
└── backend/
    ├── docker-compose.yml
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/myjara/backend/
        │   │   ├── BackendApplication.java       ← Entry point
        │   │   ├── config/
        │   │   │   └── SecurityConfig.java       ← Configuración Spring Security
        │   │   ├── controller/
        │   │   │   └── PatientController.java    ← Endpoints REST
        │   │   ├── dto/
        │   │   │   ├── PatientRequest.java       ← Body de entrada
        │   │   │   └── PatientResponse.java      ← Body de salida
        │   │   ├── entity/
        │   │   │   └── Patient.java              ← Entidad JPA
        │   │   ├── repository/
        │   │   │   └── PatientRepository.java    ← Acceso a datos
        │   │   └── service/
        │   │       └── PatientService.java       ← Lógica de negocio
        │   └── resources/
        │       ├── application.yml
        │       └── db/migration/
        │           ├── V1__create_patients_table.sql
        │           └── V2__create_appointments_table.sql
        └── test/
            └── java/com/myjara/backend/
```

---

## 4. Infraestructura

### Docker Compose

PostgreSQL corre en un contenedor Docker aislado. Se usa el puerto `5433` en el host para evitar conflictos con instalaciones locales de PostgreSQL.

```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:16
    container_name: myjara-postgres
    environment:
      POSTGRES_DB: myjara
      POSTGRES_USER: myjara
      POSTGRES_PASSWORD: myjara
      POSTGRES_HOST_AUTH_METHOD: md5
    ports:
      - "5433:5432"
    volumes:
      - myjara-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U myjara"]
      interval: 10s
      timeout: 5s
      retries: 5
```

**Comandos útiles:**

```bash
# Arrancar la base de datos
docker compose up -d

# Ver estado del contenedor
docker compose ps

# Conectarse directamente a PostgreSQL
docker exec -it myjara-postgres psql -U myjara -d myjara

# Parar y eliminar datos
docker compose down -v
```

### Flyway — Migraciones SQL

Flyway gestiona el esquema de la base de datos con archivos SQL versionados. Los archivos siguen el formato `V{número}__{descripción}.sql` y se ejecutan automáticamente al arrancar la aplicación.

| Versión | Archivo | Descripción |
|---|---|---|
| V1 | `V1__create_patients_table.sql` | Tabla `patients` con índices |
| V2 | `V2__create_appointments_table.sql` | Tablas `professionals`, `agendas`, `appointments` |

> **Importante:** `ddl-auto: validate` en JPA significa que Flyway es el único que gestiona el esquema. JPA solo valida que las entidades coincidan con las tablas existentes.

---

## 5. Base de datos

### Tabla `patients`

```sql
CREATE TABLE IF NOT EXISTS patients (
    id              BIGSERIAL PRIMARY KEY,
    health_card     VARCHAR(20)  UNIQUE NOT NULL,   -- CIP (tarjeta sanitaria)
    dni             VARCHAR(15)  UNIQUE,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    birth_date      DATE         NOT NULL,
    gender          VARCHAR(10),                    -- MALE | FEMALE | OTHER
    phone           VARCHAR(20),
    email           VARCHAR(150),
    address         VARCHAR(255),
    municipality    VARCHAR(100),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_patients_health_card ON patients(health_card);
CREATE INDEX idx_patients_dni ON patients(dni);
CREATE INDEX idx_patients_last_name ON patients(last_name);
```

> **Nota de diseño:** El campo `active` implementa baja lógica. En sistemas sanitarios nunca se eliminan registros de pacientes — se desactivan. Esto cumple con la Ley 41/2002 de autonomía del paciente.

---

## 6. Módulo 1 — Gestión de pacientes

### Arquitectura en capas

El módulo sigue el patrón estándar de Spring Boot en 4 capas:

```
PatientController  →  PatientService  →  PatientRepository  →  PostgreSQL
     (REST)            (negocio)           (JPA/SQL)             (datos)
```

### `Patient.java` — Entidad JPA

Mapea la tabla `patients` a un objeto Java. Usa anotaciones JPA para definir el esquema y Lombok para eliminar getters/setters manuales.

```java
@Entity
@Table(name = "patients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "health_card", unique = true, nullable = false, length = 20)
    private String healthCard;

    // ... resto de campos

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Gender { MALE, FEMALE, OTHER }
}
```

**Decisiones de diseño:**
- `LocalDate` para `birthDate` — sin hora ni zona horaria
- `@Enumerated(EnumType.STRING)` — guarda `"MALE"` en lugar de `0` para hacer la BD legible
- `@PrePersist` / `@PreUpdate` — rellena fechas automáticamente sin intervención del servicio
- `updatable = false` en `createdAt` — la fecha de creación es inmutable

### `PatientRepository.java` — Repositorio JPA

Interfaz que extiende `JpaRepository`. Spring genera automáticamente el SQL a partir del nombre de los métodos.

```java
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByHealthCard(String healthCard);
    Optional<Patient> findByDni(String dni);
    List<Patient> findByActiveTrue();

    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "p.healthCard LIKE CONCAT('%', :term, '%') OR " +
           "p.dni LIKE CONCAT('%', :term, '%')")
    List<Patient> search(@Param("term") String term);
}
```

**Métodos heredados de `JpaRepository` sin escribir código:**
- `findAll()`, `findById()`, `save()`, `deleteById()`, `count()`, `existsById()`

### `PatientService.java` — Servicio

Contiene la lógica de negocio. Usa `@Transactional` en operaciones de escritura y convierte entre entidades JPA y DTOs.

```java
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public List<PatientResponse> findAll() { ... }
    public PatientResponse findById(Long id) { ... }
    public List<PatientResponse> search(String term) { ... }

    @Transactional
    public PatientResponse create(PatientRequest req) { ... }

    @Transactional
    public PatientResponse update(Long id, PatientRequest req) { ... }

    @Transactional
    public void delete(Long id) {
        // Baja lógica — nunca se borran datos sanitarios
        patient.setActive(false);
        patientRepository.save(patient);
    }
}
```

### DTOs

Los DTOs separan la API pública de la entidad JPA interna, evitando exponer campos sensibles o internos.

**`PatientRequest`** — lo que recibe la API (con validaciones):
- `@NotBlank` en campos obligatorios
- `@Size(max = N)` para longitudes máximas
- `@JsonFormat(pattern = "yyyy-MM-dd")` para fechas

**`PatientResponse`** — lo que devuelve la API:
- Método estático `from(Patient p)` para convertir entidad a DTO
- No expone `createdAt` ni `updatedAt`

---

## 7. API REST

Base URL: `http://localhost:8080/api`

### Endpoints de pacientes

| Método | Endpoint | Descripción | Código éxito |
|---|---|---|---|
| `GET` | `/patients` | Lista todos los pacientes activos | 200 |
| `GET` | `/patients/{id}` | Obtiene un paciente por ID | 200 |
| `GET` | `/patients/search?term=` | Búsqueda por nombre, apellido, DNI o tarjeta | 200 |
| `POST` | `/patients` | Crea un nuevo paciente | 201 |
| `PUT` | `/patients/{id}` | Actualiza datos de un paciente | 200 |
| `DELETE` | `/patients/{id}` | Baja lógica de un paciente | 204 |

### Ejemplos de uso

**Crear paciente:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/patients" `
  -Method Post `
  -Headers @{"Content-Type"="application/json; charset=utf-8"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{
    "healthCard": "EX-123456789",
    "firstName": "Juan",
    "lastName": "Garcia",
    "birthDate": "1985-03-15",
    "dni": "12345678A",
    "gender": "MALE",
    "phone": "600123456",
    "municipality": "Caceres"
  }'))
```

**Respuesta:**
```json
{
  "id": 1,
  "healthCard": "EX-123456789",
  "dni": "12345678A",
  "firstName": "Juan",
  "lastName": "Garcia",
  "birthDate": "1985-03-15",
  "gender": "MALE",
  "phone": "600123456",
  "email": null,
  "address": null,
  "municipality": "Caceres",
  "active": true
}
```

**Buscar paciente:**
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/patients/search?term=Juan" -Method Get
```

---

## 8. Configuración

### `application.yml`

```yaml
spring:
  application:
    name: myjara-backend
  datasource:
    url: jdbc:postgresql://localhost:5433/myjara
    username: myjara
    password: myjara
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  jackson:
    serialization:
      write-dates-as-timestamps: false

server:
  port: 8080

myjara:
  jwt:
    secret: clave-secreta-cambiar-en-produccion
    expiration-ms: 86400000

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### `SecurityConfig.java`

Spring Security está configurado en modo permisivo durante el desarrollo. **Debe actualizarse antes de producción** con autenticación JWT y roles por perfil profesional.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
```

> ⚠️ **Pendiente en Fase 2:** Implementar autenticación OAuth2 + JWT con roles `MEDICO`, `ENFERMERO`, `ADMIN`, `FARMACEUTICO`.

---

## 9. Cómo arrancar el proyecto

### Requisitos previos

- Java 17+
- Maven 3.x
- Docker Desktop

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/tuusuario/myJara.git
cd myJara/backend

# 2. Arrancar PostgreSQL
docker compose up -d

# 3. Esperar a que esté healthy
docker compose ps

# 4. Arrancar la aplicación
./mvnw spring-boot:run

# 5. Verificar que funciona
curl http://localhost:8080/actuator/health
# Respuesta esperada: {"status":"UP"}
```

### Verificar la base de datos

```bash
docker exec -it myjara-postgres psql -U myjara -d myjara
# Dentro de psql:
\dt                          -- listar tablas
SELECT * FROM patients;      -- ver pacientes
SELECT * FROM flyway_schema_history;  -- ver migraciones aplicadas
```

---

## Próximos pasos — Módulo 2: Agenda y citas

- Entidades: `Professional`, `Agenda`, `Appointment`
- API REST: `/api/appointments`, `/api/professionals`
- Lógica: gestión de slots, lista de espera, notificaciones
- Frontend: calendario semanal en React

---

*Documentación generada el 11/06/2026 — MyJara v0.1.0*