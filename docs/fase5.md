# MyJara — Fase 5: Autenticación JWT y Seguridad

> Autenticación con JSON Web Tokens, roles de usuario y protección de rutas  
> Stack: Spring Security · JJWT 0.12.5 · BCrypt · React Context API

---

## Índice

1. [Descripción general](#1-descripción-general)
2. [Base de datos — Tabla users](#2-base-de-datos--tabla-users)
3. [Backend — Implementación JWT](#3-backend--implementación-jwt)
4. [Frontend — Autenticación en React](#4-frontend--autenticación-en-react)
5. [Flujo completo de autenticación](#5-flujo-completo-de-autenticación)
6. [Usuarios del sistema](#6-usuarios-del-sistema)
7. [Roles disponibles](#7-roles-disponibles)

---

## 1. Descripción general

La Fase 5 implementa autenticación stateless con JWT. Cada petición al backend incluye un token firmado que identifica al usuario y su rol. Spring Security valida el token en cada request antes de dar acceso a los endpoints protegidos.

**Características:**
- Login con usuario y contraseña hasheada con BCrypt
- Token JWT firmado con HMAC-SHA384, válido 24 horas
- Todas las rutas protegidas excepto `/api/auth/**` y `/actuator/**`
- Frontend con rutas privadas — redirige a `/login` si no hay sesión
- Logout limpia el token del almacenamiento local
- Interceptor Axios envía el token automáticamente en cada petición
- Redirección automática al login si el servidor devuelve 401

---

## 2. Base de datos — Tabla users

### Migración `V4__create_users_table.sql`

```sql
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  UNIQUE NOT NULL,
    password        VARCHAR(255) NOT NULL,    -- Hash BCrypt
    full_name       VARCHAR(150) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'MEDICO',
    professional_id BIGINT,                   -- Vinculado a tabla professionals
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (professional_id) REFERENCES professionals(id)
);
```

### Usuarios iniciales

| Usuario | Contraseña | Rol | Profesional vinculado |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | — |
| `mlopez` | `medico123` | MEDICO | Maria Lopez (id=1) |

> **Nota:** Las contraseñas se almacenan como hashes BCrypt, nunca en texto plano.

---

## 3. Backend — Implementación JWT

### Arquitectura de seguridad

```
Request → JwtAuthFilter → SecurityFilterChain → Controller
              ↓
         Extrae token del header Authorization: Bearer <token>
              ↓
         JwtService.isTokenValid()
              ↓
         SecurityContextHolder.setAuthentication()
```

### `User.java` — Entidad que implementa UserDetails

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public enum Role {
        ADMIN, MEDICO, ENFERMERO, ADMINISTRATIVO, CELADOR
    }
}
```

### `JwtService.java` — Generación y validación de tokens

```java
@Service
public class JwtService {

    public String generateToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getAuthorities().iterator().next().getAuthority());
        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
```

**Configuración en `application.yml`:**
```yaml
myjara:
  jwt:
    secret: clave-secreta-cambiar-en-produccion-minimo-256-bits
    expiration-ms: 86400000  # 24 horas
```

### `JwtAuthFilter.java` — Filtro que intercepta cada request

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    public JwtAuthFilter(JwtService jwtService,
                         @Lazy UserDetailsService userDetailsService) { ... }

    @Override
    protected void doFilterInternal(request, response, filterChain) {
        // 1. Extrae el token del header Authorization
        // 2. Valida el token con JwtService
        // 3. Si es válido, autentica en SecurityContext
        // 4. Continúa la cadena de filtros
    }
}
```

> **Nota:** `@Lazy` en `UserDetailsService` rompe la dependencia circular entre `JwtAuthFilter` y `SecurityConfig`.

### `SecurityConfig.java` — Configuración de Spring Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()   // Login público
                .requestMatchers("/actuator/**").permitAll()   // Health check público
                .anyRequest().authenticated()                  // Todo lo demás protegido
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### `AuthController.java` — Endpoint de login

```
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "username": "admin",
  "fullName": "Administrador MyJara",
  "role": "ADMIN",
  "professionalId": null
}
```

---

## 4. Frontend — Autenticación en React

### `AuthContext.jsx` — Estado global de autenticación

```jsx
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    // Recupera sesión persistida en localStorage
    const stored = localStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  })

  const login = async (username, password) => {
    const res = await loginApi(username, password)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data))
    setUser(res.data)
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
  }
}
```

### Interceptores Axios en `client.js`

```js
// Envía el token automáticamente en cada petición
client.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Redirige al login si el token expira (401)
client.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
```

### `PrivateRoute` — Protección de rutas en React

```jsx
function PrivateRoute({ children }) {
  const { user } = useAuth()
  return user ? children : <Navigate to="/login" replace />
}
```

### Navbar con información del usuario

La navbar muestra el nombre completo, el rol con badge y el botón de cerrar sesión:

```jsx
<span className="text-sm text-blue-200">{user?.fullName}</span>
<span className="text-xs bg-blue-800 px-2 py-0.5 rounded-full">{user?.role}</span>
<button onClick={handleLogout}>Cerrar sesión</button>
```

---

## 5. Flujo completo de autenticación

```
Usuario introduce credenciales en LoginPage
        ↓
AuthContext.login() llama a POST /api/auth/login
        ↓
Spring Security valida usuario y contraseña con BCrypt
        ↓
JwtService genera token firmado con HMAC-SHA384
        ↓
Frontend guarda token y datos de usuario en localStorage
        ↓
Axios interceptor añade Authorization: Bearer <token> a cada request
        ↓
JwtAuthFilter valida el token en cada petición
        ↓
Spring Security autentica la request y permite el acceso
```

**Flujo de logout:**
```
Usuario pulsa "Cerrar sesión"
        ↓
AuthContext.logout() elimina token y user de localStorage
        ↓
Navigate a /login
        ↓
PrivateRoute redirige cualquier acceso a ruta protegida a /login
```

---

## 6. Usuarios del sistema

Para añadir nuevos usuarios genera el hash BCrypt con la clase `GeneratePasswords.java`:

```java
public class GeneratePasswords {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("tu-contraseña"));
    }
}
```

Luego inserta directamente en la BD o añade una nueva migración Flyway.

---

## 7. Roles disponibles

| Rol | Descripción | Acceso |
|---|---|---|
| `ADMIN` | Administrador del sistema | Acceso total |
| `MEDICO` | Médico del SES | Historia clínica, prescripción, citas |
| `ENFERMERO` | Enfermero/a | Evolución de enfermería, constantes |
| `ADMINISTRATIVO` | Administrativo | Gestión de citas, agenda |
| `CELADOR` | Celador | Movimientos de pacientes |

> **Pendiente:** implementar restricciones por rol en los endpoints del backend (`@PreAuthorize`) y vistas diferenciadas en el frontend según el rol del usuario autenticado.

---

## Estado del proyecto MyJara

| Fase | Descripción | Estado |
|---|---|---|
| Fase 1 | Infraestructura base + pacientes | ✅ |
| Fase 2 | Agenda y citas | ✅ |
| Fase 3 | Historia clínica electrónica | ✅ |
| Fase 4 | Frontend React | ✅ |
| Fase 5 | Autenticación JWT | ✅ |
| Fase 6 | Prescripción electrónica | 🔜 |
| Fase 7 | Urgencias y triaje | 🔜 |
| Fase 8 | Hospitalización | 🔜 |
| Fase 9 | CI/CD y despliegue | 🔜 |

---

*Documentación generada el 13/06/2026 — MyJara v0.5.0*