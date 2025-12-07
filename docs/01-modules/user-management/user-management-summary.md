# User Management Module - Summary

## Objective

The **User Management Module** is the foundation of the Uqar system, providing comprehensive user, pharmacy, customer, supplier, and role management. It solves the critical problem of managing all actors in the pharmacy ecosystem, from platform administrators to customers, with proper authentication, authorization, and multi-tenancy support.

## Problem Statement

Pharmacies need to:
- Manage platform administrators and pharmacy users
- Create and manage pharmacy organizations (main and branch pharmacies)
- Manage employees with different roles and permissions
- Track customers and their purchase history
- Manage suppliers for procurement
- Control access through role-based permissions
- Support multi-tenancy (each pharmacy isolated)
- Track customer debts and payments
- Manage geographic areas for pharmacy locations

## User Roles

### Primary Users

1. **Platform Admin**
   - Full system access
   - Can create pharmacies
   - Can manage all users across all pharmacies
   - Can manage roles and permissions
   - Can view all data

2. **Pharmacy Manager**
   - Full access to their pharmacy
   - Can manage employees
   - Can manage customers
   - Can manage suppliers
   - Can view customer debts

3. **Pharmacy Employee**
   - Can view customers
   - Can view suppliers
   - Limited management capabilities

## Core Concepts

### User Hierarchy

**BaseUser** (Abstract):
- Common fields: email, password, firstName, lastName
- Base for all user types

**User** (Base Entity):
- Extends BaseUser
- Has role and permissions
- Status: active/inactive
- Position

**Employee** (extends User):
- Linked to a pharmacy
- Has working hours
- Date of hire
- Phone number

**Admin** (extends User):
- Platform-level access
- Can manage all pharmacies

### Pharmacy

A **Pharmacy** represents a pharmacy organization:
- Can be MAIN or BRANCH type
- Has license number (unique)
- Has address, email, phone
- Linked to geographic Area
- Has employees
- Has customers
- Has one MoneyBox

### Customer

A **Customer** represents a pharmacy customer:
- Can be "cash customer" (default) or named customer
- Has phone number, address, notes
- Linked to pharmacy (multi-tenancy)
- Has debt records (CustomerDebt)

### Supplier

A **Supplier** represents a product supplier:
- Has name, contact info, address
- Linked to purchase orders
- Used in procurement process

### Role and Permissions

**Role**:
- System-defined or custom roles
- Has set of permissions
- Examples: PHARMACY_MANAGER, PHARMACY_EMPLOYEE, PLATFORM_ADMIN

**Permission**:
- Granular permissions: RESOURCE:ACTION
- Examples: PRODUCT:CREATE, SALE:READ
- Can be assigned to roles or users directly

### Customer Debt

**CustomerDebt** tracks customer credit:
- Links to customer
- Links to sale invoice (if from credit sale)
- Tracks amount, paid amount, remaining amount
- Status: PENDING, PARTIALLY_PAID, PAID
- Supports debt payments

## Main User Workflows

### 1. Pharmacy Creation Workflow

**Scenario**: Platform admin creates a new pharmacy.

1. **Admin Login**: Admin logs in via `/api/v1/admin/login`
2. **Create Pharmacy**: Admin creates pharmacy
   - Enter pharmacy details (name, license, address)
   - Select pharmacy type (MAIN/BRANCH)
   - Select geographic area
3. **System Creates**: 
   - Pharmacy record created
   - MoneyBox automatically created for pharmacy
4. **Confirmation**: Pharmacy ready for use

**Outcome**: New pharmacy created, ready for employee assignment.

### 2. Employee Registration Workflow

**Scenario**: Pharmacy manager adds a new employee.

1. **Select Employee**: Manager navigates to employee management
2. **Create Employee**: 
   - Enter employee details (name, email, phone)
   - Select role (PHARMACY_MANAGER, PHARMACY_EMPLOYEE, etc.)
   - Set working hours
   - Set date of hire
3. **Assign Permissions**: Optionally add additional permissions
4. **System Creates**: Employee account created
5. **Employee Login**: Employee can now log in

**Outcome**: Employee registered, can access system.

### 3. Customer Registration Workflow

**Scenario**: Employee registers a new customer.

1. **Create Customer**: Employee creates customer
   - Enter name (or use "cash customer" default)
   - Enter phone number (optional)
   - Enter address (optional)
   - Add notes (optional)
2. **System Creates**: Customer linked to pharmacy
3. **Ready for Sales**: Customer can now make purchases

**Outcome**: Customer registered, can make credit purchases.

### 4. Customer Debt Payment Workflow

**Scenario**: Customer pays their debt.

1. **View Debts**: Employee views customer debts
2. **Select Debt**: Employee selects debt to pay
3. **Enter Payment**: 
   - Enter payment amount
   - Select payment method
4. **Process Payment**: 
   - System records payment
   - Updates debt status
   - Records MoneyBox transaction (if cash)
5. **Confirmation**: Debt updated, payment recorded

**Outcome**: Customer debt reduced, payment tracked.

### 5. Supplier Registration Workflow

**Scenario**: Manager adds a new supplier.

1. **Create Supplier**: Manager creates supplier
   - Enter supplier name
   - Enter contact information
   - Enter address
2. **System Creates**: Supplier record created
3. **Ready for Purchases**: Supplier can be used in purchase orders

**Outcome**: Supplier registered, available for procurement.

### 6. Role and Permission Management Workflow

**Scenario**: Platform admin manages roles.

1. **View Roles**: Admin views all roles
2. **Create/Edit Role**: 
   - Define role name and description
   - Assign permissions
   - Set as system role or custom
3. **Assign to Users**: Assign role to users
4. **Additional Permissions**: Add extra permissions to specific users

**Outcome**: Access control configured.

## Key Business Rules

1. **Multi-Tenancy**: All entities linked to pharmacy (employees, customers, etc.)
2. **Pharmacy Isolation**: Users can only access their pharmacy's data
3. **Role Hierarchy**: Platform admin > Pharmacy manager > Employee
4. **Customer Default**: "cash customer" is default for walk-in sales
5. **Debt Tracking**: All credit sales create CustomerDebt records
6. **MoneyBox Creation**: Each pharmacy automatically gets one MoneyBox
7. **License Uniqueness**: Pharmacy license numbers must be unique
8. **Email Uniqueness**: User emails must be unique

## Integration Points

### With All Modules
- **Base Module**: All modules depend on User Management
- **Authentication**: Provides user authentication
- **Authorization**: Provides role and permission checking

### With Sales (POS) Module
- **Customer Selection**: Links sales to customers
- **Debt Creation**: Credit sales create CustomerDebt records

### With Purchase Module
- **Supplier Management**: Links purchases to suppliers

### With MoneyBox Module
- **Debt Payments**: Customer debt payments recorded in MoneyBox
- **Pharmacy Association**: MoneyBox linked to pharmacy

### With Inventory Module
- **Pharmacy Association**: All inventory linked to pharmacy

## Success Metrics

- **User Management**: Efficient user onboarding
- **Access Control**: Proper role and permission enforcement
- **Customer Management**: Accurate customer and debt tracking
- **Multi-Tenancy**: Complete data isolation between pharmacies

