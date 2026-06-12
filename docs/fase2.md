# MyJara — Módulo 2: Agenda y Citas

> Gestión de profesionales sanitarios, agendas y citas médicas  
> Stack: Spring Boot 3.5.15 · PostgreSQL 16 · JPA · Flyway

---

## Índice

1. [Descripción general](#1-descripción-general)
2. [Base de datos](#2-base-de-datos)
3. [Entidades JPA](#3-entidades-jpa)
4. [Repositorios](#4-repositorios)
5. [DTOs](#5-dtos)
6. [Servicios](#6-servicios)
7. [Controladores](#7-controladores)
8. [Manejo de excepciones](#8-manejo-de-excepciones)
9. [API REST](#9-api-rest)
10. [Ejemplos de uso](#10-ejemplos-de-uso)

---

## 1. Descripción general

El Módulo 2 implementa la gestión completa de la agenda sanitaria. Permite registrar profesionales del SES, definir sus agendas semanales con slots de tiempo configurables, y crear citas médicas con detección automática de solapamientos.

**Flujo principal:**

```
Crear profesional → Definir agenda → Crear cita → Gestionar estado
```

**Lógica de negocio clave:**
- La hora de fin de una cita se calcula automáticamente a partir de `slotMinutes` de la agenda
- No se pueden crear dos citas para el mismo profesional en el mismo horario
- Las bajas son lógicas — ningún dato se elimina físicamente

---

## 2. Base de datos

### Migración `V2__create_appointments_table.sql`

```sql
CREATE TABLE IF NOT EXISTS professionals (
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    specialty       VARCHAR(100),
    license_number  VARCHAR(50) UNIQUE NOT NULL,
    email           VARCHAR(150),
    phone           VARCHAR(20),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS agendas (
    id              BIGSERIAL PRIMARY KEY,
    professional_id BIGINT NOT NULL,
    center          VARCHAR(150) NOT NULL,
    day_of_week     INTEGER NOT NULL,       -- 1=Lunes ... 7=Domingo
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    slot_minutes    INTEGER NOT NULL DEFAULT 15,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (professional_id) REFERENCES professionals(id)
);

CREATE TABLE IF NOT EXISTS appointments (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    agenda_id       BIGINT NOT NULL,
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    reason          VARCHAR(255),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (patient_id)      REFERENCES patients(id),
    FOREIGN KEY (professional_id) REFERENCES professionals(id),
    FOREIGN KEY (agenda_id)       REFERENCES agendas(id)
);
```

### Índices

```sql
CREATE INDEX idx_appointments_patient      ON appointments(patient_id);
CREATE INDEX idx_appointments_professional ON appointments(professional_id);
CREATE INDEX idx_appointments_start_time   ON appointments(start_time);
CREATE INDEX idx_appointments_status       ON appointments(status);
```

### Relaciones entre tablas

```
professionals ──< agendas
professionals ──< appointments
patients      ──< appointments
agendas       ──< appointments
```

---

## 3. Entidades JPA

### `Professional.java`

Representa a un profesional sanitario del SES (médico, enfermero, etc.).

```java
@Entity
@Table(name = "professionals")
public class Professional {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String specialty;

    @Column(unique = true, nullable = false)
    private String licenseNumber;   // Número de colegiado

    private String email;
    private String phone;
    private Boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
```

### `Agenda.java`

Define el horario semanal de un profesional en un centro sanitario.

```java
@Entity
@Table(name = "agendas")
public class Agenda {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    private String center;
    private Integer dayOfWeek;      // 1=Lunes, 7=Domingo
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotMinutes = 15;
    private Boolean active = true;
}
```

**Decisión de diseño:** `FetchType.LAZY` en la relación `@ManyToOne` — JPA no carga el profesional completo hasta que se accede a él, evitando consultas innecesarias en listados de agendas.

### `Appointment.java`

Representa una cita médica entre un paciente y un profesional.

```java
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id")
    private Professional professional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenda_id")
    private Agenda agenda;

    private LocalDateTime startTime;
    private LocalDateTime endTime;   // Calculado automáticamente

    @Enumerated(EnumType.STRING)
    private Status status = Status.SCHEDULED;

    private String reason;
    private String notes;

    public enum Status {
        SCHEDULED,    // Programada
        CONFIRMED,    // Confirmada por el paciente
        IN_PROGRESS,  // En consulta
        COMPLETED,    // Completada
        CANCELLED,    // Cancelada
        NO_SHOW       // Paciente no se presentó
    }
}
```

---

## 4. Repositorios

### `ProfessionalRepository`

```java
Optional<Professional> findByLicenseNumber(String licenseNumber);
List<Professional> findByActiveTrue();
List<Professional> findBySpecialtyAndActiveTrue(String specialty);
```

### `AgendaRepository`

```java
List<Agenda> findByProfessionalIdAndActiveTrue(Long professionalId);
List<Agenda> findByDayOfWeekAndActiveTrue(Integer dayOfWeek);
```

### `AppointmentRepository`

```java
List<Appointment> findByPatientId(Long patientId);

List<Appointment> findByProfessionalIdAndStartTimeBetween(
    Long professionalId, LocalDateTime from, LocalDateTime to);

// Detección de solapamiento — consulta JPQL personalizada
@Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE " +
       "a.professional.id = :professionalId AND " +
       "a.status NOT IN ('CANCELLED', 'NO_SHOW') AND " +
       "a.startTime < :endTime AND a.endTime > :startTime")
boolean existsOverlap(Long professionalId, LocalDateTime startTime, LocalDateTime endTime);
```

**Nota sobre `existsOverlap`:** el algoritmo de detección de solapamiento comprueba si `startTime < endTimeSolicitado AND endTime > startTimeSolicitado`. Esta condición cubre todos los casos posibles de solapamiento parcial o total. Las citas canceladas y no-show se excluyen de la comprobación.

---

## 5. DTOs

| DTO | Uso |
|---|---|
| `ProfessionalRequest` | Body de creación/edición de profesional |
| `ProfessionalResponse` | Respuesta JSON del profesional |
| `AgendaRequest` | Body de creación de agenda (acepta `professionalId`) |
| `AppointmentRequest` | Body de creación de cita |
| `AppointmentResponse` | Respuesta JSON de la cita con nombres resueltos |

**Por qué `AgendaRequest` en lugar de la entidad directa:**  
La entidad `Agenda` tiene una relación `@ManyToOne` con `Professional` — no se puede deserializar directamente desde `{"professionalId": 1}`. El DTO recibe el `Long professionalId` y el servicio/controller resuelve la entidad antes de persistir.

---

## 6. Servicios

### `ProfessionalService`

| Método | Descripción |
|---|---|
| `findAll()` | Lista profesionales activos |
| `findById(id)` | Obtiene profesional por ID |
| `findBySpecialty(specialty)` | Filtra por especialidad |
| `create(req)` | Crea nuevo profesional |
| `delete(id)` | Baja lógica (`active = false`) |

### `AppointmentService`

| Método | Descripción |
|---|---|
| `findByPatient(patientId)` | Historial de citas de un paciente |
| `findByProfessionalAndRange(id, from, to)` | Agenda de un profesional en un rango de fechas |
| `create(req)` | Crea cita con validación de solapamiento |
| `updateStatus(id, status)` | Cambia el estado de la cita |
| `cancel(id)` | Cancela la cita (estado `CANCELLED`) |

**Lógica de creación de cita:**

```java
// 1. Resolver entidades por ID
Patient patient = patientRepository.findById(req.getPatientId())...
Professional professional = professionalRepository.findById(req.getProfessionalId())...
Agenda agenda = agendaRepository.findById(req.getAgendaId())...

// 2. Calcular hora de fin
LocalDateTime endTime = req.getStartTime().plusMinutes(agenda.getSlotMinutes());

// 3. Comprobar solapamiento
boolean overlap = appointmentRepository.existsOverlap(
    professional.getId(), req.getStartTime(), endTime);
if (overlap) throw new RuntimeException("El profesional ya tiene una cita en ese horario");

// 4. Persistir
```

---

## 7. Controladores

### `ProfessionalController` — `/api/professionals`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/professionals` | Lista todos los activos |
| GET | `/api/professionals/{id}` | Obtiene por ID |
| GET | `/api/professionals/specialty/{specialty}` | Filtra por especialidad |
| POST | `/api/professionals` | Crea profesional |
| DELETE | `/api/professionals/{id}` | Baja lógica |

### `AgendaController` — `/api/agendas`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/agendas/professional/{id}` | Agendas de un profesional |
| POST | `/api/agendas` | Crea agenda |

### `AppointmentController` — `/api/appointments`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/appointments/patient/{id}` | Citas de un paciente |
| GET | `/api/appointments/professional/{id}?from=&to=` | Agenda del profesional en rango |
| POST | `/api/appointments` | Crea cita |
| PATCH | `/api/appointments/{id}/status?status=` | Cambia estado |
| DELETE | `/api/appointments/{id}` | Cancela cita |

---

## 8. Manejo de excepciones

`GlobalExceptionHandler` convierte excepciones Java en respuestas HTTP semánticas:

| Excepción | Código HTTP | Cuándo |
|---|---|---|
| `RuntimeException` con "no encontrado" | 404 Not Found | Entidad no existe |
| `RuntimeException` con "ya tiene una cita" | 409 Conflict | Solapamiento de citas |
| `RuntimeException` genérica | 500 Internal Server Error | Error inesperado |
| `MethodArgumentNotValidException` | 400 Bad Request | Validación de DTO fallida |

**Formato de respuesta de error:**

```json
{
  "timestamp": "2026-06-11T22:45:00",
  "status": 409,
  "error": "Conflict",
  "message": "El profesional ya tiene una cita en ese horario"
}
```

---

## 9. API REST

### Resumen de endpoints

```
GET    /api/professionals
GET    /api/professionals/{id}
GET    /api/professionals/specialty/{specialty}
POST   /api/professionals
DELETE /api/professionals/{id}

GET    /api/agendas/professional/{professionalId}
POST   /api/agendas

GET    /api/appointments/patient/{patientId}
GET    /api/appointments/professional/{professionalId}?from=&to=
POST   /api/appointments
PATCH  /api/appointments/{id}/status?status=
DELETE /api/appointments/{id}
```

---

## 10. Ejemplos de uso

### Crear profesional

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/professionals" `
  -Method Post `
  -Headers @{"Content-Type"="application/json; charset=utf-8"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{
    "firstName": "Maria",
    "lastName": "Lopez",
    "specialty": "Medicina General",
    "licenseNumber": "EX-001",
    "email": "maria@ses.es"
  }'))
```

### Crear agenda

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/agendas" `
  -Method Post `
  -Headers @{"Content-Type"="application/json; charset=utf-8"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{
    "professionalId": 1,
    "center": "Centro de Salud Caceres Norte",
    "dayOfWeek": 1,
    "startTime": "09:00:00",
    "endTime": "14:00:00",
    "slotMinutes": 15
  }'))
```

### Crear cita

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/appointments" `
  -Method Post `
  -Headers @{"Content-Type"="application/json; charset=utf-8"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{
    "patientId": 1,
    "professionalId": 1,
    "agendaId": 1,
    "startTime": "2026-06-16T09:00:00",
    "reason": "Revision anual"
  }'))
```

**Respuesta:**

```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "Juan Garcia",
  "professionalId": 1,
  "professionalName": "Maria Lopez",
  "startTime": "2026-06-16T09:00:00",
  "endTime": "2026-06-16T09:15:00",
  "status": "SCHEDULED",
  "reason": "Revision anual",
  "notes": null
}
```

### Cambiar estado de cita

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/appointments/1/status?status=CONFIRMED" `
  -Method Patch
```

### Cancelar cita

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/appointments/1" -Method Delete
```

---

## Próximos pasos — Módulo 3: Historia Clínica Electrónica

- Tablas: `encounters`, `conditions`, `allergies`, `clinical_notes`
- Codificación CIE-10 para diagnósticos
- API REST: `/api/encounters`, `/api/conditions`
- Vinculación con episodios de cita del Módulo 2

---

*Documentación generada el 11/06/2026 — MyJara v0.2.0*
