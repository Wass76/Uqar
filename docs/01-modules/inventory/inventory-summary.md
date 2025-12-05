# Inventory Module - Summary

## Objective

The **Inventory Module** manages all pharmaceutical products and stock levels for each pharmacy. It solves the critical problem of tracking product quantities, expiry dates, batch numbers, and ensuring accurate inventory counts across multiple pharmacies in the Uqar system.

## Problem Statement

Pharmacies need to:
- Track thousands of products with varying quantities
- Monitor expiry dates to prevent selling expired medications
- Manage batch numbers for traceability
- Handle multiple product types (Master Products vs Pharmacy-Specific Products)
- Maintain accurate stock levels that automatically update with purchases and sales
- Set minimum stock levels to prevent stockouts
- Search and filter products efficiently

## User Roles

### Primary Users

1. **Pharmacy Manager**
   - Full inventory management access
   - Can create/edit/delete products
   - Can adjust stock quantities manually
   - Can set minimum stock levels
   - Can view inventory reports

2. **Pharmacy Employee**
   - Can view inventory
   - Can search products
   - Can check stock availability
   - Limited editing capabilities

3. **Pharmacy Trainee**
   - Read-only access to inventory
   - Can view product details
   - Cannot modify stock

## Core Concepts

### Product Types

The system supports two types of products:

1. **Master Product** (`MasterProduct`)
   - System-wide product catalog
   - Shared across all pharmacies
   - Contains reference prices and standard information
   - Has unique barcode

2. **Pharmacy Product** (`PharmacyProduct`)
   - Pharmacy-specific product
   - Custom pricing per pharmacy
   - Can have multiple barcodes
   - Inherits structure from Master Product but allows customization

### Stock Items

**StockItem** represents actual inventory in the pharmacy:
- Links to either MasterProduct or PharmacyProduct (via `productId` + `productType`)
- Tracks quantity, expiry date, batch number
- Stores actual purchase price (may differ from reference price)
- Links to purchase invoice (tracks source)
- Pharmacy-specific (multi-tenancy)

## Main User Workflows

### 1. Product Registration Workflow

**Scenario**: A pharmacy wants to add a new product to their catalog.

1. **Check Master Catalog**: Employee searches master products to see if product exists
2. **Create Pharmacy Product**: If not found, create pharmacy-specific product
   - Enter trade name, scientific name, concentration, size
   - Set reference purchase and selling prices
   - Set minimum stock level
   - Add categories, type, form, manufacturer
   - Add barcode(s)
3. **Product Saved**: Product is now available for purchase orders

**Outcome**: Product is registered and ready for inventory management.

### 2. Stock Addition Workflow (via Purchase)

**Scenario**: Pharmacy receives a shipment from a supplier.

1. **Create Purchase Order**: Manager creates purchase order with products and quantities
2. **Receive Invoice**: When shipment arrives, create purchase invoice
   - Enter actual quantities received
   - Enter batch numbers and expiry dates
   - Enter actual purchase prices
   - System automatically creates StockItem records
3. **Stock Updated**: Inventory quantities increase automatically
4. **Audit Trail**: Each stock item links to purchase invoice for traceability

**Outcome**: Stock levels updated, products available for sale.

### 3. Stock Search and View Workflow

**Scenario**: Employee needs to find a product and check availability.

1. **Search Products**: Use search endpoint with keyword (name, barcode, trade name)
2. **View Results**: System returns products with aggregated stock information
   - Total quantity across all batches
   - Average purchase price
   - Selling price (in SYP and USD)
   - Product details
3. **View Details**: Click on product to see detailed stock breakdown
   - Individual stock items with batch numbers
   - Expiry dates
   - Quantities per batch
   - Purchase invoice references

**Outcome**: Employee finds product and knows exact stock status.

### 4. Stock Adjustment Workflow

**Scenario**: Manager needs to correct stock quantity (e.g., damaged items, found items).

1. **Select Stock Item**: Manager selects specific stock item to adjust
2. **Edit Quantity**: Update quantity with reason code
3. **Optional**: Update expiry date or minimum stock level
4. **Save Changes**: System updates stock item
5. **Audit Trail**: Change is logged with user and timestamp

**Outcome**: Stock quantity corrected, audit trail maintained.

### 5. Low Stock Monitoring Workflow

**Scenario**: System needs to alert when products are running low.

1. **Automatic Check**: System compares current stock to minimum stock level
2. **Alert Generation**: If stock below minimum, notification can be sent
3. **Manager Review**: Manager reviews low stock items
4. **Purchase Decision**: Manager creates purchase order if needed

**Outcome**: Proactive inventory management prevents stockouts.

### 6. Expired Product Management Workflow

**Scenario**: System identifies expired or expiring products.

1. **Automatic Detection**: System queries stock items with expiry dates
2. **Expired Items**: Products past expiry date are flagged
3. **Expiring Soon**: Products expiring within 6 months are warned
4. **Prevention**: System prevents selling expired products during sales
5. **Removal**: Manager can delete expired stock items

**Outcome**: Expired products are identified and prevented from sale.

## Key Business Rules

1. **Multi-Tenancy**: Each pharmacy only sees and manages their own inventory
2. **Stock Deduction**: Stock is automatically deducted when products are sold
3. **Expiry Validation**: Cannot sell products that are expired
4. **Quantity Validation**: Cannot sell more than available stock
5. **Batch Tracking**: Each stock item maintains batch number and expiry date
6. **Price Tracking**: Actual purchase price is stored per stock item (may differ from reference price)
7. **Audit Trail**: All stock changes are tracked with user and timestamp
8. **Currency Support**: Prices displayed in both SYP (stored) and USD (converted)

## Integration Points

### With Purchase Module
- **Stock Addition**: Purchase invoices automatically create StockItem records
- **Price Updates**: Purchase prices can update product reference prices

### With Sales (POS) Module
- **Stock Deduction**: Sales automatically reduce stock quantities
- **Stock Validation**: Sales check stock availability before processing
- **Expiry Check**: Sales prevent selling expired products

### With MoneyBox Module
- **Inventory Value**: Stock value calculations use purchase prices
- **Financial Tracking**: Inventory purchases are tracked in MoneyBox

## Success Metrics

- **Stock Accuracy**: Real-time stock levels match physical inventory
- **Expiry Management**: Zero expired products sold
- **Stockout Prevention**: Minimum stock levels prevent stockouts
- **Traceability**: All stock items traceable to purchase invoices
- **Search Performance**: Fast product search and retrieval

