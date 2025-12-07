# Uqar Pharmacy SaaS - Documentation Index

Welcome to the official documentation for **Uqar**, a comprehensive Pharmacy SaaS platform. This documentation follows a "Hub and Spoke" structure, providing both high-level architecture overview and detailed module documentation.

## Documentation Structure

### 📚 Hub: Global Architecture (`00-architecture/`)

The hub provides the foundational understanding of the entire system:

1. **[System Overview](00-architecture/system-overview.md)**
   - Technology stack (Spring Boot 3.2.1, Java 21, PostgreSQL)
   - Architecture pattern (Layered Architecture)
   - Main folder structure
   - Key architectural features

2. **[Database Schema](00-architecture/database-schema.md)**
   - ORM approach (Spring Data JPA with Hibernate)
   - Core database entities and relationships
   - Foreign key relationships
   - Database design patterns

3. **[Authentication and Security](00-architecture/auth-and-security.md)**
   - JWT-based authentication flow
   - Role-Based Access Control (RBAC)
   - Security configuration
   - Multi-tenancy enforcement

### 🔧 Spokes: Module Documentation (`01-modules/`)

Each module contains two files:
- **`[module]-summary.md`**: Business logic, workflows, and functional overview
- **`[module]-technical.md`**: Implementation details, data flows, endpoints, and dependencies

#### Foundation Modules

1. **[User Management Module](01-modules/user-management/)**
   - User, employee, and admin management
   - Pharmacy creation and management
   - Customer management and debt tracking
   - Supplier management
   - Role and permission system
   - Authentication and authorization
   - [Summary](01-modules/user-management/user-management-summary.md) | [Technical](01-modules/user-management/user-management-technical.md)

2. **[Language Module](01-modules/language/)**
   - Multi-language support (Arabic/English)
   - Product translation management
   - Language selection for API responses
   - [Summary](01-modules/language/language-summary.md) | [Technical](01-modules/language/language-technical.md)

#### Core Business Modules

3. **[Inventory Module](01-modules/inventory/)**
   - Product and stock management
   - Stock tracking with batch numbers and expiry dates
   - Product search and inventory reports
   - Master products and pharmacy-specific products
   - [Summary](01-modules/inventory/inventory-summary.md) | [Technical](01-modules/inventory/inventory-technical.md)

4. **[Point of Sale (POS) Module](01-modules/pos/)**
   - Sales transaction processing
   - Payment handling (cash/credit)
   - Refunds and cancellations
   - Customer debt integration
   - [Summary](01-modules/pos/pos-summary.md) | [Technical](01-modules/pos/pos-technical.md)

5. **[Purchase Module](01-modules/purchase/)**
   - Purchase order management
   - Purchase invoice creation
   - Automatic stock addition
   - Supplier integration
   - [Summary](01-modules/purchase/purchase-summary.md) | [Technical](01-modules/purchase/purchase-technical.md)

6. **[MoneyBox Module](01-modules/moneybox/)**
   - Financial transaction tracking
   - Cash flow management
   - Dual currency support (SYP/USD)
   - Exchange rate management
   - Complete audit trail
   - [Summary](01-modules/moneybox/moneybox-summary.md) | [Technical](01-modules/moneybox/moneybox-technical.md)

#### Supporting Modules

7. **[Reports Module](01-modules/reports/)**
   - Sales reports
   - Purchase reports
   - Inventory reports
   - Financial reports
   - [Summary](01-modules/reports/reports-summary.md) | [Technical](01-modules/reports/reports-technical.md)

8. **[Notification Module](01-modules/notification/)**
   - Push notifications via Firebase
   - Device token management
   - Alert system (stock, purchase limits)
   - Retry mechanism
   - [Summary](01-modules/notification/notification-summary.md) | [Technical](01-modules/notification/notification-technical.md)

9. **[Complaint Module](01-modules/complaint/)**
   - Issue tracking and management
   - Complaint status workflow (PENDING → IN_PROGRESS → RESOLVED/CLOSED/REJECTED)
   - Response tracking
   - Statistics and reporting
   - Complete audit trail
   - [Summary](01-modules/complaint/complaint-summary.md) | [Technical](01-modules/complaint/complaint-technical.md)

## Quick Navigation

### For Business Users
Start with module summaries to understand:
- What each module does
- Main user workflows
- Business rules and integration points

### For Developers
Start with:
1. [System Overview](00-architecture/system-overview.md) - Understand the tech stack
2. [Database Schema](00-architecture/database-schema.md) - Understand data structure
3. Module technical docs - Understand implementation details

### For System Architects
Review all architecture documents:
1. [System Overview](00-architecture/system-overview.md)
2. [Database Schema](00-architecture/database-schema.md)
3. [Authentication and Security](00-architecture/auth-and-security.md)

## Key Features Documented

✅ **Multi-Tenancy**: Each pharmacy operates in isolated data context  
✅ **Dual Currency**: SYP (primary) and USD (secondary) with real-time conversion  
✅ **Audit Trail**: Complete tracking of all changes with user and timestamp  
✅ **Role-Based Access Control**: Granular permissions system  
✅ **Real-Time Inventory**: Automatic stock updates on sales and purchases  
✅ **Financial Tracking**: Complete MoneyBox transaction history  
✅ **Push Notifications**: Firebase integration for real-time alerts  
✅ **Comprehensive Reporting**: Sales, purchase, inventory, and financial reports  
✅ **Multi-Language Support**: Arabic and English product translations  
✅ **Customer Debt Management**: Complete debt tracking and payment system  
✅ **AOP Logging**: Comprehensive request, audit, and performance logging  

## Module Dependencies

```
User Management Module (Foundation)
    ├── All Modules (authentication, authorization, multi-tenancy)
    │
    ├── Inventory Module
    │   ├── POS Module (stock deduction)
    │   └── Purchase Module (stock addition)
    │
    ├── MoneyBox Module
    │   ├── POS Module (sales payments)
    │   ├── Purchase Module (purchase payments)
    │   └── User Management (debt payments)
    │
    ├── Reports Module
    │   ├── POS Module (sales data)
    │   ├── Purchase Module (purchase data)
    │   ├── Inventory Module (stock data)
    │   └── MoneyBox Module (financial data)
    │
    ├── Notification Module
    │   ├── Inventory Module (stock alerts)
    │   └── Purchase Module (limit alerts)
    │
    └── Complaint Module
        └── User Management (user tracking)

Language Module
    └── Product Module (translations)
```

## Technology Stack Summary

- **Backend**: Spring Boot 3.2.1, Java 21
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Notifications**: Firebase Cloud Messaging
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose

## Getting Started

1. **New to Uqar?** Start with [System Overview](00-architecture/system-overview.md)
2. **Understanding data?** Read [Database Schema](00-architecture/database-schema.md)
3. **Working on a feature?** Check the relevant module documentation
4. **Need API details?** See module technical documentation for endpoints

## Documentation Standards

Each module documentation follows this structure:

### Summary Document
- Objective and problem statement
- User roles
- Core concepts
- Main user workflows
- Key business rules
- Integration points
- Success metrics

### Technical Document
- Data flow architecture
- Key endpoints with side effects
- Service layer components
- Repository layer
- Entity relationships
- Dependencies
- Database queries
- Error handling
- Performance considerations
- Security considerations

## Contributing to Documentation

When adding new features or modules:
1. Create module folder in `01-modules/[module-name]/`
2. Create `[module-name]-summary.md` following the template
3. Create `[module-name]-technical.md` following the template
4. Update this README with the new module

---

## Additional Documentation

- **[AOP Logging System](LOGGING_SYSTEM.md)**: Comprehensive logging and audit system
- **[Fractional Sales Analysis](02-features/fractional-sales-analysis.md)**: Feature analysis for selling products by parts

## Package Information

**Base Package**: `com.Uqar`  
**Application Class**: `UqarApplication`  
**Database Name**: `uqar` (or `teryaq` in some configurations)

---

**Last Updated**: January 2025  
**Version**: 1.1  
**Maintained by**: Uqar Development Team  
**Documentation Status**: Complete - All modules documented

