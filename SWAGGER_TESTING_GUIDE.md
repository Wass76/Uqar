# دليل اختبار API على Swagger - Full Inventory Reset

## 📝 شرح الـ Annotations

### 1. `@NotEmpty`
```java
@NotEmpty(message = "Items list cannot be empty. At least one item must be provided.")
private List<InventoryItemDTO> items;
```

**الوظيفة:**
- ✅ يتحقق من أن `items` ليست `null` **ولا فارغة**
- ✅ يرفض: `null`, `[]` (قائمة فارغة)
- ✅ يقبل: قائمة تحتوي على عنصر واحد على الأقل

**الفرق عن `@NotNull`:**
- `@NotNull`: يتحقق فقط من أن القيمة ليست `null` (لكن يمكن أن تكون قائمة فارغة `[]`)
- `@NotEmpty`: يتحقق من أن القيمة ليست `null` **ولا فارغة**

---

### 2. `@Valid`
```java
@Valid
private List<InventoryItemDTO> items;
```

**الوظيفة:**
- ✅ يفعّل validation للـ nested objects (InventoryItemDTO)
- ✅ يتحقق من جميع الـ annotations داخل `InventoryItemDTO`:
  - `@NotNull` على `productId`
  - `@NotNull` على `productType`
  - `@NotNull` و `@Min(1)` على `quantity`

**بدون `@Valid`:**
- ❌ لن يتم التحقق من validation داخل `InventoryItemDTO`
- ❌ سيتم قبول items فارغة أو غير صحيحة

---

### 3. كيف تعمل مع `@Builder`؟

**Lombok @Builder:**
- ✅ يعمل بشكل طبيعي مع validation annotations
- ✅ الـ annotations تبقى على الحقول
- ✅ يتم التحقق من validation عند استخدام `@Valid` في Controller

**مثال:**
```java
// في Controller
@PostMapping("/inventory/full-reset")
public ResponseEntity<List<StockItemDTOResponse>> performFullInventoryReset(
        @Valid @RequestBody FullInventoryResetRequest request) {  // @Valid هنا مهم!
    // ...
}
```

---

## 🧪 كيفية الاختبار على Swagger

### الخطوة 1: فتح Swagger UI

1. شغّل التطبيق
2. افتح المتصفح واذهب إلى: `http://localhost:8080/swagger-ui/index.html`
3. ابحث عن: `POST /api/v1/stock/inventory/full-reset`

---

### الخطوة 2: إعداد Request Body

#### مثال صحيح (Valid Request):

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

#### أمثلة خاطئة (Invalid Requests):

**❌ قائمة فارغة:**
```json
{
  "items": []
}
```
**الخطأ:** `Items list cannot be empty. At least one item must be provided.`

**❌ items مفقود:**
```json
{}
```
**الخطأ:** `Items list cannot be empty. At least one item must be provided.`

**❌ productId مفقود:**
```json
{
  "items": [
    {
      "productType": "PHARMACY",
      "quantity": 100
    }
  ]
}
```
**الخطأ:** `Product ID is required`

**❌ quantity = 0:**
```json
{
  "items": [
    {
      "productId": 1,
      "productType": "PHARMACY",
      "quantity": 0
    }
  ]
}
```
**الخطأ:** `Quantity must be greater than 0`

**❌ quantity مفقود:**
```json
{
  "items": [
    {
      "productId": 1,
      "productType": "PHARMACY"
    }
  ]
}
```
**الخطأ:** `Quantity is required`

---

### الخطوة 3: استخدام Swagger UI

1. **اضغط على `POST /api/v1/stock/inventory/full-reset`**
2. **اضغط على "Try it out"**
3. **في Request body، استخدم الـ JSON التالي:**

```json
{
  "items": [
    {
      "productId": 1,
      "productType": "PHARMACY",
      "quantity": 100,
      "expiryDate": "2025-12-31"
    }
  ]
}
```

4. **اضغط على "Execute"**
5. **راقب الـ Response:**
   - ✅ **200/201**: نجحت العملية
   - ❌ **400**: خطأ في validation (تحقق من الرسالة)
   - ❌ **401**: غير مصرح (تحقق من Authentication)
   - ❌ **404**: المنتج غير موجود

---

### الخطوة 4: اختبار Validation

#### اختبار `@NotEmpty`:
```json
{
  "items": []
}
```
**النتيجة المتوقعة:** 400 Bad Request مع رسالة: `Items list cannot be empty. At least one item must be provided.`

#### اختبار `@Valid` على nested object:
```json
{
  "items": [
    {
      "productType": "PHARMACY",
      "quantity": 100
    }
  ]
}
```
**النتيجة المتوقعة:** 400 Bad Request مع رسالة: `Product ID is required`

#### اختبار `@Min(1)`:
```json
{
  "items": [
    {
      "productId": 1,
      "productType": "PHARMACY",
      "quantity": 0
    }
  ]
}
```
**النتيجة المتوقعة:** 400 Bad Request مع رسالة: `Quantity must be greater than 0`

---

## 📋 أمثلة JSON كاملة للاختبار

### مثال 1: جرد كامل مع منتج واحد
```json
{
  "items": [
    {
      "productId": 1,
      "productType": "PHARMACY",
      "quantity": 150,
      "expiryDate": "2025-12-31",
      "minStockLevel": 20
    }
  ]
}
```

### مثال 2: جرد كامل مع عدة منتجات
```json
{
  "items": [
    {
      "productId": 1,
      "productType": "PHARMACY",
      "quantity": 100,
      "expiryDate": "2025-12-31"
    },
    {
      "productId": 2,
      "productType": "MASTER",
      "quantity": 50,
      "expiryDate": "2025-06-30",
      "minStockLevel": 10
    },
    {
      "productId": 3,
      "productType": "PHARMACY",
      "quantity": 200,
      "expiryDate": "2026-01-15"
    }
  ]
}
```

### مثال 3: بدون expiryDate (اختياري)
```json
{
  "items": [
    {
      "productId": 1,
      "productType": "PHARMACY",
      "quantity": 100
    }
  ]
}
```
**ملاحظة:** `expiryDate` و `minStockLevel` اختياريان، لكن `expiryDate` يُنصح بإضافته.

---

## 🔍 نصائح للاختبار

1. **استخدم Authentication:**
   - تأكد من تسجيل الدخول في Swagger
   - اضغط على "Authorize" وأدخل Token

2. **تحقق من IDs الموجودة:**
   - استخدم `productId` موجود فعلاً في قاعدة البيانات
   - تحقق من `productType` (PHARMACY أو MASTER)

3. **راقب الـ Logs:**
   - افتح console للتطبيق
   - راقب رسائل الـ logger

4. **اختبر الحالات المختلفة:**
   - ✅ Valid request
   - ❌ Empty items list
   - ❌ Missing required fields
   - ❌ Invalid product IDs
   - ❌ Zero or negative quantities

---

## ✅ الخلاصة

- **`@NotEmpty`**: يتحقق من أن القائمة ليست فارغة
- **`@Valid`**: يفعّل validation للـ nested objects
- **`@Builder`**: يعمل بشكل طبيعي مع validation
- **Swagger**: استخدم الأمثلة أعلاه للاختبار

