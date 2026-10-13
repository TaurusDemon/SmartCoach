# Roadmap & Historias de Usuario - Smart Coach AI

Este documento detalla la evolución técnica y de negocio del backend de Smart Coach AI, organizado por fases e Historias de Usuario (HU) bajo Arquitectura Hexagonal.

> **Estado general:** Fases 1–5 implementadas y con tests verdes (94 tests). La Fase 6 (AWS) está pendiente.

---

## 🟢 Fase 1: Dominio Base y Gestión de Usuarios

### HU#1: Registro de Usuario Base
- **Como** nuevo usuario,
- **quiero** registrarme con mi nombre, correo electrónico y contraseña,
- **para** crear una cuenta en la plataforma.
- **Estado:** ✅ Completa
- **Criterios de Aceptación:**
    - [x] Validación de formato de email mediante Value Object (`Email`).
    - [x] Validación de complejidad de contraseña (`Password`, mínimo 8 caracteres).
    - [x] Prevención de registros duplicados (`UserAlreadyExistsException`).
    - [x] Contraseña cifrada con BCrypt (nunca en texto plano, `PasswordHash`).
    - [x] Persistencia en PostgreSQL mediante `JpaUserRepositoryAdapter`.
    - [x] Manejo de excepciones HTTP (400, 409) con `@RestControllerAdvice`.
    - [x] Documentación de endpoints en Swagger UI.

### HU#2: Registro y Actualización de Perfil Físico
- **Como** usuario registrado,
- **quiero** ingresar mis datos físicos (edad, peso, altura, nivel de actividad y objetivo),
- **para** que el motor de IA disponga del contexto necesario para mis recomendaciones.
- **Estado:** ✅ Completa
- **Criterios de Aceptación:**
    - [x] Value Objects con validaciones de rango: `Age` (15–119), `Weight` (1–300 kg), `Height` (cm), `ActivityLevel` (enum), `FitnessGoal` (enum: Perder peso, Ganar músculo, Mantenimiento).
    - [x] Entidad de dominio `UserProfile` asociada a `User` (1:1).
    - [x] Caso de uso `CreateOrUpdateUserProfileCase`.
    - [x] Endpoint `POST /api/v1/users/profile` (crea o actualiza; el `userId` sale del JWT).

---

## 🔵 Fase 2: Autenticación, Seguridad y Gestión de Sesión

### HU#3: Autenticación y Emisión de Tokens JWT
- **Como** usuario registrado,
- **quiero** iniciar sesión con mis credenciales,
- **para** obtener un token de acceso seguro (JWT) y consultar mi información.
- **Estado:** ✅ Completa
- **Criterios de Aceptación:**
    - [x] Integración de Spring Security con filtro JWT sin estado (*stateless*).
    - [x] Almacenamiento seguro de contraseñas con `BCryptPasswordEncoder`.
    - [x] Caso de uso `AuthenticateUserUseCase`.
    - [x] Endpoint `POST /api/v1/auth/login` (devuelve el JWT con claims `email` y `role`).
    - [x] Endpoint público `POST /api/v1/auth/register`.

### HU#4: Autorización de Endpoints por Usuario y Rol
- **Como** sistema de seguridad,
- **quiero** validar que cada petición sobre un recurso pertenezca al usuario autenticado y respete su rol,
- **para** evitar vulnerabilidades de acceso directo a objetos (IDOR) y permisos no deseados.
- **Estado:** ✅ Completa (ampliada con roles `USER`/`ADMIN`)
- **Criterios de Aceptación:**
    - [x] Rol `Role` (USER | ADMIN) persistido en `users.role` y asignado `USER` por defecto en el registro.
    - [x] Claim `role` en el JWT → autoridades `ROLE_USER` / `ROLE_ADMIN` en Spring Security.
    - [x] Ruta reservada `/api/v1/admin/**` con `hasRole("ADMIN")`.
    - [x] Protección a nivel de caso de uso: `PUT`/`DELETE /api/v1/users/{id}` solo para el **dueño** o un **ADMIN**.
    - [x] Extracción del `userId` desde el JWT en los controladores (`AuthenticatedUser`), sin confiar en parámetros del cliente.
    - [x] `401 Unauthorized` (Spring Security) y `403 Forbidden` (`ForbiddenOperationException`).
- **Pendiente / límite conocido:** no existe endpoint para promocionar a `ADMIN`; el primer administrador se crea directamente en la BD.

---

## 🟣 Fase 3: Motor de IA y Recomendaciones Personalizadas

### HU#5: Generación de Rutina de Entrenamiento con IA
- **Como** usuario con perfil físico configurado,
- **quiero** solicitar una rutina de entrenamiento personalizada,
- **para** tener un plan de ejercicios semanal adaptado a mi objetivo.
- **Estado:** ✅ Completa
- **Criterios de Aceptación:**
    - [x] Puerto de salida `AiCoachPort`.
    - [x] Adaptador `GeminiAiAdapter` con Spring AI (`gemini-3.8-flash`).
    - [x] *Prompt Engineering* en infraestructura para forzar respuesta JSON estructurada.
    - [x] `MockAiAdapter` con rutina determinista para desarrollo/pruebas.
    - [x] Caso de uso `GenerateWorkoutRoutineUseCase`.
    - [x] Endpoint `POST /api/v1/routines/generate` (body opcional con preferencias).

### HU#6: Historial y Consulta de Rutinas Guardadas
- **Como** usuario,
- **quiero** consultar las rutinas generadas anteriormente,
- **para** seguir mi plan de entrenamiento día a día.
- **Estado:** ✅ Completa
- **Criterios de Aceptación:**
    - [x] Persistencia de rutinas y ejercicios en PostgreSQL (`RoutineEntity`, `ExerciseEntity`).
    - [x] Caso de uso `GetRoutinesByUserIdCase`.
    - [x] Endpoint `GET /api/v1/routines` (rutinas del usuario autenticado).

---

## 🟠 Fase 4: Registro de Progreso y Métricas

### HU#7: Registro de Pesajes y Evolución Física
- **Como** usuario en seguimiento,
- **quiero** registrar mi peso periódicamente,
- **para** visualizar mi evolución física a lo largo del tiempo.
- **Estado:** ✅ Completa
- **Criterios de Aceptación:**
    - [x] Entidad de dominio `WeightLog` (peso validado, fecha, notas).
    - [x] Casos de uso `CreateWeightLogUseCase` y `GetWeightLogHistoryUseCase`.
    - [x] Endpoints `POST /api/v1/weighlog` y `GET /api/v1/weighlog` (userId del JWT).

---

## 🔴 Fase 5: Calidad, Testing Avanzado y CI/CD

### HU#8: Pipeline de Integración Continua (CI)
- **Como** desarrollador,
- **quiero** que los tests se ejecuten automáticamente en cada `push`,
- **para** garantizar la estabilidad del proyecto antes de desplegar.
- **Estado:** ✅ Completa
- **Criterios de Aceptación:**
    - [x] Configuración de GitHub Actions (`.github/workflows/ci.yml`).
    - [x] Ejecución automática de `mvn clean test` en `master` y `desarrollo`.
    - [x] Publicación de imagen Docker en GHCR al llegar a `master`.

### HU#9: Tests de Integración con Testcontainers
- **Como** desarrollador,
- **quiero** ejecutar tests de integración contra un PostgreSQL real en contenedor,
- **para** validar la capa de persistencia JPA sin usar bases de datos en memoria (H2) para esos casos.
- **Estado:** ✅ Completa
- **Criterios de Aceptación:**
    - [x] Configuración de `Testcontainers` con `PostgreSQLContainer` en `AbstractIntegrationTest`.
    - [x] Tests `*IT` para los 4 adaptadores JPA (`User`, `UserProfile`, `Routine`, `WeightLog`).
    - [x] Perfil `test` (`application-test.yml`) con H2 en memoria y claves *dummy*: `mvn test` no requiere variables de entorno.

---

## ⚪ Fase 6: Infraestructura Cloud y Despliegue en AWS

### HU#10: Containerización de Producción y Registro de Imágenes
- **Como** DevOps / Backend Engineer,
- **quiero** construir una imagen Docker optimizada de la aplicación,
- **para** desplegarla en un registro de contenedores.
- **Estado:** 🟡 Parcial
- **Criterios de Aceptación:**
    - [x] `Dockerfile` multietapa (*multi-stage build*) con Java 17 (`mvn package` en builder, JRE en runtime).
    - [x] Publicación en **GHCR** (GitHub Container Registry) vía CI.
    - [ ] (Alternativa) Publicación en **AWS ECR (Elastic Container Registry)**.
    - [ ] Uso de variables de entorno seguras para secretos (hoy: `JWT_SECRET`, `GEMINI_API_KEY`).

### HU#11: Despliegue Cloud en AWS (RDS + ECS Fargate)
- **Como** usuario final,
- **quiero** acceder a la aplicación desplegada en la nube con alta disponibilidad,
- **para** utilizar el Smart Coach desde cualquier lugar.
- **Estado:** 🔲 Pendiente
- **Criterios de Aceptación:**
    - [ ] Instancia **AWS RDS (PostgreSQL)** en subred privada.
    - [ ] Despliegue del contenedor en **AWS ECS (Elastic Container Service)** con Fargate.
    - [ ] Configuración de variables de entorno seguras con **AWS Secrets Manager / Parameter Store**.

---

## 🧭 Próximos pasos (no priorizados)

- 🍎 **HU#12: Nutrición** — generación de planes de nutrición con IA (mismo patrón que las rutinas).
- 🔐 **Gestión de roles** — mecanismo para crear/promocionar `ADMIN` de forma controlada (seed o endpoint protegido).
- 👤 **Restricción de lectura** — decidir si `GET /api/v1/users/{id}` debe ser visible solo para el dueño/admin.
- 🗄️ **Migraciones** — sustituir `ddl-auto: update` por Flyway/Liquibase antes de producción.
- ⏱️ **Refresh tokens / logout** — gestión de expiración y revocación de sesiones.