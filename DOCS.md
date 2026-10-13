# Documentación de Estructura - Smart Coach AI (Backend)

Este documento describe la estructura de carpetas del proyecto y el propósito de cada paquete.
El backend sigue una **Arquitectura Hexagonal (Ports & Adapters)** con separación estricta de capas:
el dominio es puro y **nunca** depende de Spring ni de JPA.

## Vista General

```text
backend/
├── .github/
│   └── workflows/
│       └── ci.yml                     # Pipeline de Integración Continua (GitHub Actions)
├── .mvn/
│   └── wrapper/                       # Wrapper de Maven (mvnw / mvnw.cmd)
├── docs/
│   ├── ARCHITECTURE.md                # Diagrama y flujo de la arquitectura hexagonal
│   └── ROADMAP.md                     # Historias de Usuario (HU) y fases del proyecto
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/smartcoach/backend/
│   │   │       ├── application/       # Capa de aplicación (orquestación)
│   │   │       ├── domain/            # Núcleo del negocio (sin dependencias externas)
│   │   │       ├── infrastructure/    # Adaptadores (REST, JPA, IA, seguridad, config)
│   │   │       └── BackendApplication.java
│   │   └── resources/
│   │       └── application.yml        # Configuración principal de Spring Boot
│   └── test/                          # Pruebas unitarias y de integración
├── AGENTS.md                          # Contexto e instrucciones para agentes/herramientas
├── Dockerfile                         # Imagen Docker multi-etapa para producción
├── docker-compose.yml                 # PostgreSQL 16 + pgAdmin local para desarrollo
├── pom.xml                            # Dependencias y configuración Maven
├── mvnw / mvnw.cmd                    # Scripts del wrapper de Maven
└── DOCS.md                            # Este documento
```

## Descripción de Paquetes

### `application` — Capa de Aplicación
Orquesta la lógica de negocio invocando el dominio y los puertos. No conoce detalles de infraestructura.

```text
application/
└── usecase/                           # Casos de uso (un archivo por operación de negocio)
    ├── RegisterUserCase.java
    ├── UpdateUserCase.java
    ├── DeleteUserCase.java
    ├── GetUserByIdCase.java
    ├── AuthenticateUserUseCase.java
    ├── CreateOrUpdateUserProfileCase.java
    ├── GetUserProfileByIdCase.java
    ├── GenerateWorkoutRoutineUseCase.java
    ├── CreateRoutineUseCase.java
    ├── GetRoutinesByUserIdCase.java
    ├── CreateWeightLogUseCase.java
    └── GetWeightLogHistoryUseCase.java
```

### `domain` — Núcleo del Dominio
Entidades, Value Objects, excepciones y puertos. **Sin dependencias de framework.**

```text
domain/
├── exception/                         # Excepciones de negocio
│   ├── ForbiddenOperationException.java
│   ├── UserNotFoundException.java
│   ├── UserAlreadyExistsExpection.java
│   └── UserProfileNotFoundException.java
├── model/                             # Entidades puras del dominio
│   ├── User.java
│   ├── UserProfile.java
│   ├── Routine.java
│   ├── Exercise.java
│   └── WeightLog.java
├── port/                              # Interfaces (Puertos) de persistencia y servicios
│   ├── UserRepository.java
│   ├── UserProfileRepository.java
│   ├── RoutineRepository.java
│   ├── WeightLogRepository.java
│   ├── AiCoachPort.java
│   ├── TokenProviderPort.java
│   └── PasswordEncoderPort.java
└── vo/                                # Value Objects con validaciones
    ├── Email.java
    ├── Password.java
    ├── PasswordHash.java
    ├── Age.java
    ├── Weight.java
    ├── Height.java
    ├── ActivityLevel.java
    ├── FitnessGoal.java
    ├── RoutineGoal.java
    ├── ExerciseCategory.java
    ├── ExerciseDetails.java
    └── Role.java                      # Rol del usuario (USER | ADMIN)
```

### `infrastructure` — Adaptadores
Implementaciones concretas de los puertos y puntos de entrada/salida del sistema.

```text
infrastructure/
├── ai/                                # Adaptadores de IA (LLM)
│   ├── GeminiAiAdapter.java           # @Primary, usa Spring AI + Gemini
│   ├── MockAiAdapter.java             # Rutina de ejemplo determinista
│   └── dto/                           # Estructuras de respuesta de la IA
│       ├── RoutineAiResponse.java
│       └── ExerciseAiResponse.java
├── config/                            # Configuración Spring
│   ├── BeanConfiguration.java         # Inyección manual de los casos de uso
│   └── OpenApiConfig.java
├── persistence/                       # Adaptadores de persistencia (JPA)
│   ├── JpaUserRepositoryAdapter.java
│   ├── JpaUserProfileRepositoryAdapter.java
│   ├── JpaRoutineRepositoryAdapter.java
│   ├── JpaWeightLogRepositoryAdapter.java
│   ├── entity/                        # Entidades JPA (@Entity)
│   │   ├── UserEntity.java
│   │   ├── UserProfileEntity.java
│   │   ├── RoutineEntity.java
│   │   ├── ExerciseEntity.java
│   │   └── WeightLogEntity.java
│   ├── mapper/                        # Mappers Dominio <-> Entidad
│   │   ├── UserPersistenceMapper.java
│   │   ├── UserProfilePersistenceMapper.java
│   │   ├── RoutineMapper.java
│   │   └── WeightLogMapper.java
│   └── repository/                    # Repositorios Spring Data JPA
│       ├── SpringDataUserRepository.java
│       ├── SpringDataUserProfileRepository.java
│       ├── SpringDataRoutineRepository.java
│       └── SpringDataWeightLogRepository.java
├── rest/                              # Controladores REST y DTOs
│   ├── AuthController.java            # /api/v1/auth (register, login)
│   ├── UserController.java            # /api/v1/users (CRUD + permisos)
│   ├── UserProfileController.java     # /api/v1/users (perfil) y /profile
│   ├── RoutineController.java         # /api/v1/routines
│   ├── WeightLogController.java       # /api/v1/weighlog
│   ├── dto/                           # Request/Response DTOs
│   │   ├── AuthRequest.java / AuthResponse.java
│   │   ├── RegisterUserRequest.java / UserResponse.java
│   │   ├── CreateUserProfileRequest.java / UserProfileResponse.java
│   │   ├── CreateRoutineRequest.java / RoutineResponse.java
│   │   ├── ExerciseRequest.java / ExerciseResponse.java
│   │   ├── GenerateRoutineRequest.java
│   │   ├── WeightLogRequest.java / WeightLogResponse.java
│   │   └── ErrorResponse.java
│   └── exception/
│       └── GlobalExceptionHandler.java    # 400 / 401 / 403 / 404 / 409
└── security/                          # Seguridad y JWT
    ├── SecurityConfig.java            # Reglas de autorización por ruta
    ├── JwtService.java                # Generación/validación de tokens (jjwt)
    ├── JwtAuthenticatorFilter.java    # Filtro Bearer -> autenticación Spring
    ├── AuthenticatedUser.java         # userId y rol desde el SecurityContext
    └── BcryptPasswordEncodeAdapter.java
```

### `src/main/resources`
```text
resources/
└── application.yml                    # Configuración principal de Spring Boot
```

Los secretos (`jwt.secret`, `spring.ai.google.genai.api-key`) se inyectan por
variables de entorno (`JWT_SECRET`, `GEMINI_API_KEY`).

## Pruebas (`src/test`)

```text
test/
├── java/com/smartcoach/backend/
│   ├── BackendApplicationTests.java       # Test de arranque del contexto (perfil `test`, H2)
│   ├── application/usecase/               # Tests unitarios de casos de uso (Mockito)
│   │   ├── RegisterUserCaseTest.java
│   │   ├── UpdateUserCaseTest.java
│   │   ├── DeleteUserCaseTest.java
│   │   ├── GetUserByIdCaseTest.java
│   │   ├── AuthenticateUserUseCaseTest.java
│   │   ├── CreateOrUpdateUserProfileUseCaseTest.java
│   │   ├── GetUserProfileByIdCaseTest.java
│   │   ├── GenerateWorkoutRoutineUseCaseTest.java
│   │   ├── CreateRoutineUseCaseTest.java
│   │   ├── GetRoutinesByUserIdCaseTest.java
│   │   ├── LogWeightUseCaseTest.java      # SUT: CreateWeightLogUseCase
│   │   └── GetWeightHistoryUseCaseTest.java # SUT: GetWeightLogHistoryUseCase
│   ├── domain/
│   │   └── vo/WeightTest.java             # Tests de Value Objects (AssertJ)
│   └── infrastructure/
│       ├── AbstractIntegrationTest.java   # Base con PostgreSQL de Testcontainers
│       └── persistence/                   # Tests de integración (*IT)
│           ├── UserRepositoryAdapterITest.java
│           ├── UserProfileRepositoryAdapterITest.java
│           ├── RoutineRepositoryAdapterITest.java
│           └── WeightLogRepositoryAdapterITest.java
└── resources/
    ├── application-test.yml               # Perfil `test`: H2 en memoria + claves dummy
    └── docker-java.properties             # Ajuste de API version para Testcontainers
```

> Los tests unitarios usan `@ExtendWith(MockitoExtension.class)` y mockean **solo** los
> puertos de salida; las entidades de dominio y los Value Objects se instancian reales.

## Reglas Arquitectónicas Clave

1. El paquete `domain` **no** debe importar clases de Spring ni de JPA.
2. Los modelos de dominio y las entidades JPA se mantienen separados (`User` vs `UserEntity`).
3. Los mappers se implementan explícitamente en `infrastructure/persistence/mapper`.
4. Los métodos de búsqueda de los puertos de persistencia devuelven `Optional<T>`.
5. La comunicación entre capas se realiza siempre a través de los **puertos** del dominio.

## Referencias

- `docs/ARCHITECTURE.md`: diagrama detallado del flujo hexagonal.
- `docs/ROADMAP.md`: historias de usuario y estado de avance.
- `AGENTS.md`: reglas y contexto para el desarrollo asistido.