# MyJara — Módulo 4: Prescripción Electrónica

> Receta electrónica, catálogo de medicamentos y detección de interacciones  
> Stack: Spring Boot 3.5.15 · PostgreSQL 16 · JPA · Spring Security

---

## Índice

1. [Descripción general](#1-descripción-general)
2. [Base de datos](#2-base-de-datos)
3. [Entidades JPA](#3-entidades-jpa)
4. [Repositorios](#4-repositorios)
5. [DTOs](#5-dtos)
6. [Servicio](#6-servicio)
7. [Controlador](#7-controlador)
8. [API REST](#8-api-rest)
9. [Integración en el frontend](#9-integración-en-el-frontend)
10. [Ejemplos de uso](#10-ejemplos-de-uso)

---

## 1. Descripción general

El Módulo 4 implementa la prescripción electrónica del SES. Permite a los médicos prescribir medicamentos a pacientes con dosis, frecuencia y duración, y detecta automáticamente interacciones con la medicación activa del paciente antes de confirmar la prescripción.

**Características principales:**
- Catálogo de medicamentos basado en códigos AEMPS
- Búsqueda por nombre, principio activo o código nacional
- Prescripción con dosis, frecuencia, duración y vía de administración
- Detección automática de interacciones medicamentosas
- Visualización de medicación activa en la ficha del paciente
- Control de acceso: solo MEDICO y ADMIN pueden prescribir

**Flujo de prescripción:**
```
Médico selecciona medicamento
        ↓
Sistema comprueba interacciones con medicación activa del paciente
        ↓
Si hay interacciones → alerta con severidad y descripción
        ↓
Médico confirma o cancela la prescripción
        ↓
Prescripción activa visible en la ficha del paciente
```

---

## 2. Base de datos

### Migración `V5__create_prescription_tables.sql`

```sql
-- Catálogo de medicamentos
CREATE TABLE IF NOT EXISTS medications (
    id                    BIGSERIAL PRIMARY KEY,
    national_code         VARCHAR(20)  UNIQUE NOT NULL,
    name                  VARCHAR(255) NOT NULL,
    active_ingredient     VARCHAR(255),
    pharmaceutical_form   VARCHAR(100),
    dosage                VARCHAR(100),
    route                 VARCHAR(50),
    requires_prescription BOOLEAN DEFAULT TRUE,
    active                BOOLEAN NOT NULL DEFAULT TRUE
);

-- Prescripciones
CREATE TABLE IF NOT EXISTS prescriptions (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    encounter_id    BIGINT,
    medication_id   BIGINT NOT NULL,
    dose            VARCHAR(100) NOT NULL,
    frequency       VARCHAR(100) NOT NULL,
    duration        VARCHAR(100),
    route           VARCHAR(50),
    instructions    TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date        DATE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (patient_id)      REFERENCES patients(id),
    FOREIGN KEY (professional_id) REFERENCES professionals(id),
    FOREIGN KEY (encounter_id)    REFERENCES encounters(id),
    FOREIGN KEY (medication_id)   REFERENCES medications(id)
);

-- Interacciones medicamentosas
CREATE TABLE IF NOT EXISTS drug_interactions (
    id              BIGSERIAL PRIMARY KEY,
    medication_a_id BIGINT NOT NULL,
    medication_b_id BIGINT NOT NULL,
    severity        VARCHAR(20) NOT NULL DEFAULT 'MODERATE',
    description     TEXT NOT NULL,
    FOREIGN KEY (medication_a_id) REFERENCES medications(id),
    FOREIGN KEY (medication_b_id) REFERENCES medications(id)
);
```

### Medicamentos de muestra incluidos

| Código | Nombre | Principio activo | Vía |
|---|---|---|---|
| 656789 | Amoxicilina 500mg cápsulas | Amoxicilina | Oral |
| 723451 | Ibuprofeno 600mg comprimidos | Ibuprofeno | Oral |
| 489234 | Omeprazol 20mg cápsulas | Omeprazol | Oral |
| 534127 | Paracetamol 1g comprimidos | Paracetamol | Oral |
| 612890 | Atorvastatina 20mg comprimidos | Atorvastatina | Oral |
| 778234 | Metformina 850mg comprimidos | Metformina | Oral |
| 845671 | Salbutamol 100mcg inhalador | Salbutamol | Inhalatoria |
| 923456 | Enalapril 10mg comprimidos | Enalapril | Oral |
| 156789 | Lorazepam 1mg comprimidos | Lorazepam | Oral |
| 267834 | Azitromicina 500mg comprimidos | Azitromicina | Oral |

### Interacción de ejemplo

**Ibuprofeno + Enalapril** (MODERATE): Los AINEs pueden reducir el efecto antihipertensivo de los IECAs y aumentar el riesgo de insuficiencia renal aguda.

---

## 3. Entidades JPA

### `Medication.java`

```java
@Entity
@Table(name = "medications")
public class Medication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nationalCode;  // Código nacional AEMPS
    private String name;
    private String activeIngredient;
    private String pharmaceuticalForm;
    private String dosage;
    private String route;
    private Boolean requiresPrescription = true;
    private Boolean active = true;
}
```

### `Prescription.java`

```java
@Entity
@Table(name = "prescriptions")
public class Prescription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    private Professional professional;

    @ManyToOne(fetch = FetchType.LAZY)
    private Encounter encounter;  // Opcional — episodio asociado

    @ManyToOne(fetch = FetchType.LAZY)
    private Medication medication;

    private String dose;          // Ej: "600mg"
    private String frequency;     // Ej: "Cada 8 horas"
    private String duration;      // Ej: "5 días"
    private String route;
    private String instructions;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    private LocalDate startDate;
    private LocalDate endDate;

    public enum Status { ACTIVE, COMPLETED, CANCELLED, SUSPENDED }
}
```

### `DrugInteraction.java`

```java
@Entity
@Table(name = "drug_interactions")
public class DrugInteraction {
    @ManyToOne(fetch = FetchType.LAZY)
    private Medication medicationA;

    @ManyToOne(fetch = FetchType.LAZY)
    private Medication medicationB;

    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.MODERATE;

    private String description;

    public enum Severity { MILD, MODERATE, SEVERE }
}
```

---

## 4. Repositorios

### `MedicationRepository`

```java
// Búsqueda por nombre, principio activo o código nacional
@Query("SELECT m FROM Medication m WHERE " +
       "LOWER(m.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
       "LOWER(m.activeIngredient) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
       "m.nationalCode LIKE CONCAT('%', :term, '%')")
List<Medication> search(@Param("term") String term);
```

### `PrescriptionRepository`

```java
List<Prescription> findByPatientIdOrderByCreatedAtDesc(Long patientId);
List<Prescription> findByPatientIdAndStatus(Long patientId, Prescription.Status status);

// Medicación activa para detección de interacciones
@Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId AND p.status = 'ACTIVE'")
List<Prescription> findActiveMedication(@Param("patientId") Long patientId);
```

### `DrugInteractionRepository`

```java
// Busca interacciones entre el nuevo medicamento y cualquier medicamento activo del paciente
@Query("SELECT i FROM DrugInteraction i WHERE " +
       "(i.medicationA.id = :medId OR i.medicationB.id = :medId) AND " +
       "(i.medicationA.id IN :activeMedIds OR i.medicationB.id IN :activeMedIds)")
List<DrugInteraction> findInteractions(Long medId, List<Long> activeMedIds);
```

---

## 5. DTOs

| DTO | Uso |
|---|---|
| `PrescriptionRequest` | Body de creación de prescripción |
| `PrescriptionResponse` | Respuesta con datos completos del medicamento y profesional |
| `InteractionWarning` | Alerta de interacción con severidad y descripción |

---

## 6. Servicio

### `PrescriptionService` — métodos principales

| Método | Descripción |
|---|---|
| `findByPatient(patientId)` | Historial completo de prescripciones |
| `findActiveByPatient(patientId)` | Solo prescripciones activas |
| `checkInteractions(patientId, medicationId)` | Detecta interacciones con medicación activa |
| `create(req)` | Crea nueva prescripción |
| `updateStatus(id, status)` | Cambia estado (CANCELLED, COMPLETED, SUSPENDED) |
| `searchMedications(term)` | Búsqueda en catálogo |

**Lógica de detección de interacciones:**

```java
public List<InteractionWarning> checkInteractions(Long patientId, Long medicationId) {
    // 1. Obtiene medicación activa del paciente
    List<Prescription> active = prescriptionRepository.findActiveMedication(patientId);
    if (active.isEmpty()) return List.of();

    // 2. Extrae IDs de medicamentos activos
    List<Long> activeMedIds = active.stream()
            .map(p -> p.getMedication().getId()).toList();

    // 3. Busca interacciones conocidas en la tabla drug_interactions
    return drugInteractionRepository.findInteractions(medicationId, activeMedIds)
            .stream().map(InteractionWarning::from).toList();
}
```

---

## 7. Controlador

### Control de acceso por rol

```java
@GetMapping("/patient/{patientId}")
@PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO')")
public ResponseEntity<List<PrescriptionResponse>> findByPatient(...) { }

@PostMapping
@PreAuthorize("hasAnyRole('ADMIN','MEDICO')")  // Solo médicos pueden prescribir
public ResponseEntity<PrescriptionResponse> create(...) { }

@GetMapping("/interactions")
@PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
public ResponseEntity<List<InteractionWarning>> checkInteractions(...) { }
```

---

## 8. API REST

```
GET    /api/prescriptions/patient/{patientId}           → Historial completo
GET    /api/prescriptions/patient/{patientId}/active    → Medicación activa
GET    /api/prescriptions/interactions?patientId=&medicationId=  → Interacciones
GET    /api/prescriptions/medications/search?term=      → Buscar medicamentos
POST   /api/prescriptions                               → Crear prescripción
PATCH  /api/prescriptions/{id}/status?status=           → Cambiar estado
```

---

## 9. Integración en el frontend

La medicación activa se muestra en la ficha del paciente (`PatientDetailPage.jsx`) con nombre, dosis, frecuencia, duración y profesional prescriptor.

```jsx
const { data: prescriptions = [] } = useQuery({
  queryKey: ['prescriptions', id],
  queryFn: () => getPrescriptionsByPatient(id).then(r => r.data),
  enabled: !!patient
})
```

**Funciones en `client.js`:**

```js
export const getPrescriptionsByPatient = (id) =>
  client.get(`/prescriptions/patient/${id}/active`)

export const searchMedications = (term) =>
  client.get(`/prescriptions/medications/search?term=${term}`)

export const checkInteractions = (patientId, medicationId) =>
  client.get(`/prescriptions/interactions?patientId=${patientId}&medicationId=${medicationId}`)

export const createPrescription = (data) =>
  client.post('/prescriptions', data)
```

---

## 10. Ejemplos de uso

### Login y obtener token

```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method Post `
  -Headers @{"Content-Type"="application/json; charset=utf-8"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{"username":"mlopez","password":"medico123"}'))
$token = $response.token
```

### Buscar medicamento

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/prescriptions/medications/search?term=ibuprofeno" `
  -Method Get `
  -Headers @{"Authorization"="Bearer $token"}
```

### Crear prescripción

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/prescriptions" `
  -Method Post `
  -Headers @{"Content-Type"="application/json; charset=utf-8"; "Authorization"="Bearer $token"} `
  -Body ([System.Text.Encoding]::UTF8.GetBytes('{
    "patientId": 1,
    "professionalId": 1,
    "medicationId": 2,
    "dose": "600mg",
    "frequency": "Cada 8 horas",
    "duration": "5 dias"
  }'))
```

**Respuesta:**
```json
{
  "id": 1,
  "patientId": 1,
  "professionalId": 1,
  "professionalName": "Maria Lopez",
  "medicationId": 2,
  "medicationName": "Ibuprofeno 600mg comprimidos",
  "activeIngredient": "Ibuprofeno",
  "pharmaceuticalForm": "Comprimidos",
  "dose": "600mg",
  "frequency": "Cada 8 horas",
  "duration": "5 dias",
  "route": "Oral",
  "status": "ACTIVE",
  "startDate": "2026-06-13"
}
```

### Comprobar interacciones

```powershell
# Comprueba si Enalapril (id=8) interactúa con la medicación activa del paciente 1
Invoke-RestMethod -Uri "http://localhost:8080/api/prescriptions/interactions?patientId=1&medicationId=8" `
  -Method Get `
  -Headers @{"Authorization"="Bearer $token"}
```

**Respuesta:**
```json
{
  "interactionId": 1,
  "medicationA": "Ibuprofeno 600mg comprimidos",
  "medicationB": "Enalapril 10mg comprimidos",
  "severity": "MODERATE",
  "description": "Los AINEs pueden reducir el efecto antihipertensivo de los IECAs y aumentar el riesgo de insuficiencia renal aguda."
}
```

---

## Estado del proyecto MyJara

| Módulo | Estado |
|---|---|
| Pacientes | ✅ |
| Agenda y citas | ✅ |
| Historia clínica electrónica | ✅ |
| Frontend React + Auth JWT | ✅ |
| Prescripción electrónica | ✅ |
| Urgencias y triaje | 🔜 |
| Hospitalización | 🔜 |
| CI/CD y despliegue | 🔜 |

---

*Documentación generada el 13/06/2026 — MyJara v0.6.0*