ال شي# تطبيق ميزة الجرد - Inventory Counting Implementation

## 📋 ملخص التطبيق

تم تطبيق نظام الجرد الكامل والجزئي حسب المتطلبات المرفقة. النظام يستخدم نمط **الاستبدال (Replace Pattern)** بدلاً من التراكم، مما يجعله مناسباً تماماً للجرد الفعلي.

---

## 🎯 الميزات المطبقة

### 1. الجرد الكامل (Full Inventory Reset)

**Endpoint:** `POST /api/v1/stock/inventory/full-reset`

**الوصف:**
- يحذف **جميع** سجلات `StockItem` للصيدلية
- يعيد إدخال المخزون من الصفر
- ينشئ Batch جديد تلقائياً لكل دواء
- يسجل كل عملية في Audit Log

**Use Case:** INV-FULL-01

**Request Body:**
```json
{
  "items": [
    {
      "productId": 1,
      "productType": "PHARMACY",
      "quantity": 100,
      "expiryDate": "2025-12-31",
      "minStockLevel": 10
    },
    {
      "productId": 2,
      "productType": "MASTER",
      "quantity": 50,
      "expiryDate": "2025-06-30"
    }
  ]
}
```

**Response:** List of created `StockItemDTOResponse`

---

### 2. الجرد الجزئي (Partial Inventory Adjustment)

**Endpoint:** `POST /api/v1/stock/inventory/partial-adjustment`

**الوصف:**
- يبحث عن دواء محدد
- يحذف StockItem(s) القديمة للدواء
- ينشئ StockItem جديد بالقيم المعدلة
- ينشئ Batch جديد تلقائياً

**Use Case:** INV-PART-02

**Request Body:**
```json
{
  "productId": 1,
  "productType": "PHARMACY",
  "newQuantity": 150,
  "newExpiryDate": "2025-12-31",
  "minStockLevel": 10
}
```

**Response:** `StockItemDTOResponse` للـ StockItem الجديد

---

### 3. إحصائية الجرد (Inventory Count Summary)

**Endpoint:** `GET /api/v1/stock/inventory/summary`

**الوصف:**
- يعيد إحصائية عن مخزون الصيدلية:
  - عدد الأدوية الفريدة (unique products)
  - الكمية الإجمالية (total quantity)
  - عدد StockItems (batches)

**Response:**
```json
{
  "totalProducts": 250,
  "totalQuantity": 5000,
  "totalStockItems": 350
}
```

---

## 📁 الملفات الجديدة

### DTOs
1. **`FullInventoryResetRequest.java`**
   - يحتوي على قائمة items لإعادة الإدخال
   - كل item يحتوي على: productId, productType, quantity, expiryDate, minStockLevel

2. **`PartialInventoryAdjustmentRequest.java`**
   - يحتوي على: productId, productType, newQuantity, newExpiryDate, minStockLevel

3. **`InventoryCountSummaryResponse.java`**
   - يحتوي على: totalProducts, totalQuantity, totalStockItems

### Service Methods
في `StockService.java`:
- `performFullInventoryReset()`: حذف الكل + إعادة الإدخال
- `performPartialInventoryAdjustment()`: حذف القديم + إنشاء جديد
- `getInventoryCountSummary()`: إحصائية الجرد
- `determinePurchasePriceForInventoryCount()`: helper method لتحديد السعر
- `generateBatchNumberPrefix()`: helper method لإنشاء Batch Number تلقائياً

### Controller Endpoints
في `StockManagementController.java`:
- `POST /api/v1/stock/inventory/full-reset`
- `POST /api/v1/stock/inventory/partial-adjustment`
- `GET /api/v1/stock/inventory/summary`

---

## 🔑 النقاط المهمة

### 1. نمط الاستبدال (Replace Pattern)
- **الجرد الكامل**: يحذف كل شيء ثم يعيد الإدخال
- **الجرد الجزئي**: يحذف StockItem(s) القديمة ثم ينشئ جديدة

### 2. Batch Number تلقائي
- يتم إنشاء Batch Number تلقائياً بالصيغة: `INV-YYYYMMDD-HHMMSS-{productId}`
- مثال: `INV-20250115-143025-123`

### 3. السعر التلقائي
- للمنتجات MASTER: يستخدم `refPurchasePrice`
- للمنتجات PHARMACY: يستخدم `refPurchasePrice`

### 4. Audit Trail
- كل عملية جرد تسجل في `reason` = `INVENTORY_COUNT`
- يتم حفظ `notes` مع التاريخ والوقت
- يتم حفظ `createdBy` و `createdAt` تلقائياً

### 5. الأمان
- يتطلب صلاحيات: `PHARMACY_MANAGER` أو `PHARMACY_EMPLOYEE`
- التحقق من أن الموظف مرتبط بصيدلية
- التحقق من أن المنتج موجود قبل المعالجة

---

## 🔄 الفرق بين الطريقتين

| الميزة | InventoryAdjustmentRequest (الموجود) | نظام الجرد الجديد |
|--------|-------------------------------------|-------------------|
| **الهدف** | إضافة/تعديل عام | جرد فعلي |
| **النمط** | تراكمي (Accumulative) | استبدالي (Replace) |
| **الجرد الكامل** | ❌ غير متوفر | ✅ متوفر |
| **الجرد الجزئي** | ❌ تراكم فقط | ✅ حذف + إنشاء |
| **Batch تلقائي** | اختياري | تلقائي دائماً |
| **الاستخدام** | تعديلات عامة | الجرد السنوي/الشهري |

---

## ✅ الخلاصة

تم تطبيق نظام الجرد الكامل والجزئي بنجاح حسب المتطلبات. النظام:

1. ✅ يحذف السجلات القديمة قبل الإنشاء (استبدال)
2. ✅ ينشئ Batch Numbers تلقائياً
3. ✅ يسجل كل عملية في Audit Log
4. ✅ يوفر إحصائية الجرد
5. ✅ آمن ومحمي بالصلاحيات
6. ✅ يتحقق من صحة البيانات

**التوصية:** استخدام نظام الجرد الجديد للجرد السنوي/الشهري، والاحتفاظ بـ `InventoryAdjustmentRequest` للاستخدامات العامة الأخرى.

