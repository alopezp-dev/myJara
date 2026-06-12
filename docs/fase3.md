# MyJara — Módulo 3: Historia Clínica Electrónica

> Gestión de episodios clínicos, diagnósticos CIE-10, alergias y notas clínicas  
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
8. [API REST](#8-api-rest)
9. [Ejemplos de uso](#9-ejemplos-de-uso)

---

## 1. Descripción general

El Módulo 3 implementa la Historia Clínica Electrónica (HCE) del paciente. Es el núcleo clínico del sistema — registra cada visita médica como un episodio, los diagnósticos asociados con codificación CIE-10, las alergias conocidas y las notas clínicas del profesional.

**Conceptos clave:**

- **Encounter (Episodio):** cada visita, ingreso o contacto sanitario genera un episodio. Puede estar vinculado a una cita previa del Módulo 2.
- **Condition (Diagnóstico):** diagnóstico registrado en un episodio con código CIE-10 estándar.
- **Allergy (Alergia):** sustancias a las que el paciente es alérgico, con severidad y reacción documentada.
- **ClinicalNote (Nota clínica):** texto libre del profesional — anamnesis, exploración, evolución o informe de alta.
- **Cie10Catalog:** catálogo de códigos CIE-10 para búsqueda y autocompletado.

**Flujo principal:**

```
Crear episodio → Añadir diagnósticos CIE-10 → Añadir notas clínicas → Completar episodio
                                          ↓
                               Registrar alergias (independiente del episodio)
```

---

## 2. Base de datos

### Migración `V3__create_clinical_history_tables.sql`

```sql
-- Episodios clínicos
CREATE TABLE IF NOT EXISTS encounters (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    appointment_id  BIGINT,
    type            VARCHAR(20) NOT NULL DEFAULT 'OUTPATIENT',
    status          VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    start_date      TIMESTAMP NOT NULL DEFAULT NOW(),
    end_date        TIMESTAMP,
    reason          VARCHAR(255),
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (patient_id)      REFERENCES patients(id),
    FOREIGN KEY (professional_id) REFERENCES professionals(id),
    FOREIGN KEY (appointment_id)  REFERENCES appointments(id)
);

-- Diagnósticos con codificación CIE-10
CREATE TABLE IF NOT EXISTS conditions (
    id              BIGSERIAL PRIMARY KEY,
    encounter_id    BIGINT NOT NULL,
    patient_id      BIGINT NOT NULL,
    cie10_code      VARCHAR(10) NOT NULL,
    cie10_desc      VARCHAR(255) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    onset_date      DATE,
    resolved_date   DATE,
    notes           TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (encounter_id) REFERENCES encounters(id),
    FOREIGN KEY (patient_id)   REFERENCES patients(id)
);

-- Alergias del paciente
CREATE TABLE IF NOT EXISTS allergies (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      BIGINT NOT NULL,
    substance       VARCHAR(150) NOT NULL,
    reaction        VARCHAR(255),
    severity        VARCHAR(20) NOT NULL DEFAULT 'MODERATE',
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    onset_date      DATE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);

-- Notas clínicas del episodio
CREATE TABLE IF NOT EXISTS clinical_notes (
    id              BIGSERIAL PRIMARY KEY,
    encounter_id    BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    type            VARCHAR(30) NOT NULL DEFAULT 'EVOLUTION',
    content         TEXT NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (encounter_id)    REFERENCES encounters(id),
    FOREIGN KEY (professional_id) REFERENCES professionals(id)
);

-- Catálogo CIE-10
CREATE TABLE IF NOT EXISTS cie10_catalog (
    code            VARCHAR(10) PRIMARY KEY,
    description     VARCHAR(255) NOT NULL,
    category        VARCHAR(100)
);
```

### Índices

```sql
CREATE INDEX idx_encounters_patient   ON encounters(patient_id);
CREATE INDEX idx_encounters_status    ON encounters(status);
CREATE INDEX idx_conditions_patient   ON conditions(patient_id);
CREATE INDEX idx_conditions_cie10     ON conditions(cie10_code);
CREATE INDEX idx_cie10_description    ON cie10_catalog(description);
```

### Relaciones entre tablas

```
patients      ──< encounters
professionals ──< encounters
appointments  ──< encounters
encounters    ──< conditions
patients      ──< conditions
patients      ──< allergies
encounters    ──< clinical_notes
professionals ──< clinical_notes
```

---

## 3. Entidades JPA

### `Encounter.java` — Episodio clínico

```java
@Entity
@Table(name = "encounters")
public class Encounter {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;  // Opcional — puede no venir de una cita

    @Enumerated(EnumType.STRING)
    private Type type = Type.OUTPATIENT;

    @Enumerated(EnumType.STRING)
    private Status status = Status.IN_PROGRESS;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reason;
    private String notes;

    public enum Type    { OUTPATIENT, EMERGENCY, INPATIENT, HOME_VISIT }
    public enum Status  { IN_PROGRESS, COMPLETED, CANCELLED }
}
```

### `Condition.java` — Diagnóstico CIE-10

```java
@Entity
@Table(name = "conditions")
public class Condition {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id", nullable = false)
    private Encounter encounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private String cie10Code;   // Ej: "J45"
    private String cie10Desc;   // Ej: "Asma"

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    private LocalDate onsetDate;
    private LocalDate resolvedDate;

    public enum Status { ACTIVE, RESOLVED, CHRONIC }
}
```

### `Allergy.java` — Alergia

```java
@Entity
@Table(name = "allergies")
public class Allergy {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private String substance;  // Ej: "Penicilina"
    private String reaction;   // Ej: "Anafilaxia"

    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.MODERATE;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    public enum Severity { MILD, MODERATE, SEVERE }
    public enum Status   { ACTIVE, INACTIVE }
}
```

### `ClinicalNote.java` — Nota clínica

```java
@Entity
@Table(name = "clinical_notes")
public class ClinicalNote {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id", nullable = false)
    private Encounter encounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional;

    @Enumerated(EnumType.STRING)
    private Type type = Type.EVOLUTION;

    private String content;  // Texto libre del profesional

    public enum Type { EVOLUTION, ANAMNESIS, PHYSICAL_EXAM, DISCHARGE }
}
```

### `Cie10Catalog.java` — Catálogo CIE-10

```java
@Entity
@Table(name = "cie10_catalog")
public class Cie10Catalog {

    @Id
    @Column(length = 10)
    private String code;         // PK = código CIE-10 (ej: "J45")

    private String description;  // Ej: "Asma predominantemente alérgica"
    private String category;
}
```

> **Nota:** el ID de `Cie10Catalog` es `String` en lugar de `Long` — el propio código CIE-10 es el identificador único natural (`JpaRepository<Cie10Catalog, String>`).

---

## 4. Repositorios

### `EncounterRepository`

```java
List<Encounter> findByPatientIdOrderByStartDateDesc(Long patientId);
List<Encounter> findByProfessionalIdAndStatus(Long professionalId, Encounter.Status status);
List<Encounter> findByPatientIdAndStatus(Long patientId, Encounter.Status status);
```

### `ConditionRepository`

```java
List<Condition> findByPatientIdOrderByCreatedAtDesc(Long patientId);
List<Condition> findByEncounterId(Long encounterId);
List<Condition> findByPatientIdAndStatus(Long patientId, Condition.Status status);
```

### `AllergyRepository`

```java
List<Allergy> findByPatientIdAndStatus(Long patientId, Allergy.Status status);
List<Allergy> findByPatientId(Long patientId);
```

### `ClinicalNoteRepository`

```java
List<ClinicalNote> findByEncounterIdOrderByCreatedAtDesc(Long encounterId);
List<ClinicalNote> findByEncounterIdAndType(Long encounterId, ClinicalNote.Type type);
```

### `Cie10CatalogRepository`

```java
// Búsqueda por código o descripción (insensible a mayúsculas)
@Query("SELECT c FROM Cie10Catalog c WHERE " +
       "LOWER(c.code) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
       "LOWER(c.description) LIKE LOWER(CONCAT('%', :term, '%'))")
List<Cie10Catalog> search(@Param("term") String term);

List<Cie10Catalog> findByCategory(String category);
```

---

## 5. DTOs

| DTO | Uso |
|---|---|
| `EncounterRequest` | Crear episodio (patientId, professionalId, type, reason) |
| `EncounterResponse` | Respuesta con nombres resueltos y fechas |
| `ConditionRequest` | Añadir diagnóstico (encounterId, cie10Code, cie10Desc) |
| `ConditionResponse` | Diagnóstico con estado y fechas |
| `AllergyRequest` | Registrar alergia (substance, reaction, severity) |
| `AllergyResponse` | Alergia con severidad y estado |
| `ClinicalNoteRequest` | Añadir nota (encounterId, professionalId, type, content) |
| `ClinicalNoteResponse` | Nota con nombre del profesional y timestamp |

---

## 6. Servicios

### `EncounterService`

| Método | Descripción |
|---|---|
| `findByPatient(patientId)` | Historial de episodios del paciente ordenado por fecha |
| `findById(id)` | Obtiene episodio por ID |
| `create(req)` | Abre nuevo episodio, vincula cita si se proporciona |
| `complete(id)` | Cierra el episodio (`status = COMPLETED`, registra `endDate`) |
| `updateNotes(id, notes)` | Actualiza las notas generales del episodio |

### `ClinicalHistoryService`

| Método | Descripción |
|---|---|
| `findConditionsByPatient(id)` | Lista diagnósticos del paciente |
| `addCondition(req)` | Registra diagnóstico CIE-10 en un episodio |
| `resolveCondition(id)` | Marca diagnóstico como resuelto con fecha |
| `findAllergiesByPatient(id)` | Lista alergias del paciente |
| `addAllergy(req)` | Registra nueva alergia |
| `findNotesByEncounter(id)` | Lista notas de un episodio |
| `addNote(req)` | Añade nota clínica al episodio |
| `searchCie10(term)` | Búsqueda en catálogo CIE-10 |

---

## 7. Controladores

### `EncounterController` — `/api/encounters`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/encounters/patient/{id}` | Historial de episodios |
| GET | `/api/encounters/{id}` | Obtiene episodio por ID |
| POST | `/api/encounters` | Abre nuevo episodio |
| PATCH | `/api/encounters/{id}/complete` | Cierra el episodio |
| PATCH | `/api/encounters/{id}/notes` | Actualiza notas |

### `ClinicalHistoryController` — `/api/clinical`

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/clinical/conditions/patient/{id}` | Diagnósticos del paciente |
| POST | `/api/clinical/conditions` | Añade diagnóstico CIE-10 |
| PATCH | `/api/clinical/conditions/{id}/resolve` | Resuelve diagnóstico |
| GET | `/api/clinical/allergies/patient/{id}` | Alergias del paciente |
| POST | `/api/clinical/allergies` | Registra alergia |
| GET | `/api/clinical/notes/encounter/{id}` | Notas de un episodio |
| POST | `/api/clinical/notes` | Añade nota clínica |
| GET | `/api/clinical/cie10/search?term=` | Búsqueda en catálogo CIE-10 |

---

## 8. API REST

### Resumen completo de endpoints del Módulo 3

```
GET    /api/encounters/patient/{patientId}
GET    /api/encounters/{id}
POST   /api/encounters
PATCH  /api/encounters/{id}/complete
PATCH  /api/encounters/{id}/notes

GET    /api/clinical/conditions/patient/{patientId}
POST   /api/clinical/conditions
PATCH  /api/clinical/conditions/{id}/resolve

GET    /api/clinical/allergies/patient/{patientId}
POST   /api/clinical/allergies

GET    /api/clinical/notes/encounter/{encounterId}
POST   /api/clinical/notes

GET    /api/clinical/cie10/search?term={term}
```

---

## 9. Ejemplos de uso

### Abrir episodio clínico

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/encounters" `
  -Method Post `
  -Headers @{"Content-Type"="application/json; charset=utf-8"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{
    "patientId": 1,
    "professionalId": 1,
    "appointmentId": 1,
    "type": "OUTPATIENT",
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
  "type": "OUTPATIENT",
  "status": "IN_PROGRESS",
  "startDate": "2026-06-12T16:51:08",
  "endDate": null,
  "reason": "Revision anual",
  "notes": null
}
```

### Registrar diagnóstico CIE-10

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/clinical/conditions" `
  -Method Post `
  -Headers @{"Content-Type"="application/json; charset=utf-8"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{
    "encounterId": 1,
    "patientId": 1,
    "cie10Code": "J45",
    "cie10Desc": "Asma",
    "onsetDate": "2026-06-12"
  }'))
```

**Respuesta:**
```json
{
  "id": 1,
  "patientId": 1,
  "encounterId": 1,
  "cie10Code": "J45",
  "cie10Desc": "Asma",
  "status": "ACTIVE",
  "onsetDate": "2026-06-12",
  "resolvedDate": null,
  "createdAt": "2026-06-12T16:51:23"
}
```

### Registrar alergia

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/clinical/allergies" `
  -Method Post `
  -Headers @{"Content-Type"="application/json; charset=utf-8"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{
    "patientId": 1,
    "substance": "Penicilina",
    "reaction": "Anafilaxia",
    "severity": "SEVERE"
  }'))
```

### Añadir nota clínica

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/clinical/notes" `
  -Method Post `
  -Headers @{"Content-Type"="application/json; charset=utf-8"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{
    "encounterId": 1,
    "professionalId": 1,
    "type": "EVOLUTION",
    "content": "Paciente refiere disnea de esfuerzo. Se pauta broncodilatador."
  }'))
```

### Completar episodio

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/encounters/1/complete" -Method Patch
```

### Buscar en catálogo CIE-10

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/clinical/cie10/search?term=asma" -Method Get
```

---

## Estado del backend MyJara

| Módulo | Endpoints | Estado |
|---|---|---|
| Módulo 1 — Pacientes | 6 | ✅ Completo |
| Módulo 2 — Agenda y citas | 11 | ✅ Completo |
| Módulo 3 — Historia clínica | 13 | ✅ Completo |
| **Total** | **30** | |

---

## Próximos pasos — Frontend React

- Crear proyecto con Vite + React + Tailwind
- Pantalla de búsqueda de pacientes
- Ficha del paciente: datos, citas, episodios, alergias
- Formulario de nueva cita
- Puesto de trabajo clínico: episodio activo + diagnósticos + notas

---

*Documentación generada el 12/06/2026 — MyJara v0.3.0*