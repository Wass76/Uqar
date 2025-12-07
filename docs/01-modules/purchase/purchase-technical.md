# Purchase Module - Technical Documentation

## Data Flow Architecture

### Purchase Order Creation Flow

```
PurchaseOrderController.create()
    ↓
PurchaseOrderService.create()
    ↓
1. Validate supplier exists
2. Validate products exist
3. Create PurchaseOrder entity
4. For each item:
   - Create PurchaseOrderItem
   - Link to product
   - Set quantity and price
5. Calculate total amount
6. Set status = PENDING
7. Set pharmacy (from current user)
    ↓
Save PurchaseOrder
Save PurchaseOrderItems
    ↓
Return PurchaseOrderDTOResponse
```

### Purchase Invoice Creation Flow

```
PurchaseInvoiceController.create()
    ↓
PurchaseInvoiceService.create()
    ↓
1. Validate purchase order exists and is in correct status
2. Validate supplier
3. Create PurchaseInvoice entity
4. For each item:
   - Validate product exists
   - Get product details
   - Create PurchaseInvoiceItem
   - Calculate actual purchase price (with bonus)
   - Validate expiry date (not expired)
   - Convert prices to SYP (if USD)
5. Calculate total amount
6. Save PurchaseInvoice
7. Update order status to DONE
    ↓
createStockItemRecords():
    For each PurchaseInvoiceItem:
        1. Create StockItem entity
        2. Set productId, productType
        3. Set quantity (receivedQty + bonusQty)
        4. Set expiryDate, batchNo
        5. Set actualPurchasePrice
        6. Link to PurchaseInvoice
        7. Set pharmacy
        8. Save StockItem
        9. Update product reference prices if changed
    ↓
If cash payment:
    PurchaseIntegrationService.recordPurchasePayment()
    → MoneyBox transaction created
    ↓
Return PurchaseInvoiceDTOResponse
```

## Key Endpoints

### Purchase Order Endpoints

#### `POST /api/v1/purchase-orders`
**Purpose**: Create a new purchase order

**Input Body**: `PurchaseOrderDTORequest`
```json
{
  "supplierId": 1,
  "currency": "SYP",
  "items": [
    {
      "productId": 123,
      "productType": "MASTER",
      "quantity": 100,
      "expectedPrice": 5000
    }
  ]
}
```

**Response**: `PurchaseOrderDTOResponse`

**Side Effects**:
- Creates `purchase_order` record
- Creates `purchase_order_item` records
- Order status = PENDING

**Authorization**: Requires `PHARMACY_MANAGER` role

---

#### `GET /api/v1/purchase-orders/{id}`
**Purpose**: Get purchase order by ID

**Response**: `PurchaseOrderDTOResponse`

**Side Effects**: None (read-only)

**Authorization**: Requires authentication

---

#### `PUT /api/v1/purchase-orders/{id}`
**Purpose**: Edit purchase order

**Side Effects**:
- Updates order items
- Recalculates total amount
- Cannot edit if status is DONE or CANCELLED

**Authorization**: Requires `PHARMACY_MANAGER` role

---

#### `GET /api/v1/purchase-orders`
**Purpose**: Get paginated purchase orders

**Query Parameters**:
- `page` (default: 0)
- `size` (default: 10)
- `language` (default: "ar")

**Response**: `PaginationDTO<PurchaseOrderDTOResponse>`

**Side Effects**: None (read-only)

---

#### `GET /api/v1/purchase-orders/status/{status}`
**Purpose**: Get purchase orders by status

**Path Parameters**:
- `status`: PENDING, APPROVED, REJECTED, CANCELLED, DONE

**Response**: `PaginationDTO<PurchaseOrderDTOResponse>`

**Side Effects**: None (read-only)

---

#### `GET /api/v1/purchase-orders/time-range`
**Purpose**: Get purchase orders by date range

**Query Parameters**:
- `startDate`: Start date
- `endDate`: End date
- `page`, `size`, `language`

**Response**: `PaginationDTO<PurchaseOrderDTOResponse>`

**Side Effects**: None (read-only)

---

#### `GET /api/v1/purchase-orders/supplier/{supplierId}`
**Purpose**: Get purchase orders by supplier

**Response**: `PaginationDTO<PurchaseOrderDTOResponse>`

**Side Effects**: None (read-only)

---

#### `DELETE /api/v1/purchase-orders/{id}`
**Purpose**: Cancel purchase order

**Side Effects**:
- Updates order status to CANCELLED
- Cannot cancel if already CANCELLED or DONE

**Authorization**: Requires `PHARMACY_MANAGER` role

---

### Purchase Invoice Endpoints

#### `POST /api/v1/purchase-invoices`
**Purpose**: Create purchase invoice (receive goods)

**Input Body**: `PurchaseInvoiceDTORequest`
```json
{
  "purchaseOrderId": 789,
  "supplierId": 1,
  "currency": "USD",
  "paymentMethod": "CASH",
  "items": [
    {
      "productId": 123,
      "productType": "MASTER",
      "receivedQty": 100,
      "bonusQty": 10,
      "invoicePrice": 5.50,
      "actualPrice": 5.25,
      "batchNo": "BATCH001",
      "expiryDate": "2025-12-31",
      "minStockLevel": 50
    }
  ]
}
```

**Response**: `PurchaseInvoiceDTOResponse`

**Side Effects**:
- **Stock Addition**: Creates `stock_item` records for each item
- **Order Update**: Updates purchase order status to DONE
- **Price Updates**: Updates product reference prices
- **MoneyBox Transaction**: Records payment if cash
- **Invoice Creation**: Creates `purchase_invoice` and `purchase_invoice_item` records

**Authorization**: Requires `PHARMACY_MANAGER` role

**Validation**:
- Expiry date must not be expired
- Expiry date should be at least 6 months away (warning)
- Purchase order must exist and be in correct status

---

#### `GET /api/v1/purchase-invoices/{id}`
**Purpose**: Get purchase invoice by ID

**Response**: `PurchaseInvoiceDTOResponse`

**Side Effects**: None (read-only)

---

#### `GET /api/v1/purchase-invoices`
**Purpose**: Get paginated purchase invoices

**Response**: `PaginationDTO<PurchaseInvoiceDTOResponse>`

**Side Effects**: None (read-only)

---

## Service Layer Components

### PurchaseOrderService

**Location**: `com.Uqar.purchase.service.PurchaseOrderService`

**Key Methods**:

1. **`create(PurchaseOrderDTORequest, String language)`**
   - Validates supplier and products
   - Creates order and items
   - Calculates total
   - Returns response

2. **`edit(Long id, PurchaseOrderDTORequest, String language)`**
   - Validates order can be edited
   - Updates items
   - Recalculates total

3. **`cancel(Long id)`**
   - Validates order can be cancelled
   - Updates status to CANCELLED

4. **`getById(Long id, String language)`**
   - Gets order by ID
   - Returns response

5. **`listAllPaginated(int page, int size, String language)`**
   - Gets paginated orders for current pharmacy

**Extends**: `BaseSecurityService`

---

### PurchaseInvoiceService

**Location**: `com.Uqar.purchase.service.PurchaseInvoiceService`

**Key Methods**:

1. **`create(PurchaseInvoiceDTORequest, String language)`**
   - Validates order and supplier
   - Creates invoice and items
   - Creates stock items
   - Updates order status
   - Records payment
   - Returns response

2. **`createStockItemRecords(PurchaseInvoice invoice, PurchaseInvoiceDTORequest request)`**
   - For each invoice item:
     - Validates expiry date
     - Calculates actual purchase price
     - Creates StockItem
     - Updates product prices
     - Saves stock item

3. **`validateExpiryDate(LocalDate expiryDate)`**
   - Checks not expired
   - Warns if expiring within 6 months

4. **`calculateActualPurchasePrice(PurchaseInvoiceItem item)`**
   - Calculates price considering bonus quantity
   - Formula: (invoicePrice × receivedQty) / (receivedQty + bonusQty)

**Extends**: `BaseSecurityService`

**Dependencies**:
- `StockItemRepo` - Stock item creation
- `PurchaseIntegrationService` - MoneyBox integration
- `PharmacyProductService` - Product updates
- `MasterProductService` - Product updates

---

## Repository Layer

### PurchaseOrderRepository

**Key Methods**:
```java
List<PurchaseOrder> findByPharmacyId(Long pharmacyId);
List<PurchaseOrder> findByPharmacyIdAndStatus(Long pharmacyId, OrderStatus status);
List<PurchaseOrder> findByPharmacyIdAndCreatedAtBetween(Long pharmacyId, LocalDateTime start, LocalDateTime end);
List<PurchaseOrder> findByPharmacyIdAndSupplierId(Long pharmacyId, Long supplierId);
```

---

### PurchaseInvoiceRepository

**Key Methods**:
```java
List<PurchaseInvoice> findByPharmacyId(Long pharmacyId);
PurchaseInvoice findByIdAndPharmacyId(Long id, Long pharmacyId);
```

---

## Entity Relationships

### PurchaseOrder Entity

```java
@Entity
public class PurchaseOrder extends AuditedEntity {
    private Double total;
    private Currency currency;
    private OrderStatus status;
    
    @ManyToOne
    private Supplier supplier;
    
    @ManyToOne
    private Pharmacy pharmacy;
    
    @OneToMany
    private List<PurchaseOrderItem> items;
}
```

---

### PurchaseInvoice Entity

```java
@Entity
public class PurchaseInvoice extends AuditedEntity {
    private String invoiceNumber;
    private Double totalAmount;
    private Currency currency;
    
    @ManyToOne
    private Supplier supplier;
    
    @ManyToOne
    private Pharmacy pharmacy;
    
    @ManyToOne
    private PurchaseOrder purchaseOrder;
    
    @OneToMany
    private List<PurchaseInvoiceItem> items;
}
```

---

## Dependencies

### Internal Dependencies

1. **Inventory Module**
   - `StockItem` - Stock item creation
   - `StockItemRepo` - Stock item repository

2. **MoneyBox Module**
   - `PurchaseIntegrationService` - Payment recording

3. **User Module**
   - `Supplier` - Supplier entities
   - `BaseSecurityService` - Security utilities

4. **Product Module**
   - `MasterProduct` - Product entities
   - `PharmacyProduct` - Product entities

---

## Database Queries

### Purchase Invoice Creation

```sql
INSERT INTO purchase_invoice (
    supplier_id, pharmacy_id, purchase_order_id, 
    invoice_number, total_amount, currency, 
    created_at, created_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?);

INSERT INTO purchase_invoice_item (
    purchase_invoice_id, product_id, product_type, 
    received_qty, bonus_qty, invoice_price, actual_price, 
    batch_no, expiry_date
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);

INSERT INTO stock_item (
    product_id, product_type, quantity, bonus_qty, 
    actual_purchase_price, expiry_date, batch_no, 
    purchase_invoice_id, pharmacy_id, date_added
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

UPDATE purchase_order 
SET status = 'DONE' 
WHERE id = ?;
```

---

## Error Handling

### Common Exceptions

1. **`EntityNotFoundException`**: 
   - Purchase order not found
   - Supplier not found
   - Product not found

2. **`ConflictException`**: 
   - Expired product
   - Order already completed
   - Order already cancelled

3. **`RequestNotValidException`**: 
   - Invalid quantities
   - Invalid prices
   - Invalid expiry date

---

## Performance Considerations

1. **Indexing**: 
   - `purchase_order.pharmacy_id`
   - `purchase_order.supplier_id`
   - `purchase_order.status`
   - `purchase_invoice.purchase_order_id`

2. **Transaction Management**: 
   - All invoice creation is `@Transactional`
   - Ensures atomicity (order update + stock creation + payment)

3. **Lazy Loading**: 
   - Supplier relationship is LAZY
   - Pharmacy relationship is LAZY
   - Items are loaded eagerly

---

## Security Considerations

1. **Multi-Tenancy**: All queries filter by `pharmacy_id`
2. **Authorization**: Role-based access control
3. **Validation**: Expiry date validation before stock creation
4. **Audit Trail**: All changes tracked

---

## Business Logic Details

### Actual Purchase Price Calculation

```java
// When bonus quantity is included, actual price per unit is calculated:
actualPrice = (invoicePrice × receivedQty) / (receivedQty + bonusQty)

// Example:
// Invoice price: 5.50 per unit
// Received: 100 units
// Bonus: 10 units
// Actual price: (5.50 × 100) / 110 = 5.00 per unit
```

### Expiry Date Validation

```java
if (expiryDate.isBefore(LocalDate.now())) {
    throw new ConflictException("Cannot accept expired products");
}

if (expiryDate.isBefore(LocalDate.now().plusMonths(6))) {
    logger.warn("Product expiring within 6 months");
}
```

### Currency Conversion

```java
// All prices stored in SYP
if (requestCurrency == USD) {
    priceInSYP = priceInUSD × exchangeRate;
} else {
    priceInSYP = priceInSYP;
}
```

