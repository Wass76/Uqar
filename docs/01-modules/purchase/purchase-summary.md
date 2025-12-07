# Purchase Module - Summary

## Objective

The **Purchase Module** manages the procurement process for pharmacies, from creating purchase orders to receiving goods and updating inventory. It solves the critical problem of tracking supplier orders, managing purchase invoices, and automatically adding products to stock when shipments are received.

## Problem Statement

Pharmacies need to:
- Create and manage purchase orders with suppliers
- Track order status (pending, approved, completed, cancelled)
- Receive shipments and create purchase invoices
- Handle multiple currencies (SYP/USD) in purchases
- Track actual received quantities vs ordered quantities
- Manage bonus quantities from suppliers
- Record batch numbers and expiry dates
- Automatically add received products to inventory
- Track purchase costs for financial reporting
- Set minimum stock levels when receiving products

## User Roles

### Primary Users

1. **Pharmacy Manager**
   - Full purchase management access
   - Can create/edit/cancel purchase orders
   - Can create purchase invoices
   - Can approve/reject orders
   - Can view all purchase history

2. **Pharmacy Employee**
   - Can view purchase orders
   - Can create purchase invoices (when receiving shipments)
   - Limited editing capabilities

## Core Concepts

### Purchase Order

A **PurchaseOrder** represents an intent to purchase from a supplier:
- Links to a specific supplier
- Contains multiple items (PurchaseOrderItem)
- Has total amount and currency
- Has status: PENDING, APPROVED, REJECTED, CANCELLED, DONE
- Created before actual purchase

### Purchase Invoice

A **PurchaseInvoice** represents the actual receipt of goods:
- Links to a PurchaseOrder (when order is fulfilled)
- Contains actual received quantities
- Contains actual purchase prices (may differ from order prices)
- Records batch numbers and expiry dates
- Automatically creates StockItem records
- Updates product reference prices
- Records financial transactions in MoneyBox

### Purchase Order Item

Each **PurchaseOrderItem** represents a product in the order:
- Links to product (MasterProduct or PharmacyProduct)
- Contains ordered quantity
- Contains expected price
- Can be edited before order is completed

### Purchase Invoice Item

Each **PurchaseInvoiceItem** represents a received product:
- Links to product
- Contains received quantity (may differ from ordered)
- Contains bonus quantity (free items from supplier)
- Contains actual purchase price
- Contains batch number and expiry date
- Used to create StockItem records

## Main User Workflows

### 1. Purchase Order Creation Workflow

**Scenario**: Pharmacy needs to order products from a supplier.

1. **Select Supplier**: Manager selects supplier
2. **Add Products**: Manager adds products to order
   - Search/select products
   - Enter quantities needed
   - Enter expected prices
   - Select currency (SYP or USD)
3. **Review Order**: Manager reviews total amount
4. **Create Order**: System creates PurchaseOrder
   - Status = PENDING
   - Order saved
5. **Send to Supplier**: Order can be sent to supplier

**Outcome**: Purchase order created, ready for supplier fulfillment.

### 2. Purchase Order Approval Workflow

**Scenario**: Manager needs to approve a purchase order.

1. **View Order**: Manager views pending order
2. **Review Details**: Check products, quantities, prices
3. **Approve Order**: Manager approves order
   - Status = APPROVED
4. **Order Ready**: Order is ready for supplier processing

**Outcome**: Order approved, supplier can proceed.

### 3. Purchase Invoice Creation Workflow (Receiving Goods)

**Scenario**: Supplier delivers goods, pharmacy needs to record receipt.

1. **Select Order**: Employee selects related purchase order
2. **Create Invoice**: Employee creates purchase invoice
3. **Enter Received Quantities**: For each item:
   - Enter actual received quantity
   - Enter bonus quantity (if any)
   - Enter actual purchase price
   - Enter batch number
   - Enter expiry date
   - Set minimum stock level
4. **Calculate Totals**: System calculates total amount
5. **Select Payment Method**: Choose payment method (CASH, CREDIT, etc.)
6. **Process Invoice**: System processes invoice
   - Creates PurchaseInvoice
   - Creates StockItem records (adds to inventory)
   - Updates product reference prices
   - Records payment in MoneyBox (if cash)
   - Updates order status to DONE
7. **Confirmation**: Invoice created, stock updated

**Outcome**: Goods received, inventory updated, financial records updated.

### 4. Purchase Order Editing Workflow

**Scenario**: Manager needs to modify a purchase order before it's completed.

1. **Select Order**: Manager finds order
2. **Check Status**: Verify order is PENDING or APPROVED
3. **Edit Order**: 
   - Add/remove items
   - Modify quantities
   - Update prices
4. **Save Changes**: System updates order
5. **Confirmation**: Order updated

**Outcome**: Order modified, ready for supplier.

### 5. Purchase Order Cancellation Workflow

**Scenario**: Manager needs to cancel a purchase order.

1. **Select Order**: Manager finds order
2. **Check Status**: Verify order can be cancelled
3. **Cancel Order**: Manager cancels order
   - Status = CANCELLED
4. **Confirmation**: Order cancelled

**Outcome**: Order cancelled, no further processing.

### 6. Purchase History Search Workflow

**Scenario**: Manager needs to find previous purchases.

1. **Search Options**:
   - Search by supplier
   - Search by date range
   - Search by status
   - Search by product
2. **View Results**: System displays matching purchases
3. **View Details**: Click to see full invoice details

**Outcome**: Purchase history found and displayed.

## Key Business Rules

1. **Order Status**: Cannot edit orders that are DONE or CANCELLED
2. **Invoice Creation**: Purchase invoice must link to a purchase order
3. **Stock Addition**: Receiving goods automatically adds to inventory
4. **Price Updates**: Actual purchase prices can update product reference prices
5. **Bonus Quantities**: Bonus items are added to stock at zero cost
6. **Expiry Validation**: Cannot accept products with expired dates
7. **Currency Conversion**: All prices stored in SYP, converted from USD if needed
8. **Multi-Tenancy**: Each pharmacy only sees their own purchases
9. **Financial Tracking**: Cash purchases recorded in MoneyBox
10. **Order Completion**: Creating invoice marks order as DONE

## Integration Points

### With Inventory Module
- **Stock Addition**: Purchase invoices automatically create StockItem records
- **Price Updates**: Purchase prices update product reference prices
- **Minimum Stock Levels**: Can set min stock level when receiving

### With MoneyBox Module
- **Payment Recording**: Cash purchases recorded in MoneyBox
- **Currency Conversion**: Handles SYP/USD conversion
- **Financial Tracking**: All purchase payments tracked

### With Supplier Module
- **Supplier Management**: Links purchases to suppliers
- **Supplier History**: Tracks purchase history per supplier

### With Product Module
- **Product Selection**: Uses MasterProduct and PharmacyProduct
- **Product Updates**: Updates product information from purchases

## Success Metrics

- **Order Accuracy**: Orders match actual received goods
- **Inventory Accuracy**: Stock levels updated correctly
- **Financial Accuracy**: All payments tracked correctly
- **Traceability**: All stock items traceable to purchase invoices
- **Efficiency**: Fast order and invoice processing

