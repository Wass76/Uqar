# Notification Module - Summary

## Objective

The **Notification Module** provides push notification capabilities to keep pharmacy staff informed about important events, alerts, and system updates. It solves the critical problem of real-time communication and alerting for time-sensitive information.

## Problem Statement

Pharmacies need to:
- Receive alerts about low stock levels
- Get notified about purchase limit exceedances
- Receive system updates and announcements
- Get reminders about important tasks
- Track notification delivery status
- Support multiple devices per user
- Handle notification failures and retries

## User Roles

### Primary Users

1. **Pharmacy Manager**
   - Receives all notifications
   - Can view notification history
   - Can manage device tokens

2. **Pharmacy Employee**
   - Receives relevant notifications
   - Can view their notifications
   - Can register device tokens

## Core Concepts

### Notification

A **Notification** represents a message sent to users:
- Has title and message
- Has type (STOCK_ALERT, PURCHASE_LIMIT, SYSTEM_UPDATE, etc.)
- Has status (PENDING, SENT, FAILED)
- Links to recipient user
- Tracks delivery status
- Supports retry mechanism

### Device Token

A **DeviceToken** represents a mobile device registered for push notifications:
- Links to user
- Stores Firebase device token
- Tracks registration date
- Can have multiple devices per user

### Notification Types

- `STOCK_ALERT` - Low stock warnings
- `PURCHASE_LIMIT` - Purchase limit exceeded
- `SYSTEM_UPDATE` - System announcements
- `EXPIRY_ALERT` - Product expiry warnings
- `PAYMENT_REMINDER` - Payment reminders

## Main User Workflows

### 1. Device Registration Workflow

**Scenario**: User wants to receive push notifications on their device.

1. **Open App**: User opens mobile app
2. **Request Permission**: App requests notification permission
3. **Get Token**: App gets Firebase device token
4. **Register Device**: App sends token to backend
5. **Save Token**: System saves DeviceToken
6. **Confirmation**: Device registered for notifications

**Outcome**: Device registered, ready to receive notifications.

### 2. Low Stock Alert Workflow

**Scenario**: Product stock falls below minimum level.

1. **Stock Check**: System checks stock levels
2. **Detect Low Stock**: Product below minimum level
3. **Create Notification**: System creates notification
   - Type = STOCK_ALERT
   - Title = "Low Stock Alert"
   - Message = Product name and current quantity
4. **Send Notification**: System sends to manager
5. **User Receives**: Manager receives push notification
6. **Status Updated**: Notification status = SENT

**Outcome**: Manager alerted about low stock.

### 3. Purchase Limit Alert Workflow

**Scenario**: Purchase exceeds financial limit.

1. **Purchase Processed**: Manager creates purchase invoice
2. **Check Limit**: System checks if exceeds limit
3. **Create Notification**: If exceeded, create notification
   - Type = PURCHASE_LIMIT
   - Title = "Purchase Limit Exceeded"
   - Message = Purchase amount and limit
4. **Send Notification**: System sends to manager
5. **User Receives**: Manager receives alert

**Outcome**: Manager alerted about purchase limit.

### 4. Notification History Workflow

**Scenario**: User wants to view past notifications.

1. **View Notifications**: User opens notification history
2. **View List**: System displays notifications
3. **Filter Options**: 
   - Filter by type
   - Filter by date
   - Filter by status
4. **View Details**: Click to see full notification
5. **Mark Read**: User marks as read

**Outcome**: Notification history reviewed.

## Key Business Rules

1. **Multi-Device**: Users can have multiple devices
2. **Retry Mechanism**: Failed notifications are retried
3. **Status Tracking**: All notifications track delivery status
4. **Multi-Tenancy**: Notifications only for current pharmacy users
5. **Firebase Integration**: Uses Firebase Cloud Messaging
6. **Queue Processing**: Notifications processed asynchronously

## Integration Points

### With Inventory Module
- **Stock Alerts**: Triggers notifications for low stock

### With Purchase Module
- **Purchase Alerts**: Triggers notifications for limit exceedances

### With User Module
- **User Management**: Links notifications to users

## Success Metrics

- **Delivery Rate**: High percentage of successful deliveries
- **Response Time**: Fast notification delivery
- **User Engagement**: Users act on notifications
- **Reliability**: Consistent notification delivery

