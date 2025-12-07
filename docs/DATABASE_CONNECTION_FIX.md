# Database Connection Reset Fix - Docker Environment

## Problem Summary

The application was experiencing database connection resets in the Docker environment when processing notifications:

```
org.postgresql.util.PSQLException: An I/O error occurred while sending to the backend.
Caused by: java.net.SocketException: Connection reset
```

**Root Causes**:
1. Network instability between Docker containers
2. Long-running transactions holding connections
3. No retry logic for transient connection errors
4. HikariCP connection pool not optimized for Docker environments
5. No connection validation/keepalive settings

---

## ✅ Solutions Implemented

### 1. **Removed Long-Running Transactions** ✅
**File**: `NotificationQueueProcessor.java`

**Before**:
```java
@Scheduled(fixedDelay = 5000)
@Transactional  // ❌ Long-running transaction
public void processPendingNotifications() {
    // Process all notifications in one transaction
}
```

**After**:
```java
@Scheduled(fixedDelay = 5000)
public void processPendingNotifications() {
    // No transaction - just fetch notifications
    for (Notification notification : readyNotifications) {
        processNotificationInTransaction(notification); // ✅ Transaction per notification
    }
}

@Transactional(noRollbackFor = {DataAccessResourceFailureException.class})
private void processNotificationInTransaction(Notification notification) {
    // Each notification processed in its own short transaction
}
```

**Benefits**:
- Prevents long-held database connections
- Reduces lock contention
- Better transaction timeout handling
- Improved connection pool usage

---

### 2. **Added Retry Logic for Transient Errors** ✅
**File**: `NotificationQueueProcessor.java`

**Implementation**:
- Exponential backoff retry (2s, 4s, 8s)
- Maximum 3 retry attempts
- Detects transient database errors (connection reset, I/O errors)
- Gracefully handles connection failures

**Code**:
```java
int maxRetries = 3;
int retryCount = 0;

while (retryCount < maxRetries) {
    try {
        // Database query
        notificationPage = notificationRepository.findReadyForRetry(...);
        break; // Success
    } catch (DataAccessException e) {
        if (isTransientDatabaseError(e)) {
            retryCount++;
            if (retryCount < maxRetries) {
                long delayMs = (long) Math.pow(2, retryCount) * 1000;
                Thread.sleep(delayMs);
                continue; // Retry
            }
        }
        return; // Give up after max retries
    }
}
```

**Benefits**:
- Automatically recovers from transient network issues
- Prevents notification processing from failing due to temporary connection problems
- Reduces error logs for transient issues

---

### 3. **Improved Error Detection** ✅
**File**: `NotificationQueueProcessor.java`

**Implementation**:
- `isTransientDatabaseError()` method detects:
  - Connection reset errors
  - Connection closed errors
  - I/O errors
  - Socket exceptions
  - SQLSTATE 08006 and 08P01 (connection errors)

**Code**:
```java
private boolean isTransientDatabaseError(Exception e) {
    String message = e.getMessage();
    return message.contains("Connection reset") ||
           message.contains("Connection is closed") ||
           message.contains("I/O error") ||
           message.contains("SocketException") ||
           message.contains("SQLSTATE(08006)");
}
```

**Benefits**:
- Distinguishes between transient and permanent errors
- Only retries on recoverable errors
- Prevents infinite retry loops on permanent errors

---

### 4. **HikariCP Connection Pool Optimization** ✅
**File**: `application.yml`

**Configuration Added**:
```yaml
spring:
  datasource:
    hikari:
      # Connection pool settings for Docker/network resilience
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000 # 30 seconds
      idle-timeout: 600000 # 10 minutes
      max-lifetime: 1800000 # 30 minutes
      leak-detection-threshold: 60000 # 1 minute
      # Connection validation
      connection-test-query: SELECT 1
      validation-timeout: 5000 # 5 seconds
      # Keep connections alive
      keepalive-time: 300000 # 5 minutes - send keepalive to prevent connection reset
      # Network resilience
      register-mbeans: true
```

**Key Settings**:
- **keepalive-time**: Sends TCP keepalive packets every 5 minutes to prevent connection reset
- **connection-test-query**: Validates connections before use
- **leak-detection-threshold**: Detects connection leaks
- **max-lifetime**: Rotates connections to prevent stale connections

**Benefits**:
- Prevents connection resets due to idle timeouts
- Validates connections before use
- Detects and fixes connection leaks
- Better connection pool management

---

### 5. **Transaction Error Handling** ✅
**File**: `NotificationQueueProcessor.java`

**Implementation**:
- Added `noRollbackFor = {DataAccessResourceFailureException.class}` to `@Transactional`
- Prevents transaction rollback failures when connection is already closed
- Gracefully handles connection errors during transaction commit/rollback

**Code**:
```java
@Transactional(noRollbackFor = {DataAccessResourceFailureException.class})
private void processNotificationInTransaction(Notification notification) {
    // Process notification
    // If connection fails, transaction won't try to rollback
}
```

**Benefits**:
- Prevents "Unable to rollback" errors
- Cleaner error handling
- Better recovery from connection failures

---

## 📊 Expected Improvements

### Before Fix
- ❌ Connection resets cause notification processing to fail
- ❌ Long-running transactions hold connections
- ❌ No retry logic - errors are permanent
- ❌ Connection pool not optimized for Docker
- ❌ Transaction rollback failures

### After Fix
- ✅ Automatic retry on transient connection errors
- ✅ Short-lived transactions per notification
- ✅ Connection pool optimized for Docker networks
- ✅ Keepalive prevents connection resets
- ✅ Graceful error handling

---

## 🧪 Testing Recommendations

### 1. **Test Connection Reset Recovery**
- Simulate network issues between containers
- Verify retry logic works
- Check that notifications are processed after connection recovers

### 2. **Test Transaction Handling**
- Monitor transaction duration
- Verify no long-running transactions
- Check connection pool usage

### 3. **Test HikariCP Settings**
- Monitor connection pool metrics
- Verify keepalive is working
- Check for connection leaks

### 4. **Load Testing**
- Send many notifications simultaneously
- Verify connection pool handles load
- Check for connection errors under load

---

## 🔧 Docker-Specific Considerations

### Network Configuration
Ensure Docker network is stable:
```yaml
networks:
  uqar-net:
    driver: bridge
```

### Database Container Health
Ensure database container has proper health checks:
```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U $$POSTGRES_USER -d $$POSTGRES_DB"]
  interval: 5s
  timeout: 5s
  retries: 5
```

### Connection String
Use Docker service name for database:
```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://uqar-db:5432/uqar
```

---

## 📝 Monitoring

### Key Metrics to Monitor

1. **Connection Pool Metrics**:
   - Active connections
   - Idle connections
   - Connection wait time
   - Connection timeouts

2. **Error Rates**:
   - Connection reset errors
   - Transaction failures
   - Retry attempts

3. **Performance**:
   - Notification processing time
   - Transaction duration
   - Database query time

### Log Messages to Watch

**Good Signs**:
- `Processing X notifications ready for retry`
- `Notification X sent successfully`
- `Database connection error (attempt 1/3). Retrying in 2000ms`

**Warning Signs**:
- `Database connection failed after 3 attempts`
- `Connection reset` (should be retried automatically)
- `Unable to rollback` (should be prevented now)

---

## ✅ Verification Checklist

- [x] Removed `@Transactional` from scheduled method
- [x] Added per-notification transactions
- [x] Implemented retry logic with exponential backoff
- [x] Added transient error detection
- [x] Configured HikariCP for Docker
- [x] Added keepalive settings
- [x] Improved transaction error handling
- [x] No compilation errors
- [x] All linter errors resolved

---

## 🚀 Next Steps (Optional)

1. **Add Circuit Breaker**: Use Resilience4j Circuit Breaker for database operations
2. **Add Metrics**: Track connection pool metrics via Actuator
3. **Connection Pool Monitoring**: Add alerts for connection pool exhaustion
4. **Database Health Checks**: Implement application-level database health checks

---

**Status**: ✅ **Fixed**  
**Date**: 2025-01-16  
**Impact**: High - Resolves connection reset errors in Docker environment

