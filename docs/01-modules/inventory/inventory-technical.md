# Inventory Module - Technical Documentation

## Data Flow Architecture

### Stock Addition Flow (Purchase Invoice → Stock)

```
PurchaseInvoiceController.create()
    ↓
PurchaseInvoiceService.create()
    ↓
createStockItemRecords()
    ↓
For each PurchaseInvoiceItem:
    1. Create StockItem entity
    2. Set productId, productType (MASTER/PHARMACY)
    3. Set quantity (receivedQty + bonusQty)
    4. Set expiryDate, batchNo, invoiceNumber
    5. Set actualPurchasePrice (calculated with bonus)
    6. Link to PurchaseInvoice
    7. Set pharmacy (from current user)
    8. Save StockItem
    ↓
StockItemRepo.save()
    ↓
Database: stock_item table
```

### Stock Deduction Flow (Sale → Stock Update)

```
SaleController.createSale()
    ↓
SaleService.createSaleInvoice()
    ↓
For each SaleInvoiceItem:
    1. Get StockItem by stockItemId
    2. Validate quantity available
    3. Check expiry date
    4. Reduce stockItem.quantity
    5. Save StockItem
    ↓
StockItemRepo.save()
    ↓
Database: stock_item table (quantity decreased)
```

### Stock Search Flow

```
StockManagementController.advancedStockSearch()
    ↓
StockService.stockItemSearch()
    ↓
StockItemRepo.findUniqueProductsCombined()
    ↓
For each unique product:
    1. Aggregate stock items by productId + productType
    2. Calculate total quantity
    3. Calculate average purchase price
    4. Get product details (name, barcode, etc.)
    5. Map to StockProductOverallDTOResponse
    ↓
CurrencyConversionService (convert prices to USD)
    ↓
Return List<StockProductOverallDTOResponse>
```

## Key Endpoints

### Stock Management Endpoints

#### `GET /api/v1/stock/search`
**Purpose**: Search for products in stock

**Input Parameters**:
- `keyword` (optional): Search term for product name, barcode, or trade name
- `lang` (default: "en"): Language code for product names

**Response**: `List<StockProductOverallDTOResponse>`
- Product information with aggregated stock data
- Prices in both SYP and USD
- Total quantity across all batches

**Side Effects**: None (read-only)

**Authorization**: Requires authentication

---

#### `GET /api/v1/stock/products/Overall`
**Purpose**: Get all products in stock with aggregated information

**Input Parameters**: None

**Response**: `List<StockProductOverallDTOResponse>`
- All products with stock
- Each product appears once with total quantities

**Side Effects**: None (read-only)

**Authorization**: Requires authentication

---

#### `GET /api/v1/stock/product/{productId}/details`
**Purpose**: Get detailed stock information for a specific product

**Input Parameters**:
- `productId` (path): Product ID
- `productType` (query): MASTER or PHARMACY

**Response**: `Map<String, Object>`
- Total quantity
- List of individual stock items (batches)
- Minimum stock level
- Product details

**Side Effects**: None (read-only)

**Authorization**: Requires authentication

---

#### `PUT /api/v1/stock/{stockItemId}/edit`
**Purpose**: Edit stock quantity, expiry date, and minimum stock level

**Input Body**: `StockItemEditRequest`
```json
{
  "quantity": 100,
  "expiryDate": "2025-12-31",
  "minStockLevel": 50,
  "reasonCode": "DAMAGE",
  "additionalNotes": "Items damaged during transport"
}
```

**Response**: `StockItemDTOResponse`

**Side Effects**:
- Updates `stock_item.quantity`
- Updates `stock_item.expiry_date`
- Updates `stock_item.min_stock_level`
- Updates audit fields (`updated_at`, `last_modified_by`)
- If quantity set to 0, stock item is deleted

**Authorization**: Requires `PHARMACY_MANAGER` or `PHARMACY_EMPLOYEE` role
**Multi-Tenancy**: Validates user can only edit their pharmacy's stock

---

#### `DELETE /api/v1/stock/{stockItemId}`
**Purpose**: Delete a stock item

**Input Parameters**:
- `stockItemId` (path): Stock item ID to delete

**Response**: 204 No Content

**Side Effects**:
- Deletes stock item from database
- Stock quantity permanently removed

**Authorization**: Requires `PHARMACY_MANAGER` role
**Multi-Tenancy**: Validates user can only delete their pharmacy's stock

---

### Product Management Endpoints

#### `GET /api/v1/pharmacy_products`
**Purpose**: Get all pharmacy products with pagination

**Input Parameters**:
- `lang` (default: "ar"): Language code
- `page` (default: 0): Page number
- `size` (default: 10): Items per page
- `sortBy` (default: "createdAt"): Sort field
- `direction` (default: "desc"): Sort direction

**Response**: Paginated list of pharmacy products

**Side Effects**: None (read-only)

**Authorization**: Requires authentication

---

#### `POST /api/v1/pharmacy_products`
**Purpose**: Create a new pharmacy product

**Input Body**: `PharmacyProductDTORequest`
```json
{
  "tradeName": "Paracetamol 500mg",
  "scientificName": "Acetaminophen",
  "concentration": "500mg",
  "size": "20 tablets",
  "refPurchasePrice": 5000,
  "refSellingPrice": 8000,
  "minStockLevel": 50,
  "tax": 0.1,
  "requiresPrescription": false,
  "categoryIds": [1, 2],
  "typeId": 1,
  "formId": 1,
  "manufacturerId": 1
}
```

**Response**: `PharmacyProductDTOResponse`

**Side Effects**:
- Creates `pharmacy_product` record
- Creates `pharmacy_product_category` relationships
- Creates product translations (if multi-language)
- Product is now available for purchase orders

**Authorization**: Requires `PHARMACY_MANAGER` role
**Multi-Tenancy**: Automatically sets pharmacy from current user

---

#### `PUT /api/v1/pharmacy_products/{id}`
**Purpose**: Update a pharmacy product

**Input Body**: `PharmacyProductDTORequest`

**Response**: `PharmacyProductDTOResponse`

**Side Effects**:
- Updates product information
- Updates category relationships
- Updates translations

**Authorization**: Requires `PHARMACY_MANAGER` role
**Multi-Tenancy**: Validates user can only update their pharmacy's products

---

#### `DELETE /api/v1/pharmacy_products/{id}`
**Purpose**: Delete a pharmacy product

**Side Effects**:
- Deletes product (if no stock items reference it)
- Deletes category relationships
- Deletes translations

**Authorization**: Requires `PHARMACY_MANAGER` role
**Multi-Tenancy**: Validates user can only delete their pharmacy's products

---

## Service Layer Components

### StockService

**Location**: `com.Uqar.product.service.StockService`

**Key Methods**:

1. **`editStockQuantity(Long stockItemId, Integer newQuantity, ...)`**
   - Validates user is employee
   - Validates pharmacy access
   - Updates stock quantity
   - Handles deletion if quantity = 0

2. **`editStockQuantityAndExpiryDate(...)`**
   - Updates quantity, expiry date, and min stock level
   - Includes reason code and notes for audit

3. **`stockItemSearch(String keyword, String lang)`**
   - Searches products by name, barcode, or trade name
   - Aggregates stock items by product
   - Returns unique products with total quantities

4. **`getAllStockProductsOverall()`**
   - Gets all products with stock
   - Aggregates quantities per product
   - Returns product summaries

5. **`getProductStockDetails(Long productId, ProductType productType)`**
   - Gets detailed stock breakdown for a product
   - Returns all stock items (batches) for the product
   - Includes minimum stock level

6. **`isQuantityAvailable(Long productId, Integer requiredQuantity, ProductType productType)`**
   - Checks if sufficient stock exists
   - Used by SaleService before processing sales

7. **`deleteStockItem(Long stockItemId)`**
   - Deletes stock item
   - Validates pharmacy access

**Extends**: `BaseSecurityService` (for current user and pharmacy access)

---

### PharmacyProductService

**Location**: `com.Uqar.product.service.PharmacyProductService`

**Key Methods**:

1. **`createPharmacyProduct(PharmacyProductDTORequest)`**
   - Creates pharmacy product
   - Sets pharmacy from current user
   - Creates category relationships
   - Creates translations

2. **`updatePharmacyProduct(Long id, PharmacyProductDTORequest)`**
   - Updates product information
   - Updates relationships

3. **`getPharmacyProductPaginated(String lang, int page, int size)`**
   - Returns paginated list of products
   - Includes translations based on language

**Extends**: `BaseSecurityService`

---

## Repository Layer

### StockItemRepo

**Location**: `com.Uqar.product.repo.StockItemRepo`

**Key Methods**:

```java
List<StockItem> findByPharmacyId(Long pharmacyId);
List<StockItem> findByProductIdAndProductTypeAndPharmacyId(Long productId, ProductType productType, Long pharmacyId);
Integer getTotalQuantity(Long productId, Long pharmacyId, ProductType productType);
List<StockItem> findExpiredItems(LocalDate date, Long pharmacyId);
List<Object[]> findUniqueProductsCombined(Long pharmacyId);
```

**Custom Queries**:
- `getTotalQuantity`: Sums quantities for a product
- `findExpiredItems`: Finds expired stock items
- `findUniqueProductsCombined`: Groups stock items by product

---

## Entity Relationships

### StockItem Entity

```java
@Entity
public class StockItem extends AuditedEntity {
    private Long productId;              // References MasterProduct or PharmacyProduct
    private ProductType productType;     // MASTER or PHARMACY
    private Integer quantity;
    private Integer bonusQty;
    private LocalDate expiryDate;
    private String batchNo;
    private Double actualPurchasePrice;
    
    @ManyToOne
    private PurchaseInvoice purchaseInvoice;  // Source of stock
    
    @ManyToOne
    private Pharmacy pharmacy;                // Multi-tenancy
}
```

**Key Relationships**:
- `productId` + `productType` → References product (polymorphic)
- `purchaseInvoice` → Tracks source of stock
- `pharmacy` → Multi-tenancy isolation

---

### PharmacyProduct Entity

```java
@Entity
public class PharmacyProduct extends AuditedEntity {
    private String tradeName;
    private String scientificName;
    private float refPurchasePrice;
    private float refSellingPrice;
    private Integer minStockLevel;
    
    @ManyToOne
    private Pharmacy pharmacy;              // Multi-tenancy
    
    @ManyToMany
    private Set<Category> categories;
    
    @OneToMany
    private Set<PharmacyProductBarcode> barcodes;
}
```

---

## Dependencies

### Internal Dependencies

1. **Purchase Module**
   - `PurchaseInvoiceService` creates StockItem records
   - Stock items link to purchase invoices

2. **Sale Module**
   - `SaleService` reads and updates stock items
   - Stock validation before sales

3. **User Module**
   - `BaseSecurityService` for current user/pharmacy
   - `Pharmacy` entity for multi-tenancy

4. **MoneyBox Module**
   - `CurrencyConversionService` for price conversion
   - Inventory value calculations

### External Dependencies

- **Spring Data JPA**: Database access
- **MapStruct**: DTO mapping
- **Jakarta Validation**: Input validation

---

## Database Queries

### Stock Aggregation Query

```sql
SELECT 
    si.product_id,
    si.product_type,
    SUM(si.quantity) as total_quantity,
    AVG(si.actual_purchase_price) as avg_price
FROM stock_item si
WHERE si.pharmacy_id = ?
GROUP BY si.product_id, si.product_type
```

### Expired Items Query

```sql
SELECT si.*
FROM stock_item si
WHERE si.expiry_date < CURRENT_DATE
  AND si.quantity > 0
  AND si.pharmacy_id = ?
```

---

## Error Handling

### Common Exceptions

1. **`EntityNotFoundException`**: Stock item not found
2. **`UnAuthorizedException`**: User cannot access another pharmacy's stock
3. **`IllegalArgumentException`**: Invalid quantity (negative)
4. **`ConflictException`**: Expired product, insufficient stock

---

## Performance Considerations

1. **Indexing**: 
   - `stock_item.pharmacy_id` (for multi-tenancy filtering)
   - `stock_item.product_id` + `product_type` (for product lookups)
   - `stock_item.expiry_date` (for expiry queries)

2. **Lazy Loading**: 
   - PurchaseInvoice relationship is LAZY
   - Pharmacy relationship is LAZY

3. **Caching**: 
   - Product information can be cached
   - Stock quantities are real-time (not cached)

---

## Security Considerations

1. **Multi-Tenancy**: All queries filter by `pharmacy_id`
2. **Authorization**: Stock editing requires manager/employee role
3. **Validation**: Quantity cannot be negative
4. **Audit Trail**: All changes tracked with user and timestamp

