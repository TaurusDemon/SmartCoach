# Smart Coach AI - Backend

API REST para la generación personalizada de rutinas de entrenamiento y seguimiento nutricional mediante IA. El backend recoge el perfil físico del usuario (edad, peso, altura, objetivo...), y a partir de ahí genera rutinas con Gemini y guarda el historial de entrenamiento y de pesajes.

El proyecto sigue una arquitectura hexagonal (ports & adapters): el dominio es independiente de Spring y de JPA. En `AGENTS.md` y `docs/ARCHITECTURE.md` están las reglas que seguimos al tocar el código.

## Stack

- Java 17, Spring Boot 4.1.1 (Web, Data JPA, Validation, Security)
- PostgreSQL 16 (local con Docker Compose)
- Spring AI + Gemini (`gemini-3.8-flash`) para la generación de rutinas
- Spring Security + JWT (jjwt 0.12.5) para autenticación
- Springdoc OpenAPI (Swagger UI en `/swagger-ui.html`)
- Tests: JUnit 5, Mockito, AssertJ y Testcontainers

## Estado actual

Funcional y testeado:

- ✅ Registro, login (JWT) y CRUD de usuarios
- ✅ Perfil físico del usuario (edad, peso, altura, actividad, objetivo)
- ✅ Creación manual de rutinas y generación con IA (`POST /api/v1/routines/generate`)
- ✅ Historial de rutinas y registro de pesajes
- ✅ Manejo global de errores (400 / 401 / 404 / 409)
- 🔲 Nutrición (en la hoja de ruta, aún no implementado)

## Puesta en marcha

Requisitos: JDK 17, Maven (o usa `./mvnw`) y Docker.

```bash
# 1. Levantar PostgreSQL y pgAdmin
docker compose up -d

# 2. Variables de entorno necesarias
#    GEMINI_API_KEY -> clave de la API de Gemini (para /routines/generate)
#    JWT_SECRET     -> secreto para firmar los tokens (mínimo 32 chars)
#    En Windows (PowerShell):  $env:GEMINI_API_KEY="..."
#    En Linux/macOS:           export GEMINI_API_KEY="..."

# 3. Arrancar la app
./mvnw spring-boot:run
```

La app queda en `http://localhost:8080` y la documentación interactiva en `http://localhost:8080/swagger-ui.html`.

Si no quieres configurar `GEMINI_API_KEY`, la app funciona igual: hay un `MockAiAdapter` que genera una rutina de ejemplo determinista (se activa por defecto si la key no está disponible).

## Endpoints principales

Todos bajo `/api/v1`, menos auth que es pública:

| Método | Ruta                            | Descripción                            |
|--------|---------------------------------|----------------------------------------|
| POST   | `/auth/register`                | Registrar usuario                      |
| POST   | `/auth/login`                   | Login, devuelve JWT                    |
| POST   | `/users`                        | Crear usuario (alternativo)            |
| GET    | `/users/{id}`                   | Buscar usuario                         |
| POST   | `/users/{id}`                   | Actualizar usuario                     |
| DELETE | `/users/{id}`                   | Eliminar usuario                       |
| POST   | `/users/{userId}/profile`       | Crear/actualizar perfil físico         |
| GET    | `/users/profile`                | Perfil del usuario autenticado         |
| POST   | `/routines`                     | Crear rutina manual                    |
| POST   | `/routines/generate`            | Generar rutina con IA                  |
| GET    | `/routines`                     | Rutinas del usuario autenticado        |
| POST   | `/weighlog`                     | Registrar pesaje                       |
| GET    | `/weighlog`                     | Historial de pesajes                   |

Los endpoints de perfil, rutinas y pesajes cogen el `userId` directamente del token JWT (header `Authorization: Bearer <token>`), así que no hace falta pasarlo como parámetro.

## Tests

```bash
./mvnw test
```

Hay tests unitarios de los casos de uso (con mocks) y tests de integración (`*IT`) contra un PostgreSQL real levantado con Testcontainers. Estos últimos necesitan Docker funcionando.

## Estructura

Resumen rápido; la versión detallada con descripción de cada paquete está en `DOCS.md`:

```text
src/main/java/com/smartcoach/backend/
├── application/usecase/     # Casos de uso (registro, login, generar rutina...)
├── domain/                  # Modelos, Value Objects, excepciones y puertos (sin Spring)
├── infrastructure/          # Controladores REST, JPA, mappers, seguridad, adaptadores IA
└── BackendApplication.java
```

## Notas

- `ddl-auto: update` en `application.yml`: Hibernate crea/actualiza las tablas solo. Ok para desarrollo, pero es mejor usar migraciones (Flyway/Liquibase) antes de ir a producción.
- En `docker-compose.yml` también se levanta pgAdmin en `http://localhost:5050` (admin@smartcoach.com / adminpassword) por si quieres inspeccionar la base.
- Las credenciales de la base están hardcodeadas en el compose. No apto para producción.

## Documentación

- `DOCS.md` — estructura de carpetas y descripción de paquetes
- `docs/ARCHITECTURE.md` — detalle de la arquitectura hexagonal
- `docs/ROADMAP.md` — historias de usuario y estado de cada una
- `AGENTS.md` — reglas y contexto para desarrollo asistido