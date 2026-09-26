## 🏛️ Arquitectura del Sistema: Arquitectura Hexagonal (Ports & Adapters) + DDD

El proyecto sigue los principios de **Arquitectura Hexagonal (Puertos y Adaptadores)** y **Domain-Driven Design (DDD)** para garantizar que la lógica de negocio permanezca 100% aislada e independiente de marcos de trabajo (Spring Boot), bases de datos o bibliotecas externas.

### Diagrama de Capas

### Organización del Código
- **Domain (`domain`)**: Entidades de negocio, Objetos de Valor (*Value Objects*) inmutables con validación en construcción, y Puertos (*Interfaces*).
- **Application (`application`)**: Casos de uso que orquestan la lógica de negocio sin depender de detalles de infraestructura.
- **Infrastructure (`infrastructure`)**:
  - **Adapters In**: Controladores REST y DTOs de entrada/salida.
  - **Adapters Out**: Persistencia JPA, repositorios PostgreSQL y adaptadores de seguridad (`BCryptPasswordEncoderAdapter`, `JwtService`).
  - **Security & Config**: Filtros HTTP (`JwtAuthenticationFilter`), configuración de Spring Security 6 Stateless y OpenAPI/Swagger.

---

## 🔐 Flujo de Autenticación y Autorización (JWT Stateless)

La seguridad del sistema está construida con **Spring Security 6** utilizando una estrategia **Stateless** basada en tokens JWT (*JSON Web Tokens*).

<Image src="image_agent_tag_14536601195159773224" alt="Diagrama de secuencia de autenticación y autorización con JWT" caption="Flujo de autenticación e intercambio de tokens JWT" />

---

### Diagrama de Secuencia del Flujo

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Cliente (Postman / Frontend)
    participant AuthCtrl as AuthController
    participant AuthUC as AuthenticateUserUseCase
    participant UserRepo as UserRepository (Puerto)
    participant TokenPort as TokenProviderPort (Puerto)
    participant JwtFilter as JwtAuthenticationFilter
    participant ProtectedCtrl as Protected Controller

    %% Flujo 1: Login
    rect rgb(240, 248, 255)
    note over Cliente, TokenPort: Flujo de Autenticación (Login)
    Cliente->>AuthCtrl: POST /api/v1/auth/login (email, password)
    AuthCtrl->>AuthUC: execute(email, password)
    AuthUC->>UserRepo: findByEmail(email)
    UserRepo-->>AuthUC: User Entity
    AuthUC->>AuthUC: Validar contraseña hash (BCrypt)
    AuthUC->>TokenPort: generateToken(userId, email)
    TokenPort-->>AuthUC: JWT Token (String)
    AuthUC-->>AuthCtrl: Token
    AuthCtrl-->>Cliente: HTTP 200 OK { "token": "eyJhbG..." }
    end

    %% Flujo 2: Petición Protegida
    rect rgb(245, 255, 250)
    note over Cliente, ProtectedCtrl: Flujo de Autorización (Petición Protegida)
    Cliente->>JwtFilter: GET /api/v1/users/{id}/profile<br/>Header: Authorization: Bearer eyJhbG...
    JwtFilter->>JwtFilter: Extraer y validar firma JWT
    alt Token Válido
        JwtFilter->>JwtFilter: Extraer userId y establecer en SecurityContextHolder
        JwtFilter->>ProtectedCtrl: Continuar cadena (filterChain.doFilter)
        ProtectedCtrl-->>Cliente: HTTP 200 OK (Datos protegidos)
    else Token Inválido / Ausente
        JwtFilter-->>Cliente: HTTP 401 Unauthorized / HTTP 403 Forbidden
    end
    end
