# التكامل مع الصندوق (MoneyBox) عند إضافة المخزون بدون فاتورة
## MoneyBox Integration for Inventory Adjustment Feature

---

## 📋 نظرة عامة / Overview

هذا الملف يشرح بالتفصيل كيفية تسجيل الدفعات في الصندوق (MoneyBox) عند إضافة أدوية للمخزون بدون فاتورة شراء. هذا التكامل ضروري للحفاظ على دقة الحسابات المالية عند وجود تكلفة فعلية تم دفعها.

This document explains in detail how to record payments in MoneyBox when adding medicines to inventory without a purchase invoice. This integration is essential to maintain financial accuracy when there is an actual cost that was paid.

---

## 🎯 الهدف / Purpose

عند إضافة مخزون بدون فاتورة شراء، قد يكون هناك حالات تم فيها دفع مبلغ فعلي مقابل هذه الأدوية. في هذه الحالات، يجب تسجيل هذه الدفعة في الصندوق (MoneyBox) للحفاظ على:
- دقة الحسابات المالية
- تتبع التدفقات النقدية
- سجل كامل للعمليات المالية

When adding inventory without a purchase invoice, there may be cases where an actual payment was made for these medicines. In such cases, this payment must be recorded in MoneyBox to maintain:
- Financial accuracy
- Cash flow tracking
- Complete financial operation records

---

## 🔧 التعديلات المطلوبة / Required Modifications

### 1. تحديث DTO - إضافة حقول الدفع / Update DTO - Add Payment Fields

#### **InventoryAdjustmentRequest.java**

```java
package com.Teryaq.product.dto;

import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.user.Enum.Currency;
import com.Teryaq.product.Enum.PaymentMethod;
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
    
    // الحقول الأساسية / Basic Fields
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Product type is required")
    private ProductType productType;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;
    
    @Min(value = 0, message = "Bonus quantity cannot be negative")
    private Integer bonusQty;
    
    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase price must be greater than 0")
    private Double actualPurchasePrice;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    
    private String batchNo;
    private String invoiceNumber;
    
    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minStockLevel;
    
    @NotBlank(message = "Reason is required for audit purposes")
    private String reason;
    
    private String notes;
    
    // ⚠️ حقول الدفع في MoneyBox / MoneyBox Payment Fields
    /**
     * هل هناك تكلفة فعلية تم دفعها مقابل هذه الأدوية؟
     * true = يجب تسجيل الدفعة في MoneyBox
     * false = لا يوجد دفع، لا تسجيل في MoneyBox
     */
    private Boolean hasActualCost;
    
    /**
     * المبلغ المدفوع فعلياً (مطلوب إذا كان hasActualCost = true)
     * يجب أن يكون أكبر من 0
     */
    @DecimalMin(value = "0.0", inclusive = false, 
                message = "Paid amount must be greater than 0 when hasActualCost is true")
    private Double paidAmount;
    
    /**
     * عملة الدفع (SYP, USD, EUR)
     * مطلوب إذا كان hasActualCost = true
     * القيمة الافتراضية: SYP
     */
    private Currency paymentCurrency;
    
    /**
     * طريقة الدفع (CASH, BANK_ACCOUNT)
     * مطلوب إذا كان hasActualCost = true
     */
    private PaymentMethod paymentMethod;
    
    /**
     * معرف المورد (اختياري)
     * يستخدم لتوثيق المورد الذي تم الدفع له
     */
    private Long supplierId;
}
```

### 2. تحديث StockService - إضافة Dependency Injection / Update StockService - Add Dependencies

#### **StockService.java - Constructor**

```java
package com.Teryaq.product.service;

import com.Teryaq.moneybox.service.PurchaseIntegrationService;
import com.Teryaq.user.Enum.Currency;
import com.Teryaq.user.repository.SupplierRepo;
import com.Teryaq.product.repo.StockItemRepo;
import com.Teryaq.product.mapper.StockItemMapper;
import com.Teryaq.user.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@Transactional
public class StockService extends BaseSecurityService {

    private final StockItemRepo stockItemRepo;
    private final StockItemMapper stockItemMapper;
    private final PurchaseIntegrationService purchaseIntegrationService;
    private final SupplierRepo supplierRepo;

    public StockService(
            StockItemRepo stockItemRepo,
            @Lazy StockItemMapper stockItemMapper,
            UserRepository userRepository,
            PurchaseIntegrationService purchaseIntegrationService,
            SupplierRepo supplierRepo) {
        super(userRepository);
        this.stockItemRepo = stockItemRepo;
        this.stockItemMapper = stockItemMapper;
        this.purchaseIntegrationService = purchaseIntegrationService;
        this.supplierRepo = supplierRepo;
    }
    
    // ... باقي الـ methods
}
```

### 3. إضافة Method للتحقق من بيانات الدفع / Add Payment Data Validation Method

```java
/**
 * التحقق من صحة بيانات الدفع في MoneyBox
 * Validates payment data for MoneyBox integration
 */
private void validatePaymentData(InventoryAdjustmentRequest request) {
    // التحقق من وجود المبلغ المدفوع
    if (request.getPaidAmount() == null || request.getPaidAmount() <= 0) {
        throw new ConflictException(
            "Paid amount is required and must be greater than 0 when hasActualCost is true");
    }
    
    // التحقق من عملة الدفع
    if (request.getPaymentCurrency() == null) {
        throw new ConflictException(
            "Payment currency is required when hasActualCost is true");
    }
    
    // التحقق من طريقة الدفع
    if (request.getPaymentMethod() == null) {
        throw new ConflictException(
            "Payment method is required when hasActualCost is true");
    }
    
    // التحقق من وجود المورد (إذا تم توفيره)
    if (request.getSupplierId() != null) {
        supplierRepo.findById(request.getSupplierId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Supplier not found: " + request.getSupplierId()));
    }
    
    // تحذير إذا كان المبلغ المدفوع أكبر بكثير من التكلفة المحسوبة
    int totalQuantity = request.getQuantity() + 
                       (request.getBonusQty() != null ? request.getBonusQty() : 0);
    double calculatedCost = totalQuantity * request.getActualPurchasePrice();
    
    if (request.getPaidAmount() > calculatedCost * 1.5) { // 50% زيادة كحد أقصى
        logger.warn(
            "Paid amount ({}) is significantly higher than calculated cost ({}). " +
            "Please verify the amount.",
            request.getPaidAmount(), 
            calculatedCost
        );
    }
    
    // تحذير إذا كان المبلغ المدفوع أقل بكثير من التكلفة المحسوبة
    if (request.getPaidAmount() < calculatedCost * 0.5) { // 50% أقل كحد أدنى
        logger.warn(
            "Paid amount ({}) is significantly lower than calculated cost ({}). " +
            "Please verify the amount.",
            request.getPaidAmount(), 
            calculatedCost
        );
    }
}
```

### 4. تحديث Method إضافة المخزون - إضافة تسجيل MoneyBox / Update Add Stock Method - Add MoneyBox Recording

```java
@Transactional
public StockItemDTOResponse addStockWithoutInvoice(InventoryAdjustmentRequest request) {
    // ... التحقق من الصلاحيات والبيانات الأساسية ...
    
    // التحقق من بيانات الدفع (إذا كان هناك تكلفة فعلية)
    if (request.getHasActualCost() != null && request.getHasActualCost()) {
        validatePaymentData(request);
    }
    
    // ... إنشاء وحفظ StockItem ...
    
    // ⚠️ تسجيل الدفعة في MoneyBox (إذا كان هناك تكلفة فعلية)
    if (request.getHasActualCost() != null && request.getHasActualCost() && 
        request.getPaidAmount() != null && request.getPaidAmount() > 0) {
        
        recordExpenseInMoneyBox(request, pharmacy, totalQuantity);
    }
    
    // ... إرجاع الاستجابة ...
}

/**
 * تسجيل الدفعة كـ Expense في MoneyBox
 * Records the payment as an Expense in MoneyBox
 */
private void recordExpenseInMoneyBox(
        InventoryAdjustmentRequest request, 
        Pharmacy pharmacy, 
        int totalQuantity) {
    
    try {
        // حساب إجمالي التكلفة المحسوبة
        double calculatedCost = totalQuantity * request.getActualPurchasePrice();
        
        // استخدام المبلغ المدفوع الفعلي أو التكلفة المحسوبة (أيهما أقل)
        // هذا يضمن عدم تسجيل مبلغ أكبر من التكلفة الفعلية
        double amountToRecord = Math.min(request.getPaidAmount(), calculatedCost);
        
        // استخدام عملة الدفع المحددة أو SYP كافتراضي
        Currency currency = request.getPaymentCurrency() != null ? 
            request.getPaymentCurrency() : Currency.SYP;
        
        // إنشاء وصف تفصيلي للعملية
        String description = buildExpenseDescription(request);
        
        // تسجيل كـ EXPENSE في MoneyBox
        purchaseIntegrationService.recordExpense(
            pharmacy.getId(),
            description,
            BigDecimal.valueOf(amountToRecord),
            currency
        );
        
        logger.info(
            "Successfully recorded expense in MoneyBox for inventory adjustment. " +
            "Amount: {} {}, Reason: {}, StockItem ID: {}",
            amountToRecord, 
            currency, 
            request.getReason(),
            savedStockItem.getId()
        );
        
    } catch (Exception e) {
        logger.error(
            "Failed to record expense in MoneyBox for inventory adjustment. " +
            "Reason: {}, Error: {}",
            request.getReason(),
            e.getMessage(),
            e
        );
        
        // ⚠️ مهم: لا نوقف العملية إذا فشل تسجيل في MoneyBox
        // لأن إضافة المخزون نجحت، فقط التسجيل المالي فشل
        // يمكن إضافة warning في الـ response لإعلام المستخدم
        
        // يمكن إضافة notification للمستخدم هنا
        // notificationService.notifyAdmin("Failed to record expense in MoneyBox", ...);
    }
}

/**
 * بناء وصف تفصيلي للعملية المالية
 * Builds a detailed description for the financial operation
 */
private String buildExpenseDescription(InventoryAdjustmentRequest request) {
    StringBuilder description = new StringBuilder();
    
    // إضافة السبب الأساسي
    description.append("تعديل مخزون: ").append(request.getReason());
    
    // إضافة الملاحظات (إن وجدت)
    if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
        description.append(" - ").append(request.getNotes());
    }
    
    // إضافة معلومات المورد (إن وجد)
    if (request.getSupplierId() != null) {
        try {
            Supplier supplier = supplierRepo.findById(request.getSupplierId())
                .orElse(null);
            if (supplier != null) {
                description.append(" (المورد: ").append(supplier.getName()).append(")");
            }
        } catch (Exception e) {
            logger.warn("Could not fetch supplier name for ID: {}", request.getSupplierId());
        }
    }
    
    // إضافة طريقة الدفع
    if (request.getPaymentMethod() != null) {
        description.append(" - طريقة الدفع: ");
        switch (request.getPaymentMethod()) {
            case CASH:
                description.append("نقدي");
                break;
            case BANK_ACCOUNT:
                description.append("حساب بنكي");
                break;
            default:
                description.append(request.getPaymentMethod().name());
        }
    }
    
    return description.toString();
}
```

---

## 📊 أمثلة على الاستخدام / Usage Examples

### مثال 1: إضافة مع تسجيل في MoneyBox (شراء نقدي) / Example 1: With MoneyBox Recording

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
    "invoiceNumber": null,
    "minStockLevel": 20,
    "reason": "شراء نقدي بدون فاتورة",
    "notes": "تم الشراء نقداً من المورد بسبب عدم توفر فاتورة في الوقت الحالي",
    "hasActualCost": true,
    "paidAmount": 1275.00,
    "paymentCurrency": "SYP",
    "paymentMethod": "CASH",
    "supplierId": 5
}
```

**ما يحدث:**
1. يتم إنشاء `StockItem` جديد بدون ربط بفاتورة شراء
2. يتم تسجيل مبلغ 1275.00 SYP كـ `EXPENSE` في MoneyBox
3. يتم خصم المبلغ من رصيد الصندوق
4. يتم إنشاء سجل في `MoneyBoxTransaction` مع:
   - TransactionType: `EXPENSE`
   - Amount: 1275.00 SYP
   - Description: "تعديل مخزون: شراء نقدي بدون فاتورة - تم الشراء نقداً... - طريقة الدفع: نقدي (المورد: ...)"

### مثال 2: إضافة بدون تسجيل في MoneyBox (جرد فعلي) / Example 2: Without MoneyBox Recording

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
    "notes": "تم اكتشاف هذه الكمية أثناء الجرد الشهري. لا يمكن تحديد فاتورة الشراء الأصلية.",
    "hasActualCost": false
}
```

**ما يحدث:**
1. يتم إنشاء `StockItem` جديد بدون ربط بفاتورة شراء
2. **لا يتم** تسجيل أي دفعة في MoneyBox
3. لا يوجد تأثير على رصيد الصندوق

### مثال 3: إضافة مع تسجيل في MoneyBox (دفع بالدولار) / Example 3: With MoneyBox Recording (USD Payment)

```json
POST /api/v1/stock/adjustment/add
{
    "productId": 456,
    "productType": "PHARMACY",
    "quantity": 30,
    "actualPurchasePrice": 15.75,
    "expiryDate": "2026-06-30",
    "reason": "شراء عاجل بدون فاتورة",
    "hasActualCost": true,
    "paidAmount": 500.00,
    "paymentCurrency": "USD",
    "paymentMethod": "CASH",
    "supplierId": 8
}
```

**ما يحدث:**
1. يتم إنشاء `StockItem` جديد
2. يتم تحويل 500.00 USD إلى SYP باستخدام سعر الصرف الحالي
3. يتم تسجيل المبلغ المحول كـ `EXPENSE` في MoneyBox
4. يتم خصم المبلغ من رصيد الصندوق

---

## 🔍 التحقق من البيانات / Data Validation

### التحققات المطلوبة عند `hasActualCost = true`:

1. **paidAmount**:
   - ✅ يجب أن يكون موجود
   - ✅ يجب أن يكون أكبر من 0
   - ⚠️ تحذير إذا كان أكبر من التكلفة المحسوبة بـ 50% أو أكثر
   - ⚠️ تحذير إذا كان أقل من التكلفة المحسوبة بـ 50% أو أكثر

2. **paymentCurrency**:
   - ✅ يجب أن يكون موجود
   - ✅ يجب أن يكون قيمة صحيحة (SYP, USD, EUR)

3. **paymentMethod**:
   - ✅ يجب أن يكون موجود
   - ✅ يجب أن يكون قيمة صحيحة (CASH, BANK_ACCOUNT)

4. **supplierId** (اختياري):
   - ✅ إذا تم توفيره، يجب أن يكون موجود في قاعدة البيانات

### مثال على Validation Code:

```java
@PostMapping("/adjustment/add")
public ResponseEntity<StockItemDTOResponse> addStockWithoutInvoice(
        @Valid @RequestBody InventoryAdjustmentRequest request) {
    
    // التحقق الإضافي إذا كان hasActualCost = true
    if (Boolean.TRUE.equals(request.getHasActualCost())) {
        if (request.getPaidAmount() == null || request.getPaidAmount() <= 0) {
            throw new ValidationException(
                "Paid amount is required when hasActualCost is true");
        }
        if (request.getPaymentCurrency() == null) {
            throw new ValidationException(
                "Payment currency is required when hasActualCost is true");
        }
        if (request.getPaymentMethod() == null) {
            throw new ValidationException(
                "Payment method is required when hasActualCost is true");
        }
    }
    
    StockItemDTOResponse result = stockService.addStockWithoutInvoice(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
}
```

---

## 🛡️ معالجة الأخطاء / Error Handling

### السيناريوهات المحتملة:

#### 1. فشل تسجيل في MoneyBox / MoneyBox Recording Failure

```java
try {
    purchaseIntegrationService.recordExpense(...);
} catch (Exception e) {
    // ⚠️ لا نوقف العملية
    // لأن إضافة المخزون نجحت بالفعل
    
    logger.error("Failed to record expense in MoneyBox", e);
    
    // يمكن إضافة warning في الـ response
    // أو إرسال notification للمسؤول
    
    // Option 1: إضافة warning في response
    response.setWarning("تمت إضافة المخزون بنجاح، لكن فشل تسجيل الدفعة في الصندوق. " +
                       "يرجى التحقق يدوياً.");
    
    // Option 2: إرسال notification
    notificationService.notifyAdmin(
        "فشل تسجيل دفعة في الصندوق",
        "Inventory adjustment succeeded but MoneyBox recording failed. " +
        "StockItem ID: " + savedStockItem.getId()
    );
}
```

#### 2. MoneyBox غير موجود / MoneyBox Not Found

```java
// في PurchaseIntegrationService.recordExpense()
MoneyBox moneyBox = moneyBoxRepository.findByPharmacyId(pharmacyId)
    .orElseThrow(() -> new ConflictException(
        "Money box not found for pharmacy: " + pharmacyId));
```

#### 3. سعر الصرف غير متوفر / Exchange Rate Not Available

```java
// في EnhancedMoneyBoxAuditService
// يتم التعامل مع تحويل العملة تلقائياً
// إذا فشل التحويل، يتم استخدام سعر افتراضي أو رفض العملية
```

---

## 📈 التأثير على التقارير المالية / Impact on Financial Reports

### 1. تقارير الصندوق / MoneyBox Reports:

- ستظهر هذه الدفعات كـ `EXPENSE` في تقارير الصندوق
- يمكن تصفيتها حسب النوع: `EXPENSE` مع `expenseType: "INVENTORY_ADJUSTMENT"`
- يمكن ربطها بـ StockItem من خلال الـ description أو metadata

### 2. تقارير المخزون / Inventory Reports:

- يمكن التمييز بين المخزون من فواتير الشراء والمخزون من التعديلات
- Query example:
```sql
SELECT * FROM stock_item 
WHERE purchase_invoice_id IS NULL 
  AND added_by = ?
  AND date_added BETWEEN ? AND ?
```

### 3. تقارير التكلفة / Cost Reports:

- التكلفة الفعلية للمخزون = تكلفة من فواتير الشراء + تكلفة من التعديلات
- يمكن حساب إجمالي التكلفة:
```sql
SELECT 
    SUM(quantity * actual_purchase_price) as total_cost
FROM stock_item
WHERE pharmacy_id = ?
```

---

## ✅ Checklist للتنفيذ / Implementation Checklist

- [ ] إضافة حقول الدفع في `InventoryAdjustmentRequest`
- [ ] إضافة validation للبيانات المالية
- [ ] إضافة `PurchaseIntegrationService` في `StockService` constructor
- [ ] إضافة `SupplierRepo` في `StockService` constructor
- [ ] إنشاء method `validatePaymentData()`
- [ ] إنشاء method `recordExpenseInMoneyBox()`
- [ ] إنشاء method `buildExpenseDescription()`
- [ ] تحديث `addStockWithoutInvoice()` لاستدعاء التسجيل في MoneyBox
- [ ] إضافة error handling مناسب
- [ ] إضافة logging مناسب
- [ ] كتابة unit tests
- [ ] كتابة integration tests
- [ ] تحديث API documentation
- [ ] تحديث user guide

---

## 🔗 الملفات المرتبطة / Related Files

### الملفات التي تحتاج تعديل:
- `src/main/java/com/Teryaq/product/dto/InventoryAdjustmentRequest.java`
- `src/main/java/com/Teryaq/product/service/StockService.java`
- `src/main/java/com/Teryaq/product/controller/StockManagementController.java`

### الملفات المرتبطة:
- `src/main/java/com/Teryaq/moneybox/service/PurchaseIntegrationService.java`
- `src/main/java/com/Teryaq/moneybox/service/EnhancedMoneyBoxAuditService.java`
- `src/main/java/com/Teryaq/user/repository/SupplierRepo.java`
- `src/main/java/com/Teryaq/moneybox/entity/MoneyBox.java`
- `src/main/java/com/Teryaq/moneybox/entity/MoneyBoxTransaction.java`

---

## 📝 ملاحظات مهمة / Important Notes

1. **التحويل التلقائي للعملة**: يتم تحويل جميع المبالغ إلى SYP تلقائياً في MoneyBox
2. **Transaction Management**: يجب أن تكون العملية ضمن `@Transactional` لضمان الاتساق
3. **Error Handling**: لا يجب إيقاف عملية إضافة المخزون إذا فشل تسجيل في MoneyBox
4. **Audit Trail**: يتم حفظ سجل كامل في `MoneyBoxTransaction` لكل عملية
5. **User Tracking**: يمكن إضافة معلومات المستخدم في metadata للتدقيق

---

**تاريخ الإنشاء / Created**: 2024-01-15  
**الإصدار / Version**: 1.0  
**الملف المرتبط / Related File**: `INVENTORY_ADJUSTMENT_FEATURE.md`

