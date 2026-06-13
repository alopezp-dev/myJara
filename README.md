# MyJara

[![MyJara CI](https://github.com/alopezp-dev/myjara/actions/workflows/ci.yml/badge.svg)](https://github.com/alopezp-dev/myjara/actions/workflows/ci.yml)

Sistema de información sanitaria para el Servicio Extremeño de Salud.
Réplica del sistema JARA desarrollada con Java 17, Spring Boot, PostgreSQL y React.

## Stack tecnológico

- **Backend:** Java 17 · Spring Boot 3.5 · Spring Security · JWT · JPA · Flyway
- **Base de datos:** PostgreSQL 16
- **Frontend:** React 19 · Vite · Tailwind CSS · TanStack Query
- **Infraestructura:** Docker · GitHub Actions

## Módulos implementados

- ✅ Gestión de pacientes
- ✅ Agenda y citas médicas con slots disponibles
- ✅ Historia clínica electrónica con diagnósticos CIE-10
- ✅ Prescripción electrónica con detección de interacciones
- ✅ Urgencias y triaje Manchester
- ✅ Autenticación JWT con roles (Médico, Enfermero, Administrativo, Celador)

## Arrancar en local

```bash
# Base de datos
docker compose up -d

# Backend
cd backend && ./mvnw spring-boot:run

# Frontend
cd frontend && npm run dev
```

Accede a `http://localhost:5173`  
Usuario: `admin` / Contraseña: `admin123`