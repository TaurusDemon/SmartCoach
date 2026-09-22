# Arquitectura del Sistema: Smart Coach AI

## Diagrama de Flujo Hexagonal

```text
[ HTTP Request ]
       │
       ▼
┌────────────────────────────────────────────────────────┐
│ INFRASTRUCTURE (REST)                                  │
│ UserController ──► RegisterUserRequest (DTO)           │
└──────┬─────────────────────────────────────────────────┘
       │
       ▼
┌────────────────────────────────────────────────────────┐
│ APPLICATION (Use Cases)                                │
│ RegisterUserCase.execute("Name", "email@domain.com")   │
└──────┬─────────────────────────────────────────────────┘
       │
       ├──► Validaciones de Dominio (Email VO)
       │
       ▼
┌────────────────────────────────────────────────────────┐
│ DOMAIN PORTS (Interfaces)                              │
│ UserRepository.findByEmail() / save()                  │
└──────┬─────────────────────────────────────────────────┘
       │
       ▼ (Implementación)
┌────────────────────────────────────────────────────────┐
│ INFRASTRUCTURE (Persistence)                           │
│ JpaUserRepositoryAdapter ──► SpringDataUserRepository │
│                                       │                │
│                                       ▼                │
│                                PostgreSQL (Docker)     │
└────────────────────────────────────────────────────────┘