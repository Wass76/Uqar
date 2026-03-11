# طلبات اختبار البيع بالأجزاء (Partial Selling)

استبدل القيم التالية قبل الإرسال:
- `{{baseUrl}}` → عنوان الـ API (مثلاً `http://localhost:8080`)
- `{{token}}` → JWT token للمستخدم
- `{{stockItemId}}` → معرف صنف مخزون لمنتج له `numberOfPartsPerBox > 1` (مثلاً 10)
- `{{customerId}}` → معرف العميل (مثلاً 1)

---

## 1. بيع جزئي – عدد أجزاء فقط (السعر من المعادلة 20%)

عندما **لم** يُحدد سعر الجزء في فاتورة الشراء، يُحسب السعر: (سعر العلبة ÷ عدد الأجزاء) × 1.20.

**POST** `{{baseUrl}}/api/v1/sales`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Body:**
```json
{
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "currency": "SYP",
  "invoiceDiscountType": "FIXED_AMOUNT",
  "invoiceDiscountValue": 0,
  "paidAmount": null,
  "items": [
    {
      "stockItemId": 1,
      "quantity": 1,
      "partsToSell": 3
    }
  ]
}
```

- `quantity: 1` مطلوب للتحقق (في البيع الجزئي النظام يعتمد على `partsToSell`).
- `partsToSell: 3` = بيع 3 أجزاء (مثلاً 3 ظروف من علبة فيها 10).
- لا `unitPrice`: سعر العلبة يُؤخذ من سعر البيع في المنتج، ثم يُطبَّق الـ 20%.

---

## 2. بيع جزئي مع تحديد سعر العلبة (لحساب الـ 20%)

إذا أردت أن يكون الأساس سعر علبة معين وليس سعر المنتج الافتراضي:

```json
{
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "currency": "SYP",
  "invoiceDiscountType": "FIXED_AMOUNT",
  "invoiceDiscountValue": 0,
  "paidAmount": null,
  "items": [
    {
      "stockItemId": 1,
      "quantity": 1,
      "unitPrice": 6000,
      "partsToSell": 2
    }
  ]
}
```

- سعر الجزء = (6000 ÷ عدد الأجزاء في العلبة) × 1.20، ثم × 2 أجزاء.

---

## 3. بيع جزئي عندما سعر الجزء محدد في فاتورة الشراء

نفس الريكوست؛ الفرق أن صنف المخزون (`stockItemId`) يكون قد دخل له `partPrice` في فاتورة الشراء. النظام يستخدمه تلقائياً:

```json
{
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "currency": "SYP",
  "invoiceDiscountType": "FIXED_AMOUNT",
  "invoiceDiscountValue": 0,
  "paidAmount": null,
  "items": [
    {
      "stockItemId": 2,
      "quantity": 1,
      "partsToSell": 5
    }
  ]
}
```

- السعر = `partPrice` (من المخزون/الشراء) × 5.

---

## 4. بيع علبة كاملة (بدون أجزاء)

للمقارنة – بيع علبة كاملة بدون `partsToSell`:

```json
{
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "currency": "SYP",
  "invoiceDiscountType": "FIXED_AMOUNT",
  "invoiceDiscountValue": 0,
  "paidAmount": null,
  "items": [
    {
      "stockItemId": 1,
      "quantity": 2,
      "unitPrice": 6000
    }
  ]
}
```

- يخصم 2 علبة، السعر = 6000 × 2 (أو سعر البيع من المنتج إن لم تُرسل `unitPrice`).

---

## 5. فاتورة فيها بيع جزئي + بيع علبة كاملة

```json
{
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "currency": "SYP",
  "invoiceDiscountType": "PERCENTAGE",
  "invoiceDiscountValue": 5,
  "paidAmount": null,
  "items": [
    {
      "stockItemId": 1,
      "quantity": 1,
      "partsToSell": 4
    },
    {
      "stockItemId": 3,
      "quantity": 2,
      "unitPrice": 8000
    }
  ]
}
```

---

## cURL أمثلة

### بيع 3 أجزاء (بدون سعر جزئي من الشراء)
```bash
curl -X POST "http://localhost:8080/api/v1/sales" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "customerId": 1,
    "paymentType": "CASH",
    "paymentMethod": "CASH",
    "currency": "SYP",
    "invoiceDiscountType": "FIXED_AMOUNT",
    "invoiceDiscountValue": 0,
    "items": [
      {
        "stockItemId": 1,
        "quantity": 1,
        "partsToSell": 3
      }
    ]
  }'
```

### بيع 2 أجزاء مع سعر علبة 6000
```bash
curl -X POST "http://localhost:8080/api/v1/sales" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "customerId": 1,
    "paymentType": "CASH",
    "paymentMethod": "CASH",
    "currency": "SYP",
    "invoiceDiscountType": "FIXED_AMOUNT",
    "invoiceDiscountValue": 0,
    "items": [
      {
        "stockItemId": 1,
        "quantity": 1,
        "unitPrice": 6000,
        "partsToSell": 2
      }
    ]
  }'
```

---

## ملاحظات للاختبار

1. **صنف مخزون للبيع الجزئي:** استخدم `stockItemId` لمنتج له `numberOfPartsPerBox > 1` (مثلاً 3 أو 10).
2. **جلب stockItemId:** من API المخزون (مثلاً قائمة أصناف المخزون) واختر صنفاً لمنتج قابِل للتجزئة.
3. **سعر الجزء من الشراء:** لاختبار السيناريو 3، أنشئ فاتورة شراء لنفس المنتج وحدد فيها `partPrice` للصنف، ثم استخدم الـ `stockItemId` الناتج في ريكوست البيع.
4. **التحقق:** في الرد تأكد من `unitPrice` و `subTotal` و `partsSold` للأصناف المباعة بالأجزاء.

---

# طلبات الشراء مع تحديد سعر الجزء

في النظام الحالي **سعر الجزء (partPrice)** يُحدد في **فاتورة الشراء** عند استلام البضاعة فقط (داخل كل صنف من أصناف الفاتورة). طلبية الشراء لا تحتوي حقل سعر الجزء.

---

## فاتورة شراء مع سعر الجزء (Part Price)

عند استلام البضاعة، يمكنك تحديد سعر الشراء، سعر البيع، وسعر الجزء الواحد في نفس الصنف. سعر الجزء يُحفظ على صنف المخزون ويُستخدم لاحقاً في البيع بالأجزاء.

**POST** `{{baseUrl}}/api/v1/purchase-invoices`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Body (فاتورة شراء – صنف واحد مع partPrice):**
```json
{
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "SYP",
  "total": 15000,
  "invoiceNumber": "INV-2025-001",
  "paymentMethod": "CASH",
  "items": [
    {
      "productId": 5,
      "receivedQty": 10,
      "bonusQty": 0,
      "invoicePrice": 1200,
      "batchNo": "BATCH-001",
      "expiryDate": "2026-12-31",
      "productType": "PHARMACY",
      "sellingPrice": 6000,
      "partPrice": 2400,
      "minStockLevel": 2
    }
  ]
}
```

**شرح الحقول المهمة للبيع بالأجزاء:**
- `sellingPrice`: سعر بيع العلبة كاملة (مثلاً 6000).
- `partPrice`: سعر بيع الجزء الواحد (مثلاً 2400 لظرف واحد). إذا حددته، البيع بالأجزاء لاحقاً سيستخدم هذا السعر مباشرة بدل معادلة الـ 20%.

**Body (فاتورة شراء – أكثر من صنف، أحدها مع partPrice):**
```json
{
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "SYP",
  "total": 45000,
  "invoiceNumber": "INV-2025-002",
  "paymentMethod": "CASH",
  "items": [
    {
      "productId": 5,
      "receivedQty": 10,
      "bonusQty": 1,
      "invoicePrice": 1200,
      "batchNo": "BATCH-001",
      "expiryDate": "2026-12-31",
      "productType": "PHARMACY",
      "sellingPrice": 6000,
      "partPrice": 2400,
      "minStockLevel": 2
    },
    {
      "productId": 8,
      "receivedQty": 5,
      "bonusQty": 0,
      "invoicePrice": 500,
      "batchNo": "BATCH-002",
      "expiryDate": "2026-06-30",
      "productType": "PHARMACY",
      "sellingPrice": 3000,
      "minStockLevel": 1
    }
  ]
}
```

(الصنف الثاني بدون `partPrice` → عند البيع بالأجزاء له سيُستخدم الحساب التلقائي 20%.)

---

## طلبية شراء (Purchase Order)

طلبية الشراء لا تحتوي حقل `partPrice`؛ سعر الجزء يُحدد عند إنشاء **فاتورة الشراء** (استلام البضاعة). هذا مثال ريكوست طلبية شراء عادية:

**POST** `{{baseUrl}}/api/v1/purchase-orders`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

**Body:**
```json
{
  "supplierId": 1,
  "currency": "SYP",
  "items": [
    {
      "productId": 5,
      "quantity": 10,
      "price": 1200,
      "productType": "PHARMACY"
    },
    {
      "productId": 8,
      "quantity": 5,
      "price": 500,
      "productType": "PHARMACY"
    }
  ]
}
```

بعد استلام البضاعة وإنشاء **فاتورة الشراء** من نفس الطلبية، أضف في أصناف الفاتورة قيم `sellingPrice` و `partPrice` (إن رغبت) كما في الأمثلة أعلاه.

---

## cURL – فاتورة شراء مع partPrice

```bash
curl -X POST "http://localhost:8080/api/v1/purchase-invoices" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "purchaseOrderId": 1,
    "supplierId": 1,
    "currency": "SYP",
    "total": 15000,
    "invoiceNumber": "INV-2025-001",
    "paymentMethod": "CASH",
    "items": [
      {
        "productId": 5,
        "receivedQty": 10,
        "bonusQty": 0,
        "invoicePrice": 1200,
        "batchNo": "BATCH-001",
        "expiryDate": "2026-12-31",
        "productType": "PHARMACY",
        "sellingPrice": 6000,
        "partPrice": 2400,
        "minStockLevel": 2
      }
    ]
  }'
```
