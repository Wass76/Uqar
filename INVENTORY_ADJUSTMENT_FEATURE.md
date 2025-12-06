# ميزة إضافة الأدوية للمخزون بدون فاتورة شراء
## Inventory Adjustment Feature - Adding Stock Without Purchase Invoice

---

## 📋 نظرة عامة / Overview

هذه الميزة تسمح بإضافة أدوية جديدة إلى المخزون بدون الحاجة لإنشاء فاتورة شراء. هذا مفيد بشكل خاص أثناء عمليات الجرد عندما يتم اكتشاف أدوية إضافية في المخزون الفعلي ولكن لا يمكن تحديد فاتورة الشراء التي جاءت منها.

This feature allows adding new medicines to inventory without creating a purchase invoice. This is particularly useful during inventory counts when extra medicines are discovered in the actual stock but the source purchase invoice cannot be identified.

---

## 🎯 الهدف من الميزة / Feature Purpose

### الحالات الاستخدام / Use Cases:
1. **الجرد الفعلي / Physical Inventory**: عند اكتشاف أدوية زائدة أثناء الجرد
2. **تصحيح الأخطاء / Error Correction**: تصحيح أخطاء في إدخال البيانات السابقة
3. **الهدايا والعينات / Gifts & Samples**: إضافة عينات أو هدايا من الموردين
4. **المرتجعات غير المسجلة / Unrecorded Returns**: إضافة مرتجعات لم يتم تسجيلها بشكل صحيح

---

## 🏗️ البنية الحالية للنظام / Current System Architecture

### الكيانات الرئيسية / Main Entities:

#### 1. **StockItem Entity**
```java
@Entity
@Table(name = "stock_item")
public class StockItem extends AuditedEntity {
    private Long productId;
    private ProductType productType; // 'MASTER' or 'PHARMACY'
    private Integer quantity;
    private Integer bonusQty;
    private LocalDate expiryDate;
    private String batchNo;
    private String invoiceNumber;
    private Double actualPurchasePrice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_invoice_id")
    private PurchaseInvoice purchaseInvoice; // ⚠️ NULLABLE - يمكن أن يكون null
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;
    
    private LocalDate dateAdded;
    private Long addedBy;
}
```

**ملاحظة مهمة**: حقل `purchaseInvoice` قابل للقيمة null، مما يعني أنه يمكن إنشاء `StockItem` بدون ربطه بفاتورة شراء.

**Important Note**: The `purchaseInvoice` field is nullable, meaning `StockItem` can be created without being linked to a purchase invoice.

#### 2. **PurchaseInvoiceService.createStockItemRecords()**
الطريقة الحالية لإنشاء `StockItem` عند إنشاء فاتورة شراء:
```java
private void createStockItemRecords(PurchaseInvoice invoice, PurchaseInvoiceDTORequest request) {
    for (PurchaseInvoiceItem item : invoice.getItems()) {
        StockItem stockItem = new StockItem();
        // ... تعيين القيم
        stockItem.setPurchaseInvoice(invoice); // ربط بفاتورة الشراء
        stockItemRepo.save(stockItem);
    }
}
```

---

## 💡 التصميم المقترح / Proposed Design

### 1. إنشاء DTO جديد / New DTO Creation

#### **InventoryAdjustmentRequest.java**
```java
package com.Teryaq.product.dto;

import com.Teryaq.product.Enum.ProductType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustmentRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Product type is required")
    private ProductType productType; // MASTER or PHARMACY
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;
    
    @Min(value = 0, message = "Bonus quantity cannot be negative")
    private Integer bonusQty; // Optional, defaults to 0
    
    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
    private Double actualPurchasePrice;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate; // Optional but recommended
    
    private String batchNo; // Optional
    
    private String invoiceNumber; // Optional - يمكن أن يكون رقم فاتورة غير موجودة في النظام
    
    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minStockLevel; // Optional
    
    @NotBlank(message = "Reason is required for audit purposes")
    private String reason; // سبب الإضافة (مثل: "جرد فعلي", "تصحيح خطأ", إلخ)
    
    private String notes; // ملاحظات إضافية
}
```

### 2. إضافة Method جديد في StockService / New Method in StockService

#### **StockService.addStockWithoutInvoice()**
```java
@Transactional
public StockItemDTOResponse addStockWithoutInvoice(InventoryAdjustmentRequest request) {
    // 1. التحقق من الصلاحيات
    User currentUser = getCurrentUser();
    if (!(currentUser instanceof Employee)) {
        throw new UnAuthorizedException("Only pharmacy employees can add stock items");
    }
    
    Employee employee = (Employee) currentUser;
    if (employee.getPharmacy() == null) {
        throw new UnAuthorizedException("Employee is not associated with any pharmacy");
    }
    
    Pharmacy pharmacy = employee.getPharmacy();
    
    // 2. التحقق من وجود المنتج
    validateProductExists(request.getProductId(), request.getProductType());
    
    // 3. التحقق من تاريخ الصلاحية (إن وجد)
    if (request.getExpiryDate() != null) {
        validateExpiryDate(request.getExpiryDate());
    }
    
    // 4. إنشاء StockItem جديد
    StockItem stockItem = new StockItem();
    
    // المعلومات الأساسية
    stockItem.setProductId(request.getProductId());
    stockItem.setProductType(request.getProductType());
    stockItem.setPharmacy(pharmacy);
    
    // الكميات
    int bonusQty = request.getBonusQty() != null ? request.getBonusQty() : 0;
    int totalQuantity = request.getQuantity() + bonusQty;
    stockItem.setQuantity(totalQuantity);
    stockItem.setBonusQty(bonusQty);
    
    // الأسعار
    stockItem.setActualPurchasePrice(request.getActualPurchasePrice());
    
    // معلومات الدفعة والصلاحية
    stockItem.setExpiryDate(request.getExpiryDate());
    stockItem.setBatchNo(request.getBatchNo());
    stockItem.setInvoiceNumber(request.getInvoiceNumber());
    
    // الحد الأدنى للمخزون
    if (request.getMinStockLevel() != null) {
        stockItem.setMinStockLevel(request.getMinStockLevel());
    }
    
    // ⚠️ المهم: عدم ربط بفاتورة شراء
    stockItem.setPurchaseInvoice(null); // NULL - بدون فاتورة شراء
    
    // معلومات التدقيق (Audit)
    stockItem.setDateAdded(LocalDate.now());
    stockItem.setAddedBy(currentUser.getId());
    
    // 5. حفظ في قاعدة البيانات
    StockItem savedStockItem = stockItemRepo.save(stockItem);
    
    // 6. إرجاع الاستجابة
    StockItemDTOResponse response = stockItemMapper.toResponse(savedStockItem);
    response.setPharmacyId(pharmacy.getId());
    // purchaseInvoiceNumber سيكون null لأنها ليست مرتبطة بفاتورة
    
    // 7. (اختياري) تسجيل في سجل التدقيق
    // يمكن إضافة log أو audit trail هنا
    
    return response;
}

// Method مساعد للتحقق من وجود المنتج
private void validateProductExists(Long productId, ProductType productType) {
    if (productType == ProductType.PHARMACY) {
        pharmacyProductRepo.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "PharmacyProduct not found: " + productId));
    } else if (productType == ProductType.MASTER) {
        masterProductRepo.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "MasterProduct not found: " + productId));
    } else {
        throw new ConflictException("Invalid productType: " + productType);
    }
}

// Method مساعد للتحقق من تاريخ الصلاحية
private void validateExpiryDate(LocalDate expiryDate) {
    if (expiryDate == null) {
        return; // Optional field
    }
    
    LocalDate today = LocalDate.now();
    if (expiryDate.isBefore(today)) {
        throw new ConflictException("Cannot add items with expired date: " + expiryDate);
    }
    
    // تحذير للأدوية التي تنتهي خلال 6 أشهر
    LocalDate sixMonthsFromNow = today.plusMonths(6);
    if (expiryDate.isBefore(sixMonthsFromNow)) {
        logger.warn("Item with expiry date {} is less than 6 months from now", expiryDate);
    }
}
```

### 3. إضافة Endpoint جديد في Controller / New Endpoint in Controller

#### **StockManagementController.addStockWithoutInvoice()**
```java
@PostMapping("/adjustment/add")
@Operation(
    summary = "Add stock items without purchase invoice",
    description = "Add medicines to inventory without creating a purchase invoice. " +
                  "Useful for inventory adjustments, physical counts, or error corrections."
)
@PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACIST')")
public ResponseEntity<StockItemDTOResponse> addStockWithoutInvoice(
        @Valid @RequestBody InventoryAdjustmentRequest request) {
    
    StockItemDTOResponse result = stockService.addStockWithoutInvoice(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
}
```

---

## 🔐 الأمان والصلاحيات / Security & Permissions

### الصلاحيات المطلوبة / Required Permissions:
- **PHARMACY_MANAGER**: يمكنه إضافة مخزون بدون فاتورة
- **PHARMACIST**: يمكنه إضافة مخزون بدون فاتورة
- **EMPLOYEE**: قد يحتاج صلاحيات خاصة (حسب متطلبات العمل)

### التحقق من الصلاحيات / Permission Checks:
1. التحقق من أن المستخدم هو `Employee`
2. التحقق من أن الموظف مرتبط بصيدلية
3. التحقق من أن الصيدلية هي نفسها صيدلية المستخدم الحالي

---

## ✅ التحقق من البيانات / Data Validation

### التحققات المطلوبة / Required Validations:

1. **المنتج / Product**:
   - يجب أن يكون `productId` موجود في قاعدة البيانات
   - يجب أن يكون `productType` صحيح (MASTER أو PHARMACY)
   - يجب التحقق من أن المنتج ينتمي للصيدلية (للـ PHARMACY products)

2. **الكمية / Quantity**:
   - يجب أن تكون أكبر من 0
   - `bonusQty` يجب أن تكون أكبر من أو تساوي 0

3. **السعر / Price**:
   - `actualPurchasePrice` يجب أن يكون أكبر من 0
   - يجب تحويل السعر إلى SYP إذا كان بالعملة المحلية

4. **تاريخ الصلاحية / Expiry Date**:
   - إذا تم توفيره، يجب ألا يكون في الماضي
   - تحذير إذا كان أقل من 6 أشهر

5. **السبب / Reason**:
   - إلزامي لأغراض التدقيق (Audit)
   - يجب أن يكون نص واضح يشرح سبب الإضافة

---

## 📊 مثال على الاستخدام / Usage Example

### Request Body:
```json
POST /api/v1/stock/adjustment/add
{
    "productId": 123,
    "productType": "MASTER",
    "quantity": 50,
    "bonusQty": 5,
    "actualPurchasePrice": 25.50,
    "expiryDate": "2025-12-31",
    "batchNo": "BATCH-2024-001",
    "invoiceNumber": "INV-UNKNOWN-001",
    "minStockLevel": 20,
    "reason": "جرد فعلي - اكتشاف أدوية زائدة في المخزون",
    "notes": "تم اكتشاف هذه الكمية أثناء الجرد الشهري. لا يمكن تحديد فاتورة الشراء الأصلية."
}
```

### Response:
```json
{
    "id": 456,
    "productId": 123,
    "productName": "Paracetamol 500mg",
    "productType": "MASTER",
    "quantity": 55,
    "bonusQty": 5,
    "expiryDate": "2025-12-31",
    "batchNo": "BATCH-2024-001",
    "actualPurchasePrice": 25.50,
    "dateAdded": "2024-01-15",
    "addedBy": 789,
    "pharmacyId": 1,
    "purchaseInvoiceId": null,
    "purchaseInvoiceNumber": null
}
```

---

## 🔄 الفرق بين الطريقتين / Differences Between Methods

| الميزة / Feature | فاتورة الشراء / Purchase Invoice | تعديل المخزون / Inventory Adjustment |
|-----------------|--------------------------------|-------------------------------------|
| **PurchaseInvoice** | ✅ مطلوب | ❌ غير موجود (null) |
| **Supplier** | ✅ مطلوب | ❌ غير موجود |
| **PurchaseOrder** | ✅ مطلوب | ❌ غير موجود |
| **Payment Method** | ✅ مطلوب | ❌ غير موجود |
| **MoneyBox Integration** | ✅ يتم التسجيل | ❌ لا يتم التسجيل |
| **Reason/Audit Trail** | ❌ غير مطلوب | ✅ إلزامي |
| **Use Case** | شراء فعلي من مورد | تعديل/جرد/تصحيح |

---

## 📝 ملاحظات مهمة / Important Notes

### 1. **التدقيق (Audit Trail)**:
- يجب تسجيل كل عملية إضافة بدون فاتورة
- يجب حفظ السبب (reason) والملاحظات (notes)
- يجب حفظ المستخدم الذي أضاف العنصر والتاريخ

### 2. **التقارير (Reports)**:
- قد تحتاج التقارير للتمييز بين المخزون من فواتير الشراء والمخزون من التعديلات
- يمكن إضافة filter في التقارير: `WHERE purchase_invoice_id IS NULL`

### 3. **التكامل مع MoneyBox**:
- **لا يتم** تسجيل هذه الإضافات في MoneyBox لأنها ليست عمليات شراء فعلية
- إذا كان هناك تكلفة فعلية، يجب التعامل معها بشكل منفصل

### 4. **التكامل مع نظام الجرد**:
- يمكن ربط هذه الميزة بنظام الجرد الفعلي
- يمكن إنشاء batch من التعديلات دفعة واحدة

---

## 🚀 خطوات التنفيذ / Implementation Steps

### المرحلة 1: إنشاء DTO
1. إنشاء `InventoryAdjustmentRequest.java`
2. إضافة validation annotations
3. إضافة documentation

### المرحلة 2: تحديث Service
1. إضافة method `addStockWithoutInvoice()` في `StockService`
2. إضافة helper methods للتحقق
3. إضافة logging

### المرحلة 3: تحديث Controller
1. إضافة endpoint جديد في `StockManagementController`
2. إضافة security annotations
3. إضافة API documentation (Swagger)

### المرحلة 4: الاختبار
1. Unit tests للـ service methods
2. Integration tests للـ endpoint
3. Test cases للـ validation scenarios

### المرحلة 5: التوثيق
1. تحديث API documentation
2. إضافة examples في Swagger
3. تحديث user guide

---

## 🧪 سيناريوهات الاختبار / Test Scenarios

### 1. **إضافة ناجحة / Successful Addition**:
- ✅ إضافة منتج MASTER بجميع البيانات
- ✅ إضافة منتج PHARMACY بجميع البيانات
- ✅ إضافة بدون bonusQty
- ✅ إضافة بدون expiryDate (اختياري)

### 2. **التحقق من الأخطاء / Error Validation**:
- ❌ productId غير موجود
- ❌ productType غير صحيح
- ❌ quantity <= 0
- ❌ actualPurchasePrice <= 0
- ❌ expiryDate في الماضي
- ❌ reason فارغ

### 3. **التحقق من الصلاحيات / Permission Checks**:
- ❌ مستخدم غير موظف
- ❌ موظف غير مرتبط بصيدلية
- ❌ محاولة إضافة لمخزون صيدلية أخرى

---

## 📈 تحسينات مستقبلية محتملة / Future Enhancements

1. **Batch Adjustments**: إضافة عدة منتجات دفعة واحدة
2. **Adjustment Types**: أنواع مختلفة من التعديلات (زيادة، نقصان، تصحيح)
3. **Approval Workflow**: نظام موافقة للتعديلات الكبيرة
4. **Integration with Inventory Count**: ربط مباشر بنظام الجرد الفعلي
5. **Cost Tracking**: تتبع التكلفة الفعلية للتعديلات
6. **Reports**: تقارير خاصة بالتعديلات بدون فواتير

---

## 🔗 الملفات المرتبطة / Related Files

### الملفات التي تحتاج تعديل / Files to Modify:
- `src/main/java/com/Teryaq/product/dto/InventoryAdjustmentRequest.java` (جديد)
- `src/main/java/com/Teryaq/product/service/StockService.java` (إضافة method)
- `src/main/java/com/Teryaq/product/controller/StockManagementController.java` (إضافة endpoint)

### الملفات المرتبطة / Related Files:
- `src/main/java/com/Teryaq/product/entity/StockItem.java`
- `src/main/java/com/Teryaq/product/repo/StockItemRepo.java`
- `src/main/java/com/Teryaq/product/mapper/StockItemMapper.java`
- `src/main/java/com/Teryaq/purchase/service/PurchaseInvoiceService.java`

---

## ✅ الخلاصة / Summary

هذه الميزة تسمح بإضافة أدوية للمخزون بدون فاتورة شراء، مما يوفر مرونة أكبر في إدارة المخزون خاصة أثناء عمليات الجرد. التصميم المقترح يحافظ على سلامة البيانات ويوفر audit trail كامل لكل عملية.

This feature allows adding medicines to inventory without a purchase invoice, providing greater flexibility in inventory management, especially during inventory counts. The proposed design maintains data integrity and provides a complete audit trail for each operation.

---

**تاريخ الإنشاء / Created**: 2024-01-15  
**الإصدار / Version**: 1.0

