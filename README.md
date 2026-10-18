# Smart Coach AI - Backend

API REST para la generación personalizada de rutinas de entrenamiento y seguimiento de pesajes mediante IA. El backend recoge el perfil físico del usuario (edad, peso, altura, objetivo...), y a partir de ahí genera rutinas con Gemini y guarda el historial de entrenamiento y de pesajes.

El proyecto sigue una arquitectura hexagonal (ports & adapters): el dominio es independiente de Spring y de JPA. En `AGENTS.md` y `docs/ARCHITECTURE.md` están las reglas que seguimos al tocar el código.

## Stack

- Java 17, Spring Boot 4.1.1 (Web, Data JPA, Validation, Security)
- PostgreSQL 16 (local con Docker Compose)
- Spring AI + Gemini (`gemini-3.8-flash`) para la generación de rutinas
- Spring Security + JWT (jjwt 0.12.5) para autenticación y roles `USER` / `ADMIN`
- Springdoc OpenAPI (Swagger UI en `/swagger-ui.html`)
- Tests: JUnit 5, Mockito, AssertJ, H2 (perfil `test`) y Testcontainers

## Estado actual

Funcional y testeado:

- ✅ Registro, login (JWT) y CRUD de usuarios
- ✅ Roles `USER` / `ADMIN`: solo el dueño del recurso o un `ADMIN` puede modificar/eliminar un usuario
- ✅ Perfil físico del usuario (edad, peso, altura, actividad, objetivo)
- ✅ Creación manual de rutinas y generación con IA (`POST /api/v1/routines/generate`)
- ✅ Historial de rutinas y registro de pesajes
- ✅ Actualización con IA de rutinas en base a historial de pesajes (`POST /api/v1/routines/update/{routineId}`)
- ✅ Manejo global de errores (400 / 401 / 403 / 404 / 409)
- 🔲 Nutrición (en la hoja de ruta, aún no implementado)

## Puesta en marcha

Requisitos: JDK 17, Maven (o usa `./mvnw`) y Docker.

```bash
# 1. Levantar PostgreSQL y pgAdmin
docker compose up -d

# 2. Variables de entorno necesarias para arrancar la app
#    GEMINI_API_KEY -> clave de la API de Gemini (para /routines/generate)
#    JWT_SECRET     -> secreto para firmar los tokens (mínimo 32 chars)
#    En Windows (PowerShell):  $env:GEMINI_API_KEY="..." ; $env:JWT_SECRET="..."
#    En Linux/macOS:           export GEMINI_API_KEY="..." JWT_SECRET="..."

# 3. Arrancar la app
./mvnw spring-boot:run
```

La app queda en `http://localhost:8080` y la documentación interactiva en `http://localhost:8080/swagger-ui.html`.

> **Nota sobre el arranque sin Gemini:** el adaptador de IA (`GeminiAiAdapter`) es `@Primary` y necesita la key configurada para construirse. Si no tienes una key, pon una *dummy* para poder arrancar (la llamada real a `/routines/generate` fallará); el `MockAiAdapter` del código genera una rutina determinista y sirve como referencia para desarrollo offline si se quiere activar por configuración.

## Autenticación y roles

- Seguridad *stateless* con JWT: `POST /api/v1/auth/login` devuelve un token que el cliente envía como `Authorization: Bearer <token>`.
- El token incluye los claims `email` y `role`. El `JwtAuthenticatorFilter` los convierte en autoridades de Spring Security (`ROLE_USER` / `ROLE_ADMIN`).
- `POST /api/v1/auth/register` crea los usuarios con rol **`USER`** por defecto.
- Los endpoints de perfil, rutinas y pesajes toman el `userId` directamente del token, así que **no hace falta pasarlo como parámetro** (blinda contra IDOR).
- `PUT` y `DELETE /api/v1/users/{id}` solo están permitidos al **dueño del usuario** o a un **`ADMIN`**; cualquier otro recibe `403 Forbidden`.
- La ruta `/api/v1/admin/**` está reservada y exige rol `ADMIN` (aún sin endpoints definidos).

> ⚠️ Actualmente no hay endpoint para promocionar a un usuario a `ADMIN`: el primer administrador debe crearse directamente en la base de datos (columna `role` de la tabla `users`).

## Endpoints principales

Todos bajo `/api/v1`. Solo `auth` es pública:

### Autenticación (pública)

| Método | Ruta               | Descripción                                  |
|--------|--------------------|----------------------------------------------|
| POST   | `/auth/register`   | Registrar usuario (rol `USER` por defecto)   |
| POST   | `/auth/login`      | Login, devuelve el JWT                       |

### Usuarios y perfil (autenticado)

| Método | Ruta                 | Descripción                     | Permiso           |
|--------|----------------------|---------------------------------|-------------------|
| GET    | `/users/{id}`        | Buscar usuario por id           | Autenticado       |
| PUT    | `/users/{id}`        | Actualizar usuario              | Dueño o ADMIN     |
| DELETE | `/users/{id}`        | Eliminar usuario                | Dueño o ADMIN     |
| GET    | `/users`             | Perfil del usuario autenticado  | Autenticado       |
| POST   | `/users/profile`     | Crear/actualizar perfil físico  | Autenticado       |

### Rutinas (autenticado)

| Método | Ruta                 | Descripción                          |
|--------|----------------------|--------------------------------------|
| POST   | `/routines`          | Crear rutina manual                  |
| GET    | `/routines`          | Rutinas del usuario autenticado      |
| POST   | `/routines/generate` | Generar rutina con IA (body opcional con preferencias) |
| POST   | `/routines/udate/{routineId}` | Actualizar rutina con IA (body opcional con preferencias) teniendo en cuenta hostorial de pesos |

### Pesajes (autenticado)

| Método | Ruta          | Descripción                |
|--------|---------------|----------------------------|
| POST   | `/weighlog`   | Registrar pesaje           |
| GET    | `/weighlog`   | Historial de pesajes       |

## Tests

```bash
./mvnw test
```

- **Unitarios** de los casos de uso (Mockito) y de los Value Objects (AssertJ).
- **Test de contexto** (`BackendApplicationTests`) con el perfil `test`: usa una base H2 en memoria y claves *dummy*, por lo que **no requiere variables de entorno**.
- **Integración** (`*IT`) de la capa de persistencia contra un PostgreSQL real levantado con **Testcontainers**; estos necesitan Docker funcionando.
- Todo corre de forma automática en GitHub Actions (`.github/workflows/ci.yml`).

## Estructura

Resumen rápido; la versión detallada con descripción de cada paquete está en `DOCS.md`:

```text
src/main/java/com/smartcoach/backend/
├── application/usecase/     # Casos de uso (registro, login, generar rutina...)
├── domain/                  # Modelos, Value Objects, excepciones y puertos (sin Spring)
├── infrastructure/          # Controladores REST, JPA, mappers, seguridad, adaptadores IA
└── BackendApplication.java
```


## Documentación

- `DOCS.md` — estructura de carpetas y descripción de paquetes
- `docs/ARCHITECTURE.md` — detalle de la arquitectura hexagonal
- `docs/ROADMAP.md` — historias de usuario y estado de cada una
- `AGENTS.md` — reglas y contexto para desarrollo asistido
