# AOP Logging System Documentation

## Overview

The Uqar project now includes a comprehensive AOP (Aspect-Oriented Programming) logging system that provides structured, secure, and performant logging across the application.

## Logging Aspects

### 1. RequestLoggingAspect
**Purpose**: Logs all HTTP requests and responses

**Features**:
- Logs HTTP method, endpoint, query parameters
- Captures user information and IP address
- Records request duration
- Determines HTTP status codes
- Logs errors with exception details

**Coverage**: All HTTP endpoints (GET, POST, PUT, DELETE, PATCH) and methods annotated with `@Loggable`

**Example Log Output**:
```
→ POST /api/v1/users | User: admin | IP: 192.168.1.1
✓ POST /api/v1/users | User: admin | Duration: 45ms | Status: 201
```

**Security**: Automatically redacts sensitive data from logs

### 2. AuditAspect
**Purpose**: Records audit events for critical operations

**Features**:
- Records action, target type, and target ID
- Captures user and IP address
- Logs success and failure status
- Includes method details and arguments (optional)

**Usage**: Annotate methods with `@Audited` annotation

**Example**:
```java
@Audited(action = "CREATE_USER", targetType = "USER", includeArgs = false)
public UserResponseDTO createUser(UserRequestDTO request) {
    // ...
}
```

**Example Log Output**:
```
AUDIT | Action: CREATE_USER | Target: USER[123] | User: admin | IP: 192.168.1.1 | Status: SUCCESS | Details: {...}
```

### 3. PerformanceMetricsAspect
**Purpose**: Records performance metrics for service methods

**Features**:
- Records execution time for service methods
- Emits metrics to Micrometer (if available)
- Falls back to debug logging if Micrometer is not available
- Supports custom metric names via `@Measured` annotation

**Coverage**: All service methods in:
- `com.Uqar.product.service.*`
- `com.Uqar.user.service.*`
- `com.Uqar.purchase.service.*`
- `com.Uqar.sale.service.*`
- `com.Uqar.moneybox.service.*`
- `com.Uqar.notification.service.*`
- `com.Uqar.reports.service.*`
- `com.Uqar.complaint.service.*`

**Usage**: Automatically applied, or use `@Measured` for custom metric names

**Example**:
```java
@Measured(name = "user-creation")
public UserResponseDTO createUser(UserRequestDTO request) {
    // ...
}
```

### 4. ServiceExceptionLoggingAspect
**Purpose**: Logs exceptions in service methods with full context

**Features**:
- Logs exception type and message
- Includes class and method name
- Provides full stack trace
- Complements RequestLoggingAspect (which handles controller exceptions)

**Coverage**: All service methods (same as PerformanceMetricsAspect)

**Example Log Output**:
```
Exception in service method: UserService.createUser() | Exception: ResourceNotFoundException | Message: User not found
```

## Annotations

### @Loggable
Marks methods or classes for request logging with optional payload logging.

```java
@Loggable(logPayload = false, logResponse = false)
public ResponseEntity<UserResponseDTO> getUser(Long id) {
    // ...
}
```

### @Audited
Marks methods for audit event recording.

```java
@Audited(action = "UPDATE_PRODUCT", targetType = "PRODUCT", includeArgs = true)
public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request) {
    // ...
}
```

### @Measured
Marks methods for performance metrics collection.

```java
@Measured(name = "custom-metric-name")
public void performComplexOperation() {
    // ...
}
```

## Benefits

### 1. **Comprehensive Coverage**
- HTTP requests automatically logged
- Service method exceptions tracked
- Performance metrics collected
- Audit events recorded

### 2. **Security**
- Sensitive data automatically redacted
- Passwords, tokens, and keys never logged
- IP addresses and user information captured for security analysis

### 3. **Performance**
- Minimal overhead (AOP is efficient)
- Metrics collection optional (Micrometer)
- Debug-level logging for non-critical information

### 4. **Maintainability**
- Centralized logging logic
- Easy to modify logging behavior
- Clear separation of concerns

### 5. **Observability**
- Request/response logging for debugging
- Performance metrics for optimization
- Audit trail for compliance
- Exception tracking for error analysis

## Side Effects & Considerations

### ✅ Safe Aspects (No Side Effects)

1. **RequestLoggingAspect**: 
   - Only logs, doesn't modify requests/responses
   - Safe to use in production

2. **ServiceExceptionLoggingAspect**:
   - Only logs exceptions, doesn't interfere with exception handling
   - Safe to use in production

3. **AuditAspect**:
   - Only logs audit events
   - Can be extended to call AuditService when implemented
   - Safe to use in production

### ⚠️ Considerations

1. **PerformanceMetricsAspect**:
   - Uses reflection for Micrometer (if available)
   - Falls back gracefully if Micrometer is not available
   - Minimal performance impact
   - Safe to use in production

2. **Log Volume**:
   - RequestLoggingAspect logs every HTTP request
   - Consider log rotation and retention policies
   - May generate significant log volume in high-traffic scenarios

3. **Sensitive Data**:
   - Automatic redaction is implemented
   - Review `sanitizeSensitiveData` methods if adding new sensitive fields
   - Test with real data to ensure proper redaction

## Configuration

### Log Levels

Configure log levels in `application.yml`:

```yaml
logging:
  level:
    com.Uqar.utils.Aspect: INFO  # For production
    # com.Uqar.utils.Aspect: DEBUG  # For development
```

### Micrometer (Optional)

If you want to use Micrometer for metrics, add to `pom.xml`:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
</dependency>
```

The PerformanceMetricsAspect will automatically use Micrometer if available.

## Migration from Old AspectClass

The old `AspectClass` has been replaced with:
- **RequestLoggingAspect**: Handles HTTP request/response logging (replaces controller logging)
- **ServiceExceptionLoggingAspect**: Handles service method exceptions (replaces service exception logging)
- **PerformanceMetricsAspect**: Handles performance metrics (replaces execution time logging)

The new system provides:
- Better structured logging
- Security (sensitive data redaction)
- More detailed context (user, IP, status codes)
- Optional metrics collection

## Best Practices

1. **Use @Audited for Critical Operations**:
   - User creation/deletion
   - Financial transactions
   - Permission changes
   - Data modifications

2. **Use @Measured for Performance-Critical Methods**:
   - Complex calculations
   - Database queries
   - External API calls
   - Batch operations

3. **Monitor Log Volume**:
   - Set up log rotation
   - Configure log retention
   - Use log aggregation tools (ELK, Splunk, etc.)

4. **Review Audit Logs Regularly**:
   - Check for suspicious activities
   - Verify compliance requirements
   - Analyze user behavior patterns

## Troubleshooting

### Logs Not Appearing
- Check log level configuration
- Verify aspect is enabled (@Component annotation)
- Check if pointcut matches your methods

### Performance Impact
- Review log level (use DEBUG only in development)
- Consider log aggregation tools
- Monitor application performance metrics

### Missing Metrics
- Verify Micrometer is in classpath (optional)
- Check if @Measured annotation is used correctly
- Review PerformanceMetricsAspect logs

## Future Enhancements

1. **AuditService Integration**: When AuditService is implemented, AuditAspect will call it
2. **Structured Logging**: Consider JSON logging format for better parsing
3. **Log Sampling**: Implement log sampling for high-traffic endpoints
4. **Custom Metrics**: Add business-specific metrics
5. **Alerting**: Integrate with alerting systems for critical errors

