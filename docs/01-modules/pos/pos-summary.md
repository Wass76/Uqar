# Point of Sale (POS) Module - Summary

## Objective

The **Point of Sale (POS) Module** handles all customer sales transactions in the pharmacy. It solves the critical problem of processing sales quickly and accurately while maintaining inventory accuracy, financial tracking, and customer relationship management.

## Problem Statement

Pharmacies need to:
- Process sales transactions quickly at the point of sale
- Automatically deduct inventory when products are sold
- Handle multiple payment methods (cash, credit, card, transfer)
- Support partial payments and customer debt tracking
- Apply discounts (percentage or fixed amount)
- Prevent selling expired products
- Prevent selling more than available stock
- Process refunds and returns
- Track sales history and generate invoices
- Integrate with financial systems (MoneyBox)

## User Roles

### Primary Users

1. **Pharmacy Manager**
   - Full access to all sales operations
   - Can process sales
   - Can cancel sales
   - Can process refunds
   - Can view all sales reports

2. **Pharmacy Employee**
   - Can process sales
   - Can view sales history
   - Can process refunds (with manager approval in some cases)
   - Cannot cancel sales (typically)

3. **Pharmacy Trainee**
   - Can process sales (under supervision)
   - Can view sales
   - Limited refund capabilities

## Core Concepts

### Sale Invoice

A **SaleInvoice** represents a complete sales transaction:
- Links to a customer (or "cash customer" for walk-ins)
- Contains multiple items (SaleInvoiceItem)
- Has total amount, discount, paid amount, remaining amount
- Tracks payment status (FULLY_PAID, PARTIALLY_PAID, UNPAID)
- Tracks refund status (NO_REFUND, PARTIALLY_REFUNDED, FULLY_REFUNDED)
- Has invoice status (SOLD, CANCELLED, VOID)
- Supports dual currency (SYP/USD)

### Sale Invoice Item

Each **SaleInvoiceItem** represents one product in the sale:
- Links to a specific StockItem (batch)
- Contains quantity sold
- Contains unit price and subtotal
- Tracks refunded quantity (for partial refunds)

### Payment Types

1. **CASH**: Full payment required immediately
2. **CREDIT**: Customer can pay later (debt tracking)

### Payment Methods

1. **CASH**: Physical cash payment
2. **CARD**: Card payment
3. **TRANSFER**: Bank transfer

### Discount Types

1. **PERCENTAGE**: Discount as percentage of total
2. **FIXED**: Fixed amount discount

## Main User Workflows

### 1. Standard Sale Workflow

**Scenario**: Customer wants to buy medications.

1. **Select Customer**: Employee selects customer (or uses "cash customer" for walk-ins)
2. **Add Products**: Employee scans/searches products and adds to cart
   - System checks stock availability
   - System checks expiry dates
   - System shows current prices
3. **Apply Discount** (optional): Employee applies discount if applicable
4. **Select Payment Type**: 
   - **CASH**: Customer pays full amount
   - **CREDIT**: Customer will pay later (debt created)
5. **Process Payment**: 
   - Enter paid amount (if partial payment)
   - Select payment method
6. **Complete Sale**: System processes transaction
   - Creates SaleInvoice
   - Deducts stock quantities
   - Records payment in MoneyBox (if cash)
   - Creates customer debt (if credit)
   - Generates invoice number
7. **Print/Display Invoice**: Customer receives invoice

**Outcome**: Sale completed, inventory updated, financial records updated.

### 2. Credit Sale Workflow

**Scenario**: Regular customer wants to buy on credit.

1. **Select Customer**: Employee selects existing customer
2. **Add Products**: Add products to cart
3. **Select Payment Type**: Choose CREDIT
4. **Process Sale**: System creates sale
   - SaleInvoice created with paymentStatus = UNPAID or PARTIALLY_PAID
   - CustomerDebt record created
   - Stock deducted
   - No MoneyBox transaction (no cash received)
5. **Customer Receives Invoice**: Invoice shows remaining amount

**Outcome**: Sale recorded, customer debt tracked, stock deducted.

### 3. Partial Payment Workflow

**Scenario**: Customer pays part of invoice amount.

1. **Create Sale**: Standard sale process
2. **Enter Paid Amount**: Employee enters amount less than total
3. **System Calculates**: 
   - Remaining amount = Total - Paid
   - Payment status = PARTIALLY_PAID
4. **Process**: 
   - Cash payment recorded in MoneyBox
   - Customer debt created for remaining amount
5. **Future Payment**: Customer can pay remaining amount later

**Outcome**: Partial payment recorded, debt tracked for remaining.

### 4. Sale Cancellation Workflow

**Scenario**: Manager needs to cancel a sale (e.g., customer changed mind).

1. **Select Sale**: Manager finds sale invoice
2. **Cancel Sale**: Manager cancels sale
3. **System Processes**:
   - Restores stock quantities
   - Reverses MoneyBox transaction (if cash was paid)
   - Updates invoice status to CANCELLED
   - Reduces customer debt (if credit sale)
4. **Confirmation**: Sale is cancelled and stock restored

**Outcome**: Sale cancelled, stock restored, financial records reversed.

### 5. Refund Workflow

**Scenario**: Customer wants to return products.

1. **Select Sale**: Employee finds original sale invoice
2. **Select Items**: Employee selects items to refund
3. **Enter Quantities**: For each item, enter quantity to refund
4. **Enter Reason**: Employee enters refund reason
5. **Process Refund**: System processes refund
   - Creates SaleRefund record
   - Restores stock quantities
   - Calculates refund amount
   - Updates invoice refund status
6. **Handle Payment**:
   - **If Cash Sale**: Refund cash (reduces MoneyBox)
   - **If Credit Sale**: Reduces customer debt
   - **If Partial**: Combination of cash refund and debt reduction
7. **Confirmation**: Refund processed, stock restored

**Outcome**: Products returned, stock restored, payment/debt adjusted.

### 6. Full Invoice Refund Workflow

**Scenario**: Customer wants to return entire purchase.

1. **Select Sale**: Find sale invoice
2. **Select All Items**: Select all items for refund
3. **Process Refund**: System processes full refund
   - All items refunded
   - All stock restored
   - Full refund amount calculated
   - Invoice status = FULLY_REFUNDED
4. **Payment Handling**: Full refund processed (cash or debt reduction)

**Outcome**: Complete refund, all stock restored.

### 7. Search Sales History Workflow

**Scenario**: Employee needs to find a previous sale.

1. **Search Options**:
   - Search by date range
   - Search by customer
   - Search by invoice number
2. **View Results**: System displays matching sales
3. **View Details**: Click on sale to see full details
   - Items sold
   - Payment information
   - Refund history

**Outcome**: Sale found and details displayed.

## Key Business Rules

1. **Stock Validation**: Cannot sell more than available stock
2. **Expiry Validation**: Cannot sell expired products
3. **Cash Payment**: Cash sales must be fully paid
4. **Credit Payment**: Credit sales can have remaining amount
5. **Stock Deduction**: Stock is automatically deducted on sale
6. **Stock Restoration**: Stock is restored on cancellation/refund
7. **MoneyBox Integration**: Cash payments automatically recorded in MoneyBox
8. **Debt Tracking**: Credit sales create customer debt records
9. **Refund Limits**: Cannot refund more than original quantity
10. **Multi-Tenancy**: Each pharmacy only sees their own sales

## Integration Points

### With Inventory Module
- **Stock Validation**: Checks stock availability before sale
- **Stock Deduction**: Automatically reduces stock on sale
- **Stock Restoration**: Restores stock on cancellation/refund
- **Expiry Check**: Prevents selling expired products

### With MoneyBox Module
- **Cash Payments**: Records cash receipts in MoneyBox
- **Refunds**: Reduces MoneyBox balance on cash refunds
- **Currency Conversion**: Handles SYP/USD conversion

### With Customer Module
- **Debt Creation**: Creates CustomerDebt records for credit sales
- **Debt Reduction**: Reduces debt on refunds
- **Customer History**: Links sales to customers

### With Purchase Module
- **Price Tracking**: Uses purchase prices for cost calculations
- **Stock Source**: Stock items link to purchase invoices

## Success Metrics

- **Transaction Speed**: Fast sale processing (< 30 seconds)
- **Accuracy**: Zero stock discrepancies
- **Customer Satisfaction**: Quick refund processing
- **Financial Accuracy**: All payments tracked correctly
- **Debt Management**: Accurate customer debt tracking

