# Notification Module - Technical Documentation

## Data Flow Architecture

### Notification Sending Flow

```
NotificationService.sendNotification()
    ↓
1. Create Notification entity
   - Set title, message, type
   - Set recipient user
   - Set status = PENDING
2. Save Notification
    ↓
NotificationProducer.send()
    ↓
Add to notification queue
    ↓
NotificationQueueProcessor.process()
    ↓
For each recipient device:
    1. Get DeviceToken
    2. FirebaseMessagingService.send()
    3. Update notification status
    4. Handle failures (retry if needed)
    ↓
Notification status = SENT or FAILED
```

### Device Registration Flow

```
NotificationController.registerDevice()
    ↓
DeviceTokenService.registerDevice()
    ↓
1. Get current user
2. Check if token exists
3. Create or update DeviceToken
   - Link to user
   - Store Firebase token
4. Save DeviceToken
    ↓
Return success
```

## Key Endpoints

### Notification Endpoints

#### `POST /api/v1/notifications`
**Purpose**: Send notification

**Input Body**: `NotificationRequest`
```json
{
  "title": "Low Stock Alert",
  "message": "Product X is running low",
  "type": "STOCK_ALERT",
  "userId": 123
}
```

**Response**: `NotificationResponse`

**Side Effects**:
- Creates `notification` record
- Queues notification for sending
- Sends push notification via Firebase

**Authorization**: Requires authentication

---

#### `GET /api/v1/notifications`
**Purpose**: Get user's notifications

**Query Parameters**:
- `type` (optional): Filter by type
- `status` (optional): Filter by status

**Response**: `List<NotificationResponse>`

**Side Effects**: None (read-only)

---

#### `POST /api/v1/notifications/device-token`
**Purpose**: Register device for push notifications

**Input Body**: `DeviceTokenRequest`
```json
{
  "token": "firebase-device-token-here",
  "deviceType": "ANDROID"
}
```

**Response**: Success

**Side Effects**:
- Creates or updates `device_token` record
- Links token to current user

**Authorization**: Requires authentication

---

## Service Layer Components

### NotificationService

**Location**: `com.Uqar.notification.service.NotificationService`

**Key Methods**:

1. **`sendNotification(NotificationRequest)`**
   - Creates notification
   - Queues for sending
   - Returns response

2. **`getUserNotifications(...)`**
   - Gets notifications for current user
   - Applies filters
   - Returns list

3. **`markAsRead(Long notificationId)`**
   - Marks notification as read
   - Updates status

**Extends**: `BaseSecurityService`

---

### FirebaseMessagingService

**Location**: `com.Uqar.notification.service.FirebaseMessagingService`

**Purpose**: Handles Firebase Cloud Messaging integration

**Key Methods**:

1. **`sendNotification(String token, String title, String message)`**
   - Sends push notification via Firebase
   - Handles errors
   - Returns success/failure

**Dependencies**:
- Firebase Admin SDK

---

### DeviceTokenService

**Location**: `com.Uqar.notification.service.DeviceTokenService`

**Key Methods**:

1. **`registerDevice(DeviceTokenRequest)`**
   - Registers device token
   - Links to user
   - Handles updates

---

### NotificationQueueProcessor

**Location**: `com.Uqar.notification.service.NotificationQueueProcessor`

**Purpose**: Processes notification queue asynchronously

**Key Methods**:

1. **`processNotification(Notification notification)`**
   - Gets recipient devices
   - Sends to each device
   - Updates status
   - Handles retries

---

### NotificationSchedulerService

**Location**: `com.Uqar.notification.scheduler.NotificationSchedulerService`

**Purpose**: Scheduled tasks for notification processing

**Key Methods**:

1. **`retryFailedNotifications()`**
   - Finds failed notifications
   - Retries sending
   - Updates retry count

---

## Repository Layer

### NotificationRepository

**Key Methods**:
```java
List<Notification> findByRecipientId(Long userId);
List<Notification> findByRecipientIdAndType(Long userId, NotificationType type);
List<Notification> findByStatusAndNextRetryAtBefore(NotificationStatus status, LocalDateTime date);
```

---

### DeviceTokenRepository

**Key Methods**:
```java
List<DeviceToken> findByUserId(Long userId);
Optional<DeviceToken> findByToken(String token);
```

---

## Entity Relationships

### Notification Entity

```java
@Entity
public class Notification extends AuditedEntity {
    private String title;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private LocalDateTime nextRetryAt;
    private Integer retryCount;
    
    @ManyToOne
    private User recipient;
}
```

---

### DeviceToken Entity

```java
@Entity
public class DeviceToken extends AuditedEntity {
    private String token;
    private String deviceType;
    
    @ManyToOne
    private User user;
}
```

---

## Dependencies

### External Dependencies

1. **Firebase Admin SDK**
   - Push notification delivery
   - Device token management

2. **Spring Scheduling**
   - Retry mechanism
   - Scheduled processing

---

## Database Queries

### Notification Creation

```sql
INSERT INTO notification (
    recipient_id, title, message, type, 
    status, next_retry_at, retry_count, 
    created_at, created_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
```

### Device Registration

```sql
INSERT INTO device_token (
    user_id, token, device_type, 
    created_at, created_by
) VALUES (?, ?, ?, ?, ?)
ON CONFLICT (token) DO UPDATE 
SET user_id = ?, updated_at = ?;
```

---

## Error Handling

### Common Exceptions

1. **`EntityNotFoundException`**: 
   - User not found
   - Device token not found

2. **`RequestNotValidException`**: 
   - Invalid notification data
   - Invalid device token

---

## Performance Considerations

1. **Async Processing**: 
   - Notifications sent asynchronously
   - Queue-based processing

2. **Retry Mechanism**: 
   - Failed notifications retried
   - Exponential backoff

3. **Batching**: 
   - Multiple notifications can be batched

---

## Security Considerations

1. **Multi-Tenancy**: Notifications only for pharmacy users
2. **Token Security**: Device tokens stored securely
3. **Authorization**: Users can only see their notifications

---

## Known Issues and Fixes

### Firebase Notification Failure Issue (Resolved)

**Issue:** All notifications were failing with `FAILED` status in the database.

**Root Cause:** The `FirebaseMessaging` bean was never created, causing `FirebaseMessagingService` to have a null `firebaseMessaging` field.

**Resolution:** 
- Added `firebaseMessaging()` bean method in `FirebaseConfig`
- Improved error handling in `FirebaseMessagingService`
- Enhanced `NotificationQueueProcessor` with better error detection
- Added `SimpleNotificationService` as fallback

**Documentation:** See [`../../notification-firebase-fix.md`](../../notification-firebase-fix.md) for detailed information about the issue and fixes.

