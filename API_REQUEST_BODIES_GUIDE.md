# API Request Bodies Guide - Uqar Pharmacy System

This guide provides comprehensive examples of request bodies for creating purchase orders, purchase invoices, sales invoices, and managing employees in the Uqar pharmacy management system.

---

## 📋 Table of Contents

1. [Purchase Order](#purchase-order)
2. [Purchase Invoice](#purchase-invoice)
3. [Sale Invoice](#sale-invoice)
4. [Master Products](#master-products)
5. [Pharmacy Products](#pharmacy-products)
6. [Suppliers](#suppliers)
7. [Customers](#customers)
8. [Employees](#employees)
9. [Field Descriptions](#field-descriptions)
10. [Validation Rules](#validation-rules)
11. [Common Error Scenarios](#common-error-scenarios)
12. [Testing Examples](#testing-examples)

---

## 🛒 Purchase Order

**Endpoint:** `POST /api/v1/purchase-orders`

**Description:** Creates a new purchase order for products from suppliers.

### Request Body Structure

```json
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
    }
  ]
}
```

### Complete Example with Multiple Items

```json
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
      "price": 12.75,
      "barcode": "9876543210987",
      "productType": "PHARMACY"
    },
    {
      "productId": 3,
      "quantity": 200,
      "price": 3.25,
      "barcode": "5556667778889",
      "productType": "MASTER"
    }
  ]
}
```

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `language` | String | No | "ar" | Language code (ar/en) |

### Headers Required

```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

---

## 🧾 Purchase Invoice

**Endpoint:** `POST /api/v1/purchase-invoices`

**Description:** Creates a purchase invoice to record received products from suppliers.

### Request Body Structure

```json
{
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "USD",
  "total": 550.00,
  "invoiceNumber": "INV-2024-001",
  "paymentMethod": "CASH",
  "items": [
    {
      "productId": 1,
      "receivedQty": 100,
      "bonusQty": 10,
      "invoicePrice": 5.50,
      "batchNo": "BATCH001",
      "expiryDate": "2025-12-31",
      "productType": "MASTER",
      "sellingPrice": 8.50,
      "minStockLevel": 10
    }
  ]
}
```

### Complete Example with Multiple Items

```json
{
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "USD",
  "total": 1050.00,
  "invoiceNumber": "INV-2024-001",
  "paymentMethod": "BANK_ACCOUNT",
  "items": [
    {
      "productId": 1,
      "receivedQty": 100,
      "bonusQty": 10,
      "invoicePrice": 5.50,
      "batchNo": "BATCH001",
      "expiryDate": "2025-12-31",
      "productType": "MASTER",
      "sellingPrice": 8.50,
      "minStockLevel": 10
    },
    {
      "productId": 2,
      "receivedQty": 50,
      "bonusQty": 5,
      "invoicePrice": 12.75,
      "batchNo": "BATCH002",
      "expiryDate": "2026-06-30",
      "productType": "PHARMACY",
      "sellingPrice": 18.00,
      "minStockLevel": 15
    },
    {
      "productId": 3,
      "receivedQty": 200,
      "bonusQty": 20,
      "invoicePrice": 3.25,
      "batchNo": "BATCH003",
      "expiryDate": "2025-08-15",
      "productType": "MASTER",
      "sellingPrice": 5.00,
      "minStockLevel": 25
    }
  ]
}
```

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `language` | String | No | "ar" | Language code (ar/en) |

### Headers Required

```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

---

## 💰 Sale Invoice

**Endpoint:** `POST /api/v1/sales`

**Description:** Creates a sale invoice for products sold to customers.

### Request Body Structure

```json
{
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "currency": "SYP",
  "invoiceDiscountType": "PERCENTAGE",
  "invoiceDiscountValue": 10.0,
  "paidAmount": null,
  "debtDueDate": null,
  "items": [
    {
      "stockItemId": 1,
      "quantity": 2,
      "unitPrice": 800.0
    }
  ]
}
```

### Complete Example with Multiple Items

```json
{
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "currency": "SYP",
  "invoiceDiscountType": "PERCENTAGE",
  "invoiceDiscountValue": 15.0,
  "paidAmount": null,
  "debtDueDate": null,
  "items": [
    {
      "stockItemId": 1,
      "quantity": 2,
      "unitPrice": 800.0
    },
    {
      "stockItemId": 2,
      "quantity": 1,
      "unitPrice": 1200.0
    },
    {
      "stockItemId": 3,
      "quantity": 3,
      "unitPrice": 450.0
    }
  ]
}
```

### Credit Sale Example

```json
{
  "customerId": 1,
  "paymentType": "CREDIT",
  "paymentMethod": "BANK_ACCOUNT",
  "currency": "USD",
  "invoiceDiscountType": "FIXED_AMOUNT",
  "invoiceDiscountValue": 50.0,
  "paidAmount": 200.0,
  "debtDueDate": "2024-12-31",
  "items": [
    {
      "stockItemId": 1,
      "quantity": 3,
      "unitPrice": 100.0
    }
  ]
}
```

### Headers Required

```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

---

## 🏭 Master Products

**Endpoint:** `POST /api/v1/products/master`

**Description:** Creates mastermaster products that can be used across multiple pharmacies.

### Request Body Structure

```json
{
  "tradeName": "Paracetamol",
  "scientificName": "Acetaminophen",
  "concentration": "500mg",
  "size": "20 tablets",
  "refPurchasePrice": 150.0,
  "refSellingPrice": 300.0,
  "notes": "Pain relief and fever reducer",
  "tax": 5.0,
  "barcode": "1234567890123",
  "requiresPrescription": false,
  "typeId": 1,
  "formId": 1,
  "manufacturerId": 1,
  "categoryIds": [1, 2],
  "translations": [
    {
      "tradeName": "باراسيتامول",
      "scientificName": "أسيتامينوفين",
      "lang": "ar"
    }
  ]
}
```

### Complete Examples

#### 1. Paracetamol 500mg
```json
{
  "tradeName": "Paracetamol",
  "scientificName": "Acetaminophen",
  "concentration": "500mg",
  "size": "20 tablets",
  "refPurchasePrice": 150.0,
  "refSellingPrice": 300.0,
  "notes": "Pain relief and fever reducer medication",
  "tax": 5.0,
  "barcode": "1234567890123",
  "requiresPrescription": false,
  "typeId": 1,
  "formId": 1,
  "manufacturerId": 1,
  "categoryIds": [1, 2],
  "translations": [
    {
      "tradeName": "باراسيتامول",
      "scientificName": "أسيتامينوفين",
      "lang": "ar"
    }
  ]
}
```

#### 2. Amoxicillin 250mg
```json
{
  "tradeName": "Amoxicillin",
  "scientificName": "Amoxicillin Trihydrate",
  "concentration": "250mg",
  "size": "21 capsules",
  "refPurchasePrice": 200.0,
  "refSellingPrice": 400.0,
  "notes": "Antibiotic for bacterial infections",
  "tax": 5.0,
  "barcode": "2345678901234",
  "requiresPrescription": true,
  "typeId": 2,
  "formId": 2,
  "manufacturerId": 2,
  "categoryIds": [3, 4],
  "translations": [
    {
      "tradeName": "أموكسيسيلين",
      "scientificName": "أموكسيسيلين تريهيدرات",
      "lang": "ar"
    }
  ]
}
```

#### 3. Vitamin D3 1000 IU
```json
{
  "tradeName": "Vitamin D3",
  "scientificName": "Cholecalciferol",
  "concentration": "1000 IU",
  "size": "60 capsules",
  "refPurchasePrice": 100.0,
  "refSellingPrice": 200.0,
  "notes": "Vitamin D supplement for bone health",
  "tax": 5.0,
  "barcode": "3456789012345",
  "requiresPrescription": false,
  "typeId": 3,
  "formId": 2,
  "manufacturerId": 3,
  "categoryIds": [5, 6],
  "translations": [
    {
      "tradeName": "فيتامين د3",
      "scientificName": "كوليكالسيفيرول",
      "lang": "ar"
    }
  ]
}
```

### Headers Required

```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

### Translation Structure

The `translations` field is an optional array that allows you to provide multilingual support for product trade names and scientific names:

```json
{
  "translations": [
    {
      "tradeName": "الاسم التجاري باللغة العربية",
      "scientificName": "الاسم العلمي باللغة العربية",
      "lang": "ar"
    },
    {
      "tradeName": "Trade Name in English",
      "scientificName": "Scientific Name in English",
      "lang": "en"
    }
  ]
}
```

**Supported Languages:**
- `ar`: Arabic (العربية)
- `en`: English
- `fr`: French (Français)
- `de`: German (Deutsch)
- `es`: Spanish (Español)

---

## 💊 Pharmacy Products

**Endpoint:** `POST /api/v1/products/pharmacy`

**Description:** Creates pharmacy-specific products with pricing and stock management.

### Request Body Structure

```json
{
  "tradeName": "Ibuprofen",
  "scientificName": "Ibuprofen",
  "concentration": "400mg",
  "size": "30 tablets",
  "notes": "Anti-inflammatory pain relief",
  "tax": 7.0,
  "barcodes": ["9876543210987"],
  "requiresPrescription": false,
  "typeId": 2,
  "formId": 1,
  "manufacturerId": 2,
  "categoryIds": [2, 3],
  "translations": [
    {
      "tradeName": "ايبوبروفين",
      "scientificName": "ايبوبروفين",
      "lang": "ar"
    }
  ]
}
```

### Complete Examples

#### 1. Paracetamol Pharmacy Product
```json
{
  "tradeName": "Paracetamol",
  "scientificName": "Acetaminophen",
  "concentration": "500mg",
  "size": "20 tablets",
  "notes": "Fast-moving pain relief medication",
  "tax": 5.0,
  "barcodes": ["1234567890123"],
  "requiresPrescription": false,
  "typeId": 1,
  "formId": 1,
  "manufacturerId": 1,
  "categoryIds": [1, 2],
  "translations": [
    {
      "tradeName": "باراسيتامول",
      "scientificName": "أسيتامينوفين",
      "lang": "ar"
    }
  ]
}
```

#### 2. Amoxicillin Pharmacy Product
```json
{
  "tradeName": "Amoxicillin",
  "scientificName": "Amoxicillin Trihydrate",
  "concentration": "250mg",
  "size": "21 capsules",
  "notes": "Prescription required - controlled stock",
  "tax": 5.0,
  "barcodes": ["2345678901234"],
  "requiresPrescription": true,
  "typeId": 2,
  "formId": 2,
  "manufacturerId": 2,
  "categoryIds": [3, 4],
  "translations": [
    {
      "tradeName": "أموكسيسيلين",
      "scientificName": "أموكسيسيلين تريهيدرات",
      "lang": "ar"
    }
  ]
}
```

#### 3. Vitamin D3 Pharmacy Product
```json
{
  "tradeName": "Vitamin D3",
  "scientificName": "Cholecalciferol",
  "concentration": "1000 IU",
  "size": "60 capsules",
  "notes": "Seasonal demand - winter months",
  "tax": 5.0,
  "barcodes": ["3456789012345"],
  "requiresPrescription": false,
  "typeId": 3,
  "formId": 2,
  "manufacturerId": 3,
  "categoryIds": [5, 6],
  "translations": [
    {
      "tradeName": "فيتامين د3",
      "scientificName": "كوليكالسيفيرول",
      "lang": "ar"
    }
  ]
}
```

### Headers Required

```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

---

## 🏢 Suppliers

**Endpoint:** `POST /api/v1/suppliers`

**Description:** Creates supplier records for purchase management.

### Request Body Structure

```json
{
  "name": "PharmaCorp International",
  "phone": "+963-11-123-4567",
  "address": "Al-Mazzeh, Damascus, Syria",
  "preferredCurrency": "USD"
}
```

### Complete Examples

#### 1. PharmaCorp International
```json
{
  "name": "PharmaCorp International",
  "phone": "+963-11-123-4567",
  "address": "Al-Mazzeh, Damascus, Syria",
  "preferredCurrency": "USD"
}
```

#### 2. MediPharm Distribution
```json
{
  "name": "MediPharm Distribution",
  "phone": "+963-11-987-6543",
  "address": "Bab Sharqi, Damascus, Syria",
  "preferredCurrency": "EUR"
}
```

#### 3. NutriHealth Solutions
```json
{
  "name": "NutriHealth Solutions",
  "phone": "+963-11-555-7777",
  "address": "Kafr Sousa, Damascus, Syria",
  "preferredCurrency": "SYP"
}
```

### Headers Required

```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

---

## 👥 Customers

**Endpoint:** `POST /api/v1/customers`

**Description:** Creates customer records for sales management.

### Request Body Structure

```json
{
  "name": "Mohammed Al-Ahmad",
  "phoneNumber": "1112223333",
  "address": "Midan, Damascus, Syria",
  "notes": "Regular customer"
}
```

### Complete Examples

#### 1. Mohammed Al-Ahmad
```json
{
  "name": "Mohammed Al-Ahmad",
  "phoneNumber": "1112223333",
  "address": "Midan, Damascus, Syria",
  "notes": "Regular customer - prefers cash payments"
}
```

#### 2. Aisha Al-Hassan
```json
{
  "name": "Aisha Al-Hassan",
  "phoneNumber": "3334445555",
  "address": "Sarouja, Damascus, Syria",
  "notes": "Prefers credit payments - good payment history"
}
```

#### 3. Khalid Al-Rashid
```json
{
  "name": "Khalid Al-Rashid",
  "phoneNumber": "5556667777",
  "address": "Bab Tuma, Damascus, Syria",
  "notes": "Bulk purchases - corporate customer"
}
```

### Headers Required

```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

---

## 👨‍💼 Employees

**Endpoint:** `POST /api/v1/employees`

**Description:** Creates a new employee in the pharmacy with working hours configuration.

### Request Body Structure

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "password": "Password!1",
  "phoneNumber": "1234567890",
  "status": "ACTIVE",
  "dateOfHire": "2024-01-15",
  "roleId": 2,
  "workingHoursRequests": [
    {
      "daysOfWeek": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
      "shifts": [
        {
          "startTime": "09:00",
          "endTime": "17:00",
          "description": "Regular Shift"
        }
      ]
    },
    {
      "daysOfWeek": ["SATURDAY"],
      "shifts": [
        {
          "startTime": "10:00",
          "endTime": "14:00",
          "description": "Weekend Shift"
        }
      ]
    }
  ]
}
```

### Complete Examples

#### 1. Full-Time Employee with Regular Hours
```json
{
  "firstName": "Ahmed",
  "lastName": "Al-Hassan",
  "password": "SecurePass123!",
  "phoneNumber": "1112223333",
  "status": "ACTIVE",
  "dateOfHire": "2024-01-15",
  "roleId": 2,
  "workingHoursRequests": [
    {
      "daysOfWeek": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
      "shifts": [
        {
          "startTime": "08:00",
          "endTime": "16:00",
          "description": "Morning Shift"
        }
      ]
    }
  ]
}
```

#### 2. Part-Time Employee with Multiple Shifts
```json
{
  "firstName": "Fatima",
  "lastName": "Al-Rashid",
  "password": "MyPassword456!",
  "phoneNumber": "4445556666",
  "status": "ACTIVE",
  "dateOfHire": "2024-02-01",
  "roleId": 3,
  "workingHoursRequests": [
    {
      "daysOfWeek": ["MONDAY", "WEDNESDAY", "FRIDAY"],
      "shifts": [
        {
          "startTime": "09:00",
          "endTime": "13:00",
          "description": "Morning Part-Time"
        },
        {
          "startTime": "14:00",
          "endTime": "18:00",
          "description": "Afternoon Part-Time"
        }
      ]
    },
    {
      "daysOfWeek": ["SATURDAY"],
      "shifts": [
        {
          "startTime": "10:00",
          "endTime": "16:00",
          "description": "Weekend Shift"
        }
      ]
    }
  ]
}
```

#### 3. Manager with Flexible Hours
```json
{
  "firstName": "Mohammed",
  "lastName": "Al-Ahmad",
  "password": "ManagerPass789!",
  "phoneNumber": "7778889999",
  "status": "ACTIVE",
  "dateOfHire": "2024-01-01",
  "roleId": 1,
  "workingHoursRequests": [
    {
      "daysOfWeek": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
      "shifts": [
        {
          "startTime": "07:00",
          "endTime": "15:00",
          "description": "Early Shift"
        }
      ]
    },
    {
      "daysOfWeek": ["SATURDAY"],
      "shifts": [
        {
          "startTime": "09:00",
          "endTime": "13:00",
          "description": "Weekend Management"
        }
      ]
    }
  ]
}
```

### Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `language` | String | No | "ar" | Language code (ar/en) |

### Headers Required

```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

---

## 📝 Field Descriptions

### Purchase Order Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `supplierId` | Long | Yes | ID of the supplier | 1 |
| `currency` | Currency | Yes | Currency for the order | "USD", "EUR", "SYP" |
| `items` | List | Yes | List of products to purchase | Array of items |

#### Purchase Order Item Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `productId` | Long | Yes | ID of the product | 1 |
| `quantity` | Integer | Yes | Quantity to purchase | 100 |
| `price` | Double | Yes | Unit price | 5.50 |
| `barcode` | String | No | Product barcode | "1234567890123" |
| `productType` | ProductType | Yes | Type of product | "MASTER", "PHARMACY" |

### Purchase Invoice Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `purchaseOrderId` | Long | Yes | ID of the purchase order | 1 |
| `supplierId` | Long | Yes | ID of the supplier | 1 |
| `currency` | Currency | Yes | Currency for the invoice | "USD", "EUR" |
| `total` | Double | Yes | Total invoice amount | 550.00 |
| `invoiceNumber` | String | No | Invoice number | "INV-2024-001" |
| `paymentMethod` | PaymentMethod | No | Payment method (defaults to CASH) | "CASH", "BANK_ACCOUNT", "CHECK" |
| `items` | List | Yes | List of received items | Array of items |

#### Purchase Invoice Item Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `productId` | Long | Yes | ID of the product | 1 |
| `receivedQty` | Integer | Yes | Quantity received | 100 |
| `bonusQty` | Integer | No | Bonus quantity | 10 |
| `invoicePrice` | Double | Yes | Invoice price per unit | 5.50 |
| `batchNo` | String | Yes | Batch number | "BATCH001" |
| `expiryDate` | LocalDate | Yes | Expiry date (YYYY-MM-DD) | "2025-12-31" |
| `productType` | ProductType | Yes | Type of product | "MASTER", "PHARMACY" |
| `sellingPrice` | Double | Conditional | Selling price (required for PHARMACY) | 8.50 |
| `minStockLevel` | Integer | No | Minimum stock level | 10 |

### Sale Invoice Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `customerId` | Long | Yes | ID of the customer | 1 |
| `paymentType` | PaymentType | Yes | Type of payment | "CASH", "CREDIT" |
| `paymentMethod` | PaymentMethod | Yes | Method of payment | "CASH", "BANK_ACCOUNT" |
| `currency` | Currency | No | Currency (defaults to SYP) | "SYP", "USD" |
| `invoiceDiscountType` | DiscountType | No | Type of discount | "PERCENTAGE", "FIXED_AMOUNT" |
| `invoiceDiscountValue` | Float | No | Value of discount | 10.0 (%), 50.0 (fixed) |
| `paidAmount` | Float | No | Amount paid (null for auto-calculate) | null |
| `debtDueDate` | LocalDate | Conditional | Due date for credit sales | "2024-12-31" |
| `items` | List | Yes | List of items sold | Array of items |

#### Sale Invoice Item Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `stockItemId` | Long | Yes | ID of the stock item | 1 |
| `quantity` | Integer | Yes | Quantity sold (1-10000) | 2 |
| `unitPrice` | Float | No | Unit price (uses stock price if not provided) | 800.0 |

### Master Product Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `tradeName` | String | Yes | Trade name of the product | "Paracetamol" |
| `scientificName` | String | Yes | Scientific name of the product | "Acetaminophen" |
| `concentration` | String | Yes | Product concentration | "500mg" |
| `size` | String | Yes | Product size/packaging | "20 tablets" |
| `refPurchasePrice` | Float | Yes | Reference purchase price | 150.0 |
| `refSellingPrice` | Float | Yes | Reference selling price | 300.0 |
| `notes` | String | No | Additional notes | "Pain relief medication" |
| `tax` | Float | No | Tax percentage | 5.0 |
| `barcode` | String | Yes | Product barcode | "1234567890123" |
| `requiresPrescription` | Boolean | No | Requires prescription (default: false) | true/false |
| `typeId` | Long | No | Product type ID | 1 |
| `formId` | Long | No | Product form ID | 1 |
| `manufacturerId` | Long | No | Manufacturer ID | 1 |
| `categoryIds` | Set<Long> | No | Category IDs | [1, 2] |
| `translations` | Array | No | Multilingual translations | `[{"tradeName": "باراسيتامول", "scientificName": "أسيتامينوفين", "lang": "ar"}]` |

### Pharmacy Product Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `tradeName` | String | Yes | Trade name of the product | "Paracetamol" |
| `scientificName` | String | No | Scientific name of the product | "Acetaminophen" |
| `concentration` | String | No | Product concentration | "500mg" |
| `size` | String | Yes | Product size/packaging | "20 tablets" |
| `notes` | String | No | Additional notes | "Pain relief medication" |
| `tax` | Float | No | Tax percentage | 5.0 |
| `barcodes` | Set<String> | Yes | Product barcodes | ["1234567890123"] |
| `requiresPrescription` | Boolean | No | Requires prescription (default: false) | true/false |
| `typeId` | Long | No | Product type ID | 1 |
| `formId` | Long | Yes | Product form ID | 1 |
| `manufacturerId` | Long | Yes | Manufacturer ID | 1 |
| `categoryIds` | Set<Long> | No | Category IDs | [1, 2] |
| `translations` | Array | No | Multilingual translations | `[{"tradeName": "باراسيتامول", "scientificName": "أسيتامينوفين", "lang": "ar"}]` |

### Supplier Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `name` | String | No | Supplier company name | "PharmaCorp International" |
| `phone` | String | No | Phone number | "+963-11-123-4567" |
| `address` | String | No | Full address | "Al-Mazzeh, Damascus, Syria" |
| `preferredCurrency` | Currency | No | Preferred currency | "USD", "EUR", "GBP", "SAR", "AED" |

### Customer Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `name` | String | Yes | Customer full name | "Mohammed Al-Ahmad" |
| `phoneNumber` | String | No | Phone number (10 digits) | "1112223333" |
| `address` | String | No | Customer address | "Midan, Damascus, Syria" |
| `notes` | String | No | Additional notes | "Regular customer" |

### Employee Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `firstName` | String | Yes | Employee's first name | "John" |
| `lastName` | String | Yes | Employee's last name | "Doe" |
| `password` | String | Yes | Employee's password (must meet security requirements) | "Password!1" |
| `phoneNumber` | String | No | Employee's phone number | "1234567890" |
| `status` | UserStatus | No | Employee's status (default: ACTIVE) | "ACTIVE", "INACTIVE" |
| `dateOfHire` | LocalDate | No | Employee's hire date (YYYY-MM-DD) | "2024-01-15" |
| `roleId` | Long | No | Role ID for the employee | 1, 2, 3 |
| `workingHoursRequests` | List | No | Working hours configuration | Array of working hours |

#### Working Hours Request Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `daysOfWeek` | List<DayOfWeek> | Yes | Days to apply the same working hours | ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"] |
| `shifts` | List<WorkShiftDTO> | Yes | Shifts to apply to specified days | Array of shift objects |

#### Work Shift Fields

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `startTime` | LocalTime | Yes | Start time of the shift (HH:mm) | "09:00" |
| `endTime` | LocalTime | Yes | End time of the shift (HH:mm) | "17:00" |
| `description` | String | No | Description of the shift | "Regular Shift" |

---

## ✅ Validation Rules

### Purchase Order Validation

- **supplierId**: Must exist in database
- **currency**: Must be one of: USD, EUR, GBP, SAR, AED, SYP
- **items**: Must contain at least one item
- **productId**: Must exist in database
- **quantity**: Must be greater than 0
- **price**: Must be greater than 0
- **productType**: Must be either "MASTER" or "PHARMACY"

### Purchase Invoice Validation

- **purchaseOrderId**: Must exist and be in PENDING status
- **supplierId**: Must match the purchase order supplier
- **currency**: Must be one of: USD, EUR, GBP, SAR, AED
- **total**: Should match the calculated total from items
- **items**: Must contain at least one item
- **receivedQty**: Must be greater than 0
- **bonusQty**: Must be 0 or greater
- **invoicePrice**: Must be greater than 0
- **expiryDate**: Must be a future date
- **sellingPrice**: Required for PHARMACY products, optional for MASTER products
- **minStockLevel**: Must be 0 or greater

### Sale Invoice Validation

- **customerId**: Must exist in database
- **paymentType**: Must be either "CASH" or "CREDIT"
- **paymentMethod**: Must be either "CASH" or "BANK_ACCOUNT"
- **currency**: Must be either "SYP" or "USD"
- **invoiceDiscountValue**: 
  - For PERCENTAGE: Must be 0-100
  - For FIXED_AMOUNT: Must be 0 or greater
- **debtDueDate**: Required when paymentType is "CREDIT"
- **items**: Must contain at least one item
- **stockItemId**: Must exist and have sufficient stock
- **quantity**: Must be between 1 and 10000

### Master Product Validation

- **tradeName**: Must not be blank
- **scientificName**: Must not be blank
- **concentration**: Must not be blank
- **size**: Must not be blank
- **refPurchasePrice**: Must be >= 0
- **refSellingPrice**: Must be >= 0
- **tax**: Must be >= 0
- **barcode**: Must not be blank
- **requiresPrescription**: Boolean value (default: false)
- **translations**: Optional array of translation objects
- **translations[].tradeName**: Must not be blank if provided
- **translations[].scientificName**: Must not be blank if provided
- **translations[].lang**: Language code (ar, en, etc.)

### Pharmacy Product Validation

- **tradeName**: Must not be blank
- **size**: Must not be blank
- **barcodes**: Must not be null, at least one barcode required
- **formId**: Must not be null
- **manufacturerId**: Must not be null
- **requiresPrescription**: Boolean value (default: false)
- **translations**: Optional array of translation objects
- **translations[].tradeName**: Must not be blank if provided
- **translations[].scientificName**: Must not be blank if provided
- **translations[].lang**: Language code (ar, en, etc.)

### Supplier Validation

- **name**: Optional string
- **phone**: Optional string
- **address**: Optional string
- **preferredCurrency**: Must be one of: USD, EUR, GBP, SAR, AED

### Customer Validation

- **name**: Must not be blank
- **phoneNumber**: Must be exactly 10 digits (if provided)
- **address**: Optional string
- **notes**: Optional string

### Employee Validation

- **firstName**: Must not be blank
- **lastName**: Must not be blank
- **password**: Must meet password security requirements (minimum length, complexity)
- **phoneNumber**: Optional string
- **status**: Must be either "ACTIVE" or "INACTIVE" (default: ACTIVE)
- **dateOfHire**: Must be a valid date in YYYY-MM-DD format
- **roleId**: Must exist in database (if provided)
- **workingHoursRequests**: Optional array of working hours configurations
- **workingHoursRequests[].daysOfWeek**: Must contain valid day names (MONDAY, TUESDAY, etc.)
- **workingHoursRequests[].shifts**: Must contain at least one shift
- **workingHoursRequests[].shifts[].startTime**: Must be valid time in HH:mm format
- **workingHoursRequests[].shifts[].endTime**: Must be valid time in HH:mm format and after startTime
- **workingHoursRequests[].shifts[].description**: Optional string

---

## 🚨 Common Error Scenarios

### HTTP Status Codes

| Status | Description | Common Causes |
|--------|-------------|---------------|
| `200` | Success | Request processed successfully |
| `400` | Bad Request | Invalid data, validation errors |
| `401` | Unauthorized | Missing or invalid JWT token |
| `403` | Forbidden | Insufficient permissions |
| `404` | Not Found | Resource not found |
| `409` | Conflict | Business rule violation |
| `500` | Internal Server Error | Server-side error |

### Purchase Order Errors

| Error | Status | Cause | Solution |
|-------|--------|-------|----------|
| Invalid supplier ID | 400 | Supplier doesn't exist | Use valid supplier ID |
| Empty items list | 400 | No items provided | Add at least one item |
| Invalid product type | 400 | Product type not recognized | Use "MASTER" or "PHARMACY" |
| Negative quantity | 400 | Quantity less than 1 | Use positive quantity |
| Negative price | 400 | Price less than 0 | Use positive price |

### Purchase Invoice Errors

| Error | Status | Cause | Solution |
|-------|--------|-------|----------|
| Purchase order not found | 400 | Invalid purchase order ID | Use valid purchase order ID |
| Order not in PENDING status | 409 | Order already processed | Use order with PENDING status |
| Supplier mismatch | 400 | Supplier doesn't match order | Use correct supplier ID |
| Invalid expiry date | 400 | Past expiry date | Use future date |
| Empty items list | 400 | No items provided | Add at least one item |

### Sale Invoice Errors

| Error | Status | Cause | Solution |
|-------|--------|-------|----------|
| Customer not found | 400 | Invalid customer ID | Use valid customer ID |
| Insufficient stock | 409 | Not enough stock available | Reduce quantity or choose different item |
| Invalid discount value | 400 | Discount exceeds limits | Use valid discount value |
| Missing debt due date | 400 | Credit sale without due date | Add debt due date for credit sales |
| Invalid payment amount | 400 | Payment amount validation failed | Use valid payment amount |

### Employee Errors

| Error | Status | Cause | Solution |
|-------|--------|-------|----------|
| Invalid password format | 400 | Password doesn't meet security requirements | Use strong password with required criteria |
| Invalid role ID | 400 | Role doesn't exist | Use valid role ID |
| Invalid status | 400 | Status not recognized | Use "ACTIVE" or "INACTIVE" |
| Invalid date format | 400 | Date not in YYYY-MM-DD format | Use correct date format |
| Invalid time format | 400 | Time not in HH:mm format | Use correct time format |
| End time before start time | 400 | Shift end time is before start time | Ensure end time is after start time |
| Invalid day of week | 400 | Day name not recognized | Use valid day names (MONDAY, TUESDAY, etc.) |
| Access denied | 403 | Insufficient permissions | Requires PHARMACY_MANAGER role |

---

## 🧪 Testing Examples

### cURL Examples

#### 1. Create Purchase Order
```bash
curl -X POST "http://localhost:8080/api/v1/purchase-orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "supplierId": 1,
    "currency": "USD",
    "items": [
      {
        "productId": 1,
        "quantity": 100,
        "price": 5.50,
        "barcode": "1234567890123",
        "productType": "MASTER"
      }
    ]
  }'
```

#### 2. Create Purchase Invoice
```bash
curl -X POST "http://localhost:8080/api/v1/purchase-invoices" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "purchaseOrderId": 1,
    "supplierId": 1,
    "currency": "USD",
    "total": 550.00,
    "invoiceNumber": "INV-2024-001",
    "paymentMethod": "CASH",
    "items": [
      {
        "productId": 1,
        "receivedQty": 100,
        "bonusQty": 10,
        "invoicePrice": 5.50,
        "batchNo": "BATCH001",
        "expiryDate": "2025-12-31",
        "productType": "MASTER",
        "sellingPrice": 8.50,
        "minStockLevel": 10
      }
    ]
  }'
```

#### 3. Create Sale Invoice
```bash
curl -X POST "http://localhost:8080/api/v1/sales" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "customerId": 1,
    "paymentType": "CASH",
    "paymentMethod": "CASH",
    "currency": "SYP",
    "invoiceDiscountType": "PERCENTAGE",
    "invoiceDiscountValue": 10.0,
    "paidAmount": null,
    "debtDueDate": null,
    "items": [
      {
        "stockItemId": 1,
        "quantity": 2,
        "unitPrice": 800.0
      }
    ]
  }'
```

#### 4. Create Master Product
```bash
curl -X POST "http://localhost:8080/api/v1/products/master" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "tradeName": "Paracetamol",
    "scientificName": "Acetaminophen",
    "concentration": "500mg",
    "size": "20 tablets",
    "refPurchasePrice": 150.0,
    "refSellingPrice": 300.0,
    "notes": "Pain relief and fever reducer medication",
    "tax": 5.0,
    "barcode": "1234567890123",
    "requiresPrescription": false,
    "typeId": 1,
    "formId": 1,
    "manufacturerId": 1,
    "categoryIds": [1, 2],
    "translations": [
      {
        "tradeName": "باراسيتامول",
        "scientificName": "أسيتامينوفين",
        "lang": "ar"
      }
    ]
  }'
```

#### 5. Create Pharmacy Product
```bash
curl -X POST "http://localhost:8080/api/v1/products/pharmacy" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "tradeName": "Paracetamol",
    "scientificName": "Acetaminophen",
    "concentration": "500mg",
    "size": "20 tablets",
    "notes": "Fast-moving pain relief medication",
    "tax": 5.0,
    "barcodes": ["1234567890123"],
    "requiresPrescription": false,
    "typeId": 1,
    "formId": 1,
    "manufacturerId": 1,
    "categoryIds": [1, 2],
    "translations": [
      {
        "tradeName": "باراسيتامول",
        "scientificName": "أسيتامينوفين",
        "lang": "ar"
      }
    ]
  }'
```

#### 6. Create Supplier
```bash
curl -X POST "http://localhost:8080/api/v1/suppliers" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "PharmaCorp International",
    "phone": "+963-11-123-4567",
    "address": "Al-Mazzeh, Damascus, Syria",
    "preferredCurrency": "USD"
  }'
```

#### 7. Create Customer
```bash
curl -X POST "http://localhost:8080/api/v1/customers" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Mohammed Al-Ahmad",
    "phoneNumber": "1112223333",
    "address": "Midan, Damascus, Syria",
    "notes": "Regular customer - prefers cash payments"
  }'
```

#### 8. Create Employee
```bash
curl -X POST "http://localhost:8080/api/v1/employees" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "firstName": "Ahmed",
    "lastName": "Al-Hassan",
    "password": "SecurePass123!",
    "phoneNumber": "1112223333",
    "status": "ACTIVE",
    "dateOfHire": "2024-01-15",
    "roleId": 2,
    "workingHoursRequests": [
      {
        "daysOfWeek": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
        "shifts": [
          {
            "startTime": "08:00",
            "endTime": "16:00",
            "description": "Morning Shift"
          }
        ]
      },
      {
        "daysOfWeek": ["SATURDAY"],
        "shifts": [
          {
            "startTime": "10:00",
            "endTime": "14:00",
            "description": "Weekend Shift"
          }
        ]
      }
    ]
  }'
```

### Postman Examples

#### Environment Variables
```json
{
  "base_url": "http://localhost:8080",
  "jwt_token": "YOUR_JWT_TOKEN_HERE",
  "supplier_id": "1",
  "customer_id": "1",
  "product_id": "1",
  "stock_item_id": "1",
  "employee_id": "1",
  "role_id": "2"
}
```

#### Collection Variables
```json
{
  "supplierId": "{{supplier_id}}",
  "customerId": "{{customer_id}}",
  "productId": "{{product_id}}",
  "stockItemId": "{{stock_item_id}}",
  "employeeId": "{{employee_id}}",
  "roleId": "{{role_id}}"
}
```

---

## 🔐 Authentication & Permissions

### Required JWT Token
All endpoints require a valid JWT token in the Authorization header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Required Roles
- **Purchase Orders**: PHARMACY_MANAGER or EMPLOYEE
- **Purchase Invoices**: PHARMACY_MANAGER or EMPLOYEE
- **Sales**: PHARMACY_MANAGER
- **Employee Management**: PHARMACY_MANAGER

### Pharmacy Scoping
- All operations are automatically scoped to the current user's pharmacy
- Users cannot access data from other pharmacies

---

## 📊 Response Examples

### Successful Purchase Order Response
```json
{
  "id": 1,
  "supplierId": 1,
  "currency": "USD",
  "total": 550.00,
  "status": "PENDING",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "quantity": 100,
      "price": 5.50,
      "productType": "MASTER"
    }
  ],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Successful Purchase Invoice Response
```json
{
  "id": 1,
  "purchaseOrderId": 1,
  "supplierId": 1,
  "currency": "USD",
  "total": 550.00,
  "invoiceNumber": "INV-2024-001",
  "paymentMethod": "CASH",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "receivedQty": 100,
      "bonusQty": 10,
      "invoicePrice": 5.50,
      "batchNo": "BATCH001",
      "expiryDate": "2025-12-31",
      "productType": "MASTER",
      "sellingPrice": 8.50,
      "minStockLevel": 10
    }
  ],
  "createdAt": "2024-01-15T10:30:00"
}
```

### Successful Sale Invoice Response
```json
{
  "id": 1,
  "customerId": 1,
  "paymentType": "CASH",
  "paymentMethod": "CASH",
  "currency": "SYP",
  "totalAmount": 1440.00,
  "discount": 160.00,
  "paidAmount": 1440.00,
  "remainingAmount": 0.00,
  "items": [
    {
      "id": 1,
      "stockItemId": 1,
      "quantity": 2,
      "unitPrice": 800.0,
      "subTotal": 1600.0
    }
  ],
  "createdAt": "2024-01-15T10:30:00"
}
```

### Successful Employee Response
```json
{
  "id": 1,
  "firstName": "Ahmed",
  "lastName": "Al-Hassan",
  "phoneNumber": "1112223333",
  "status": "ACTIVE",
  "dateOfHire": "2024-01-15",
  "roleId": 2,
  "workingHours": [
    {
      "id": 1,
      "dayOfWeek": "MONDAY",
      "startTime": "08:00",
      "endTime": "16:00",
      "description": "Morning Shift"
    },
    {
      "id": 2,
      "dayOfWeek": "TUESDAY",
      "startTime": "08:00",
      "endTime": "16:00",
      "description": "Morning Shift"
    },
    {
      "id": 3,
      "dayOfWeek": "WEDNESDAY",
      "startTime": "08:00",
      "endTime": "16:00",
      "description": "Morning Shift"
    },
    {
      "id": 4,
      "dayOfWeek": "THURSDAY",
      "startTime": "08:00",
      "endTime": "16:00",
      "description": "Morning Shift"
    },
    {
      "id": 5,
      "dayOfWeek": "FRIDAY",
      "startTime": "08:00",
      "endTime": "16:00",
      "description": "Morning Shift"
    },
    {
      "id": 6,
      "dayOfWeek": "SATURDAY",
      "startTime": "10:00",
      "endTime": "14:00",
      "description": "Weekend Shift"
    }
  ],
  "createdAt": "2024-01-15T10:30:00"
}
```

---

## 📚 Additional Resources

- **API Documentation**: Swagger UI available at `/swagger-ui.html`
- **Error Codes**: See response body for detailed error messages
- **Validation**: All requests are validated against business rules
- **Logging**: Check application logs for detailed error information

---

## 🆘 Support

For technical support or questions about the API:
- Check the application logs
- Review the Swagger documentation
- Contact the development team

---

*Last updated: January 2024*
*Version: 1.0*
