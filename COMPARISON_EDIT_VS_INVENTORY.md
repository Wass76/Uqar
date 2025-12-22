# المقارنة: تعديل المخزون vs الجرد الجزئي

## 🔍 الفروقات الأساسية

### 1. **تعديل المخزون** (`editStockQuantityAndExpiryDate`)

**الموقع:** `PUT /api/v1/stock/{stockItemId}/edit`

**الكيفية:**
- ✅ يعدل StockItem **الموجود** مباشرة (UPDATE)
- ✅ يحافظ على نفس StockItem ID
- ✅ يحافظ على Batch Number القديم
- ✅ يحافظ على createdAt و createdBy الأصليين
- ✅ يعدل فقط: quantity, expiryDate, minStockLevel
- ✅ يحدث updatedAt و lastModifiedBy

**الكود:**
```java
stockItem.setQuantity(newQuantity);  // تعديل مباشر
stockItem.setExpiryDate(newExpiryDate);  // تعديل مباشر
stockItem.setLastModifiedBy(currentUser.getId());
stockItem.setUpdatedAt(LocalDateTime.now());
stockItemRepo.save(stockItem);  // UPDATE فقط
```

**الاستخدام:**
- تصحيح أخطاء بسيطة
- تحديث الكمية بعد عملية بيع/شراء
- تعديل تاريخ صلاحية
- تعديلات يومية عادية

---

### 2. **الجرد الجزئي** (`performPartialInventoryAdjustment`)

**الموقع:** `POST /api/v1/stock/inventory/partial-adjustment`

**الكيفية:**
- ✅ **يحذف** StockItem(s) القديمة (DELETE)
- ✅ **ينشئ** StockItem جديد (CREATE)
- ✅ StockItem ID جديد
- ✅ Batch Number جديد تلقائياً
- ✅ createdAt و createdBy جديدان
- ✅ reason = INVENTORY_COUNT

**الكود:**
```java
// 1. حذف القديم
stockItemRepo.deleteAll(existingStockItems);  // DELETE

// 2. إنشاء جديد
StockItem newStockItem = new StockItem();  // CREATE جديد
newStockItem.setBatchNo(generateBatchNumberPrefix() + "-" + productId);
newStockItem.setReason(InventoryAdjustmentReason.INVENTORY_COUNT);
stockItemRepo.save(newStockItem);  // CREATE جديد
```

**الاستخدام:**
- الجرد الفعلي
- تصحيح فروقات كبيرة
- عندما تريد سجل جديد بالكامل

---

## 📊 جدول المقارنة

| الميزة | تعديل المخزون | الجرد الجزئي |
|--------|--------------|-------------|
| **العميلة** | UPDATE | DELETE + CREATE |
| **StockItem ID** | نفس ID | ID جديد |
| **Batch Number** | يبقى كما هو | جديد تلقائياً |
| **createdAt** | يبقى كما هو | تاريخ جديد |
| **updatedAt** | يتحدث | لا يوجد (سجل جديد) |
| **reason** | لا يوجد | INVENTORY_COUNT |
| **المدخلات** | stockItemId | productId + productType |
| **الاستخدام** | تعديلات يومية | الجرد الفعلي |

---

## ⚠️ نقطة مهمة: `updateProductInformationIfNeeded`

الكود في السطور 562-602 (`updateProductInformationIfNeeded`) هو **method مساعد** يستخدم فقط في:
- `addStockWithoutInvoice` (إضافة مخزون بدون فاتورة)

**لا يستخدم في:**
- ❌ `editStockQuantityAndExpiryDate` (تعديل المخزون)
- ❌ `performPartialInventoryAdjustment` (الجرد الجزئي)

**الوظيفة:**
- يحدث معلومات المنتج (refPurchasePrice و refSellingPrice) بعد إضافة مخزون جديد
- خاص بـ `InventoryAdjustmentRequest` فقط

---

## 🎯 هل يؤثر على الجرد؟

**الإجابة: لا، لا يؤثر**

### الأسباب:

1. **`updateProductInformationIfNeeded`**:
   - Method مساعد فقط
   - لا يؤثر على عمليات الجرد
   - يستخدم فقط في `addStockWithoutInvoice`

2. **`editStockQuantityAndExpiryDate`** (تعديل المخزون):
   - **مختلف تماماً** عن الجرد الجزئي
   - يستخدم نمط UPDATE (تعديل مباشر)
   - الجرد الجزئي يستخدم DELETE + CREATE (استبدال)
   - كلاهما موجودان لأغراض مختلفة

---

## 💡 التوصية

### متى تستخدم كل واحد؟

#### استخدم **تعديل المخزون** (`editStockQuantityAndExpiryDate`) عندما:
- ✅ تريد تعديل سجل موجود فقط
- ✅ التعديل بسيط (تصحيح كمية أو تاريخ)
- ✅ تريد المحافظة على تاريخ الإنشاء الأصلي
- ✅ تريد المحافظة على Batch Number الأصلي
- ✅ تعديلات يومية عادية

#### استخدم **الجرد الجزئي** (`performPartialInventoryAdjustment`) عندما:
- ✅ تقوم بجرد فعلي
- ✅ تريد سجل جديد بالكامل
- ✅ تريد Batch Number جديد
- ✅ تريد تتبع أن هذه عملية جرد (reason = INVENTORY_COUNT)
- ✅ تريد تاريخ إنشاء جديد

---

## ✅ الخلاصة

1. **`updateProductInformationIfNeeded`**: Method مساعد لا يؤثر على الجرد
2. **`editStockQuantityAndExpiryDate`**: تعديل مباشر (UPDATE) - مختلف عن الجرد
3. **`performPartialInventoryAdjustment`**: الجرد الجزئي (DELETE + CREATE) - الجرد الفعلي

**الخلاصة:** وجود تعديل المخزون **لا يؤثر** على نظام الجرد. كل واحد له استخدامه الخاص.

