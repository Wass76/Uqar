# Point of Sale (POS) Module - Technical Documentation

## Data Flow Architecture

### Sale Creation Flow

```
SaleController.createSale()
    ↓
SaleService.createSaleInvoice()
    ↓
1. Validate pharmacy access
2. Get/Create customer
3. Create SaleInvoice entity
    ↓
For each SaleInvoiceItem:
    1. Get StockItem by stockItemId
    2. Validate quantity available (StockService.isQuantityAvailable)
    3. Validate expiry date (not expired)
    4. Reduce StockItem.quantity
    5. Calculate subtotal (unitPrice × quantity)
    6. Save StockItem (updated quantity)
    ↓
Calculate total amount
Apply discount (if any)
Calculate paid/remaining amounts
Validate payment (PaymentValidationService)
    ↓
Set invoice statuses:
- status = SOLD
- paymentStatus = FULLY_PAID/PARTIALLY_PAID/UNPAID
- refundStatus = NO_REFUND
    ↓
Save SaleInvoice
Save SaleInvoiceItems
    ↓
If CASH payment:
    SalesIntegrationService.recordSalePayment()
    → MoneyBox transaction created
    ↓
If CREDIT payment:
    CustomerDebt record created
    ↓
Return SaleInvoiceDTOResponse
```

### Sale Cancellation Flow

```
SaleController.cancelSale()
    ↓
SaleService.cancelSale()
    ↓
1. Get SaleInvoice
2. Validate not already cancelled
3. For each SaleInvoiceItem:
   - Get StockItem
   - Restore quantity (stockItem.quantity += item.quantity)
   - Save StockItem
4. Reverse MoneyBox transaction (if cash paid)
5. Reduce customer debt (if credit sale)
6. Update invoice status = CANCELLED
    ↓
Save SaleInvoice
Return success
```

### Refund Processing Flow

```
SaleController.processRefund()
    ↓
SaleService.processRefund()
    ↓
1. Get SaleInvoice
2. Validate not fully refunded
3. Create SaleRefund entity
    ↓
For each refund item:
    1. Get original SaleInvoiceItem
    2. Validate refund quantity (≤ available quantity)
    3. Create SaleRefundItem
    4. Update originalItem.refundedQuantity
    5. Calculate refund subtotal
    ↓
Calculate totalRefundAmount
Save SaleRefund
Save SaleRefundItems
    ↓
restoreStock():
    For each refund item:
        - Get original StockItem
        - Restore quantity (stockItem.quantity += refundQuantity)
        - Save StockItem
        - Mark refundItem.stockRestored = true
    ↓
handleRefundPayment():
    If cash sale:
        - Reduce MoneyBox balance
    If credit sale:
        - Reduce CustomerDebt
    If partial:
        - Combination of both
    ↓
updateInvoiceStatus():
    - Calculate refund status
    - Update payment status
    - Save SaleInvoice
    ↓
Return SaleRefundDTOResponse
```

## Key Endpoints

### Sale Management Endpoints

#### `POST /api/v1/sales`
**Purpose**: Create a new sale invoice

**Input Body**: `SaleInvoiceDTORequest`
```json
{
  "customerId": 1,
  "currency": "SYP",
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "discount": 0,
  "discountType": "PERCENTAGE",
  "paidAmount": 16000,
  "items": [
    {
      "stockItemId": 123,
      "quantity": 2,
      "unitPrice": 8000
    }
  ]
}
```

**Response**: `SaleInvoiceDTOResponse`
- Invoice details
- Items sold
- Payment information
- Customer information

**Side Effects**:
- **Stock Deduction**: Reduces `stock_item.quantity` for each item
- **MoneyBox Transaction**: Creates transaction if cash payment
- **Customer Debt**: Creates debt record if credit payment
- **Invoice Creation**: Creates `sale_invoice` and `sale_invoice_item` records

**Authorization**: Requires `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, or `PHARMACY_TRAINEE` role

**Validation**:
- Stock quantity must be sufficient
- Products must not be expired
- Cash payments must be fully paid
- Stock items must belong to current pharmacy

---

#### `GET /api/v1/sales`
**Purpose**: Get all sale invoices for current pharmacy

**Input Parameters**: None

**Response**: `List<SaleInvoiceDTOResponse>`

**Side Effects**: None (read-only)

**Authorization**: Requires `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, or `PHARMACY_TRAINEE` role

---

#### `GET /api/v1/sales/{id}`
**Purpose**: Get sale invoice by ID

**Input Parameters**:
- `id` (path): Sale invoice ID

**Response**: `SaleInvoiceDTOResponse`

**Side Effects**: None (read-only)

**Authorization**: Requires `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, or `PHARMACY_TRAINEE` role

---

#### `POST /api/v1/sales/{id}/cancel`
**Purpose**: Cancel a sale invoice

**Input Parameters**:
- `id` (path): Sale invoice ID

**Response**: 200 OK

**Side Effects**:
- **Stock Restoration**: Restores `stock_item.quantity` for all items
- **MoneyBox Reversal**: Reverses MoneyBox transaction (if cash paid)
- **Debt Reduction**: Reduces customer debt (if credit sale)
- **Status Update**: Sets invoice `status = CANCELLED`

**Authorization**: Requires `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, or `PHARMACY_TRAINEE` role

**Validation**:
- Invoice must not already be cancelled
- Invoice must belong to current pharmacy

---

#### `POST /api/v1/sales/{id}/refund`
**Purpose**: Process a refund for a sale invoice

**Input Body**: `SaleRefundDTORequest`
```json
{
  "refundReason": "Customer returned damaged product",
  "refundItems": [
    {
      "itemId": 456,
      "quantity": 1,
      "itemRefundReason": "Product damaged"
    }
  ]
}
```

**Response**: `SaleRefundDTOResponse`
- Refund details
- Refunded items
- Payment/debt adjustments

**Side Effects**:
- **Stock Restoration**: Restores `stock_item.quantity` for refunded items
- **Refund Records**: Creates `sale_refund` and `sale_refund_item` records
- **MoneyBox Reduction**: Reduces MoneyBox balance (if cash refund)
- **Debt Reduction**: Reduces customer debt (if credit refund)
- **Status Updates**: Updates invoice refund and payment statuses

**Authorization**: Requires `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, or `PHARMACY_TRAINEE` role

**Validation**:
- Invoice must not be fully refunded
- Refund quantity cannot exceed available quantity
- Invoice must belong to current pharmacy

---

#### `GET /api/v1/sales/{id}/refunds`
**Purpose**: Get all refunds for a sale invoice

**Input Parameters**:
- `id` (path): Sale invoice ID

**Response**: `List<SaleRefundDTOResponse>`

**Side Effects**: None (read-only)

**Authorization**: Requires `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, or `PHARMACY_TRAINEE` role

---

#### `GET /api/v1/sales/refunds`
**Purpose**: Get all refunds for current pharmacy

**Input Parameters**: None

**Response**: `List<SaleRefundDTOResponse>`

**Side Effects**: None (read-only)

**Authorization**: Requires `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, or `PHARMACY_TRAINEE` role

---

#### `GET /api/v1/sales/searchByDateRange`
**Purpose**: Search sale invoices by date range

**Input Parameters**:
- `startDate` (query): Start date (format: YYYY-MM-DD)
- `endDate` (query): End date (format: YYYY-MM-DD)

**Response**: `List<SaleInvoiceDTOResponse>`

**Side Effects**: None (read-only)

**Authorization**: Requires `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, or `PHARMACY_TRAINEE` role

---

## Service Layer Components

### SaleService

**Location**: `com.Uqar.sale.service.SaleService`

**Key Methods**:

1. **`createSaleInvoice(SaleInvoiceDTORequest)`**
   - Validates pharmacy access
   - Gets/creates customer
   - Validates stock and expiry
   - Deducts stock
   - Calculates totals and discounts
   - Creates invoice and items
   - Records payment (MoneyBox or debt)
   - Returns response

2. **`cancelSale(Long saleId)`**
   - Gets sale invoice
   - Restores stock quantities
   - Reverses MoneyBox transaction
   - Reduces customer debt
   - Updates invoice status

3. **`processRefund(Long saleId, SaleRefundDTORequest)`**
   - Validates refund request
   - Creates refund records
   - Restores stock
   - Handles payment/debt adjustments
   - Updates invoice statuses

4. **`getSaleById(Long id)`**
   - Gets sale invoice by ID
   - Validates pharmacy access
   - Returns response

5. **`getAllSales()`**
   - Gets all sales for current pharmacy
   - Returns list of responses

6. **`getRefundsBySaleId(Long saleId)`**
   - Gets all refunds for a sale
   - Returns list of refund responses

7. **`searchSaleInvoiceByDateRange(LocalDate startDate, LocalDate endDate)`**
   - Searches sales by date range
   - Filters by current pharmacy
   - Returns matching sales

**Extends**: `BaseSecurityService` (for current user and pharmacy access)

**Dependencies**:
- `StockService` - Stock validation and updates
- `SalesIntegrationService` - MoneyBox integration
- `DiscountCalculationService` - Discount calculations
- `PaymentValidationService` - Payment validation
- `CustomerDebtRepository` - Debt management

---

### DiscountCalculationService

**Location**: `com.Uqar.sale.service.DiscountCalculationService`

**Purpose**: Calculates discounts (percentage or fixed)

**Key Methods**:
- `calculateDiscount(float total, DiscountType type, float discount)`

---

### PaymentValidationService

**Location**: `com.Uqar.sale.service.PaymentValidationService`

**Purpose**: Validates payment amounts and types

**Key Methods**:
- `validatePaidAmount(float totalAmount, float paidAmount, PaymentType paymentType)`
- `calculateRemainingAmount(float totalAmount, float paidAmount)`

---

## Repository Layer

### SaleInvoiceRepository

**Location**: `com.Uqar.sale.repo.SaleInvoiceRepository`

**Key Methods**:
```java
List<SaleInvoice> findByPharmacyId(Long pharmacyId);
SaleInvoice findByIdAndPharmacyId(Long id, Long pharmacyId);
List<SaleInvoice> findByPharmacyIdAndInvoiceDateBetween(Long pharmacyId, LocalDate start, LocalDate end);
```

---

### SaleRefundRepository

**Location**: `com.Uqar.sale.repo.SaleRefundRepo`

**Key Methods**:
```java
List<SaleRefund> findBySaleInvoiceId(Long saleInvoiceId);
List<SaleRefund> findByPharmacyId(Long pharmacyId);
```

---

## Entity Relationships

### SaleInvoice Entity

```java
@Entity
public class SaleInvoice extends AuditedEntity {
    private String invoiceNumber;
    private float totalAmount;
    private float paidAmount;
    private float remainingAmount;
    private PaymentType paymentType;
    private PaymentMethod paymentMethod;
    private InvoiceStatus status;
    private PaymentStatus paymentStatus;
    private RefundStatus refundStatus;
    private Currency currency;
    
    @ManyToOne
    private Customer customer;
    
    @ManyToOne
    private Pharmacy pharmacy;
    
    @OneToMany
    private List<SaleInvoiceItem> items;
}
```

**Key Relationships**:
- `customer` → Customer who made purchase
- `pharmacy` → Multi-tenancy isolation
- `items` → Products sold

---

### SaleInvoiceItem Entity

```java
@Entity
public class SaleInvoiceItem extends AuditedEntity {
    private Integer quantity;
    private Float unitPrice;
    private Float subTotal;
    private Integer refundedQuantity;
    
    @ManyToOne
    private SaleInvoice saleInvoice;
    
    @ManyToOne
    private StockItem stockItem;
}
```

**Key Relationships**:
- `saleInvoice` → Parent invoice
- `stockItem` → **CRITICAL**: Links to actual stock batch

---

### SaleRefund Entity

```java
@Entity
public class SaleRefund extends AuditedEntity {
    private Float totalRefundAmount;
    private String refundReason;
    private RefundStatus refundStatus;
    private Boolean stockRestored;
    
    @ManyToOne
    private SaleInvoice saleInvoice;
    
    @ManyToOne
    private Pharmacy pharmacy;
    
    @OneToMany
    private List<SaleRefundItem> refundItems;
}
```

---

## Dependencies

### Internal Dependencies

1. **Inventory Module**
   - `StockService` - Stock validation and updates
   - `StockItem` - Stock item entities
   - `StockItemRepo` - Stock item repository

2. **MoneyBox Module**
   - `SalesIntegrationService` - Records cash payments
   - `MoneyBoxTransaction` - Financial tracking

3. **User Module**
   - `Customer` - Customer entities
   - `CustomerDebt` - Debt tracking
   - `BaseSecurityService` - Security utilities

### External Dependencies

- **Spring Data JPA**: Database access
- **MapStruct**: DTO mapping
- **Jakarta Validation**: Input validation

---

## Database Queries

### Sale Creation Query

```sql
INSERT INTO sale_invoices (
    customer_id, pharmacy_id, total_amount, paid_amount, 
    remaining_amount, payment_type, payment_method, 
    status, payment_status, refund_status, currency, 
    invoice_date, created_at, created_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

INSERT INTO sale_invoice_items (
    sale_invoice_id, stock_item_id, quantity, 
    unit_price, sub_total, refunded_quantity
) VALUES (?, ?, ?, ?, ?, 0);

UPDATE stock_item 
SET quantity = quantity - ? 
WHERE id = ?;
```

### Refund Query

```sql
INSERT INTO sale_refunds (
    sale_invoice_id, pharmacy_id, total_refund_amount, 
    refund_reason, refund_status, stock_restored
) VALUES (?, ?, ?, ?, ?, false);

INSERT INTO sale_refund_items (
    sale_refund_id, sale_invoice_item_id, refund_quantity, 
    unit_price, subtotal, stock_restored
) VALUES (?, ?, ?, ?, ?, false);

UPDATE stock_item 
SET quantity = quantity + ? 
WHERE id = ?;

UPDATE sale_invoice_items 
SET refunded_quantity = refunded_quantity + ? 
WHERE id = ?;
```

---

## Error Handling

### Common Exceptions

1. **`RequestNotValidException`**: 
   - Insufficient stock
   - Expired product
   - Invalid payment amount
   - Invalid refund quantity

2. **`EntityNotFoundException`**: 
   - Sale invoice not found
   - Stock item not found
   - Customer not found

3. **`UnAuthorizedException`**: 
   - User cannot access another pharmacy's sales
   - User not authorized to perform operation

4. **`ConflictException`**: 
   - Sale already cancelled
   - Sale already fully refunded

---

## Performance Considerations

1. **Indexing**: 
   - `sale_invoices.pharmacy_id` (for multi-tenancy)
   - `sale_invoices.customer_id` (for customer history)
   - `sale_invoices.invoice_date` (for date range queries)
   - `sale_invoice_items.stock_item_id` (for stock lookups)

2. **Transaction Management**: 
   - All sale operations are `@Transactional`
   - Ensures atomicity (all or nothing)

3. **Lazy Loading**: 
   - Customer relationship is LAZY
   - Pharmacy relationship is LAZY
   - Items are loaded eagerly (needed for calculations)

---

## Security Considerations

1. **Multi-Tenancy**: All queries filter by `pharmacy_id`
2. **Authorization**: Role-based access control on all endpoints
3. **Validation**: Stock and expiry validation before sale
4. **Audit Trail**: All changes tracked with user and timestamp
5. **Financial Integrity**: MoneyBox transactions are atomic

---

## Business Logic Details

### Discount Calculation

```java
if (discountType == PERCENTAGE) {
    discountAmount = total * (discount / 100);
} else if (discountType == FIXED) {
    discountAmount = discount;
}
finalAmount = total - discountAmount;
```

### Payment Validation

```java
if (paymentType == CASH) {
    if (paidAmount != totalAmount) {
        throw new RequestNotValidException("Cash payment must be full");
    }
} else if (paymentType == CREDIT) {
    if (paidAmount > totalAmount) {
        throw new RequestNotValidException("Paid amount cannot exceed total");
    }
    remainingAmount = totalAmount - paidAmount;
}
```

### Refund Quantity Validation

```java
int availableForRefund = originalItem.getQuantity() - originalItem.getRefundedQuantity();
if (refundQuantity > availableForRefund) {
    throw new RequestNotValidException("Refund quantity exceeds available");
}
```

