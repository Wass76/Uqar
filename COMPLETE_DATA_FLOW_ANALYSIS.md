# 🔄 Complete Data Flow Analysis - Uqar Pharmacy System

## 📋 **1. Purchase Order Creation Flow**

### **🎯 Scenario: Adding New Purchase Order**

#### **📥 Input (Request)**
```json
POST /api/v1/purchase-orders
{
  "supplierId": 1,
  "currency": "USD",
  "items": [
    {
      "productId": 1,
      "quantity": 100,
      "price": 5.50,
      "barcode": "1234567890123",
      "productType": "MASTER"
    },
    {
      "productId": 2,
      "quantity": 50,
      "price": 12.00,
      "barcode": "9876543210987",
      "productType": "PHARMACY"
    }
  ]
}
```

#### **🔄 Data Flow Logic**

**1. Controller Layer (`PurchaseOrderController.create()`)**
- ✅ Validates request using `@Valid`
- ✅ Calls `PurchaseOrderService.create(request, language)`
- ✅ Returns `ResponseEntity<PurchaseOrderDTOResponse>`

**2. Service Layer (`PurchaseOrderService.create()`)**
- ✅ **Security Check**: Gets current user pharmacy ID
- ✅ **Validation**: Validates supplier exists
- ✅ **Product Processing**: For each item:
  - Gets product details (PharmacyProduct or MasterProduct)
  - Calculates price based on product type:
    - **PharmacyProduct**: Uses `price` directly (stored in SYP)
    - **MasterProduct**: Converts `price` from SYP to order currency
- ✅ **Total Calculation**: 
  - Converts all item prices to SYP for consistent calculation
  - Sums quantities × prices in SYP
  - Converts final total back to order currency
- ✅ **Entity Creation**: Creates `PurchaseOrder` entity
- ✅ **Database Save**: Saves order and items
- ✅ **Response Mapping**: Maps to `PurchaseOrderDTOResponse`

#### **💾 Database Operations**

**Tables Updated:**
```sql
-- 1. Purchase Order
INSERT INTO purchase_order (
    supplier_id, pharmacy_id, currency, total, status, 
    created_at, created_by
) VALUES (
    1, 123, 'USD', 1150.00, 'PENDING', 
    '2024-12-01 10:30:00', 456
);

-- 2. Purchase Order Items
INSERT INTO purchase_order_item (
    purchase_order_id, product_id, quantity, unit_price, 
    product_type, total_price
) VALUES 
(789, 1, 100, 5.50, 'PHARMACY', 550.00),
(789, 2, 50, 12.00, 'MASTER', 600.00);
```

#### **📤 Output (Response)**
```json
{
  "id": 789,
  "supplierId": 1,
  "supplierName": "ABC Medical Supplies",
  "currency": "USD",
  "total": 1150.00,
  "status": "PENDING",
  "createdAt": "2024-12-01T10:30:00",
  "createdBy": 456,
  "items": [
    {
      "id": 1,
      "productName": "Paracetamol 500mg",
      "quantity": 100,
      "price": 5.50,
      "barcode": "1234567890123",
      "productId": 1,
      "productType": "MASTER",
      "refSellingPrice": 8.00,
      "minStockLevel": 50
    },
    {
      "id": 2,
      "productName": "Amoxicillin 250mg",
      "quantity": 50,
      "price": 12.00,
      "barcode": "9876543210987",
      "productId": 2,
      "productType": "PHARMACY",
      "refSellingPrice": 18.00,
      "minStockLevel": 25
    }
  ]
}
```

---

## 📊 **2. Purchase Report Generation Flow**

### **🎯 Scenario: Monthly Purchase Report**

#### **📥 Input (Request)**
```http
GET /api/v1/reports/purchase/monthly?startDate=2024-01-01&endDate=2024-01-31&currency=SYP&language=EN
```

#### **🔄 Data Flow Logic**

**1. Controller Layer (`ReportController.getMonthlyPurchaseReport()`)**
- ✅ Validates parameters (dates, currency, language)
- ✅ Calls `ReportService.getMonthlyPurchaseReport()`
- ✅ Returns `ResponseEntity<PurchaseReportResponse>`

**2. Service Layer (`ReportService.getMonthlyPurchaseReport()`)**
- ✅ **Data Aggregation**: 
  - Queries purchase invoices by date range
  - Groups by currency for accurate totals
  - Converts all amounts to requested currency
- ✅ **Daily Breakdown**: 
  - Groups data by day
  - Calculates daily totals
  - Handles currency conversion per day
- ✅ **Summary Calculation**:
  - Total purchases across all currencies
  - Average daily purchase
  - Currency distribution
- ✅ **Response Building**: Creates `PurchaseReportResponse`

#### **💾 Database Queries**

**Main Query:**
```sql
SELECT 
    DATE(pi.created_at) as purchase_date,
    pi.currency,
    COUNT(*) as invoice_count,
    SUM(pi.total) as daily_total,
    SUM(CASE 
        WHEN pi.currency = 'SYP' THEN pi.total
        WHEN pi.currency = 'USD' THEN pi.total * er.usd_to_syp_rate
        WHEN pi.currency = 'EUR' THEN pi.total * er.eur_to_syp_rate
    END) as total_in_syp
FROM purchase_invoice pi
LEFT JOIN exchange_rate er ON DATE(pi.created_at) = DATE(er.created_at)
WHERE pi.pharmacy_id = ? 
  AND pi.created_at BETWEEN ? AND ?
GROUP BY DATE(pi.created_at), pi.currency
ORDER BY purchase_date;
```

#### **📤 Output (Response)**
```json
{
  "success": true,
  "pharmacyId": 123,
  "generatedAt": "2024-12-01T10:30:00",
  "currency": "SYP",
  "language": "EN",
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "dailyData": [
    {
      "date": "2024-01-01",
      "totalInvoices": 3,
      "totalAmount": 3500.00,
      "totalPaid": 3500.00,
      "averageAmount": 1166.67
    }
    // ... more daily entries
  ],
  "summary": {
    "totalInvoices": 45,
    "totalAmount": 125000.00,
    "totalPaid": 125000.00,
    "averageAmount": 2777.78,
    "totalItems": 1250
  }
}
```

---

## 📋 **3. Purchase Invoice Creation Flow**

### **🎯 Scenario: Creating Purchase Invoice from Purchase Order**

#### **📥 Input (Request)**
```json
POST /api/v1/purchase-invoices
{
  "purchaseOrderId": 789,
  "supplierId": 1,
  "currency": "USD",
  "items": [
    {
      "productId": 1,
      "quantity": 100,
      "invoicePrice": 5.50,
      "actualPrice": 5.25,
      "refSellingPrice": 8.00,
      "productType": "MASTER"
    },
    {
      "productId": 2,
      "quantity": 50,
      "invoicePrice": 12.00,
      "actualPrice": 11.50,
      "refSellingPrice": 18.00,
      "productType": "PHARMACY"
    }
  ]
}
```

#### **🔄 Data Flow Logic**

**1. Controller Layer (`PurchaseInvoiceController.create()`)**
- ✅ **Authorization**: Checks `@PreAuthorize("hasRole('PHARMACY_MANAGER')")`
- ✅ **Validation**: Validates request using `@Valid`
- ✅ Calls `PurchaseInvoiceService.create(request, language)`

**2. Service Layer (`PurchaseInvoiceService.create()`)**
- ✅ **Entity Validation**: 
  - Validates purchase order exists and is in correct status
  - Validates supplier exists
  - Validates pharmacy products and master products
- ✅ **Item Processing**: For each item:
  - Gets product details (PharmacyProduct or MasterProduct)
  - Calculates prices with currency conversion:
    - **Invoice Price**: Converted to SYP for storage
    - **Actual Price**: Converted to SYP for storage
    - **Ref Selling Price**: Converted to SYP for storage
- ✅ **Total Calculation**: 
  - Converts all item prices to SYP for consistent calculation
  - Sums quantities × actual prices in SYP
  - Converts final total back to requested currency
- ✅ **Invoice Creation**: Creates `PurchaseInvoice` entity
- ✅ **Order Status Update**: Updates purchase order status to "COMPLETED"
- ✅ **Stock Item Records**: Creates audit trail records
- ✅ **MoneyBox Integration**: 
  - Records payment transaction if cash payment
  - Uses `EnhancedMoneyBoxAuditService` for comprehensive tracking
- ✅ **Response Mapping**: Maps to `PurchaseInvoiceDTOResponse`

#### **💾 Database Operations**

**Tables Updated:**
```sql
-- 1. Purchase Invoice
INSERT INTO purchase_invoice (
    purchase_order_id, supplier_id, pharmacy_id, currency, total, 
    status, created_at, created_by
) VALUES (
    789, 1, 123, 'USD', 1150.00, 'COMPLETED', 
    '2024-12-01 10:30:00', 456
);

-- 2. Purchase Invoice Items
INSERT INTO purchase_invoice_item (
    purchase_invoice_id, product_id, quantity, invoice_price, 
    actual_price, ref_selling_price, product_type, total_price
) VALUES 
(890, 1, 100, 5.50, 5.25, 8.00, 'MASTER', 525.00),
(890, 2, 50, 12.00, 11.50, 18.00, 'PHARMACY', 575.00);

-- 3. Purchase Order Status Update
UPDATE purchase_order 
SET status = 'COMPLETED', updated_at = '2024-12-01 10:30:00'
WHERE id = 789;

-- 4. Stock Item Records (for audit trail)
INSERT INTO stock_item (
    pharmacy_product_id, master_product_id, quantity, 
    purchase_invoice_id, created_at
) VALUES 
(1, NULL, 100, 890, '2024-12-01 10:30:00'),
(NULL, 2, 50, 890, '2024-12-01 10:30:00');

-- 5. MoneyBox Transaction (if cash payment)
INSERT INTO money_box_transaction (
    money_box_id, transaction_type, amount, original_currency, 
    original_amount, converted_currency, converted_amount, 
    exchange_rate, balance_before, balance_after, 
    description, reference_id, reference_type, 
    operation_status, created_by, created_at
) VALUES (
    123, 'PURCHASE_PAYMENT', 1150.00, 'USD', 1150.00, 'SYP', 115000.00, 
    100.00, 50000.0, 38500.0, 'Purchase payment for invoice ID: 890', 
    '890', 'PURCHASE_INVOICE', 'SUCCESS', 456, '2024-12-01 10:30:00'
);
```

#### **📤 Output (Response)**
```json
{
  "id": 890,
  "purchaseOrderId": 789,
  "supplierId": 1,
  "supplierName": "ABC Medical Supplies",
  "currency": "USD",
  "total": 1150.00,
  "status": "COMPLETED",
  "createdAt": "2024-12-01T10:30:00",
  "createdBy": 456,
  "items": [
    {
      "id": 1,
      "productName": "Paracetamol 500mg",
      "quantity": 100,
      "invoicePrice": 5.50,
      "actualPrice": 5.25,
      "refSellingPrice": 8.00,
      "productId": 1,
      "productType": "MASTER",
      "totalPrice": 525.00
    },
    {
      "id": 2,
      "productName": "Amoxicillin 250mg",
      "quantity": 50,
      "invoicePrice": 12.00,
      "actualPrice": 11.50,
      "refSellingPrice": 18.00,
      "productId": 2,
      "productType": "PHARMACY",
      "totalPrice": 575.00
    }
  ]
}
```

---

## 💰 **4. Manual Transaction Flow**

### **🎯 Scenario: Adding Manual Transaction to MoneyBox**

#### **📥 Input (Request)**
```json
POST /api/v1/moneybox/transaction
{
  "amount": 5000.00,
  "description": "Cash deposit from daily sales",
  "currency": "SYP"
}
```

#### **🔄 Data Flow Logic**

**1. Controller Layer (`MoneyBoxController.addTransaction()`)**
- ✅ **Authorization**: Checks `@PreAuthorize("hasRole('PHARMACY_MANAGER')")`
- ✅ **Validation**: Validates request parameters
- ✅ Calls `MoneyBoxService.addTransaction(amount, description, currency)`

**2. Service Layer (`MoneyBoxService.addTransaction()`)**
- ✅ **MoneyBox Validation**: Gets current user's MoneyBox
- ✅ **Currency Conversion**: 
  - Converts amount to SYP for consistent storage
  - Records exchange rate used for conversion
- ✅ **Balance Calculation**: 
  - Gets current balance from latest transaction
  - Calculates new balance after transaction
- ✅ **Transaction Creation**: 
  - Creates `MoneyBoxTransaction` entity
  - Uses `EnhancedMoneyBoxAuditService` for comprehensive tracking
- ✅ **Response Building**: Creates `MoneyBoxResponseDTO`

#### **💾 Database Operations**

**Tables Updated:**
```sql
-- 1. MoneyBox Transaction
INSERT INTO money_box_transaction (
    money_box_id, transaction_type, amount, original_currency, 
    original_amount, converted_currency, converted_amount, 
    exchange_rate, balance_before, balance_after, 
    description, reference_id, reference_type, 
    operation_status, ip_address, user_agent, session_id, 
    user_type, created_by, created_at
) VALUES (
    123, 'CASH_DEPOSIT', 5000.00, 'SYP', 5000.00, 'SYP', 5000.00, 
    1.00, 45000.0, 50000.0, 'Cash deposit from daily sales', 
    NULL, 'MANUAL_TRANSACTION', 'SUCCESS', '192.168.1.100', 
    'Mozilla/5.0...', 'abc123', 'PHARMACIST', 456, '2024-12-01 10:30:00'
);
```

#### **📤 Output (Response)**
```json
{
  "success": true,
  "message": "Transaction added successfully",
  "moneyBoxId": 123,
  "transactionId": 1001,
  "amount": 5000.00,
  "currency": "SYP",
  "balanceBefore": 45000.00,
  "balanceAfter": 50000.00,
  "transactionType": "CASH_DEPOSIT",
  "description": "Cash deposit from daily sales",
  "timestamp": "2024-12-01T10:30:00"
}
```

---

## 💰 **5. Sales Transaction Flow**

### **🎯 Scenario: Creating Sale Invoice**

#### **📥 Input (Request)**
```json
POST /api/v1/sales
{
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "currency": "SYP",
  "invoiceDiscountType": "PERCENTAGE",
  "invoiceDiscountValue": 10.0,
  "paidAmount": null,
  "items": [
    {
      "stockItemId": 1,
      "quantity": 2,
      "unitPrice": 800.0
    }
  ]
}
```

#### **🔄 Data Flow Logic**

**1. Controller Layer (`SaleController.createSale()`)**
- ✅ **Authorization**: Checks `@PreAuthorize("hasRole('PHARMACY_MANAGER')")`
- ✅ **Validation**: Validates request using `@Valid`
- ✅ Calls `SaleService.createSaleInvoice(request)`

**2. Service Layer (`SaleService.createSaleInvoice()`)**
- ✅ **Stock Validation**: Checks sufficient stock for each item using `stockItemId`
- ✅ **Price Calculation**: Calculates total amount with discount handling
- ✅ **Stock Deduction**: Reduces stock quantities
- ✅ **Invoice Creation**: Creates `SaleInvoice` entity
- ✅ **MoneyBox Integration**: 
  - Calls `SalesIntegrationService.recordSalePayment()`
  - Records transaction in MoneyBox with currency conversion
- ✅ **Enhanced Audit**: Records comprehensive audit trail
- ✅ **Response Mapping**: Maps to `SaleInvoiceDTOResponse`

#### **💾 Database Operations**

**Tables Updated:**
```sql
-- 1. Sale Invoice
INSERT INTO sale_invoice (
    customer_id, pharmacy_id, total_amount, currency, 
    payment_method, status, created_at, created_by
) VALUES (
    1, 123, 16.00, 'SYP', 'CASH', 'COMPLETED', 
    '2024-12-01 10:30:00', 456
);

-- 2. Sale Invoice Items
INSERT INTO sale_invoice_item (
    sale_invoice_id, stock_item_id, quantity, unit_price, sub_total
) VALUES (789, 1, 2, 800.0, 1600.0);

-- 3. Stock Update
UPDATE pharmacy_product 
SET quantity = quantity - 2 
WHERE id = 1 AND pharmacy_id = 123;

-- 4. MoneyBox Transaction (via SalesIntegrationService)
INSERT INTO money_box_transaction (
    money_box_id, transaction_type, amount, original_currency, 
    original_amount, converted_currency, converted_amount, 
    exchange_rate, balance_before, balance_after, 
    description, reference_id, reference_type, 
    operation_status, created_by, created_at
) VALUES (
    123, 'SALE_PAYMENT', 1600.0, 'SYP', 1600.0, 'SYP', 1600.0, 
    1.00, 50000.0, 51600.0, 'Sale payment for sale ID: 789', 
    '789', 'SALE', 'SUCCESS', 456, '2024-12-01 10:30:00'
);
```

#### **📤 Output (Response)**
```json
{
  "id": 789,
  "customerId": 1,
  "customerName": "Ahmed Ali",
  "invoiceDate": "01-12-2024, 10:30:00",
  "totalAmount": 1600.0,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "currency": "SYP",
  "discount": 160.0,
  "discountType": "PERCENTAGE",
  "paidAmount": 1440.0,
  "remainingAmount": 0.0,
  "paymentStatus": "PAID",
  "refundStatus": "NONE",
  "items": [
    {
      "id": 1,
      "stockItemId": 1,
      "productName": "Paracetamol 500mg",
      "quantity": 2,
      "refundedQuantity": 0,
      "availableForRefund": 2,
      "unitPrice": 800.0,
      "subTotal": 1600.0
    }
  ]
}
```

---

## 📈 **6. Profit Report Generation Flow**

### **🎯 Scenario: Monthly Profit Report**

#### **📥 Input (Request)**
```http
GET /api/v1/reports/profit/monthly?startDate=2024-01-01&endDate=2024-01-31&currency=SYP&language=EN
```

#### **🔄 Data Flow Logic**

**1. Controller Layer (`ReportController.getMonthlyProfitReport()`)**
- ✅ Validates parameters
- ✅ Calls `ReportService.getMonthlyProfitReport()`

**2. Service Layer (`ReportService.getMonthlyProfitReport()`)**
- ✅ **Sales Data**: 
  - Queries sale invoices by date range
  - Calculates total sales revenue
  - Handles currency conversion
- ✅ **Purchase Data**:
  - Queries purchase invoices by date range
  - Calculates total purchase costs
  - Handles currency conversion
- ✅ **Profit Calculation**:
  - Profit = Sales Revenue - Purchase Costs
  - Profit Margin = (Profit / Sales Revenue) × 100
- ✅ **Daily Breakdown**: 
  - Groups by day
  - Calculates daily profit/loss
- ✅ **Response Building**: Creates `ProfitReportResponse`

#### **💾 Database Queries**

**Sales Query:**
```sql
SELECT 
    DATE(si.created_at) as sale_date,
    si.currency,
    SUM(si.total_amount) as daily_sales,
    COUNT(*) as sale_count
FROM sale_invoice si
WHERE si.pharmacy_id = ? 
  AND si.created_at BETWEEN ? AND ?
  AND si.status = 'COMPLETED'
GROUP BY DATE(si.created_at), si.currency;
```

**Purchase Query:**
```sql
SELECT 
    DATE(pi.created_at) as purchase_date,
    pi.currency,
    SUM(pi.total) as daily_purchases,
    COUNT(*) as purchase_count
FROM purchase_invoice pi
WHERE pi.pharmacy_id = ? 
  AND pi.created_at BETWEEN ? AND ?
GROUP BY DATE(pi.created_at), pi.currency;
```

#### **📤 Output (Response)**
```json
{
  "success": true,
  "pharmacyId": 123,
  "generatedAt": "2024-12-01T10:30:00",
  "currency": "SYP",
  "language": "EN",
  "startDate": "2024-01-01",
  "endDate": "2024-01-31",
  "dailyData": [
    {
      "date": "2024-01-01",
      "totalInvoices": 5,
      "totalRevenue": 5000.00,
      "totalCost": 3000.00,
      "totalProfit": 2000.00,
      "profitMargin": 40.00,
      "averageRevenue": 1000.00
    }
    // ... more daily entries
  ],
  "summary": {
    "totalInvoices": 150,
    "totalRevenue": 150000.00,
    "totalCost": 100000.00,
    "totalProfit": 50000.00,
    "profitMargin": 33.33,
    "averageProfit": 333.33,
    "averageRevenue": 1000.00
  }
}
```

---

## 🔄 **7. Enhanced Audit Trail Integration**

### **🎯 All Financial Operations Include:**

#### **📊 Enhanced MoneyBox Audit**
Every financial operation now records:
```json
{
  "moneyBoxId": 123,
  "transactionType": "SALE_PAYMENT",
  "originalAmount": 1600.0,
  "originalCurrency": "SYP",
  "convertedAmount": 1600.0,
  "convertedCurrency": "SYP",
  "exchangeRate": 1.00,
  "balanceBefore": 50000.0,
  "balanceAfter": 51600.0,
  "description": "Sale payment for sale ID: 789",
  "referenceId": "789",
  "referenceType": "SALE",
  "operationStatus": "SUCCESS",
  "createdBy": 456,
  "userType": "PHARMACIST",
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "sessionId": "abc123",
  "additionalData": "{\"saleId\":789,\"pharmacyId\":123}"
}
```

#### **🛡️ Comprehensive Tracking**
- ✅ **User Context**: Who performed the operation
- ✅ **Currency Conversion**: Original and converted amounts
- ✅ **Balance Tracking**: Before and after balances
- ✅ **Error Handling**: Failed operations tracked
- ✅ **Additional Data**: Rich metadata for analytics

---

## 🎯 **Key Benefits of Enhanced System**

### **✅ Financial Accuracy**
- **Currency Consistency**: All calculations in SYP base currency
- **Exchange Rate Tracking**: Historical rate changes recorded
- **Balance Integrity**: Real-time balance updates

### **✅ Comprehensive Auditing**
- **Complete Trail**: Every financial operation tracked
- **User Context**: Who, when, where, how
- **Error Tracking**: Failed operations monitored
- **Compliance Ready**: Full audit trail for regulations

### **✅ Advanced Analytics**
- **Multi-Currency Reports**: Accurate conversion handling
- **Trend Analysis**: Historical data for insights
- **Performance Metrics**: Success rates, error analysis
- **Risk Assessment**: Failed operation patterns

### **✅ Production Ready**
- **Zero Disruption**: Builds on existing infrastructure
- **Backward Compatible**: Existing APIs unchanged
- **Scalable**: Handles high transaction volumes
- **Maintainable**: Single source of truth for audit logic

**The enhanced MoneyBox audit system provides world-class financial tracking and analytics!** 🚀
