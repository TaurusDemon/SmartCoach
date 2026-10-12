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
│   │   │       ├── BackendApplication.java
│   │   │       └── Main.java
│   │   └── resources/                 # Configuración y recursos estáticos
│   └── test/                          # Pruebas unitarias y de integración
├── AGENTS.md                          # Contexto e instrucciones para agentes/herramientas
├── Dockerfile                         # Imagen Docker multi-etapa para producción
├── docker-compose.yml                 # PostgreSQL 16 local para desarrollo
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
    ├── Weight.java
    ├── Height.java
    ├── Age.java
    ├── ActivityLevel.java
    ├── FitnessGoal.java
    ├── RoutineGoal.java
    ├── ExerciseCategory.java
    └── ExerciseDetails.java
```

### `infrastructure` — Adaptadores
Implementaciones concretas de los puertos y puntos de entrada/salida del sistema.

```text
infrastructure/
├── ai/                                # Adaptadores de IA (LLM)
│   ├── MockAiAdapter.java
│   ├── GeminiAiAdapter.java
│   └── dto/                           # Estructuras de respuesta de la IA
│       ├── RoutineAiResponse.java
│       └── ExerciseAiResponse.java
├── config/                            # Configuración Spring
│   ├── BeanConfiguration.java
│   └── OpenApiConfig.java
├── persistence/                       # Adaptadores de persistencia (JPA)
│   ├── JpaUserRepositoryAdapter.java
│   ├── JpaUserProfileRepositoryAdapter.java
│   ├── JpaRoutineRepositoryAdapter.java
│   ├── JpaWeightLogRepositoryAdapter.java
│   ├── InMemoryUserRepository.java
│   ├── entity/                        # Entidades JPA (@Entity)
│   ├── mapper/                        # Mappers Dominio <-> Entidad
│   └── repository/                    # Repositorios Spring Data
├── rest/                              # Controladores REST y DTOs
│   ├── UserController.java
│   ├── UserProfileController.java
│   ├── RoutineController.java
│   ├── WeightLogController.java
│   ├── AuthController.java
│   ├── dto/                           # Request/Response DTOs
│   └── exception/
│       └── GlobalExceptionHandler.java
└── security/                          # Seguridad y JWT
    ├── SecurityConfig.java
    ├── JwtService.java
    ├── JwtAuthenticatorFilter.java
    ├── AuthenticatedUser.java
    └── BcryptPasswordEncodeAdapter.java
```

### `src/main/resources`
```text
resources/
├── application.yml                    # Configuración principal de Spring Boot
├── application.properties
├── static/                            # Recursos estáticos
└── templates/                         # Plantillas (si aplica)
```

## Pruebas (`src/test`)

```text
test/
├── java/com/smartcoach/backend/
│   ├── BackendApplicationTests.java   # Test de arranque del contexto
│   ├── application/usecase/           # Tests unitarios de casos de uso (Mockito)
│   ├── domain/vo/                     # Tests de Value Objects (AssertJ)
│   └── infrastructure/
│       ├── AbstractIntegrationTest.java
│       └── persistence/               # Tests de integración (*IT) con Testcontainers
└── resources/
    ├── application-test.yml
    └── docker-java.properties
```

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
