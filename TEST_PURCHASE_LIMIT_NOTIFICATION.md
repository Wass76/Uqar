# دليل اختبار إشعار تجاوز الحد المالي للشراء

## 📋 معلومات عامة

- **الحد المالي الحالي:** `100,000` (من `application.yml`)
- **API Endpoint:** `POST /api/v1/purchase-invoices`
- **متى يتم الإرسال:** عند إنشاء أو تعديل فاتورة شراء إذا كان `total > 100,000`
- **المستلمون:** PHARMACY_MANAGER و PHARMACY_EMPLOYEE فقط (بدون المتدربين)

---

## 🧪 طريقة الاختبار

### **الطريقة 1: اختبار سريع (تغيير الحد مؤقتاً)**

#### 1. تغيير الحد في `application.yml`:
```yaml
notifications:
  purchase:
    financial-limit: 100  # بدلاً من 100000 (للتجربة فقط)
```

#### 2. إعادة تشغيل التطبيق

#### 3. إنشاء فاتورة شراء بمبلغ أكبر من 100:
```json
POST /api/v1/purchase-invoices
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "SYP",
  "total": 150.00,  // أكبر من 100
  "invoiceNumber": "TEST-INV-001",
  "paymentMethod": "CASH",
  "items": [
    {
      "productId": 1,
      "receivedQty": 10,
      "bonusQty": 0,
      "invoicePrice": 15.00,
      "batchNo": "TEST-BATCH",
      "expiryDate": "2025-12-31",
      "productType": "MASTER",
      "sellingPrice": 20.00,
      "minStockLevel": 5
    }
  ]
}
```

#### 4. التحقق من الإشعار:
- انتظر 5 ثوانٍ (معالجة Database Queue)
- تحقق من جدول `notification`:
```sql
SELECT * FROM notification 
WHERE notification_type = 'PURCHASE_LIMIT_EXCEEDED' 
ORDER BY created_at DESC 
LIMIT 5;
```

#### 5. إعادة الحد الأصلي:
```yaml
notifications:
  purchase:
    financial-limit: 100000
```

---

### **الطريقة 2: اختبار بالحد الأصلي (100,000)**

#### 1. إنشاء فاتورة شراء بمبلغ أكبر من 100,000:
```json
POST /api/v1/purchase-invoices
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "SYP",
  "total": 150000.00,  // أكبر من 100000
  "invoiceNumber": "TEST-INV-002",
  "paymentMethod": "CASH",
  "items": [
    {
      "productId": 1,
      "receivedQty": 1000,
      "bonusQty": 100,
      "invoicePrice": 150.00,
      "batchNo": "BATCH-001",
      "expiryDate": "2025-12-31",
      "productType": "MASTER",
      "sellingPrice": 200.00,
      "minStockLevel": 50
    }
  ]
}
```

#### 2. التحقق من الإشعار:
- انتظر 5 ثوانٍ
- تحقق من Database أو من تطبيق Flutter

---

### **الطريقة 3: اختبار عبر تعديل فاتورة موجودة**

#### 1. إنشاء فاتورة بمبلغ أقل من الحد:
```json
POST /api/v1/purchase-invoices
{
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "SYP",
  "total": 50000.00,  // أقل من 100000
  ...
}
```

#### 2. تعديل الفاتورة لتصبح أكبر من الحد:
```json
PUT /api/v1/purchase-invoices/{id}
{
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "SYP",
  "total": 150000.00,  // أكبر من 100000
  ...
}
```

#### 3. التحقق من الإشعار

---

## 🔍 التحقق من النتائج

### 1. من Database:
```sql
-- التحقق من الإشعارات المعلقة
SELECT id, user_id, title, body, notification_type, status, created_at
FROM notification
WHERE notification_type = 'PURCHASE_LIMIT_EXCEEDED'
  AND status = 'PENDING'
ORDER BY created_at DESC;

-- التحقق من الإشعارات المرسلة
SELECT id, user_id, title, body, notification_type, status, sent_at
FROM notification
WHERE notification_type = 'PURCHASE_LIMIT_EXCEEDED'
  AND status = 'SENT'
ORDER BY sent_at DESC;
```

### 2. من Logs:
ابحث عن:
```
Notification X enqueued to database queue for user Y
Processing X pending notifications
Notification X sent successfully via database queue processor
```

### 3. من تطبيق Flutter:
- يجب أن يصل الإشعار للمستخدمين (PHARMACY_MANAGER و PHARMACY_EMPLOYEE)
- العنوان: "تنبيه: تجاوز حد الشراء"
- المحتوى: "فاتورة الشراء رقم X تجاوزت الحد المالي..."

---

## ⚙️ تخصيص الحد المالي

### تغيير الحد في `application.yml`:
```yaml
notifications:
  purchase:
    financial-limit: 50000  # أي قيمة تريدها
```

### تغيير الحد عبر Environment Variable:
```bash
# Windows PowerShell
$env:NOTIFICATIONS_PURCHASE_FINANCIAL_LIMIT="50000"

# Linux/Mac
export NOTIFICATIONS_PURCHASE_FINANCIAL_LIMIT=50000
```

ثم في `application.yml`:
```yaml
notifications:
  purchase:
    financial-limit: ${NOTIFICATIONS_PURCHASE_FINANCIAL_LIMIT:100000}
```

---

## 📝 ملاحظات مهمة

1. **الإشعارات تُرسل فقط لـ:**
   - PHARMACY_MANAGER
   - PHARMACY_EMPLOYEE
   - **لا تُرسل لـ:** PHARMACY_TRAINEE

2. **معالجة الإشعارات:**
   - تُحفظ في Database بحالة `PENDING`
   - تُعالج كل 5 ثوانٍ بواسطة `NotificationQueueProcessor`
   - تُرسل عبر FCM
   - تتغير الحالة إلى `SENT` أو `FAILED`

3. **إعادة المحاولة:**
   - 3 محاولات تلقائياً
   - إذا فشلت 3 مرات، تصبح `FAILED`

4. **لا يؤثر على المعاملة:**
   - إذا فشل إرسال الإشعار، لا تفشل عملية إنشاء/تعديل الفاتورة
   - الإشعارات في `try-catch` منفصلة

---

## 🐛 استكشاف الأخطاء

### الإشعار لا يصل:
1. تحقق من وجود Device Token للمستخدم:
```sql
SELECT * FROM device_token WHERE user_id = X AND is_active = true;
```

2. تحقق من حالة الإشعار:
```sql
SELECT * FROM notification WHERE id = X;
```

3. تحقق من Logs:
```bash
# ابحث عن أخطاء في معالجة الإشعارات
grep "NotificationQueueProcessor" logs/application.log
```

### الإشعار يصل لكن بعد وقت طويل:
- هذا طبيعي، معالجة Database Queue كل 5 ثوانٍ
- يمكن تقليل `fixedDelay` في `NotificationQueueProcessor` (لكن يزيد الضغط على DB)

---

## ✅ Checklist للاختبار

- [ ] إنشاء فاتورة بمبلغ > الحد المالي
- [ ] التحقق من وجود إشعار في Database (status = PENDING)
- [ ] انتظار 5 ثوانٍ
- [ ] التحقق من تغيير الحالة إلى SENT
- [ ] التحقق من وصول الإشعار في Flutter
- [ ] التحقق من أن الإشعار وصل فقط لـ MANAGER و EMPLOYEE
- [ ] اختبار تعديل فاتورة لتجاوز الحد
- [ ] اختبار فاتورة بمبلغ < الحد (لا يجب أن يصل إشعار)

