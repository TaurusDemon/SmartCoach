# Roadmap & Historias de Usuario - Smart Coach AI

Este documento detalla la evolución técnica y de negocio del backend de Smart Coach AI, organizado por fases e Historias de Usuario (HU) bajo Arquitectura Hexagonal.

---

## 🟢 Fase 1: Dominio Base y Gestión de Usuarios

### HU#1: Registro de Usuario Base
- **Como** nuevo usuario,
- **quiero** registrarme con mi nombre y correo electrónico,
- **para** crear una cuenta en la plataforma.
- **Estado:** 🟢 Completado
- **Criterios de Aceptación:**
    - [x] Validación de formato de email mediante Value Object (`Email`).
    - [x] Prevención de registros duplicados (`UserAlreadyExistsException`).
    - [x] Persistencia en PostgreSQL mediante `JpaUserRepositoryAdapter`.
    - [x] Manejo de excepciones HTTP (400, 409) con `@RestControllerAdvice`.
    - [x] Documentación de endpoints en Swagger UI.

### HU#2: Registro y Actualización de Perfil Físico
- **Como** usuario registrado,
- **quiero** ingresar mis datos físicos (edad, peso, altura, nivel de actividad y objetivo),
- **para** que el motor de IA disponga del contexto necesario para mis recomendaciones.
- **Estado:** 🟡 Siguiente Paso
- **Criterios de Aceptación:**
    - [ ] Value Objects con validaciones de rango: `Weight` (kg), `Height` (cm), `Age`, `ActivityLevel` (enum), `FitnessGoal` (enum: Perder peso, Ganar músculo, Mantenimiento).
    - [ ] Entidad de dominio `UserProfile` asociada a `User` (1:1).
    - [ ] Caso de uso `CreateOrUpdateUserProfileCase`.
    - [ ] Endpoint `POST /api/v1/users/{userId}/profile`.

---

## 🔵 Fase 2: Autenticación, Seguridad y Gestión de Sesión

### HU#3: Autenticación y Emisión de Tokens JWT
- **Como** usuario registrado,
- **quiero** iniciar sesión con mis credenciales,
- **para** obtener un token de acceso seguro (JWT) y consultar mi información.
- **Criterios de Aceptación:**
    - [ ] Integración de Spring Security 6 con filtro JWT sin estado (*stateless*).
    - [ ] Almacenamiento seguro de contraseñas con `BCryptPasswordEncoder`.
    - [ ] Caso de uso `AuthenticateUserCase`.
    - [ ] Endpoint `POST /api/v1/auth/login`.

### HU#4: Autorización de Endpoints por Usuario
- **Como** sistema de seguridad,
- **quiero** validar que cada petición sobre un recurso pertenezca al usuario autenticado,
- **para** evitar vulnerabilidades de acceso directo a objetos (IDOR).
- **Criterios de Aceptación:**
    - [ ] Extracción del `userId` directamente desde el Token JWT en los controladores.
    - [ ] Bloqueo de accesos no autorizados devolviendo `HTTP 401 Unauthorized` / `HTTP 403 Forbidden`.

---

## 🟣 Fase 3: Motor de IA y Recomendaciones Personalizadas

### HU#5: Generación de Rutina de Entrenamiento con IA
- **Como** usuario con perfil físico configurado,
- **quiero** solicitar una rutina de entrenamiento personalizada,
- **para** tener un plan de ejercicios semanal adaptado a mi objetivo.
- **Criterios de Aceptación:**
    - [ ] Definición del Puerto de Salida `AiCoachPort`.
    - [ ] Adaptador `OpenAiAdapter` / `SpringAiAdapter` para comunicación con LLM.
    - [ ] *Prompt Engineering* en infraestructura para forzar respuestas estrictas en formato JSON.
    - [ ] Mapeo del JSON de respuesta a la entidad de dominio `WorkoutRoutine`.
    - [ ] Caso de uso `GenerateWorkoutRoutineCase`.
    - [ ] Endpoint `POST /api/v1/users/{userId}/routines/generate`.

### HU#6: Historial y Consulta de Rutinas Guardadas
- **Como** usuario,
- **quiero** consultar las rutinas generadas anteriormente,
- **para** seguir mi plan de entrenamiento día a día.
- **Criterios de Aceptación:**
    - [ ] Persistencia de rutinas y ejercicios en PostgreSQL (`WorkoutRoutineEntity`, `ExerciseEntity`).
    - [ ] Caso de uso `GetUserRoutinesCase`.
    - [ ] Endpoint `GET /api/v1/users/{userId}/routines`.

---

## 🟠 Fase 4: Registro de Progreso y Métricas

### HU#7: Registro de Pesajes y Evolución Física
- **Como** usuario en seguimiento,
- **quiero** registrar mi peso periódicamente,
- **para** visualizar mi evolución física a lo largo del tiempo.
- **Criterios de Aceptación:**
    - [ ] Entidad de dominio `WeightLog` (peso, fecha, notas).
    - [ ] Caso de uso `LogWeightCase` y `GetWeightHistoryCase`.
    - [ ] Endpoint `POST /api/v1/users/{userId}/weight-logs`.

---

## 🔴 Fase 5: Calidad, Testing Avanzado y CI/CD

### HU#8: Pipeline de Integración Continua (CI)
- **Como** desarrollador,
- **quiero** que los tests se ejecuten automáticamente en cada `push`,
- **para** garantizar la estabilidad del proyecto antes de desplegar.
- **Criterios de Aceptación:**
    - [ ] Configuración de GitHub Actions (`.github/workflows/ci.yml`).
    - [ ] Ejecución automática de `mvn clean test`.

### HU#9: Tests de Integración con Testcontainers
- **Como** desarrollador,
- **quiero** ejecutar tests de integración contra un PostgreSQL real en contenedor,
- **para** validar la capa de persistencia JPA sin usar bases de datos en memoria (H2).
- **Criterios de Aceptación:**
    - [ ] Configuración de `Testcontainers` con `PostgreSQLContainer` para los repositorios JPA.

---

## ⚪ Fase 6: Infraestructura Cloud y Despliegue en AWS

### HU#10: Containerización de Producción y ECR
- **Como** DevOps / Backend Engineer,
- **quiero** construir una imagen Docker optimizada de la aplicación,
- **para** subirla al registro privado de AWS (ECR).
- **Criterios de Aceptación:**
    - [ ] `Dockerfile` multietapa (*multi-stage build*) optimizado para producción con Java 17.
    - [ ] Configuración de repositorio en **AWS ECR (Elastic Container Registry)**.

### HU#11: Despliegue Cloud en AWS (RDS + ECS Fargate)
- **Como** usuario final,
- **quiero** acceder a la aplicación desplegada en la nube con alta disponibilidad,
- **para** utilizar el Smart Coach desde cualquier lugar.
- **Criterios de Aceptación:**
    - [ ] Instancia **AWS RDS (PostgreSQL)** en subred privada.
    - [ ] Despliegue del contenedor en **AWS ECS (Elastic Container Service)** con Fargate.
    - [ ] Configuración de variables de entorno seguras con **AWS Secrets Manager / Parameter Store**.