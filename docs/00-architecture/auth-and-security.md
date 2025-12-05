# Authentication and Security

## Authentication Architecture

**Uqar** implements **stateless JWT-based authentication** using Spring Security. The system uses a token-based approach where clients authenticate once and receive a JWT token for subsequent requests.

## Authentication Flow

### 1. Login Process

```
Client → POST /api/v1/admin/login or /api/v1/pharmacy/login
         ↓
    AuthenticationProvider (DaoAuthenticationProvider)
         ↓
    UserDetailsService (loads user by email)
         ↓
    PasswordEncoder (BCrypt) validates password
         ↓
    JwtService generates JWT token
         ↓
    Client receives token
```

### 2. Request Authentication

```
Client Request with Header: Authorization: Bearer <token>
         ↓
    JwtAuthenticationFilter (OncePerRequestFilter)
         ↓
    Extracts token from Authorization header
         ↓
    JwtService validates token
         ↓
    UserDetailsService loads user
         ↓
    Sets Authentication in SecurityContext
         ↓
    Request proceeds to controller
```

## JWT Implementation

### Token Generation (`JwtService`)

- **Algorithm**: HS256 (HMAC SHA-256)
- **Secret Key**: Configured via `jwtKey` in `application.yml`
- **Token Expiration**: 24 hours (1000 * 60 * 60 * 24 milliseconds)
- **Claims**: 
  - `subject`: User email
  - `issuedAt`: Token creation timestamp
  - `expiration`: Token expiry timestamp

### Token Validation

The `JwtAuthenticationFilter` performs the following checks:
1. **Token Presence**: Verifies `Authorization` header starts with "Bearer "
2. **Token Extraction**: Extracts JWT from header (removes "Bearer " prefix)
3. **Token Parsing**: Validates token signature and expiration
4. **User Loading**: Loads user from database using email from token
5. **Authentication**: Sets `UsernamePasswordAuthenticationToken` in `SecurityContext`

### Token Expiration Handling

When a token is expired or invalid:
- **HTTP Status**: 401 Unauthorized
- **Response Body**: JSON with error message and timestamp
- **Exception**: `TokenExpiredException` is caught and handled gracefully

## User Details Service

The `UserDetailsService` bean is configured in `ApplicationConfig`:

```java
@Bean
public UserDetailsService userDetailsService() {
    return email -> {
        // First tries User table
        var user = userRepository.findByEmail(email).orElse(null);
        // Falls back to Employee table
        if (user == null) {
            user = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User or Employee not found"));
        }
        return user;
    };
}
```

**Key Points**:
- Searches both `User` and `Employee` tables
- Uses email as the unique identifier
- Returns `User` entity which implements `UserDetails`

## Password Security

### Password Encoding

- **Algorithm**: BCrypt (via `BCryptPasswordEncoder`)
- **Strength**: Default BCrypt strength (10 rounds)
- **Storage**: Passwords are never stored in plain text

### Password Validation

The system uses **Passay** library (v1.6.0) for password strength validation:
- Minimum length requirements
- Character complexity rules
- Custom validation rules can be configured

## Role-Based Access Control (RBAC)

### Role Hierarchy

The system implements a **three-tier permission model**:

1. **Roles** (`Role` entity)
   - System-defined roles: `PLATFORM_ADMIN`, `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, `PHARMACY_TRAINEE`
   - Each role has a set of permissions

2. **Permissions** (`Permission` entity)
   - Granular permissions: `RESOURCE:ACTION` format
   - Examples: `PRODUCT:CREATE`, `SALE:READ`, `PURCHASE:UPDATE`
   - Can be system-generated or custom

3. **User Permissions** (Additional)
   - Users can have additional permissions beyond their role
   - Stored in `user_permissions` join table

### Permission Structure

```
Permission
├── name: "PRODUCT:CREATE"
├── resource: "PRODUCT"
├── action: "CREATE"
└── isActive: true
```

### Role-Permission Mapping

- **Many-to-Many**: `Role` ↔ `Permission` (via `role_permissions` table)
- **Many-to-Many**: `User` ↔ `Permission` (via `user_permissions` table)
- **One-to-Many**: `User` → `Role` (via `role_id` foreign key)

## Authorization Mechanisms

### 1. Method-Level Security

**`@PreAuthorize`** annotation is used extensively for method-level authorization:

```java
@PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE')")
public ResponseEntity<SaleInvoiceDTOResponse> createSale(...) {
    // Method implementation
}
```

**Common Patterns**:
- `hasRole('ROLE_NAME')` - Checks if user has specific role
- `hasRole('ROLE1') or hasRole('ROLE2')` - Multiple roles
- `@PreAuthorize("hasPermission('RESOURCE:ACTION')")` - Permission-based

### 2. Security Configuration

**`SecurityConfiguration`** defines:
- **White List URLs**: Public endpoints (login, Swagger UI)
- **Protected Endpoints**: Requires authentication
- **Session Management**: Stateless (no session storage)
- **CORS**: Configured for cross-origin requests

**White List Endpoints**:
```java
private static final String[] WHITE_LIST_URL = {
    "/api/v1/admin/login",
    "/api/v1/pharmacy/login",
    "/v3/api-docs/**",
    "/swagger-ui/**",
    // ... Swagger endpoints
};
```

### 3. Base Security Service

**`BaseSecurityService`** provides common security utilities:

- `getCurrentUser()` - Gets authenticated user from SecurityContext
- `getCurrentUserPharmacyId()` - Gets pharmacy ID for multi-tenancy
- `validatePharmacyAccess(pharmacyId)` - Ensures user can only access their pharmacy's data
- `isAdmin()` - Checks if user is platform admin
- `hasRole(roleName)` - Checks specific role

**Multi-Tenancy Enforcement**:
```java
protected void validatePharmacyAccess(Long pharmacyId) {
    Long currentUserPharmacyId = getCurrentUserPharmacyId();
    if (!currentUserPharmacyId.equals(pharmacyId)) {
        throw new UnAuthorizedException("User does not have access to pharmacy");
    }
}
```

## Security Features

### 1. CSRF Protection

- **Status**: Disabled (via `csrf(AbstractHttpConfigurer::disable)`)
- **Reason**: Stateless JWT authentication doesn't require CSRF protection
- **Note**: CSRF is only needed for session-based authentication

### 2. CORS Configuration

```java
registry.addMapping("/**")
    .allowedOrigins("*")
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    .allowedHeaders("*");
```

**Current Configuration**: Allows all origins (development setup)
**Production Recommendation**: Restrict to specific domains

### 3. Rate Limiting

- **Library**: Resilience4j
- **Configuration**: `RateLimiterConfig`
- **Purpose**: Prevents API abuse and DDoS attacks

### 4. Input Validation

- **Framework**: Jakarta Bean Validation
- **Annotations**: `@Valid`, `@NotNull`, `@Min`, `@Max`, etc.
- **Location**: Controller and DTO layers

## User Roles and Permissions

### Standard Roles

1. **PLATFORM_ADMIN**
   - Full system access
   - Can manage all pharmacies
   - Can manage roles and permissions

2. **PHARMACY_MANAGER**
   - Full access to their pharmacy
   - Can manage employees
   - Can manage inventory, sales, purchases
   - Financial management access

3. **PHARMACY_EMPLOYEE**
   - Can process sales
   - Can view inventory
   - Limited financial access

4. **PHARMACY_TRAINEE**
   - Read-only access in most areas
   - Can process sales under supervision
   - Limited modification permissions

### Permission Examples

- `PRODUCT:CREATE` - Create products
- `PRODUCT:READ` - View products
- `PRODUCT:UPDATE` - Modify products
- `PRODUCT:DELETE` - Delete products
- `SALE:CREATE` - Process sales
- `SALE:CANCEL` - Cancel sales
- `PURCHASE:CREATE` - Create purchase orders
- `MONEYBOX:READ` - View financial data
- `MONEYBOX:UPDATE` - Modify financial records

## Security Best Practices

### 1. Token Security
- ✅ Tokens expire after 24 hours
- ✅ Tokens are validated on every request
- ✅ Secret key is stored in configuration (should be in environment variables in production)

### 2. Password Security
- ✅ Passwords are hashed with BCrypt
- ✅ Password validation enforces strength requirements
- ✅ Passwords are never returned in API responses

### 3. Multi-Tenancy
- ✅ All queries filter by `pharmacy_id`
- ✅ Pharmacy access is validated in service layer
- ✅ Users can only access their pharmacy's data

### 4. Authorization
- ✅ Method-level security on all sensitive endpoints
- ✅ Role-based and permission-based checks
- ✅ Pharmacy-level data isolation

## Security Configuration Classes

### `SecurityConfiguration`
- Configures Spring Security filter chain
- Defines public and protected endpoints
- Sets up JWT authentication filter
- Configures CORS

### `JwtAuthenticationFilter`
- Extends `OncePerRequestFilter`
- Intercepts all requests
- Validates JWT tokens
- Sets authentication context

### `JwtService`
- Generates JWT tokens
- Validates token signatures
- Extracts claims from tokens
- Handles token expiration

### `ApplicationConfig`
- Configures `UserDetailsService`
- Sets up `AuthenticationProvider`
- Configures password encoder
- Configures audit trail

## Security Headers

The system uses standard HTTP security headers:
- **Authorization**: `Bearer <token>` for authenticated requests
- **Content-Type**: `application/json` for API responses

## Error Handling

### Authentication Errors
- **401 Unauthorized**: Invalid or expired token
- **403 Forbidden**: Valid token but insufficient permissions
- **404 Not Found**: User not found during authentication

### Security Exception Responses

```json
{
  "message": "Token expired",
  "status": "UNAUTHORIZED",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Future Security Enhancements

1. **Token Refresh**: Implement refresh token mechanism
2. **Rate Limiting**: Per-user rate limiting
3. **IP Whitelisting**: Restrict access by IP address
4. **Audit Logging**: Log all security events
5. **Two-Factor Authentication**: Add 2FA support
6. **OAuth2 Integration**: Support for OAuth2 providers

