# تحليل متطلب: البيع بالتجزئة (Fractional Sales)

## 📋 وصف المتطلب

### المشكلة
في الصيدليات السورية، بعض الأدوية تباع **بالأجزاء** وليس بالوحدة الكاملة. على سبيل المثال:
- **علبة دواء** تحتوي على **10 ظروف**
- يمكن بيع **ظرف واحد** فقط للعميل
- يجب تتبع **الأجزاء المتبقية** من كل علبة

### المتطلب
إضافة خاصية تسمح ببيع الأدوية **بالأجزاء** مع:
1. تحديد ما إذا كان الدواء قابل للتجزئة
2. تحديد عدد الأجزاء في الوحدة الواحدة
3. حساب سعر الجزء تلقائياً
4. تتبع الأجزاء المتبقية من كل وحدة في المخزون
5. إنقاص كمية الدواء (quantity) عندما تنتهي جميع أجزاء الوحدة

---

## 🔍 التحليل التفصيلي

### الحالة الحالية في النظام

#### 1. هيكل البيانات الحالي

**MasterProduct / PharmacyProduct:**
- `refSellingPrice`: سعر البيع المرجعي (للوحدة الكاملة)
- `quantity`: الكمية في المخزون (بالوحدات الكاملة)

**StockItem:**
- `quantity`: عدد الوحدات الكاملة
- `productId`: معرف المنتج
- `productType`: نوع المنتج (MASTER/PHARMACY)

**SaleInvoiceItem:**
- `quantity`: الكمية المباعة (بالوحدات الكاملة)
- `unitPrice`: سعر الوحدة
- `subTotal`: الإجمالي الفرعي

#### 2. منطق البيع الحالي

```
عند البيع:
1. التحقق من الكمية المتاحة (quantity >= المطلوبة)
2. إنقاص quantity من StockItem
3. حساب subTotal = quantity × unitPrice
```

### الحالة المطلوبة بعد التعديل

#### 1. مثال عملي

**سيناريو:**
- **علبة دواء** تحتوي على **10 ظروف**
- السعر الكامل للعلبة: **10,000 SYP**
- سعر الظرف الواحد: **1,000 SYP** (10,000 ÷ 10)

**حالات البيع:**
- **الحالة 1**: بيع علبة كاملة (10 ظروف) → `quantity` ينقص بـ 1
- **الحالة 2**: بيع 3 ظروف من علبة → `remainingParts` ينقص بـ 3
- **الحالة 3**: بيع 7 ظروف أخرى من نفس العلبة → `remainingParts` يصبح 0 → `quantity` ينقص بـ 1

---

## 🎯 التوصيف التقني

### 1. تعديلات قاعدة البيانات

#### أ. MasterProduct Entity

```java
@Entity
public class MasterProduct extends AuditedEntity {
    // ... الحقول الحالية
    
    // ✅ إضافة حقول جديدة
    @Column(nullable = false)
    private Boolean isDivisible = false;  // هل قابل للتجزئة
    
    @Column
    private Integer partsPerUnit;  // عدد الأجزاء في الوحدة (مثلاً 10)
    
    // Validation: إذا isDivisible = true، يجب partsPerUnit > 0
}
```

**قاعدة البيانات:**
```sql
ALTER TABLE master_product 
ADD COLUMN is_divisible BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN parts_per_unit INTEGER;

-- Constraint: إذا is_divisible = true، يجب parts_per_unit > 0
ALTER TABLE master_product 
ADD CONSTRAINT check_parts_per_unit 
CHECK (is_divisible = false OR (is_divisible = true AND parts_per_unit > 0));
```

#### ب. PharmacyProduct Entity

```java
@Entity
public class PharmacyProduct extends AuditedEntity {
    // ... الحقول الحالية
    
    // ✅ إضافة حقول جديدة (نفس MasterProduct)
    @Column(nullable = false)
    private Boolean isDivisible = false;
    
    @Column
    private Integer partsPerUnit;
}
```

**قاعدة البيانات:**
```sql
ALTER TABLE pharmacy_product 
ADD COLUMN is_divisible BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN parts_per_unit INTEGER;

ALTER TABLE pharmacy_product 
ADD CONSTRAINT check_parts_per_unit 
CHECK (is_divisible = false OR (is_divisible = true AND parts_per_unit > 0));
```

#### ج. StockItem Entity

```java
@Entity
public class StockItem extends AuditedEntity {
    // ... الحقول الحالية
    
    // ✅ إضافة حقل جديد
    @Column
    private Integer remainingParts;  // الأجزاء المتبقية من الوحدة الحالية
    
    // Logic:
    // - إذا isDivisible = false: remainingParts = null
    // - إذا isDivisible = true: remainingParts = partsPerUnit عند الإضافة
    // - عند البيع بالأجزاء: remainingParts ينقص
    // - عندما remainingParts = 0: quantity ينقص بـ 1، و remainingParts يعود إلى partsPerUnit
}
```

**قاعدة البيانات:**
```sql
ALTER TABLE stock_item 
ADD COLUMN remaining_parts INTEGER;

-- Constraint: إذا المنتج قابل للتجزئة، يجب remaining_parts >= 0
-- (سيتم التحقق في Service Layer)
```

#### د. SaleInvoiceItem Entity

```java
@Entity
public class SaleInvoiceItem extends AuditedEntity {
    // ... الحقول الحالية
    
    // ✅ إضافة حقول جديدة
    @Column
    private Boolean soldAsParts = false;  // هل تم البيع بالأجزاء
    
    @Column
    private Integer partsSold;  // عدد الأجزاء المباعة (إذا soldAsParts = true)
    
    // Logic:
    // - إذا soldAsParts = false: quantity = عدد الوحدات الكاملة
    // - إذا soldAsParts = true: partsSold = عدد الأجزاء، quantity = 0
}
```

**قاعدة البيانات:**
```sql
ALTER TABLE sale_invoice_items 
ADD COLUMN sold_as_parts BOOLEAN DEFAULT false,
ADD COLUMN parts_sold INTEGER;
```

---

### 2. منطق العمل (Business Logic)

#### أ. حساب سعر الجزء

```java
public Float calculatePartPrice(Long productId, ProductType productType) {
    Product product = getProduct(productId, productType);
    
    if (!product.getIsDivisible()) {
        throw new IllegalArgumentException("Product is not divisible");
    }
    
    Float fullPrice = product.getRefSellingPrice();
    Integer partsPerUnit = product.getPartsPerUnit();
    
    return fullPrice / partsPerUnit;
}
```

#### ب. منطق البيع بالتجزئة

```java
public void processFractionalSale(SaleInvoiceItem item, StockItem stockItem) {
    Product product = getProduct(stockItem.getProductId(), stockItem.getProductType());
    
    if (!product.getIsDivisible()) {
        // البيع العادي (بالوحدات الكاملة)
        processNormalSale(item, stockItem);
        return;
    }
    
    // البيع بالتجزئة
    if (item.getSoldAsParts()) {
        Integer partsToSell = item.getPartsSold();
        Integer availableParts = calculateAvailableParts(stockItem);
        
        // التحقق من الأجزاء المتاحة
        if (partsToSell > availableParts) {
            throw new InsufficientStockException("Not enough parts available");
        }
        
        // إنقاص الأجزاء المتبقية
        Integer currentRemaining = stockItem.getRemainingParts() != null 
            ? stockItem.getRemainingParts() 
            : product.getPartsPerUnit();
        
        Integer newRemaining = currentRemaining - partsToSell;
        
        if (newRemaining == 0) {
            // انتهت الأجزاء، إنقاص quantity
            stockItem.setQuantity(stockItem.getQuantity() - 1);
            stockItem.setRemainingParts(null);  // سيتم تعيينه عند الوحدة التالية
        } else {
            stockItem.setRemainingParts(newRemaining);
        }
        
        // حساب السعر
        Float partPrice = calculatePartPrice(product.getId(), product.getProductType());
        item.setUnitPrice(partPrice);
        item.setSubTotal(partPrice * partsToSell);
        
    } else {
        // البيع بالوحدة الكاملة
        processNormalSale(item, stockItem);
    }
}
```

#### ج. حساب الأجزاء المتاحة

```java
public Integer calculateAvailableParts(StockItem stockItem) {
    Product product = getProduct(stockItem.getProductId(), stockItem.getProductType());
    
    if (!product.getIsDivisible()) {
        return null;  // غير قابل للتجزئة
    }
    
    Integer fullUnits = stockItem.getQuantity();
    Integer remainingParts = stockItem.getRemainingParts() != null 
        ? stockItem.getRemainingParts() 
        : product.getPartsPerUnit();
    
    // الأجزاء المتاحة = (الوحدات الكاملة × أجزاء الوحدة) + الأجزاء المتبقية
    return (fullUnits * product.getPartsPerUnit()) + remainingParts;
}
```

#### د. عند إضافة منتج جديد للمخزون

```java
public void addProductToStock(StockItem stockItem, Product product) {
    // ... الكود الحالي
    
    // ✅ إضافة منطق الأجزاء
    if (product.getIsDivisible() && product.getPartsPerUnit() != null) {
        // عند إضافة وحدة جديدة، الأجزاء المتبقية = عدد الأجزاء الكامل
        stockItem.setRemainingParts(product.getPartsPerUnit());
    } else {
        stockItem.setRemainingParts(null);
    }
}
```

---

### 3. تعديلات API

#### أ. SaleInvoiceDTORequest

```java
public class SaleInvoiceItemDTORequest {
    // ... الحقول الحالية
    
    // ✅ إضافة حقول جديدة
    private Boolean soldAsParts;  // هل البيع بالأجزاء
    private Integer partsSold;    // عدد الأجزاء (إذا soldAsParts = true)
    
    // Validation:
    // - إذا soldAsParts = true: يجب partsSold > 0
    // - إذا soldAsParts = false: quantity يجب أن يكون > 0
}
```

#### ب. Product DTOs

```java
public class PharmacyProductDTORequest {
    // ... الحقول الحالية
    
    // ✅ إضافة حقول جديدة
    private Boolean isDivisible;
    private Integer partsPerUnit;
}
```

---

### 4. سيناريوهات الاستخدام

#### السيناريو 1: بيع علبة كاملة (10 ظروف)

**Input:**
```json
{
  "stockItemId": 123,
  "quantity": 1,  // علبة واحدة
  "soldAsParts": false
}
```

**Processing:**
- `quantity` في StockItem ينقص بـ 1
- `remainingParts` يبقى كما هو (أو يعود إلى `partsPerUnit`)

---

#### السيناريو 2: بيع 3 ظروف من علبة

**Input:**
```json
{
  "stockItemId": 123,
  "soldAsParts": true,
  "partsSold": 3
}
```

**Processing:**
- `remainingParts` ينقص من 10 إلى 7
- `quantity` يبقى 1 (العلبة ما زالت موجودة)
- السعر = `refSellingPrice / 10 * 3`

---

#### السيناريو 3: بيع 7 ظروف أخرى (إكمال العلبة)

**Input:**
```json
{
  "stockItemId": 123,
  "soldAsParts": true,
  "partsSold": 7
}
```

**Processing:**
- `remainingParts` ينقص من 7 إلى 0
- **عندما `remainingParts = 0`**: `quantity` ينقص بـ 1
- `remainingParts` يعود إلى `null` (أو `partsPerUnit` للوحدة التالية)

---

#### السيناريو 4: بيع من وحدات متعددة

**Input:**
```json
{
  "stockItemId": 123,
  "soldAsParts": true,
  "partsSold": 15  // أكثر من علبة واحدة
}
```

**Processing:**
- الوحدة الأولى: `remainingParts` من 10 إلى 0 → `quantity` ينقص بـ 1
- الوحدة الثانية: `remainingParts` من 10 إلى 5 → `quantity` يبقى
- النتيجة: `quantity` ينقص بـ 1، `remainingParts` = 5

---

### 5. تحديثات واجهة المستخدم (Frontend)

#### أ. عند إضافة منتج للبيع

```javascript
// إذا كان المنتج قابل للتجزئة
if (product.isDivisible) {
    // عرض خيارين:
    // 1. البيع بالوحدة الكاملة
    // 2. البيع بالأجزاء
    
    // إذا اختار البيع بالأجزاء:
    // - عرض عدد الأجزاء المتاحة
    // - عرض سعر الجزء الواحد
    // - السماح بإدخال عدد الأجزاء
}
```

#### ب. عرض المخزون

```javascript
// في قائمة المخزون
if (stockItem.product.isDivisible) {
    display = `
        الوحدات الكاملة: ${stockItem.quantity}
        الأجزاء المتبقية: ${stockItem.remainingParts || stockItem.product.partsPerUnit}
        الإجمالي بالأجزاء: ${calculateTotalParts(stockItem)}
    `;
}
```

---

## 📝 ملخص التعديلات المطلوبة

### 1. قاعدة البيانات
- ✅ إضافة `isDivisible` و `partsPerUnit` في `master_product`
- ✅ إضافة `isDivisible` و `partsPerUnit` في `pharmacy_product`
- ✅ إضافة `remainingParts` في `stock_item`
- ✅ إضافة `soldAsParts` و `partsSold` في `sale_invoice_items`

### 2. Entities
- ✅ تحديث `MasterProduct.java`
- ✅ تحديث `PharmacyProduct.java`
- ✅ تحديث `StockItem.java`
- ✅ تحديث `SaleInvoiceItem.java`

### 3. Services
- ✅ تحديث `SaleService.createSaleInvoice()` لدعم البيع بالأجزاء
- ✅ تحديث `StockService` لحساب الأجزاء المتاحة
- ✅ تحديث `PurchaseInvoiceService` لتعيين `remainingParts` عند الإضافة

### 4. DTOs
- ✅ تحديث `SaleInvoiceItemDTORequest`
- ✅ تحديث `PharmacyProductDTORequest`
- ✅ تحديث `MasterProductDTORequest`

### 5. Validation
- ✅ التحقق من `partsPerUnit > 0` إذا `isDivisible = true`
- ✅ التحقق من الأجزاء المتاحة قبل البيع
- ✅ التحقق من `partsSold > 0` إذا `soldAsParts = true`

### 6. Business Logic
- ✅ حساب سعر الجزء تلقائياً
- ✅ تتبع الأجزاء المتبقية
- ✅ إنقاص `quantity` عندما `remainingParts = 0`

---

## ⚠️ اعتبارات إضافية

### 1. المرتجعات (Refunds)
- عند إرجاع جزء: يجب إضافة `remainingParts` مرة أخرى
- عند إرجاع وحدة كاملة: إضافة `quantity` + `remainingParts`

### 2. التقارير
- تحديث تقارير المخزون لعرض الأجزاء المتبقية
- تحديث تقارير المبيعات لعرض المبيعات بالأجزاء

### 3. التوافق مع النظام الحالي
- المنتجات الموجودة: `isDivisible = false` (الافتراضي)
- البيع العادي: يبقى كما هو (لا يتأثر)

---

## 🎯 خطة التنفيذ المقترحة

### المرحلة 1: قاعدة البيانات والـ Entities
1. إنشاء migration script
2. تحديث Entities
3. تحديث DTOs

### المرحلة 2: Business Logic
1. تحديث `SaleService`
2. تحديث `StockService`
3. إضافة validation

### المرحلة 3: API و Frontend
1. تحديث API endpoints
2. تحديث Frontend forms
3. تحديث عرض المخزون

### المرحلة 4: الاختبار
1. اختبار البيع بالوحدة الكاملة
2. اختبار البيع بالأجزاء
3. اختبار الحالات الحدية

---

**تم التحليل والتوصيف بواسطة:** وسيم تنبكجي
**التاريخ:** 2025-21-01  
**الحالة:** جاهز للتنفيذ


