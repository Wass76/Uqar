# MoneyBox Module - Technical Documentation

## Data Flow Architecture

### Sale Payment Recording Flow

```
SaleService.createSaleInvoice()
    ↓
If paymentMethod == CASH:
    SalesIntegrationService.recordSalePayment()
        ↓
    EnhancedMoneyBoxAuditService.recordTransaction()
        1. Get MoneyBox for pharmacy
        2. Get current balance
        3. Convert amount to SYP (if USD)
        4. Calculate new balance (balance + amount)
        5. Create MoneyBoxTransaction
           - type = SALE_PAYMENT
           - amount = paidAmount
           - balanceBefore = currentBalance
           - balanceAfter = newBalance
           - referenceId = saleInvoice.id
           - referenceType = SALE
        6. Save transaction
        7. Update MoneyBox balance
        ↓
    MoneyBoxTransaction saved
    MoneyBox balance updated
```

### Purchase Payment Recording Flow

```
PurchaseInvoiceService.create()
    ↓
If paymentMethod == CASH:
    PurchaseIntegrationService.recordPurchasePayment()
        ↓
    EnhancedMoneyBoxAuditService.recordTransaction()
        1. Get MoneyBox for pharmacy
        2. Get current balance
        3. Convert amount to SYP (if USD)
        4. Calculate new balance (balance - amount)
        5. Create MoneyBoxTransaction
           - type = PURCHASE_PAYMENT
           - amount = purchaseAmount
           - balanceBefore = currentBalance
           - balanceAfter = newBalance
           - referenceId = purchaseInvoice.id
           - referenceType = PURCHASE
        6. Save transaction
        7. Update MoneyBox balance
```

### Manual Transaction Flow

```
MoneyBoxController.createTransaction()
    ↓
MoneyBoxService.createTransaction()
    ↓
EnhancedMoneyBoxAuditService.recordTransaction()
    1. Validate transaction type
    2. Get MoneyBox
    3. Get current balance
    4. Convert currency if needed
    5. Calculate new balance based on type:
       - Revenue: balance + amount
       - Expense: balance - amount
       - Adjustment: balance + amount (can be negative)
    6. Create transaction
    7. Update balance
    ↓
Return MoneyBoxTransactionResponseDTO
```

## Key Endpoints

### MoneyBox Endpoints

#### `GET /api/v1/moneybox`
**Purpose**: Get MoneyBox for current pharmacy

**Response**: `MoneyBoxResponseDTO`
- Current balance
- Currency
- Status
- Summary information

**Side Effects**: None (read-only)

**Authorization**: Requires authentication

---

#### `GET /api/v1/moneybox/summary`
**Purpose**: Get financial summary

**Response**: `MoneyBoxSummaryDTO`
- Current balance
- Total revenue (period)
- Total expenses (period)
- Transaction count
- Recent transactions

**Side Effects**: None (read-only)

---

#### `GET /api/v1/moneybox/transactions`
**Purpose**: Get all transactions

**Query Parameters**:
- `startDate` (optional): Filter start date
- `endDate` (optional): Filter end date
- `transactionType` (optional): Filter by type
- `currency` (optional): Filter by currency

**Response**: `List<MoneyBoxTransactionResponseDTO>`

**Side Effects**: None (read-only)

---

#### `POST /api/v1/moneybox/transactions`
**Purpose**: Create manual transaction

**Input Body**: `TransactionRequestDTO`
```json
{
  "transactionType": "CASH_DEPOSIT",
  "amount": 10000,
  "currency": "SYP",
  "description": "Daily cash deposit"
}
```

**Response**: `MoneyBoxTransactionResponseDTO`

**Side Effects**:
- Creates `money_box_transaction` record
- Updates `money_box.current_balance`
- Balance increases (for revenue) or decreases (for expense)

**Authorization**: Requires `PHARMACY_MANAGER` role

---

#### `GET /api/v1/moneybox/transactions/{id}`
**Purpose**: Get transaction by ID

**Response**: `MoneyBoxTransactionResponseDTO`

**Side Effects**: None (read-only)

---

### Exchange Rate Endpoints

#### `GET /api/v1/exchange-rates`
**Purpose**: Get current exchange rates

**Response**: `List<ExchangeRateResponseDTO>`

**Side Effects**: None (read-only)

---

#### `POST /api/v1/exchange-rates`
**Purpose**: Create/update exchange rate

**Input Body**: `ExchangeRateRequestDTO`
```json
{
  "fromCurrency": "USD",
  "toCurrency": "SYP",
  "rate": 15000,
  "effectiveDate": "2024-01-15"
}
```

**Response**: `ExchangeRateResponseDTO`

**Side Effects**:
- Creates or updates `exchange_rate` record
- Used for future currency conversions

**Authorization**: Requires `PHARMACY_MANAGER` role

---

## Service Layer Components

### MoneyBoxService

**Location**: `com.Uqar.moneybox.service.MoneyBoxService`

**Key Methods**:

1. **`getMoneyBox()`**
   - Gets MoneyBox for current pharmacy
   - Returns response with balance

2. **`getSummary()`**
   - Calculates financial summary
   - Aggregates transactions
   - Returns summary DTO

3. **`getTransactions(...)`**
   - Gets transactions with filters
   - Returns list of transactions

4. **`createTransaction(TransactionRequestDTO)`**
   - Validates transaction
   - Records transaction via EnhancedMoneyBoxAuditService
   - Returns transaction response

**Extends**: `BaseSecurityService`

---

### EnhancedMoneyBoxAuditService

**Location**: `com.Uqar.moneybox.service.EnhancedMoneyBoxAuditService`

**Purpose**: Core service for recording all MoneyBox transactions with comprehensive audit trail

**Key Methods**:

1. **`recordTransaction(...)`**
   - Main method for recording transactions
   - Handles currency conversion
   - Calculates balance
   - Creates transaction record
   - Updates MoneyBox balance
   - Returns new balance

2. **`calculateNewBalance(BigDecimal currentBalance, BigDecimal amount, TransactionType type)`**
   - Calculates new balance based on transaction type
   - Revenue transactions: balance + amount
   - Expense transactions: balance - amount
   - Adjustments: balance + amount (can be negative)

3. **`isRevenueTransaction(TransactionType type)`**
   - Checks if transaction increases balance

4. **`isExpenseTransaction(TransactionType type)`**
   - Checks if transaction decreases balance

**Key Features**:
- Comprehensive audit trail
- Balance validation
- Currency conversion
- Reference tracking

---

### SalesIntegrationService

**Location**: `com.Uqar.moneybox.service.SalesIntegrationService`

**Purpose**: Integrates sales with MoneyBox

**Key Methods**:

1. **`recordSalePayment(SaleInvoice invoice, Currency currency)`**
   - Records cash payment from sale
   - Calls EnhancedMoneyBoxAuditService
   - Type = SALE_PAYMENT

---

### PurchaseIntegrationService

**Location**: `com.Uqar.moneybox.service.PurchaseIntegrationService`

**Purpose**: Integrates purchases with MoneyBox

**Key Methods**:

1. **`recordPurchasePayment(PurchaseInvoice invoice, Currency currency)`**
   - Records cash payment for purchase
   - Calls EnhancedMoneyBoxAuditService
   - Type = PURCHASE_PAYMENT

---

### ExchangeRateService

**Location**: `com.Uqar.moneybox.service.ExchangeRateService`

**Key Methods**:

1. **`getCurrentRate(Currency from, Currency to)`**
   - Gets current exchange rate
   - Returns rate for conversion

2. **`createOrUpdateRate(ExchangeRateRequestDTO)`**
   - Creates or updates exchange rate
   - Sets effective date

---

## Repository Layer

### MoneyBoxRepository

**Key Methods**:
```java
Optional<MoneyBox> findByPharmacyId(Long pharmacyId);
MoneyBox findByPharmacyIdOrCreate(Long pharmacyId);
```

---

### MoneyBoxTransactionRepository

**Key Methods**:
```java
List<MoneyBoxTransaction> findByMoneyBoxId(Long moneyBoxId);
List<MoneyBoxTransaction> findByMoneyBoxIdAndTransactionDateBetween(Long moneyBoxId, LocalDateTime start, LocalDateTime end);
List<MoneyBoxTransaction> findByMoneyBoxIdAndTransactionType(Long moneyBoxId, TransactionType type);
List<MoneyBoxTransaction> findByReferenceIdAndReferenceType(String referenceId, String referenceType);
```

---

### ExchangeRateRepository

**Key Methods**:
```java
Optional<ExchangeRate> findByFromCurrencyAndToCurrencyAndEffectiveDate(Currency from, Currency to, LocalDate date);
ExchangeRate findLatestByFromCurrencyAndToCurrency(Currency from, Currency to);
```

---

## Entity Relationships

### MoneyBox Entity

```java
@Entity
public class MoneyBox extends AuditedEntity {
    private BigDecimal currentBalance;
    private Currency currency;
    private MoneyBoxStatus status;
    
    @ManyToOne
    private Pharmacy pharmacy;  // One per pharmacy
    
    @OneToMany
    private List<MoneyBoxTransaction> transactions;
}
```

---

### MoneyBoxTransaction Entity

```java
@Entity
public class MoneyBoxTransaction extends AuditedEntity {
    private TransactionType transactionType;
    private BigDecimal amount;
    private Currency originalCurrency;
    private BigDecimal originalAmount;
    private Currency convertedCurrency;
    private BigDecimal convertedAmount;
    private BigDecimal exchangeRate;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;
    private String referenceId;  // Links to sale/purchase ID
    private String referenceType;  // SALE, PURCHASE, etc.
    private OperationStatus operationStatus;
    
    @ManyToOne
    private MoneyBox moneyBox;
}
```

---

## Dependencies

### Internal Dependencies

1. **Sales Module**
   - `SalesIntegrationService` records sale payments

2. **Purchase Module**
   - `PurchaseIntegrationService` records purchase payments

3. **User Module**
   - `Pharmacy` - MoneyBox belongs to pharmacy
   - `BaseSecurityService` - Security utilities

### External Dependencies

- **Spring Data JPA**: Database access
- **BigDecimal**: Precise decimal calculations
- **MapStruct**: DTO mapping

---

## Database Queries

### Transaction Recording

```sql
INSERT INTO money_box_transaction (
    money_box_id, transaction_type, amount, 
    original_currency, original_amount, 
    converted_currency, converted_amount, 
    exchange_rate, balance_before, balance_after, 
    description, reference_id, reference_type, 
    operation_status, created_at, created_by
) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

UPDATE money_box 
SET current_balance = ? 
WHERE id = ?;
```

### Balance Calculation

```sql
SELECT COALESCE(SUM(
    CASE 
        WHEN transaction_type IN ('SALE_PAYMENT', 'DEBT_PAYMENT', 'CASH_DEPOSIT', 'INCOME', 'TRANSFER_IN', 'OPENING_BALANCE')
        THEN converted_amount
        ELSE -converted_amount
    END
), 0) as current_balance
FROM money_box_transaction
WHERE money_box_id = ?;
```

---

## Error Handling

### Common Exceptions

1. **`EntityNotFoundException`**: 
   - MoneyBox not found
   - Exchange rate not found

2. **`RequestNotValidException`**: 
   - Invalid transaction amount
   - Invalid transaction type
   - Insufficient balance for withdrawal

3. **`ConflictException`**: 
   - Exchange rate already exists for date

---

## Performance Considerations

1. **Indexing**: 
   - `money_box_transaction.money_box_id`
   - `money_box_transaction.transaction_date`
   - `money_box_transaction.transaction_type`
   - `money_box_transaction.reference_id` + `reference_type`

2. **Balance Calculation**: 
   - Balance stored in MoneyBox (not calculated each time)
   - Validated against transaction sum periodically

3. **Currency Conversion**: 
   - Exchange rates cached
   - Conversion happens at transaction time

---

## Security Considerations

1. **Multi-Tenancy**: All queries filter by pharmacy
2. **Authorization**: Manual transactions require manager role
3. **Audit Trail**: All transactions immutable (cannot delete)
4. **Balance Validation**: Balance must match transaction sum

---

## Business Logic Details

### Balance Calculation

```java
// Revenue transactions increase balance
if (isRevenueTransaction(type)) {
    newBalance = currentBalance.add(amount.abs());
}
// Expense transactions decrease balance
else if (isExpenseTransaction(type)) {
    newBalance = currentBalance.subtract(amount.abs());
}
// Adjustments can be positive or negative
else if (type == ADJUSTMENT) {
    newBalance = currentBalance.add(amount);  // amount can be negative
}
```

### Currency Conversion

```java
// Get current exchange rate
ExchangeRate rate = exchangeRateService.getCurrentRate(USD, SYP);

// Convert amount
BigDecimal convertedAmount = originalAmount.multiply(rate.getRate());

// Store both currencies
transaction.setOriginalCurrency(USD);
transaction.setOriginalAmount(originalAmount);
transaction.setConvertedCurrency(SYP);
transaction.setConvertedAmount(convertedAmount);
transaction.setExchangeRate(rate.getRate());
```

### Transaction Types

**Revenue** (balance increases):
- SALE_PAYMENT
- DEBT_PAYMENT
- CASH_DEPOSIT
- INCOME
- TRANSFER_IN
- OPENING_BALANCE

**Expense** (balance decreases):
- PURCHASE_PAYMENT
- CASH_WITHDRAWAL
- EXPENSE
- TRANSFER_OUT

**Adjustment** (can be positive or negative):
- ADJUSTMENT

