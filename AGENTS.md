# Agent Context & Instructions - Smart Coach AI Backend

## 1. Contexto del Proyecto
Smart Coach AI es un backend desarrollado en Java 17 y Spring Boot 4 enfocado en la generación personalizada de rutinas y nutrición mediante IA.

## 2. Arquitectura (Hexagonal / Ports & Adapters)
El proyecto sigue aislamiento estricto de capas. **Regra de oro:** El dominio NUNCA depende de Spring Boot ni de JPA.

- `com.smartcoach.backend.domain`: Entidades puras, Value Objects y Excepciones. Sin dependencias externas.
- `com.smartcoach.backend.domain.port`: Interfaces Java para persistencia o servicios externos.
- `com.smartcoach.backend.application.usecase`: Casos de uso de negocio (orquestación pura).
- `com.smartcoach.backend.infrastructure`: Adaptadores JPA, Controladores REST, DTOs, Mappers y Configuración Spring.

## 3. Tech Stack & Herramientas
- **Lenguaje:** Java 17
- **Framework:** Spring Boot 4.1.1 (Web, Data JPA, Validation)
- **Base de Datos:** PostgreSQL 16 (Local vía Docker Compose)
- **Documentación API:** Springdoc OpenAPI 3.1.1 (`/swagger-ui.html`)
- **Testing:** JUnit 5, Mockito, AssertJ


## 4. Estado Actual del Proyecto
- [x] HU#1: CRUD completo de Usuario (POST, GET, PUT, DELETE).
- [x] Adaptador JPA y repositorio en PostgreSQL (Docker).
- [x] Global Exception Handler (`@RestControllerAdvice`) para HTTP 400, 404 y 409.
- [x] Documentación Swagger UI integrada (`2.5.0`).
- [ ] HU#2 (Siguiente paso): Perfil Físico del usuario (`UserProfile`).
## 5. Reglas para Generar Código
1. Manten la separación estricta entre modelos de Dominio y Entidades JPA (`User` vs `UserEntity`).
2. Usa Mappers explícitos en la capa de infraestructura.
3. No uses librerías de Spring dentro del paquete `domain`.
4. Devuelve siempre `Optional<T>` en los métodos de búsqueda del puerto de persistencia.