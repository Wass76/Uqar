# Database Schema

## ORM Approach

**Uqar** uses **Spring Data JPA** with **Hibernate** as the JPA implementation. The system follows a **code-first approach** where entities are defined in Java and Hibernate generates/updates the database schema using `ddl-auto: update`.

### Database Configuration
- **Database**: PostgreSQL
- **ORM**: Hibernate (via Spring Data JPA)
- **Migration Tool**: Flyway (currently disabled, baseline version: 8)
- **Schema Generation**: `hibernate.ddl-auto: update` (automatic schema updates)

## Core Database Entities

### User Management Domain

#### `User` (Base Entity)
- **Inheritance Strategy**: `TABLE_PER_CLASS` (each subclass has its own table)
- **Key Fields**: `email`, `password`, `position`, `status`
- **Relationships**:
  - `@ManyToOne` → `Role` (user role)
  - `@ManyToMany` → `Permission` (additional permissions via `user_permissions` table)

#### `Employee` (extends User)
- **Table**: `employee`
- **Key Fields**: `phoneNumber`, `dateOfHire`
- **Relationships**:
  - `@ManyToOne` → `Pharmacy` (pharmacy_id)
  - `@OneToMany` → `EmployeeWorkingHours`

#### `Pharmacy`
- **Table**: `pharmacy`
- **Key Fields**: `name`, `licenseNumber`, `address`, `email`, `type` (MAIN/BRANCH), `isActive`
- **Relationships**:
  - `@OneToMany` → `Employee` (employees)
  - `@ManyToOne` → `Area` (geographic area)

#### `Customer`
- **Table**: `customers`
- **Key Fields**: `name`, `phoneNumber`, `address`, `notes`
- **Relationships**:
  - `@ManyToOne` → `Pharmacy` (pharmacy_id)
  - `@OneToMany` → `CustomerDebt` (debt records)

#### `Supplier`
- **Table**: `supplier`
- **Key Fields**: `name`, `contactInfo`, `address`
- **Relationships**:
  - `@OneToMany` → `PurchaseOrder`

#### `Role`
- **Table**: `roles`
- **Key Fields**: `name`, `description`, `isActive`, `isSystem`, `isSystemGenerated`
- **Relationships**:
  - `@ManyToMany` → `Permission` (via `role_permissions` table)

#### `Permission`
- **Table**: `permissions`
- **Key Fields**: `name`, `description`, `resource`, `action`, `isActive`, `isSystemGenerated`
- **Purpose**: Granular permission control (e.g., "PRODUCT:CREATE", "SALE:READ")

### Product & Inventory Domain

#### `MasterProduct`
- **Table**: `master_product`
- **Key Fields**: `tradeName`, `scientificName`, `concentration`, `size`, `barcode` (unique), `refPurchasePrice`, `refSellingPrice`, `minStockLevel`, `tax`, `requiresPrescription`
- **Relationships**:
  - `@ManyToMany` → `Category` (via `product_category` table)
  - `@ManyToOne` → `Type` (type_id)
  - `@ManyToOne` → `Form` (form_id)
  - `@ManyToOne` → `Manufacturer` (manufacturer_id)
  - `@OneToMany` → `MasterProductTranslation` (multi-language support)

#### `PharmacyProduct`
- **Table**: `pharmacy_product`
- **Key Fields**: Similar to MasterProduct, but pharmacy-specific
- **Relationships**:
  - `@ManyToOne` → `Pharmacy` (pharmacy_id) - **CRITICAL**: Links product to pharmacy
  - `@ManyToMany` → `Category` (via `pharmacy_product_category` table)
  - `@ManyToOne` → `Type`, `Form`, `Manufacturer`
  - `@OneToMany` → `PharmacyProductBarcode` (multiple barcodes per product)
  - `@OneToMany` → `PharmacyProductTranslation`

#### `StockItem`
- **Table**: `stock_item`
- **Purpose**: Tracks actual inventory quantities with batch/expiry information
- **Key Fields**: 
  - `productId` (references MasterProduct or PharmacyProduct)
  - `productType` (MASTER or PHARMACY enum)
  - `quantity`, `bonusQty`, `minStockLevel`
  - `expiryDate`, `batchNo`
  - `actualPurchasePrice`, `invoiceNumber`
  - `dateAdded`, `addedBy`
- **Relationships**:
  - `@ManyToOne` → `PurchaseInvoice` (purchase_invoice_id) - tracks source
  - `@ManyToOne` → `Pharmacy` (pharmacy_id) - **CRITICAL**: Multi-tenancy

#### `Category`
- **Table**: `category`
- **Key Fields**: `name`, `description`
- **Relationships**: Many-to-many with products

#### `Type`, `Form`, `Manufacturer`
- **Tables**: `type`, `form`, `manufacturer`
- **Purpose**: Product classification and metadata

### Sales (POS) Domain

#### `SaleInvoice`
- **Table**: `sale_invoices`
- **Key Fields**: 
  - `invoiceNumber` (unique)
  - `invoiceDate`, `totalAmount`, `paidAmount`, `remainingAmount`
  - `discount`, `discountType` (PERCENTAGE/FIXED)
  - `paymentType` (CASH/CREDIT), `paymentMethod` (CASH/CARD/TRANSFER)
  - `currency` (SYP/USD)
  - `status` (SOLD/CANCELLED/VOID)
  - `paymentStatus` (FULLY_PAID/PARTIALLY_PAID/UNPAID)
  - `refundStatus` (NO_REFUND/PARTIALLY_REFUNDED/FULLY_REFUNDED)
- **Relationships**:
  - `@ManyToOne` → `Customer` (customer_id)
  - `@ManyToOne` → `Pharmacy` (pharmacy_id)
  - `@OneToMany` → `SaleInvoiceItem`

#### `SaleInvoiceItem`
- **Table**: `sale_invoice_item`
- **Key Fields**: `quantity`, `unitPrice`, `subTotal`
- **Relationships**:
  - `@ManyToOne` → `SaleInvoice` (sale_invoice_id)
  - `@ManyToOne` → `StockItem` (stock_item_id) - **CRITICAL**: Links to actual stock

### Purchase Domain

#### `PurchaseOrder`
- **Table**: `purchase_order`
- **Key Fields**: `total`, `currency`, `status` (PENDING/COMPLETED/CANCELLED)
- **Relationships**:
  - `@ManyToOne` → `Supplier` (supplier_id)
  - `@ManyToOne` → `Pharmacy` (pharmacy_id)
  - `@OneToMany` → `PurchaseOrderItem`

#### `PurchaseInvoice`
- **Table**: `purchase_invoice`
- **Key Fields**: `invoiceNumber`, `totalAmount`, `currency`, `status`
- **Relationships**:
  - `@ManyToOne` → `Supplier` (supplier_id)
  - `@ManyToOne` → `Pharmacy` (pharmacy_id)
  - `@ManyToOne` → `PurchaseOrder` (purchase_order_id)
  - `@OneToMany` → `PurchaseInvoiceItem`

### Financial Domain

#### `MoneyBox`
- **Table**: `money_box`
- **Purpose**: Tracks pharmacy cash flow
- **Key Fields**: `currentBalance`, `currency`, `status` (ACTIVE/INACTIVE)
- **Relationships**:
  - `@ManyToOne` → `Pharmacy` (pharmacy_id) - one per pharmacy
  - `@OneToMany` → `MoneyBoxTransaction`

#### `MoneyBoxTransaction`
- **Table**: `money_box_transaction`
- **Purpose**: Audit trail for all financial transactions
- **Key Fields**:
  - `transactionType` (SALE_PAYMENT, PURCHASE_PAYMENT, CASH_DEPOSIT, etc.)
  - `amount`, `originalCurrency`, `convertedCurrency`
  - `exchangeRate`, `balanceBefore`, `balanceAfter`
  - `referenceId`, `referenceType` (SALE/PURCHASE/etc.)
  - `operationStatus` (SUCCESS/FAILED)
- **Relationships**:
  - `@ManyToOne` → `MoneyBox` (money_box_id)

#### `ExchangeRate`
- **Table**: `exchange_rate`
- **Purpose**: Tracks SYP ↔ USD conversion rates
- **Key Fields**: `rate`, `fromCurrency`, `toCurrency`, `effectiveDate`

### Supporting Domains

#### `Notification`
- **Table**: `notification`
- **Key Fields**: `title`, `message`, `type`, `status`, `nextRetryAt`
- **Relationships**:
  - `@ManyToOne` → `User` (recipient)

#### `Complaint`
- **Table**: `complaints`
- **Key Fields**: `title`, `description`, `status` (PENDING/IN_PROGRESS/RESOLVED/CLOSED/REJECTED), `response`, `pharmacy_id`
- **Relationships**:
  - Logical: `pharmacyId` → `Pharmacy.id` (Long, not JPA relationship)
  - Logical: `createdBy` → `User.id` (from AuditedEntity)
  - Logical: `respondedBy` → `User.id` (Long, not JPA relationship)
- **Audit Fields**: `ipAddress`, `userAgent`, `sessionId`, `userType`, `additionalData`

## Foreign Key Relationships Summary

### Critical Relationships for Multi-Tenancy

1. **Pharmacy → Everything**
   - `Employee.pharmacy_id` → `Pharmacy.id`
   - `Customer.pharmacy_id` → `Pharmacy.id`
   - `PharmacyProduct.pharmacy_id` → `Pharmacy.id`
   - `StockItem.pharmacy_id` → `Pharmacy.id`
   - `SaleInvoice.pharmacy_id` → `Pharmacy.id`
   - `PurchaseOrder.pharmacy_id` → `Pharmacy.id`
   - `MoneyBox.pharmacy_id` → `Pharmacy.id`

### Product Relationships

2. **Product Hierarchy**
   - `StockItem.productId` + `productType` → References `MasterProduct.id` OR `PharmacyProduct.id`
   - `MasterProduct.type_id` → `Type.id`
   - `MasterProduct.form_id` → `Form.id`
   - `MasterProduct.manufacturer_id` → `Manufacturer.id`

3. **Category Relationships**
   - `product_category` (join table): `product_id` → `MasterProduct.id`, `category_id` → `Category.id`
   - `pharmacy_product_category` (join table): `product_id` → `PharmacyProduct.id`, `category_id` → `Category.id`

### Sales Relationships

4. **Sale Flow**
   - `SaleInvoice.customer_id` → `Customer.id`
   - `SaleInvoiceItem.sale_invoice_id` → `SaleInvoice.id`
   - `SaleInvoiceItem.stock_item_id` → `StockItem.id` (**CRITICAL**: Links sale to actual inventory)

### Purchase Relationships

5. **Purchase Flow**
   - `PurchaseOrder.supplier_id` → `Supplier.id`
   - `PurchaseInvoice.purchase_order_id` → `PurchaseOrder.id`
   - `StockItem.purchase_invoice_id` → `PurchaseInvoice.id` (tracks stock source)

### Financial Relationships

6. **MoneyBox Flow**
   - `MoneyBoxTransaction.money_box_id` → `MoneyBox.id`
   - `MoneyBoxTransaction.referenceId` + `referenceType` → Links to Sale/Purchase invoices

### User & Security Relationships

7. **RBAC Relationships**
   - `User.role_id` → `Role.id`
   - `role_permissions` (join table): `role_id` → `Role.id`, `permission_id` → `Permission.id`
   - `user_permissions` (join table): `user_id` → `User.id`, `permission_id` → `Permission.id`

## Database Design Patterns

### 1. Audit Trail Pattern
All entities extend `AuditedEntity` which provides:
- `id` (auto-generated)
- `createdAt`, `updatedAt` (timestamps)
- `createdBy`, `updatedBy` (user references)

### 2. Soft Delete Pattern
Some entities use status flags (`isActive`, `status`) instead of hard deletes.

### 3. Multi-Tenancy Pattern
Every business entity includes `pharmacy_id` foreign key for data isolation.

### 4. Dual Currency Pattern
- All monetary values stored in **SYP** (base currency)
- `ExchangeRate` table tracks conversion rates
- Currency conversion happens at service layer for display

### 5. Polymorphic Product Reference
`StockItem` uses `productId` + `productType` enum to reference either `MasterProduct` or `PharmacyProduct`.

## Indexes

Key indexes for performance:
- `master_product.barcode` (unique index)
- `pharmacy.licenseNumber` (unique)
- `sale_invoices.invoiceNumber` (unique)
- `user.email` (unique, inherited from BaseUser)

