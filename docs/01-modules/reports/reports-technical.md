# Reports Module - Technical Documentation

## Data Flow Architecture

### Sales Report Generation Flow

```
ReportController.getSalesReport()
    ↓
ReportService.generateSalesReport()
    ↓
1. Validate date range
2. Get current pharmacy ID
3. Query sale invoices:
   - Filter by pharmacy
   - Filter by date range
   - Filter by additional criteria
4. Aggregate data:
   - Total sales
   - Number of transactions
   - Average sale amount
   - Sales by product
   - Sales by customer
5. Map to report DTO
    ↓
Return SalesReportDTO
```

### Purchase Report Generation Flow

```
ReportController.getPurchaseReport()
    ↓
ReportService.generatePurchaseReport()
    ↓
1. Validate date range
2. Get current pharmacy ID
3. Query purchase invoices:
   - Filter by pharmacy
   - Filter by date range
   - Filter by supplier (optional)
4. Aggregate data:
   - Total purchases
   - Number of transactions
   - Average purchase amount
   - Purchases by supplier
5. Map to report DTO
    ↓
Return PurchaseReportDTO
```

## Key Endpoints

### Report Endpoints

#### `GET /api/v1/reports/sales`
**Purpose**: Generate sales report

**Query Parameters**:
- `startDate` (required): Start date
- `endDate` (required): End date
- `currency` (optional): SYP or USD
- `productId` (optional): Filter by product
- `customerId` (optional): Filter by customer

**Response**: `SalesReportDTO`
- Total sales
- Number of transactions
- Average sale amount
- Sales breakdown

**Side Effects**: None (read-only)

**Authorization**: Requires authentication

---

#### `GET /api/v1/reports/purchases`
**Purpose**: Generate purchase report

**Query Parameters**:
- `startDate` (required): Start date
- `endDate` (required): End date
- `currency` (optional): SYP or USD
- `supplierId` (optional): Filter by supplier

**Response**: `PurchaseReportDTO`
- Total purchases
- Number of transactions
- Average purchase amount
- Purchase breakdown

**Side Effects**: None (read-only)

---

#### `GET /api/v1/reports/inventory`
**Purpose**: Generate inventory report

**Query Parameters**:
- `productType` (optional): MASTER or PHARMACY
- `categoryId` (optional): Filter by category

**Response**: `InventoryReportDTO`
- Total stock value
- Product counts
- Low stock items
- Expired products

**Side Effects**: None (read-only)

---

#### `GET /api/v1/reports/financial`
**Purpose**: Generate financial report

**Query Parameters**:
- `startDate` (required): Start date
- `endDate` (required): End date
- `currency` (optional): SYP or USD

**Response**: `FinancialReportDTO`
- Revenue summary
- Expense summary
- Net profit
- Cash flow

**Side Effects**: None (read-only)

**Authorization**: Requires `PHARMACY_MANAGER` role

---

## Service Layer Components

### ReportService

**Location**: `com.Uqar.reports.service.ReportService`

**Key Methods**:

1. **`generateSalesReport(...)`**
   - Queries sale invoices
   - Aggregates sales data
   - Calculates metrics
   - Returns report DTO

2. **`generatePurchaseReport(...)`**
   - Queries purchase invoices
   - Aggregates purchase data
   - Calculates metrics
   - Returns report DTO

3. **`generateInventoryReport(...)`**
   - Queries stock items
   - Calculates stock value
   - Identifies low stock
   - Returns report DTO

4. **`generateFinancialReport(...)`**
   - Queries MoneyBox transactions
   - Calculates revenue and expenses
   - Calculates profit
   - Returns report DTO

**Extends**: `BaseSecurityService`

---

## Repository Layer

Reports use existing repositories:
- `SaleInvoiceRepository` - For sales reports
- `PurchaseInvoiceRepository` - For purchase reports
- `StockItemRepository` - For inventory reports
- `MoneyBoxTransactionRepository` - For financial reports

---

## Database Queries

### Sales Report Query

```sql
SELECT 
    COUNT(*) as transaction_count,
    SUM(total_amount) as total_sales,
    AVG(total_amount) as average_sale,
    SUM(paid_amount) as total_paid
FROM sale_invoices
WHERE pharmacy_id = ?
  AND invoice_date BETWEEN ? AND ?
  AND status = 'SOLD';
```

### Purchase Report Query

```sql
SELECT 
    COUNT(*) as transaction_count,
    SUM(total_amount) as total_purchases,
    AVG(total_amount) as average_purchase
FROM purchase_invoice
WHERE pharmacy_id = ?
  AND created_at BETWEEN ? AND ?;
```

### Inventory Report Query

```sql
SELECT 
    COUNT(DISTINCT product_id) as product_count,
    SUM(quantity * actual_purchase_price) as total_value,
    SUM(quantity) as total_quantity
FROM stock_item
WHERE pharmacy_id = ?
  AND quantity > 0;
```

---

## Dependencies

### Internal Dependencies

1. **Sales Module**
   - `SaleInvoiceRepository` - Sales data

2. **Purchase Module**
   - `PurchaseInvoiceRepository` - Purchase data

3. **Inventory Module**
   - `StockItemRepository` - Stock data

4. **MoneyBox Module**
   - `MoneyBoxTransactionRepository` - Financial data

---

## Performance Considerations

1. **Indexing**: 
   - Date fields indexed for range queries
   - Pharmacy ID indexed for filtering

2. **Caching**: 
   - Report results can be cached for frequently accessed reports

3. **Pagination**: 
   - Large reports can be paginated

---

## Security Considerations

1. **Multi-Tenancy**: All reports filter by pharmacy
2. **Authorization**: Financial reports require manager role
3. **Data Privacy**: Reports only show authorized data

