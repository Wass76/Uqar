# دليل اختبار الإشعارات على Swagger

## 📋 الخطوات الكاملة لإرسال إشعار

---

## الخطوة 1: تسجيل الدخول (Login) للحصول على JWT Token

### Endpoint:
```
POST /api/v1/auth/login
```

### Request Body:
```json
{
  "email": "admin@example.com",
  "password": "password123"
}
```

### Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "admin@example.com",
  "firstName": "Admin",
  "lastName": "User",
  "role": "PLATFORM_ADMIN",
  "isActive": true
}
```

### ⚠️ ملاحظة مهمة:
- **لإرسال إشعار**: تحتاج `PLATFORM_ADMIN` role
- **لتسجيل Device Token**: تحتاج `PHARMACY_MANAGER` أو `PHARMACY_EMPLOYEE`

---

## الخطوة 2: تفعيل Authorization في Swagger

1. في Swagger UI، ابحث عن زر **"Authorize"** 🔒 (عادة في الأعلى)
2. انقر عليه
3. في حقل **"Value"**، أدخل:
   ```
   Bearer <your-token-here>
   ```
   مثال:
   ```
   Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
4. انقر **"Authorize"**
5. انقر **"Close"**

✅ الآن جميع الـ requests ستتضمن الـ token تلقائياً

---

## الخطوة 3: تسجيل Device Token (اختياري لكن مهم)

> **ملاحظة**: بدون Device Token، الإشعار سيُحفظ في DB لكن لن يصل للجهاز!

### Endpoint:
```
POST /api/v1/notifications/register-token
```

### Headers:
```
Authorization: Bearer <token>
```

### Request Body:
```json
{
  "deviceToken": "fcm-device-token-here-123456789",
  "deviceType": "ANDROID"
}
```

### أنواع Device Type:
- `ANDROID`
- `IOS`
- `WEB`

### Response:
```json
{
  "id": 1,
  "userId": 5,
  "deviceToken": "fcm-device-token-here-123456789",
  "deviceType": "ANDROID",
  "isActive": true,
  "createdAt": "2025-01-15T10:00:00",
  "updatedAt": "2025-01-15T10:00:00"
}
```

---

## الخطوة 4: إرسال الإشعار

### Endpoint:
```
POST /api/v1/notifications/send
```

### Headers:
```
Authorization: Bearer <token>
Content-Type: application/json
```

### Request Body (مثال بسيط):
```json
{
  "userId": 5,
  "title": "تنبيه: مخزون منخفض",
  "body": "يوجد 5 منتجات بمخزون منخفض",
  "notificationType": "STOCK_LOW"
}
```

### Request Body (مثال مع data):
```json
{
  "userId": 5,
  "title": "تنبيه: مخزون منخفض",
  "body": "يوجد 5 منتجات بمخزون منخفض",
  "notificationType": "STOCK_LOW",
  "data": {
    "pharmacyId": 10,
    "pharmacyName": "صيدلية النور",
    "lowStockCount": 5,
    "products": [
      "باراسيتامول (المتاح: 10، الحد الأدنى: 20)",
      "إيبوبروفين (المتاح: 5، الحد الأدنى: 15)"
    ]
  }
}
```

### Response (نجاح):
```json
{
  "id": 100,
  "userId": 5,
  "title": "تنبيه: مخزون منخفض",
  "body": "يوجد 5 منتجات بمخزون منخفض",
  "notificationType": "STOCK_LOW",
  "data": {
    "pharmacyId": 10,
    "pharmacyName": "صيدلية النور",
    "lowStockCount": 5
  },
  "status": "SENT",
  "sentAt": "2025-01-15T10:05:00",
  "readAt": null,
  "createdAt": "2025-01-15T10:05:00"
}
```

### Response (فشل - لا يوجد Device Token):
```json
{
  "id": 101,
  "userId": 5,
  "title": "تنبيه: مخزون منخفض",
  "body": "يوجد 5 منتجات بمخزون منخفض",
  "notificationType": "STOCK_LOW",
  "status": "FAILED",
  "sentAt": null,
  "readAt": null,
  "createdAt": "2025-01-15T10:05:00"
}
```

---

## الخطوة 5: التحقق من الإشعارات

### 5.1: الحصول على جميع الإشعارات

#### Endpoint:
```
GET /api/v1/notifications?page=0&size=20
```

#### Response:
```json
{
  "content": [
    {
      "id": 100,
      "userId": 5,
      "title": "تنبيه: مخزون منخفض",
      "body": "يوجد 5 منتجات بمخزون منخفض",
      "status": "SENT",
      "sentAt": "2025-01-15T10:05:00",
      "readAt": null
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### 5.2: الحصول على الإشعارات غير المقروءة

#### Endpoint:
```
GET /api/v1/notifications/unread
```

### 5.3: عدد الإشعارات غير المقروءة

#### Endpoint:
```
GET /api/v1/notifications/unread/count
```

#### Response:
```json
3
```

### 5.4: تحديد إشعار كمقروء

#### Endpoint:
```
PUT /api/v1/notifications/{id}/read
```

#### Response:
```json
{
  "id": 100,
  "userId": 5,
  "title": "تنبيه: مخزون منخفض",
  "body": "يوجد 5 منتجات بمخزون منخفض",
  "status": "SENT",
  "sentAt": "2025-01-15T10:05:00",
  "readAt": "2025-01-15T10:10:00"
}
```

---

## 📝 أمثلة إشعارات مختلفة

### 1. إشعار دين متأخر:
```json
{
  "userId": 5,
  "title": "تنبيه: دين متأخر",
  "body": "يوجد 3 ديون متأخرة في صيدلية النور",
  "notificationType": "DEBT_OVERDUE",
  "data": {
    "pharmacyId": 10,
    "pharmacyName": "صيدلية النور",
    "overdueDebtCount": 3,
    "totalAmount": 5000.50
  }
}
```

### 2. إشعار منتج منتهي الصلاحية:
```json
{
  "userId": 5,
  "title": "تنبيه: منتجات منتهية الصلاحية",
  "body": "يوجد 2 منتج منتهي الصلاحية",
  "notificationType": "STOCK_EXPIRED",
  "data": {
    "pharmacyId": 10,
    "expiredCount": 2,
    "products": [
      "باراسيتامول (تاريخ الانتهاء: 2025-01-10)",
      "إيبوبروفين (تاريخ الانتهاء: 2025-01-12)"
    ]
  }
}
```

### 3. إشعار مرتجع:
```json
{
  "userId": 5,
  "title": "مرتجع جديد",
  "body": "تم إرجاع فاتورة #1234 بقيمة 150.00",
  "notificationType": "SALE_REFUNDED",
  "data": {
    "saleId": 1234,
    "refundAmount": 150.00,
    "currency": "SYP"
  }
}
```

---

## 🔍 أنواع الإشعارات المتاحة

### تنبيهات المخزون:
- `STOCK_LOW` - انخفاض المخزون
- `STOCK_EXPIRED` - منتج منتهي الصلاحية
- `STOCK_EXPIRING_SOON` - منتج قريب منتهي الصلاحية

### تنبيهات مالية:
- `DEBT_CREATED` - دين جديد
- `DEBT_OVERDUE` - دين متأخر
- `DEBT_PAID` - دين تم سداده
- `PURCHASE_LIMIT_EXCEEDED` - تجاوز حد مالي في الشراء

### تنبيهات المبيعات:
- `SALE_CREATED` - فاتورة بيع جديدة
- `SALE_REFUNDED` - مرتجع

### تنبيهات المشتريات:
- `PURCHASE_ORDER_CREATED` - طلب شراء جديد
- `PURCHASE_INVOICE_RECEIVED` - وصول فاتورة شراء

---

## ⚠️ أخطاء شائعة وحلولها

### 1. Error 401 (Unauthorized):
**السبب**: لم تقم بتفعيل Authorization في Swagger  
**الحل**: اتبع الخطوة 2 أعلاه

### 2. Error 403 (Forbidden):
**السبب**: المستخدم ليس لديه الصلاحية المطلوبة  
**الحل**: 
- لإرسال إشعار: تحتاج `PLATFORM_ADMIN`
- لتسجيل Device Token: تحتاج `PHARMACY_MANAGER` أو `PHARMACY_EMPLOYEE`

### 3. الإشعار status = "FAILED":
**السبب**: المستخدم ليس لديه Device Token مسجل  
**الحل**: سجل Device Token أولاً (الخطوة 3)

### 4. الإشعار لا يصل للجهاز:
**السبب**: 
- Device Token غير صحيح
- Firebase غير مُعد بشكل صحيح
- الجهاز غير متصل بالإنترنت

**الحل**: 
- تحقق من Firebase configuration
- تأكد من صحة Device Token
- تحقق من logs في console

---

## 🎯 Flow Diagram

```
1. Login → Get JWT Token
   ↓
2. Authorize in Swagger (Bearer Token)
   ↓
3. Register Device Token (اختياري)
   ↓
4. Send Notification
   ↓
5. Check Notification Status
   ↓
6. Mark as Read (اختياري)
```

---

## 📌 ملاحظات مهمة

1. **Device Token**: بدون Device Token، الإشعار سيُحفظ في DB لكن لن يصل للجهاز
2. **Firebase**: تأكد من إعداد Firebase بشكل صحيح
3. **Roles**: كل endpoint يحتاج role محدد
4. **Testing**: يمكنك اختبار الإشعارات حتى بدون Device Token (ستكون status = "FAILED")

---

## 🚀 Quick Test (بدون Device Token)

إذا أردت اختبار النظام بسرعة بدون Device Token:

1. Login كـ `PLATFORM_ADMIN`
2. Authorize في Swagger
3. Send Notification
4. Check Status (ستكون "FAILED" لكن الإشعار محفوظ في DB)
5. Get Notifications (ستجد الإشعار في القائمة)

---

## 📞 Support

إذا واجهت أي مشاكل:
1. تحقق من logs في console
2. تحقق من Firebase configuration
3. تحقق من Database (جدول `notification`)
4. تحقق من Device Token في جدول `device_token`

