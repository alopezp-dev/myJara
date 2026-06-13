# MyJara — Fase 4: Frontend React

> Interfaz web del sistema de información sanitaria  
> Stack: React 19 · Vite · Tailwind CSS · React Router · TanStack Query · Axios

---

## Índice

1. [Descripción general](#1-descripción-general)
2. [Stack tecnológico](#2-stack-tecnológico)
3. [Estructura del proyecto](#3-estructura-del-proyecto)
4. [Configuración](#4-configuración)
5. [Cliente API](#5-cliente-api)
6. [Componentes](#6-componentes)
7. [Páginas](#7-páginas)
8. [Flujos implementados](#8-flujos-implementados)
9. [Correcciones al backend](#9-correcciones-al-backend)

---

## 1. Descripción general

La Fase 4 implementa la interfaz web de MyJara. El frontend consume la API REST del backend Spring Boot y presenta una interfaz moderna similar a JARA para gestión de pacientes, visualización de fichas clínicas y programación de citas con slots disponibles en tiempo real.

**Características principales:**
- Buscador de pacientes en tiempo real (búsqueda desde 2 caracteres)
- Ficha completa del paciente con datos personales, citas, diagnósticos, alergias y episodios
- Formulario de nueva cita con selección de slots disponibles — solo muestra huecos libres
- Validación de día de semana según la agenda del profesional
- Proxy hacia el backend en desarrollo para evitar CORS

---

## 2. Stack tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| React | 19 | Framework UI |
| Vite | 6.x | Build tool y dev server |
| Tailwind CSS | 4.x | Estilos utility-first |
| React Router | 7.x | Navegación SPA |
| TanStack Query | 5.x | Fetching, caché y estados de carga |
| Axios | 1.x | Cliente HTTP |

---

## 3. Estructura del proyecto

```
frontend/
├── vite.config.js
├── package.json
└── src/
    ├── api/
    │   └── client.js               ← Todas las llamadas al backend
    ├── components/
    │   ├── common/
    │   │   ├── Navbar.jsx
    │   │   ├── Spinner.jsx
    │   │   └── ErrorMessage.jsx
    │   ├── patients/
    │   │   ├── PatientSearch.jsx
    │   │   └── PatientCard.jsx
    │   ├── appointments/
    │   │   └── AppointmentForm.jsx
    │   └── clinical/
    │       └── EncounterPanel.jsx
    ├── pages/
    │   ├── HomePage.jsx
    │   ├── PatientsPage.jsx
    │   ├── PatientDetailPage.jsx
    │   └── NewAppointmentPage.jsx
    ├── hooks/
    │   └── usePatients.js
    ├── App.jsx
    └── main.jsx
```

---

## 4. Configuración

### `vite.config.js`

```js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080'  // Proxy al backend Spring Boot
    }
  }
})
```

El proxy redirige todas las peticiones `/api/*` al backend en `localhost:8080`, evitando errores de CORS en desarrollo sin necesidad de configurar CORS en Spring.

### `src/index.css`

```css
@import "tailwindcss";
```

### `src/main.jsx`

```jsx
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

const queryClient = new QueryClient()

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <QueryClientProvider client={queryClient}>
        <App />
      </QueryClientProvider>
    </BrowserRouter>
  </StrictMode>
)
```

**TanStack Query** gestiona el estado del servidor — caché automático, revalidación, estados de carga y error sin Redux ni Context manual.

---

## 5. Cliente API

`src/api/client.js` centraliza todas las llamadas al backend:

```js
import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

// Pacientes
export const getPatients = () => client.get('/patients')
export const getPatientById = (id) => client.get(`/patients/${id}`)
export const searchPatients = (term) => client.get(`/patients/search?term=${term}`)
export const createPatient = (data) => client.post('/patients', data)

// Citas
export const getAppointmentsByPatient = (id) => client.get(`/appointments/patient/${id}`)
export const getAvailableSlots = (professionalId, agendaId, date) =>
  client.get(`/appointments/slots?professionalId=${professionalId}&agendaId=${agendaId}&date=${date}`)
export const createAppointment = (data) => client.post('/appointments', data)

// Profesionales y agendas
export const getProfessionals = () => client.get('/professionals')
export const getAgendas = (professionalId) => client.get(`/agendas/professional/${professionalId}`)

// Historia clínica
export const getEncountersByPatient = (id) => client.get(`/encounters/patient/${id}`)
export const getConditionsByPatient = (id) => client.get(`/clinical/conditions/patient/${id}`)
export const getAllergiesByPatient = (id) => client.get(`/clinical/allergies/patient/${id}`)
export const getNotesByEncounter = (id) => client.get(`/clinical/notes/encounter/${id}`)
```

---

## 6. Componentes

### `Navbar.jsx`

Barra de navegación superior con enlace activo resaltado.

```jsx
const links = [
  { to: '/',         label: 'Inicio' },
  { to: '/patients', label: 'Pacientes' },
]
```

### `Spinner.jsx`

Indicador de carga circular animado con Tailwind.

### `ErrorMessage.jsx`

Caja de error con fondo rojo suave. Acepta prop `message`.

---

## 7. Páginas

### `HomePage.jsx`

Pantalla de inicio con tarjetas de acceso rápido a los módulos principales.

### `PatientsPage.jsx`

Buscador de pacientes con tabla de resultados.

**Lógica de búsqueda:**
- Si `term.length < 2` → llama a `getPatients()` (lista todos)
- Si `term.length >= 2` → llama a `searchPatients(term)` (búsqueda en backend)
- TanStack Query cachea los resultados por término

```jsx
const { data, isLoading, isError } = useQuery({
  queryKey: ['patients', term],
  queryFn: () => term.length >= 2
    ? searchPatients(term).then(r => r.data)
    : getPatients().then(r => r.data),
})
```

**Tabla de resultados:**

| Columna | Campo |
|---|---|
| Paciente | firstName + lastName |
| DNI | dni |
| Tarjeta sanitaria | healthCard |
| Municipio | municipality |
| Acción | Enlace a ficha |

### `PatientDetailPage.jsx`

Ficha completa del paciente en layout de 3 columnas:

**Columna izquierda:**
- Datos personales (nombre, apellidos, fecha nacimiento, género, teléfono, email, municipio, dirección)
- Alergias con badge de severidad (MILD=gris, MODERATE=amarillo, SEVERE=rojo)

**Columna central:**
- Historial de citas con fecha, profesional, motivo y badge de estado

**Columna derecha:**
- Diagnósticos CIE-10 con código, descripción, estado y fecha de inicio
- Episodios clínicos con fecha, profesional y estado

Todas las secciones cargan en paralelo con `useQuery` independientes — si una falla, las demás siguen mostrándose.

**Badges de estado:**

| Estado | Color |
|---|---|
| SCHEDULED | Azul |
| COMPLETED | Verde |
| CANCELLED | Rojo |
| IN_PROGRESS | Azul |
| ACTIVE | Rojo |
| RESOLVED | Verde |
| SEVERE | Rojo |
| MODERATE | Amarillo |

### `NewAppointmentPage.jsx`

Formulario de nueva cita con flujo guiado en 5 pasos:

**Paso 1 — Paciente:** si viene de la ficha (`?patientId=1`), se muestra el paciente preseleccionado. Si no, campo de entrada manual.

**Paso 2 — Profesional:** selector con nombre y especialidad.

**Paso 3 — Agenda:** selector con día, horario, centro y duración del slot. Solo aparece cuando hay profesional seleccionado.

**Paso 4 — Fecha:** selector de fecha. Valida que el día de la semana coincida con la agenda seleccionada.

**Paso 5 — Slots disponibles:** consulta al backend los huecos libres y los muestra como botones seleccionables. Los slots ya ocupados no aparecen.

```jsx
const { data: slots = [] } = useQuery({
  queryKey: ['slots', form.professionalId, form.agendaId, form.date],
  queryFn: () => getAvailableSlots(
    form.professionalId, form.agendaId, form.date
  ).then(r => r.data),
  enabled: !!form.professionalId && !!form.agendaId && !!form.date && dateValid
})
```

**Validaciones:**
- Día de la semana debe coincidir con la agenda
- Debe seleccionarse un slot antes de poder enviar
- Si hay solapamiento en el backend devuelve error 409 que se muestra en el formulario

**Tras crear la cita:** redirige automáticamente a la ficha del paciente.

---

## 8. Flujos implementados

### Buscar y ver un paciente

```
/patients → escribir en buscador → tabla actualiza en tiempo real
         → pulsar "Ver ficha →" → /patients/:id → ficha completa
```

### Crear una cita

```
/patients/:id → "+ Nueva cita" → /appointments/new?patientId=:id
→ seleccionar profesional → seleccionar agenda
→ seleccionar fecha → ver slots disponibles
→ pulsar slot → rellenar motivo → "Crear cita"
→ redirige a /patients/:id con la cita ya visible
```

---

## 9. Correcciones al backend

Durante el desarrollo del frontend se detectaron y corrigieron varios problemas en el backend:

### Problema 1 — Lazy loading fuera de sesión

**Error:** `Could not initialize proxy - no session`

**Causa:** `FetchType.LAZY` en relaciones `@ManyToOne`. Jackson intentaba serializar las relaciones después de que la sesión JPA se cerrara.

**Solución A — `@JsonIgnore`:** en entidades que se serializan directamente (como `Agenda`):
```java
@JsonIgnore
@ManyToOne(fetch = FetchType.LAZY)
private Professional professional;
```

**Solución B — `@Transactional(readOnly = true)`:** en métodos de servicio que usan DTOs con `from()`:
```java
@Transactional(readOnly = true)
public List<AppointmentResponse> findByPatient(Long patientId) { ... }
```

### Problema 2 — Endpoint de slots disponibles

Se añadió un nuevo endpoint al backend para calcular los huecos libres de una agenda en una fecha:

```
GET /api/appointments/slots?professionalId=1&agendaId=1&date=2026-06-23
```

**Lógica:**
1. Genera todos los slots del día según `startTime`, `endTime` y `slotMinutes`
2. Consulta las citas existentes en ese rango de tiempo
3. Excluye los slots con citas activas (no `CANCELLED` ni `NO_SHOW`)
4. Devuelve solo los slots libres

```java
List<String> allSlots = new ArrayList<>();
LocalTime current = agenda.getStartTime();
while (current.plusMinutes(agenda.getSlotMinutes())
       .compareTo(agenda.getEndTime()) <= 0) {
    allSlots.add(current.toString());
    current = current.plusMinutes(agenda.getSlotMinutes());
}
```

---

## Estado del proyecto MyJara

| Fase | Descripción | Estado |
|---|---|---|
| Fase 1 | Infraestructura base + pacientes | ✅ |
| Fase 2 | Agenda y citas | ✅ |
| Fase 3 | Historia clínica electrónica | ✅ |
| Fase 4 | Frontend React | ✅ |
| Fase 5 | Prescripción electrónica | 🔜 |
| Fase 6 | Urgencias y triaje | 🔜 |
| Fase 7 | Hospitalización | 🔜 |
| Fase 8 | Autenticación JWT + roles | 🔜 |
| Fase 9 | Infraestructura de producción | 🔜 |

---

## Próximos pasos — Fase 5: Prescripción electrónica

- Catálogo de medicamentos AEMPS
- Modelo `MedicationRequest` con dosis, pauta y vía de administración
- Detección de interacciones medicamentosas
- Pantalla de receta electrónica en el frontend
- Dispensación en farmacia

---

*Documentación generada el 12/06/2026 — MyJara v0.4.0*