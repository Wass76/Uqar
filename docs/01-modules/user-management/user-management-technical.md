# User Management Module - Technical Documentation

## Data Flow Architecture

### Admin Login Flow

```
AdminController.adminLogin()
    ↓
PharmacyService.adminLogin()
    ↓
1. Validate credentials (email, password)
2. Load user from database
3. Validate user is admin
4. Generate JWT token
5. Record login audit info
    ↓
Return UserAuthenticationResponse (with token)
```

### Pharmacy Creation Flow

```
AdminController.createPharmacy()
    ↓
PharmacyService.create()
    ↓
1. Validate pharmacy data
2. Check license number uniqueness
3. Create Pharmacy entity
4. Create MoneyBox for pharmacy (automatic)
5. Save pharmacy
    ↓
Return PharmacyResponseDTO
```

### Employee Creation Flow

```
EmployeeController.create()
    ↓
EmployeeService.create()
    ↓
1. Validate employee data
2. Check email uniqueness
3. Hash password
4. Create Employee entity
   - Link to pharmacy
   - Assign role
   - Set working hours
5. Save employee
    ↓
Return EmployeeResponseDTO
```

### Customer Debt Payment Flow

```
CustomerDebtController.payDebt()
    ↓
CustomerDebtService.payDebt()
    ↓
1. Get debt by ID
2. Validate payment amount
3. Update debt:
   - Reduce remaining amount
   - Update paid amount
   - Update status
4. Record MoneyBox transaction (if cash)
5. Save debt
    ↓
Return CustomerDebtDTOResponse
```

## Key Endpoints

### Authentication Endpoints

#### `POST /api/v1/admin/login`
**Purpose**: Platform admin login

**Input Body**: `AuthenticationRequest`
```json
{
  "email": "admin@uqar.com",
  "password": "password123"
}
```

**Response**: `UserAuthenticationResponse`
- JWT token
- User information
- Role and permissions

**Side Effects**:
- Records login audit information
- No database changes (read-only authentication)

**Authorization**: Public endpoint (no auth required)

---

#### `POST /api/v1/pharmacy/login`
**Purpose**: Pharmacy user login (employee/manager)

**Input Body**: `AuthenticationRequest`

**Response**: `UserAuthenticationResponse`

**Side Effects**: Records login audit information

**Authorization**: Public endpoint

---

### User Endpoints

#### `GET /api/v1/users/me`
**Purpose**: Get current authenticated user information

**Response**: User information with role and permissions

**Side Effects**: None (read-only)

**Authorization**: Requires authentication

---

### Pharmacy Management Endpoints

#### `POST /api/v1/admin/pharmacies`
**Purpose**: Create new pharmacy (admin only)

**Input Body**: `PharmacyCreateRequestDTO`
```json
{
  "name": "Al-Sham Pharmacy",
  "licenseNumber": "PH-12345",
  "address": "Damascus, Syria",
  "email": "pharmacy@example.com",
  "phoneNumber": "+963-11-1234567",
  "type": "MAIN",
  "areaId": 1,
  "openingHours": "09:00-21:00"
}
```

**Response**: `PharmacyResponseDTO`

**Side Effects**:
- Creates `pharmacy` record
- **Automatically creates MoneyBox** for pharmacy
- Sets `isActive = true`

**Authorization**: Requires `PLATFORM_ADMIN` role

---

#### `GET /api/v1/pharmacies`
**Purpose**: Get all pharmacies (admin) or current pharmacy (employee)

**Response**: `List<PharmacyResponseDTO>` or single pharmacy

**Side Effects**: None (read-only)

**Authorization**: 
- Admin: All pharmacies
- Employee: Only their pharmacy

---

### Employee Management Endpoints

#### `POST /api/v1/employees`
**Purpose**: Create new employee

**Input Body**: `EmployeeRequestDTO`
```json
{
  "email": "employee@pharmacy.com",
  "password": "password123",
  "firstName": "Ahmed",
  "lastName": "Ali",
  "phoneNumber": "+963-11-1234567",
  "position": "Pharmacist",
  "roleId": 2,
  "dateOfHire": "2024-01-15",
  "workingHours": [...]
}
```

**Response**: `EmployeeResponseDTO`

**Side Effects**:
- Creates `employee` record (extends User)
- Links to pharmacy (from current user)
- Creates user account
- Password hashed with BCrypt

**Authorization**: Requires `PHARMACY_MANAGER` or `PLATFORM_ADMIN` role

---

#### `GET /api/v1/employees`
**Purpose**: Get all employees for current pharmacy

**Response**: `List<EmployeeResponseDTO>`

**Side Effects**: None (read-only)

**Authorization**: Requires authentication

---

#### `PUT /api/v1/employees/{id}`
**Purpose**: Update employee

**Side Effects**:
- Updates employee information
- Can update role and permissions

**Authorization**: Requires `PHARMACY_MANAGER` role

---

### Customer Management Endpoints

#### `POST /api/v1/customers`
**Purpose**: Create new customer

**Input Body**: `CustomerDTORequest`
```json
{
  "name": "Mohammed Hassan",
  "phoneNumber": "+963-11-1234567",
  "address": "Damascus, Syria",
  "notes": "Regular customer"
}
```

**Response**: `CustomerDTOResponse`

**Side Effects**:
- Creates `customers` record
- Linked to current pharmacy
- Default name: "cash customer" if not provided

**Authorization**: Requires authentication

---

#### `GET /api/v1/customers`
**Purpose**: Get all customers for current pharmacy

**Response**: `List<CustomerDTOResponse>`
- Includes debt information

**Side Effects**: None (read-only)

---

#### `GET /api/v1/customers/search`
**Purpose**: Search customers by name

**Query Parameters**:
- `name`: Search term (partial match)

**Response**: `List<CustomerDTOResponse>`

**Side Effects**: None (read-only)

---

### Customer Debt Endpoints

#### `POST /api/v1/customer-debts`
**Purpose**: Create new customer debt

**Input Body**: `CustomerDebtDTORequest`
```json
{
  "customerId": 1,
  "amount": 5000,
  "currency": "SYP",
  "description": "Credit sale invoice #123"
}
```

**Response**: `CustomerDebtDTOResponse`

**Side Effects**:
- Creates `customer_debt` record
- Status = PENDING
- Links to customer

**Authorization**: Requires authentication

---

#### `GET /api/v1/customer-debts/customer/{customerId}`
**Purpose**: Get all debts for a customer

**Response**: `List<CustomerDebtDTOResponse>`

**Side Effects**: None (read-only)

---

#### `POST /api/v1/customer-debts/{debtId}/pay`
**Purpose**: Pay customer debt

**Input Body**: `PayDebtDTORequest`
```json
{
  "amount": 3000,
  "paymentMethod": "CASH",
  "currency": "SYP"
}
```

**Response**: `CustomerDebtDTOResponse`

**Side Effects**:
- Updates `customer_debt` record
- Reduces remaining amount
- Updates paid amount
- Updates status (if fully paid)
- **Records MoneyBox transaction** (if cash payment)

**Authorization**: Requires authentication

---

#### `POST /api/v1/customer-debts/pay-multiple`
**Purpose**: Pay multiple debts at once

**Input Body**: `PayCustomerDebtsRequest`
```json
{
  "customerId": 1,
  "payments": [
    {"debtId": 1, "amount": 2000},
    {"debtId": 2, "amount": 3000}
  ],
  "paymentMethod": "CASH",
  "currency": "SYP"
}
```

**Response**: `PayCustomerDebtsResponse`
- Summary of payments
- Updated debts

**Side Effects**:
- Updates multiple debt records
- Records MoneyBox transactions

---

### Supplier Management Endpoints

#### `POST /api/v1/suppliers`
**Purpose**: Create new supplier

**Input Body**: `SupplierDTORequest`
```json
{
  "name": "Pharma Supply Co.",
  "contactInfo": "contact@pharma.com",
  "address": "Damascus, Syria",
  "phoneNumber": "+963-11-1234567"
}
```

**Response**: `SupplierDTOResponse`

**Side Effects**:
- Creates `supplier` record

**Authorization**: Requires authentication

---

#### `GET /api/v1/suppliers`
**Purpose**: Get all suppliers

**Response**: `List<SupplierDTOResponse>`

**Side Effects**: None (read-only)

---

### Role and Permission Endpoints

#### `GET /api/v1/roles`
**Purpose**: Get all roles

**Response**: `List<Role>`

**Side Effects**: None (read-only)

**Authorization**: Requires `PLATFORM_ADMIN` role

---

#### `GET /api/v1/roles/{id}`
**Purpose**: Get role by ID with permissions

**Response**: `Role` with permissions

**Side Effects**: None (read-only)

---

## Service Layer Components

### UserService

**Location**: `com.Uqar.user.service.UserService`

**Key Methods**:
- `getCurrentUserResponse()` - Gets current authenticated user

---

### PharmacyService

**Location**: `com.Uqar.user.service.PharmacyService`

**Key Methods**:

1. **`adminLogin(AuthenticationRequest, HttpServletRequest)`**
   - Validates admin credentials
   - Generates JWT token
   - Returns authentication response

2. **`create(PharmacyCreateRequestDTO)`**
   - Creates pharmacy
   - Automatically creates MoneyBox
   - Returns pharmacy response

3. **`getAllPharmacies()`**
   - Gets all pharmacies (admin only)

---

### EmployeeService

**Location**: `com.Uqar.user.service.EmployeeService`

**Key Methods**:

1. **`create(EmployeeRequestDTO)`**
   - Creates employee
   - Links to pharmacy
   - Assigns role
   - Sets working hours
   - Hashes password

2. **`getAllEmployees()`**
   - Gets employees for current pharmacy

---

### CustomerService

**Location**: `com.Uqar.user.service.CustomerService`

**Key Methods**:

1. **`create(CustomerDTORequest)`**
   - Creates customer
   - Links to pharmacy
   - Default name: "cash customer"

2. **`getAllCustomers()`**
   - Gets customers for current pharmacy
   - Includes debt information

3. **`searchCustomersByName(String name)`**
   - Searches customers by name (partial match)

---

### CustomerDebtService

**Location**: `com.Uqar.user.service.CustomerDebtService`

**Key Methods**:

1. **`createDebt(CustomerDebtDTORequest)`**
   - Creates debt record
   - Links to customer
   - Status = PENDING

2. **`payDebt(Long debtId, PayDebtDTORequest)`**
   - Updates debt with payment
   - Records MoneyBox transaction (if cash)
   - Updates status

3. **`getCustomerDebts(Long customerId)`**
   - Gets all debts for customer

4. **`payMultipleDebts(PayCustomerDebtsRequest)`**
   - Processes multiple debt payments
   - Records all transactions

---

### SupplierService

**Location**: `com.Uqar.user.service.SupplierService`

**Key Methods**:

1. **`create(SupplierDTORequest)`**
   - Creates supplier

2. **`getAllSuppliers()`**
   - Gets all suppliers

---

## Repository Layer

### UserRepository

**Key Methods**:
```java
Optional<User> findByEmail(String email);
Optional<User> findById(Long id);
```

### PharmacyRepository

**Key Methods**:
```java
Optional<Pharmacy> findByLicenseNumber(String licenseNumber);
List<Pharmacy> findAll();
Optional<Pharmacy> findById(Long id);
```

### CustomerRepository

**Key Methods**:
```java
List<Customer> findByPharmacyId(Long pharmacyId);
List<Customer> findByPharmacyIdAndNameContaining(Long pharmacyId, String name);
Optional<Customer> findById(Long id);
```

### CustomerDebtRepository

**Key Methods**:
```java
List<CustomerDebt> findByCustomerId(Long customerId);
List<CustomerDebt> findByCustomerIdAndStatus(Long customerId, DebtStatus status);
Optional<CustomerDebt> findById(Long id);
```

---

## Entity Relationships

### User Entity Hierarchy

```java
// Base class
public abstract class BaseUser {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}

// Main user entity
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class User extends BaseUser {
    private String position;
    private UserStatus status;
    
    @ManyToOne
    private Role role;
    
    @ManyToMany
    private Set<Permission> additionalPermissions;
}

// Employee extends User
@Entity
public class Employee extends User {
    @ManyToOne
    private Pharmacy pharmacy;
    
    private String phoneNumber;
    private LocalDate dateOfHire;
    
    @OneToMany
    private List<EmployeeWorkingHours> employeeWorkingHoursList;
}
```

### Pharmacy Entity

```java
@Entity
public class Pharmacy extends AuditedEntity {
    private String name;
    private String licenseNumber;  // Unique
    private String address;
    private String email;
    private PharmacyType type;  // MAIN or BRANCH
    private Boolean isActive;
    
    @ManyToOne
    private Area area;  // Geographic area
    
    @OneToMany
    private Set<Employee> employees;
}
```

### Customer Entity

```java
@Entity
public class Customer extends AuditedEntity {
    private String name;  // Default: "cash customer"
    private String phoneNumber;
    private String address;
    private String notes;
    
    @ManyToOne
    private Pharmacy pharmacy;
    
    @OneToMany
    private List<CustomerDebt> debts;
}
```

### CustomerDebt Entity

```java
@Entity
public class CustomerDebt extends AuditedEntity {
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private Currency currency;
    private DebtStatus status;
    private String description;
    
    @ManyToOne
    private Customer customer;
    
    @ManyToOne
    private SaleInvoice saleInvoice;  // If from credit sale
}
```

---

## Dependencies

### Internal Dependencies

1. **MoneyBox Module**
   - `MoneyBoxService` - Auto-creates MoneyBox for pharmacy
   - `EnhancedMoneyBoxAuditService` - Records debt payments

2. **Sales Module**
   - `SaleInvoice` - Links to customer debts

### External Dependencies

- **Spring Security**: Authentication and authorization
- **JWT**: Token generation
- **BCrypt**: Password hashing
- **MapStruct**: DTO mapping

---

## Database Queries

### Pharmacy Creation

```sql
INSERT INTO pharmacy (
    name, license_number, address, email, 
    type, area_id, is_active, created_at, created_by
) VALUES (?, ?, ?, ?, ?, ?, true, ?, ?);

-- Automatically creates MoneyBox
INSERT INTO money_box (
    pharmacy_id, current_balance, currency, status, created_at
) VALUES (?, 0, 'SYP', 'ACTIVE', ?);
```

### Employee Creation

```sql
INSERT INTO employee (
    email, password, first_name, last_name, 
    pharmacy_id, role_id, position, status, 
    phone_number, date_of_hire, created_at, created_by
) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', ?, ?, ?, ?);
```

### Customer Debt Payment

```sql
UPDATE customer_debt 
SET paid_amount = paid_amount + ?, 
    remaining_amount = remaining_amount - ?,
    status = CASE 
        WHEN remaining_amount - ? <= 0 THEN 'PAID'
        ELSE 'PARTIALLY_PAID'
    END,
    updated_at = ?, last_modified_by = ?
WHERE id = ?;

-- If cash payment, record MoneyBox transaction
INSERT INTO money_box_transaction (
    money_box_id, transaction_type, amount, 
    reference_type, reference_id, ...
) VALUES (?, 'DEBT_PAYMENT', ?, 'CUSTOMER_DEBT', ?, ...);
```

---

## Error Handling

### Common Exceptions

1. **`ResourceNotFoundException`**: 
   - User not found
   - Pharmacy not found
   - Customer not found

2. **`UnAuthorizedException`**: 
   - User cannot access another pharmacy's data
   - Insufficient permissions

3. **`ConflictException`**: 
   - Email already exists
   - License number already exists

---

## Performance Considerations

1. **Indexing**: 
   - `user.email` (unique, for login)
   - `pharmacy.license_number` (unique)
   - `customer.pharmacy_id` (for multi-tenancy)
   - `customer_debt.customer_id` (for debt lookup)

2. **Password Hashing**: 
   - BCrypt with 10 rounds
   - One-way hashing (cannot reverse)

3. **Lazy Loading**: 
   - Employee → Pharmacy (LAZY)
   - Customer → Debts (LAZY)

---

## Security Considerations

1. **Multi-Tenancy**: All queries filter by pharmacy
2. **Password Security**: Passwords never stored in plain text
3. **JWT Tokens**: Stateless authentication
4. **Role-Based Access**: Granular permission control
5. **Audit Trail**: All changes tracked

