# MoneyBox Module - Summary

## Objective

The **MoneyBox Module** provides comprehensive financial management for pharmacies, tracking all cash flow, transactions, and maintaining accurate balances. It solves the critical problem of tracking money in and out of the pharmacy, supporting dual currency (SYP/USD), and providing a complete audit trail of all financial transactions.

## Problem Statement

Pharmacies need to:
- Track cash balance in real-time
- Record all financial transactions (sales, purchases, deposits, withdrawals)
- Support dual currency operations (SYP and USD)
- Maintain exchange rates for currency conversion
- Provide complete audit trail for financial accountability
- Track transaction history with detailed information
- Handle manual adjustments and corrections
- Generate financial summaries and reports
- Integrate with sales and purchase modules automatically

## User Roles

### Primary Users

1. **Pharmacy Manager**
   - Full MoneyBox access
   - Can view all transactions
   - Can make manual deposits/withdrawals
   - Can make adjustments
   - Can view financial summaries
   - Can manage exchange rates

2. **Pharmacy Employee**
   - Can view transactions
   - Can view balance
   - Limited modification access

## Core Concepts

### MoneyBox

A **MoneyBox** represents the cash register/cash box for a pharmacy:
- One MoneyBox per pharmacy
- Tracks current balance
- Supports SYP and USD currencies
- Has status: ACTIVE or INACTIVE
- All transactions affect the balance

### MoneyBox Transaction

A **MoneyBoxTransaction** represents a single financial event:
- Records amount and currency
- Tracks balance before and after
- Links to source (sale, purchase, etc.)
- Has transaction type (SALE_PAYMENT, PURCHASE_PAYMENT, etc.)
- Stores exchange rate used
- Maintains audit trail

### Transaction Types

**Revenue Transactions** (increase balance):
- `SALE_PAYMENT` - Cash received from sales
- `DEBT_PAYMENT` - Customer debt payments
- `CASH_DEPOSIT` - Manual cash deposits
- `INCOME` - Other income
- `TRANSFER_IN` - Money transferred in
- `OPENING_BALANCE` - Initial balance

**Expense Transactions** (decrease balance):
- `PURCHASE_PAYMENT` - Cash paid for purchases
- `CASH_WITHDRAWAL` - Manual cash withdrawals
- `EXPENSE` - Other expenses
- `TRANSFER_OUT` - Money transferred out

**Adjustment Transactions**:
- `ADJUSTMENT` - Manual balance corrections

### Exchange Rate

An **ExchangeRate** tracks currency conversion rates:
- SYP ↔ USD conversion rates
- Effective date for rate validity
- Used for automatic currency conversion
- Can be updated as rates change

## Main User Workflows

### 1. Automatic Sale Payment Recording

**Scenario**: Customer pays cash for a sale.

1. **Sale Processed**: Employee processes sale with cash payment
2. **Automatic Recording**: System automatically:
   - Creates MoneyBoxTransaction
   - Type = SALE_PAYMENT
   - Amount = paid amount
   - Records balance before and after
   - Links to sale invoice
3. **Balance Updated**: MoneyBox balance increases
4. **Audit Trail**: Transaction recorded with full details

**Outcome**: Sale payment automatically tracked, balance updated.

### 2. Automatic Purchase Payment Recording

**Scenario**: Pharmacy pays cash for a purchase.

1. **Purchase Processed**: Manager creates purchase invoice with cash payment
2. **Automatic Recording**: System automatically:
   - Creates MoneyBoxTransaction
   - Type = PURCHASE_PAYMENT
   - Amount = purchase amount
   - Records balance before and after
   - Links to purchase invoice
3. **Balance Updated**: MoneyBox balance decreases
4. **Audit Trail**: Transaction recorded

**Outcome**: Purchase payment tracked, balance updated.

### 3. Manual Cash Deposit Workflow

**Scenario**: Manager deposits cash into MoneyBox.

1. **Select Deposit**: Manager chooses to deposit cash
2. **Enter Amount**: Enter deposit amount and currency
3. **Enter Description**: Optional description
4. **Process Deposit**: System creates transaction
   - Type = CASH_DEPOSIT
   - Balance increases
5. **Confirmation**: Deposit recorded, balance updated

**Outcome**: Cash deposited, balance increased.

### 4. Manual Cash Withdrawal Workflow

**Scenario**: Manager withdraws cash from MoneyBox.

1. **Select Withdrawal**: Manager chooses to withdraw cash
2. **Enter Amount**: Enter withdrawal amount and currency
3. **Enter Description**: Optional description
4. **Process Withdrawal**: System creates transaction
   - Type = CASH_WITHDRAWAL
   - Balance decreases
5. **Confirmation**: Withdrawal recorded, balance updated

**Outcome**: Cash withdrawn, balance decreased.

### 5. Balance Adjustment Workflow

**Scenario**: Manager needs to correct balance (e.g., found discrepancy).

1. **Select Adjustment**: Manager chooses adjustment
2. **Enter Amount**: Enter adjustment amount (positive or negative)
3. **Enter Reason**: Enter reason for adjustment
4. **Process Adjustment**: System creates transaction
   - Type = ADJUSTMENT
   - Balance adjusted
5. **Confirmation**: Adjustment recorded, balance corrected

**Outcome**: Balance corrected, audit trail maintained.

### 6. Currency Conversion Workflow

**Scenario**: Transaction in different currency needs conversion.

1. **Transaction Created**: Sale or purchase in USD
2. **Get Exchange Rate**: System gets current exchange rate
3. **Convert Amount**: 
   - Original amount in USD
   - Converted amount in SYP (for storage)
   - Exchange rate recorded
4. **Store Both**: Transaction stores both currencies
5. **Display**: Can display in either currency

**Outcome**: Transaction recorded with dual currency support.

### 7. Transaction History Review Workflow

**Scenario**: Manager needs to review financial history.

1. **View Transactions**: Manager views transaction list
2. **Filter Options**:
   - Filter by date range
   - Filter by transaction type
   - Filter by currency
3. **View Details**: Click transaction to see details
   - Source reference
   - Balance before/after
   - Exchange rate used
4. **Export**: Can export for accounting

**Outcome**: Complete financial history reviewed.

### 8. Financial Summary Workflow

**Scenario**: Manager needs financial overview.

1. **View Summary**: Manager views MoneyBox summary
2. **Summary Includes**:
   - Current balance (SYP and USD)
   - Total revenue (period)
   - Total expenses (period)
   - Transaction count
   - Recent transactions
3. **Analysis**: Manager analyzes financial health

**Outcome**: Financial overview provided.

## Key Business Rules

1. **One MoneyBox Per Pharmacy**: Each pharmacy has exactly one MoneyBox
2. **Balance Calculation**: Balance = Sum of all transactions
3. **Currency Storage**: All amounts stored in SYP, USD converted
4. **Transaction Immutability**: Transactions cannot be deleted (audit trail)
5. **Automatic Integration**: Sales and purchases automatically create transactions
6. **Balance Validation**: Balance must match sum of transactions
7. **Exchange Rate**: Must have valid exchange rate for conversions
8. **Multi-Tenancy**: Each pharmacy only sees their own MoneyBox
9. **Audit Trail**: All transactions tracked with user and timestamp
10. **Reference Tracking**: Transactions link to source (sale, purchase, etc.)

## Integration Points

### With Sales (POS) Module
- **Sale Payments**: Automatically records cash payments
- **Sale Refunds**: Reduces balance on cash refunds
- **Debt Payments**: Records customer debt payments

### With Purchase Module
- **Purchase Payments**: Automatically records cash payments
- **Purchase Refunds**: Increases balance on purchase refunds

### With Reports Module
- **Financial Reports**: Provides data for financial reports
- **Transaction History**: Used in transaction reports

## Success Metrics

- **Balance Accuracy**: Balance matches actual cash
- **Transaction Completeness**: All financial events recorded
- **Audit Trail**: Complete history of all transactions
- **Currency Accuracy**: Accurate currency conversions
- **Performance**: Fast transaction recording and retrieval

