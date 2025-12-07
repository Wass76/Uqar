# System Overview

## High-Level Architecture

**Uqar** is a comprehensive Pharmacy SaaS (Software as a Service) platform built using **Spring Boot 3.2.1** with **Java 21**. The system follows a **layered architecture pattern** (similar to MVC) with clear separation between controllers, services, repositories, and entities.

## Technology Stack

### Core Framework
- **Spring Boot 3.2.1** - Main application framework
- **Java 21** - Programming language
- **Maven** - Build and dependency management

### Database & ORM
- **PostgreSQL** - Primary relational database
- **Spring Data JPA** - ORM layer for database operations
- **Hibernate** - JPA implementation
- **Flyway** - Database migration tool (currently disabled in production, baseline version: 8)

### Security & Authentication
- **Spring Security** - Security framework
- **JWT (JSON Web Tokens)** - Stateless authentication using `jjwt` library (v0.11.5)
- **Role-Based Access Control (RBAC)** - Permission-based authorization system

### API Documentation
- **SpringDoc OpenAPI** (v2.4.0) - Swagger UI for API documentation
- **Swagger UI** - Interactive API testing interface

### Additional Technologies
- **MapStruct** (v1.5.5) - DTO mapping and transformation
- **Lombok** - Code generation for boilerplate
- **EhCache** - Caching layer
- **Firebase Admin SDK** (v9.4.0) - Push notifications
- **Resilience4j** - Rate limiting and circuit breaking
- **Jackson** - JSON serialization/deserialization with JSR310 support

### Infrastructure
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **PostgreSQL** (latest) - Database container

## Architecture Pattern

The system follows a **Layered Architecture** with the following structure:

```
┌─────────────────────────────────────┐
│      Controller Layer (REST API)    │
│  - Handles HTTP requests/responses  │
│  - Input validation                 │
│  - Authorization checks             │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│        Service Layer (Business)      │
│  - Business logic                   │
│  - Transaction management           │
│  - Cross-cutting concerns           │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│    Repository Layer (Data Access)    │
│  - JPA repositories                 │
│  - Custom queries                   │
│  - Database operations              │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│      Entity Layer (Domain Model)    │
│  - JPA entities                     │
│  - Relationships                    │
│  - Business rules                   │
└─────────────────────────────────────┘
```

## Main Folder Structure

```
src/main/java/com/Uqar/
├── UqarApplication.java          # Main entry point
│
├── config/                          # Configuration classes
│   ├── SecurityConfiguration.java   # Spring Security setup
│   ├── JwtAuthenticationFilter.java  # JWT token validation
│   ├── JwtService.java              # JWT generation/validation
│   ├── ApplicationConfig.java       # Application beans
│   ├── OpenApiConfig.java           # Swagger configuration
│   └── RateLimiterConfig.java       # Rate limiting setup
│
├── user/                            # User Management Module
│   ├── entity/                      # User, Employee, Pharmacy, Customer, Supplier, Role, Permission
│   ├── controller/                  # REST controllers
│   ├── service/                     # Business logic
│   ├── repository/                  # Data access
│   ├── dto/                         # Data Transfer Objects
│   └── mapper/                      # MapStruct mappers
│
├── product/                         # Product & Inventory Module
│   ├── entity/                      # MasterProduct, PharmacyProduct, StockItem, Category, etc.
│   ├── controller/                  # Product management APIs
│   ├── service/                     # Stock management, product services
│   ├── repo/                        # Repositories
│   └── dto/                         # Product DTOs
│
├── sale/                           # Point of Sale (POS) Module
│   ├── entity/                      # SaleInvoice, SaleInvoiceItem
│   ├── controller/                  # SaleController
│   ├── service/                     # SaleService
│   └── dto/                         # Sale DTOs
│
├── purchase/                       # Purchase Management Module
│   ├── entity/                      # PurchaseOrder, PurchaseInvoice
│   ├── controller/                  # Purchase controllers
│   ├── service/                     # Purchase services
│   └── dto/                         # Purchase DTOs
│
├── moneybox/                       # Financial Management Module
│   ├── entity/                      # MoneyBox, MoneyBoxTransaction, ExchangeRate
│   ├── controller/                  # MoneyBox APIs
│   ├── service/                     # Financial services
│   └── dto/                         # MoneyBox DTOs
│
├── reports/                        # Reporting Module
│   ├── controller/                  # Report controllers
│   ├── service/                     # Report generation
│   └── dto/                         # Report DTOs
│
├── notification/                   # Notification Module
│   ├── entity/                      # Notification, DeviceToken
│   ├── controller/                  # Notification APIs
│   ├── service/                     # Firebase messaging
│   └── scheduler/                   # Scheduled notifications
│
├── complaint/                      # Complaint Management Module
│   ├── entity/                      # Complaint
│   ├── controller/                  # Complaint APIs
│   ├── service/                     # Complaint handling
│   ├── repository/                  # ComplaintRepository
│   ├── dto/                         # Complaint DTOs
│   ├── mapper/                      # ComplaintMapper
│   └── enums/                       # ComplaintStatus
│
├── language/                       # Multi-language Support
│   └── Language.java                # Language entity
│
└── utils/                          # Shared Utilities
    ├── exception/                   # Custom exceptions
    ├── entity/                      # Base entities (AuditedEntity)
    ├── auditing/                    # Audit trail support
    └── Validator/                   # Custom validators
```

## Key Architectural Features

### 1. Multi-Tenancy
- **Pharmacy-based isolation**: Each pharmacy operates in its own data context
- **Employee association**: Users are linked to specific pharmacies
- **Data segregation**: All queries filter by `pharmacy_id`

### 2. Audit Trail
- **JPA Auditing**: Automatic tracking of `createdBy`, `updatedBy`, `createdAt`, `updatedAt`
- **Custom AuditorAware**: Tracks current user for audit fields
- **AuditedEntity base class**: All entities extend this for audit support

### 3. Caching Strategy
- **EhCache**: Configured for performance optimization
- **@EnableCaching**: Caching annotations enabled at application level

### 4. Async Processing
- **@EnableAsync**: Asynchronous method execution
- **@EnableScheduling**: Scheduled tasks (e.g., notification retries)

### 5. Aspect-Oriented Programming
- **@EnableAspectJAutoProxy**: AOP support for cross-cutting concerns

### 6. Dual Currency Support
- **SYP (Syrian Pound)** - Primary currency stored in database
- **USD (US Dollar)** - Secondary currency with real-time conversion
- **ExchangeRate entity**: Tracks conversion rates
- **Currency conversion service**: Automatic price conversion in responses

## Application Entry Point

**`UqarApplication.java`** is the main entry point with the following enabled features:

```java
@SpringBootApplication
@EnableAspectJAutoProxy      // AOP support
@EnableCaching                // Cache management
@EnableAsync                  // Async processing
@EnableScheduling             // Scheduled tasks
@EnableJpaAuditing            // Audit trail
@EnableWebSecurity            // Security
```

## Server Configuration

- **Port**: 3002 (development), 3000 (Docker container)
- **Database**: PostgreSQL on port 5432 (15432 for Docker host access)
- **Context Path**: Root (`/`)

## Deployment

- **Docker**: Multi-stage build with Maven and JDK 21
- **Docker Compose**: Orchestrates application and database containers
- **Health Checks**: Database health check ensures readiness before app startup
- **Resource Limits**: CPU and memory limits configured for stability

