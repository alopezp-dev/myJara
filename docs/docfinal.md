# MyJara — Documentación Completa del Proyecto

> Sistema de Información Sanitaria para el Servicio Extremeño de Salud  
> Réplica del sistema JARA desarrollada con Java 17, Spring Boot, PostgreSQL y React

---

## Índice

1. [Descripción general](#1-descripción-general)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Arquitectura del sistema](#3-arquitectura-del-sistema)
4. [Módulos implementados](#4-módulos-implementados)
5. [Base de datos](#5-base-de-datos)
6. [API REST](#6-api-rest)
7. [Seguridad y roles](#7-seguridad-y-roles)
8. [Frontend](#8-frontend)
9. [CI/CD](#9-cicd)
10. [Arrancar en local](#10-arrancar-en-local)
11. [Usuarios del sistema](#11-usuarios-del-sistema)

---

## 1. Descripción general

MyJara es una réplica funcional del sistema JARA (Junta de Extremadura - Atención en Red Asistencial), el sistema de información sanitaria del Servicio Extremeño de Salud (SES). Desarrollado como proyecto de portafolio para demostrar competencias en desarrollo fullstack con tecnologías enterprise.

**Funcionalidades principales:**
- Gestión de pacientes con búsqueda fonética
- Agenda y citas médicas con slots disponibles en tiempo real
- Historia clínica electrónica con diagnósticos CIE-10
- Prescripción electrónica con detección de interacciones
- Urgencias y triaje Manchester
- Hospitalización con gestión de camas
- Autenticación JWT con roles diferenciados por perfil sanitario
- CI/CD con GitHub Actions

---

## 2. Stack tecnológico

### Backend
| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.5.15 | Framework principal |
| Spring Security | 6.5 | Autenticación y autorización |
| JJWT | 0.12.5 | JSON Web Tokens |
| Spring Data JPA | 3.5 | Persistencia |
| Hibernate | 6.6 | ORM |
| Flyway | 11.7 | Migraciones de BD |
| Lombok | 1.18 | Reducción de boilerplate |
| Maven | 3.9 | Build tool |

### Frontend
| Tecnología | Versión | Uso |
|---|---|---|
| React | 19 | Framework UI |
| Vite | 6.x | Build tool y dev server |
| Tailwind CSS | 4.x | Estilos utility-first |
| React Router | 7.x | Navegación SPA |
| TanStack Query | 5.x | Fetching y caché |
| Axios | 1.x | Cliente HTTP |

### Infraestructura
| Tecnología | Uso |
|---|---|
| PostgreSQL 16 | Base de datos principal |
| Docker / Docker Compose | Contenedor de BD en desarrollo |
| GitHub Actions | CI/CD pipeline |

---

## 3. Arquitectura del sistema

```
┌─────────────────────────────────────────────────────┐
│                   FRONTEND (React)                   │
│  LoginPage → HomePage → PatientsPage → PatientDetail │
│  NewAppointmentPage (6 pasos al estilo JARA)         │
│  Axios + TanStack Query + AuthContext (JWT)          │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP + JWT Bearer Token
                       │ Proxy Vite → localhost:8080
┌──────────────────────▼──────────────────────────────┐
│                  BACKEND (Spring Boot)               │
│  JwtAuthFilter → SecurityFilterChain → Controllers  │
│  Services → Repositories → JPA/Hibernate            │
└──────────────────────┬──────────────────────────────┘
                       │ JDBC
┌──────────────────────▼──────────────────────────────┐
│              PostgreSQL 16 (puerto 5433)             │
│  Flyway migrations V1 → V7                          │
└─────────────────────────────────────────────────────┘
```

### Estructura de paquetes del backend

```
com.myjara.backend/
├── config/          SecurityConfig
├── controller/      REST Controllers
├── dto/             Request/Response DTOs
├── entity/          JPA Entities
├── infrastructure/  JwtService, JwtAuthFilter
├── repository/      Spring Data Repositories
└── service/         Business Logic
```

---

## 4. Módulos implementados

### Módulo 1 — Gestión de pacientes

**Endpoints:** `/api/patients`

- CRUD completo de pacientes
- Búsqueda fonética por nombre, apellido, DNI o tarjeta sanitaria
- Baja lógica (nunca borrado físico — `active = false`)
- Tarjeta sanitaria única (CIP Extremadura: `EX-XXXXXXXXX`)

**Entidades:** `Patient`

### Módulo 2 — Agenda y citas

**Endpoints:** `/api/appointments`, `/api/professionals`, `/api/agendas`

- Profesionales con especialidad y centro sanitario
- Agendas semanales con slots configurables (cada N minutos)
- Cálculo de slots disponibles descontando citas existentes
- Detección de solapamiento con query JPQL
- Estados de cita: SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW

**Entidades:** `Professional`, `Agenda`, `Appointment`

**Endpoint clave:**
```
GET /api/appointments/slots?professionalId=1&agendaId=1&date=2026-06-23
→ ["09:00","09:15","09:30",...]  (slots libres)
```

### Módulo 3 — Historia clínica electrónica

**Endpoints:** `/api/encounters`, `/api/clinical/*`

- Episodios clínicos: OUTPATIENT, EMERGENCY, INPATIENT, HOME_VISIT
- Diagnósticos CIE-10 con catálogo (`Cie10Catalog`)
- Alergias con severidad: MILD, MODERATE, SEVERE
- Notas clínicas por episodio
- `@Transactional(readOnly=true)` en todos los métodos de lectura para evitar lazy loading

**Entidades:** `Encounter`, `Condition`, `Allergy`, `ClinicalNote`, `Cie10Catalog`

### Módulo 4 — Prescripción electrónica

**Endpoints:** `/api/prescriptions`

- Catálogo de medicamentos con código nacional AEMPS
- Búsqueda por nombre, principio activo o código
- Prescripción con dosis, frecuencia, duración y vía de administración
- **Detección automática de interacciones** con medicación activa del paciente
- Solo MEDICO y ADMIN pueden prescribir (`@PreAuthorize`)

**Entidades:** `Medication`, `Prescription`, `DrugInteraction`

**Interacción de ejemplo:**
- Ibuprofeno + Enalapril → MODERATE: riesgo de insuficiencia renal

### Módulo 5 — Urgencias y triaje Manchester

**Endpoints:** `/api/emergency`

- Registro de llegada con nivel de triaje (1-5) y color automático
- Boxes de urgencias: TRIAGE, GENERAL, RESUSCITATION, OBSERVATION
- Panel activo ordenado por nivel de triaje (más urgente primero)
- Registro de signos vitales: TA, FC, FR, Tª, SpO2, EVA dolor
- Estados de episodio: WAITING, TRIAGE, IN_ATTENTION, OBSERVATION, DISCHARGED, ADMITTED
- Tipos de alta: HOME, ADMISSION, TRANSFER, DEATH, VOLUNTARY

**Entidades:** `EmergencyEpisode`, `EmergencyBox`, `VitalSigns`

**Triaje Manchester:**
| Nivel | Color | Nombre |
|---|---|---|
| 1 | 🔴 RED | Resucitación |
| 2 | 🟠 ORANGE | Emergencia |
| 3 | 🟡 YELLOW | Urgente |
| 4 | 🟢 GREEN | Menos urgente |
| 5 | 🔵 BLUE | No urgente |

### Módulo 6 — Hospitalización

**Endpoints:** `/api/hospitalization`

- Unidades de hospitalización con planta y número de camas
- Gestión de camas: FREE, OCCUPIED, CLEANING, RESERVED
- Ingresos: SCHEDULED, EMERGENCY, TRANSFER
- Alta hospitalaria con liberación automática de cama
- Evoluciones clínicas por ingreso: EVOLUTION, ORDER, NURSING, DISCHARGE
- Cálculo de días de estancia en tiempo real

**Entidades:** `HospitalUnit`, `HospitalBed`, `Admission`, `AdmissionNote`

---

## 5. Base de datos

### Migraciones Flyway

| Versión | Archivo | Contenido |
|---|---|---|
| V1 | `V1__create_patients_table.sql` | Tabla `patients` |
| V2 | `V2__create_appointments_table.sql` | `professionals`, `agendas`, `appointments` |
| V3 | `V3__create_clinical_history_tables.sql` | `encounters`, `conditions`, `allergies`, `clinical_notes`, `cie10_catalog` |
| V4 | `V4__create_users_table.sql` | Tabla `users` |
| V4.1 | `V4_1__seed_users.sql` | Usuarios iniciales con BCrypt |
| V5 | `V5__create_prescription_tables.sql` | `medications`, `prescriptions`, `drug_interactions` |
| V6 | `V6__create_emergency_tables.sql` | `emergency_episodes`, `emergency_boxes`, `vital_signs` |
| V7 | `V7__create_hospitalization_tables.sql` | `hospital_units`, `hospital_beds`, `admissions`, `admission_notes` |

### Diagrama de tablas principales

```
patients ──────────────────────────────────────────────┐
    │                                                   │
    ├── appointments ── professionals ── agendas        │
    │                                                   │
    ├── encounters ── conditions                        │
    │              └── allergies                        │
    │              └── clinical_notes                   │
    │                                                   │
    ├── prescriptions ── medications                    │
    │                 └── drug_interactions             │
    │                                                   │
    ├── emergency_episodes ── emergency_boxes           │
    │                     └── vital_signs               │
    │                                                   │
    └── admissions ── hospital_beds ── hospital_units   │
                  └── admission_notes                   │
                                                        │
users ── professionals ─────────────────────────────────┘
```

---

## 6. API REST

### Autenticación
```
POST   /api/auth/login                    → JWT token
```

### Pacientes
```
GET    /api/patients                      → Lista todos
GET    /api/patients/{id}                 → Por ID
GET    /api/patients/search?term=         → Búsqueda fonética
POST   /api/patients                      → Crear
PUT    /api/patients/{id}                 → Actualizar
DELETE /api/patients/{id}                 → Baja lógica
```

### Citas
```
GET    /api/appointments/patient/{id}     → Citas del paciente
GET    /api/appointments/slots?...        → Slots disponibles
POST   /api/appointments                  → Crear cita
PATCH  /api/appointments/{id}/status      → Cambiar estado
DELETE /api/appointments/{id}             → Cancelar
```

### Historia clínica
```
GET    /api/encounters/patient/{id}       → Episodios
POST   /api/encounters                    → Nuevo episodio
GET    /api/clinical/conditions/patient/{id} → Diagnósticos
POST   /api/clinical/conditions           → Añadir diagnóstico
GET    /api/clinical/allergies/patient/{id}  → Alergias
POST   /api/clinical/notes                → Añadir nota
```

### Prescripción
```
GET    /api/prescriptions/patient/{id}/active     → Medicación activa
GET    /api/prescriptions/medications/search?term= → Buscar medicamento
GET    /api/prescriptions/interactions?...         → Comprobar interacciones
POST   /api/prescriptions                          → Prescribir
PATCH  /api/prescriptions/{id}/status              → Cambiar estado
```

### Urgencias
```
GET    /api/emergency/active              → Panel activo
GET    /api/emergency/boxes               → Boxes
POST   /api/emergency/arrival             → Registrar llegada
PATCH  /api/emergency/{id}/box            → Asignar box
PATCH  /api/emergency/{id}/discharge      → Alta
POST   /api/emergency/vital-signs         → Registrar constantes
```

### Hospitalización
```
GET    /api/hospitalization/units         → Unidades
GET    /api/hospitalization/beds/free     → Camas libres
GET    /api/hospitalization/active        → Ingresos activos
POST   /api/hospitalization/admit         → Ingresar paciente
PATCH  /api/hospitalization/{id}/discharge → Alta
POST   /api/hospitalization/notes         → Añadir evolución
```

---

## 7. Seguridad y roles

### Autenticación JWT

- Tokens firmados con HMAC-SHA384, válidos 24 horas
- Spring Security Stateless (sin sesión)
- `JwtAuthFilter` valida el token en cada request
- Interceptor Axios envía `Authorization: Bearer <token>` automáticamente

### Roles disponibles

| Rol | Descripción | Acceso |
|---|---|---|
| `ADMIN` | Administrador | Todo |
| `MEDICO` | Médico/a | Historia clínica, prescripción, citas, urgencias, hospitalización |
| `ENFERMERO` | Enfermero/a | Historia clínica (lectura), urgencias, hospitalización |
| `ADMINISTRATIVO` | Administrativo/a | Citas, pacientes |
| `CELADOR` | Celador/a | Pacientes (lectura), urgencias, hospitalización (lectura) |

### Control de acceso por endpoint

```java
@PreAuthorize("hasAnyRole('ADMIN','MEDICO')")           // Solo médicos prescriben
@PreAuthorize("hasAnyRole('ADMIN','MEDICO','ENFERMERO')") // Enfermería puede ver HC
@PreAuthorize("hasAnyRole('ADMIN','ADMINISTRATIVO')")    // Admin gestiona citas
```

---

## 8. Frontend

### Páginas

| Página | Ruta | Descripción |
|---|---|---|
| LoginPage | `/login` | Acceso al sistema |
| HomePage | `/` | Menú con módulos filtrados por rol |
| PatientsPage | `/patients` | Buscador en tiempo real |
| PatientDetailPage | `/patients/:id` | Ficha completa (citas, HC, medicación, alergias) |
| NewAppointmentPage | `/appointments/new` | Formulario 6 pasos estilo JARA |

### Flujo de citas (6 pasos)

```
1/6 Identificación del paciente   → búsqueda por nombre/DNI/TIS
2/6 Tipo de profesional           → Médico de familia, Enfermería...
3/6 Selección de servicio         → Profesional + agenda
4/6 Fecha y hora                  → Selector de fecha con validación de día
5/6 Selección de hueco            → Slots disponibles como lista
6/6 Confirmación                  → Resumen + motivo → CONFIRMAR CITA
```

### Características del frontend

- **Rutas protegidas** — `PrivateRoute` redirige a `/login` sin sesión
- **Token automático** — interceptor Axios añade JWT en cada petición
- **Logout automático** — redirección a login si el servidor devuelve 401
- **Menú por rol** — cada perfil ve solo los módulos que le corresponden
- **TanStack Query** — caché automático, estados de carga y error

---

## 9. CI/CD

### GitHub Actions — `.github/workflows/ci.yml`

**Job Frontend:**
- Node.js 20
- `npm ci` + `npm run build`
- Artefacto: `myjara-frontend-build` (dist/)

**Job Backend:**
- Java 17 (Temurin)
- PostgreSQL 16 como servicio
- `mvn clean verify` (compila + tests)
- `mvn package -DskipTests` (genera JAR)
- Artefacto: `myjara-backend-jar`

**Triggers:** push y pull_request a `main`

**Estado actual:** ✅ All checks passing

---

## 10. Arrancar en local

### Requisitos
- Java 17+
- Node.js 20+
- Docker Desktop

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/TUUSUARIO/myjara.git
cd myjara

# 2. Arrancar la base de datos
docker compose up -d

# 3. Arrancar el backend
cd backend
./mvnw spring-boot:run

# 4. En otra terminal, arrancar el frontend
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173` en el navegador.

### Docker Compose

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: myjara
      POSTGRES_USER: myjara
      POSTGRES_PASSWORD: myjara
    ports:
      - "5433:5432"
```

> Puerto 5433 en lugar de 5432 para evitar conflictos con instalaciones locales de PostgreSQL.

---

## 11. Usuarios del sistema

| Usuario | Contraseña | Rol | Descripción |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Administrador del sistema |
| `mlopez` | `medico123` | MEDICO | Dra. Maria Lopez — Medicina General |

### Añadir nuevos usuarios

Genera el hash BCrypt con:

```java
public class GeneratePasswords {
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("tu-contraseña"));
    }
}
```

Luego inserta en la BD:

```sql
INSERT INTO users (username, password, full_name, role, professional_id)
VALUES ('nuevo', '$2a$10$...hash...', 'Nombre Completo', 'MEDICO', 1)
ON CONFLICT (username) DO NOTHING;
```

---

## Estado final del proyecto

| Módulo | Estado |
|---|---|
| Módulo 1 — Gestión de pacientes | ✅ |
| Módulo 2 — Agenda y citas | ✅ |
| Módulo 3 — Historia clínica electrónica | ✅ |
| Módulo 4 — Prescripción electrónica | ✅ |
| Módulo 5 — Urgencias y triaje Manchester | ✅ |
| Módulo 6 — Hospitalización | ✅ |
| Frontend React con autenticación JWT | ✅ |
| Roles y control de acceso | ✅ |
| GitHub Actions CI/CD | ✅ |

---

*Documentación generada el 14/06/2026 — MyJara v1.0.0*  
*Proyecto desarrollado como réplica del sistema JARA del Servicio Extremeño de Salud*