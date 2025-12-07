package com.Uqar.moneybox.service;

import com.Uqar.moneybox.dto.CurrencyConversionResponseDTO;
import com.Uqar.moneybox.dto.ExchangeRateResponseDTO;
import com.Uqar.moneybox.dto.MoneyBoxRequestDTO;
import com.Uqar.moneybox.dto.MoneyBoxResponseDTO;
import com.Uqar.moneybox.dto.MoneyBoxTransactionResponseDTO;
import com.Uqar.moneybox.entity.MoneyBox;
import com.Uqar.moneybox.entity.MoneyBoxTransaction;
import com.Uqar.moneybox.enums.MoneyBoxStatus;
import com.Uqar.moneybox.enums.TransactionType;
import com.Uqar.moneybox.mapper.MoneyBoxMapper;
import com.Uqar.moneybox.repository.MoneyBoxRepository;
import com.Uqar.moneybox.repository.MoneyBoxTransactionRepository;
import com.Uqar.user.Enum.Currency;
import com.Uqar.user.service.BaseSecurityService;
import com.Uqar.utils.annotation.Audited;
import com.Uqar.utils.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.Uqar.moneybox.mapper.ExchangeRateMapper;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.product.dto.PaginationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@Slf4j
public class MoneyBoxService extends BaseSecurityService {
    
    private final MoneyBoxRepository moneyBoxRepository;
    private final MoneyBoxTransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final UserRepository userRepository;
    private final MoneyBoxMapper moneyBoxMapper;
    private final EnhancedMoneyBoxAuditService enhancedAuditService;
    
    // Default exchange rates for production fallback
    private static final BigDecimal DEFAULT_USD_TO_SYP_RATE = new BigDecimal("10000");
    private static final BigDecimal DEFAULT_EUR_TO_SYP_RATE = new BigDecimal("11000");
    
    public MoneyBoxService(MoneyBoxRepository moneyBoxRepository,
                          MoneyBoxTransactionRepository transactionRepository,
                          ExchangeRateService exchangeRateService,
                          com.Uqar.user.repository.UserRepository userRepository,
                          MoneyBoxMapper moneyBoxMapper,
                          EnhancedMoneyBoxAuditService enhancedAuditService) {
        super(userRepository);
        this.moneyBoxRepository = moneyBoxRepository;
        this.transactionRepository = transactionRepository;
        this.exchangeRateService = exchangeRateService;
        this.userRepository = userRepository;
        this.moneyBoxMapper = moneyBoxMapper;
        this.enhancedAuditService = enhancedAuditService;
    }
    
    @Transactional
    @Audited(action = "CREATE_MONEY_BOX", targetType = "MONEY_BOX", includeArgs = false)
    public MoneyBoxResponseDTO createMoneyBox(MoneyBoxRequestDTO request) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        log.info("Creating new money box for pharmacy: {}", currentPharmacyId);
        
        // Check if pharmacy already has a money box
        if (moneyBoxRepository.findByPharmacyId(currentPharmacyId).isPresent()) {
            throw new IllegalStateException("Pharmacy already has a money box");
        }
        
        MoneyBox moneyBox = MoneyBoxMapper.toEntity(request);
        moneyBox.setPharmacyId(currentPharmacyId); // Set pharmacyId from current user context
        moneyBox.setStatus(MoneyBoxStatus.OPEN); // Set to OPEN status
        
        // Always set currency to SYP for consistency
        moneyBox.setCurrency("SYP");
        
        MoneyBox savedMoneyBox = moneyBoxRepository.save(moneyBox);
        
        // Convert initial balance to SYP if it's not already in SYP
        BigDecimal initialBalanceInSYP = request.getInitialBalance();
        Currency requestCurrency = request.getCurrency();
        
        if (requestCurrency != null && !Currency.SYP.equals(requestCurrency)) {
            try {
                initialBalanceInSYP = exchangeRateService.convertToSYP(request.getInitialBalance(), requestCurrency);
                log.info("Converted initial balance from {} {} to {} SYP", 
                        request.getInitialBalance(), requestCurrency, initialBalanceInSYP);
            } catch (Exception e) {
                log.warn("Failed to convert initial balance currency: {}. Using original amount.", e.getMessage());
                // Fallback: use original amount
                initialBalanceInSYP = request.getInitialBalance();
            }
        }
        
        // Update the saved money box with converted balance
        savedMoneyBox.setCurrentBalance(initialBalanceInSYP);
        savedMoneyBox.setInitialBalance(initialBalanceInSYP);
        savedMoneyBox = moneyBoxRepository.save(savedMoneyBox);
        
        // Create opening balance transaction record using enhanced audit service
        enhancedAuditService.recordFinancialOperation(
            savedMoneyBox.getId(),
            TransactionType.OPENING_BALANCE,
            initialBalanceInSYP,
            requestCurrency != null ? requestCurrency : Currency.SYP,
            "Initial money box balance",
            String.valueOf(savedMoneyBox.getId()),
            "MONEYBOX_CREATION",
            getCurrentUser().getId(),
            getCurrentUser().getClass().getSimpleName(),
            null, null, null,
            Map.of("pharmacyId", currentPharmacyId, "initialBalance", initialBalanceInSYP)
        );
        
        log.info("Money box created for pharmacy: {} with initial balance: {} SYP", 
                savedMoneyBox.getPharmacyId(), initialBalanceInSYP);
        
        MoneyBoxResponseDTO response = MoneyBoxMapper.toResponseDTO(savedMoneyBox);
        response.setTotalBalanceInUSD(calculateUSDBalance(savedMoneyBox.getCurrentBalance()));
        response.setTotalBalanceInEUR(calculateEURBalance(savedMoneyBox.getCurrentBalance()));
        
        // Set current exchange rates
        setExchangeRates(response);
        
        return response;
    }
    
    public MoneyBoxResponseDTO getMoneyBoxByCurrentPharmacy() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        MoneyBoxResponseDTO response = MoneyBoxMapper.toResponseDTO(moneyBox);
        
        // Calculate currency conversions in service layer
        response.setTotalBalanceInUSD(calculateUSDBalance(moneyBox.getCurrentBalance()));
        response.setTotalBalanceInEUR(calculateEURBalance(moneyBox.getCurrentBalance()));
        
        // Set current exchange rates
        setExchangeRates(response);
        
        return response;
    }
    
    /**
     * Set current exchange rates in MoneyBoxResponseDTO with fallback to defaults
     */
    private void setExchangeRates(MoneyBoxResponseDTO response) {
        // Get current exchange rates from database with fallback to defaults
        try {
            BigDecimal usdToSypRate = exchangeRateService.getExchangeRate(Currency.USD, Currency.SYP);
            response.setCurrentUSDToSYPRate(usdToSypRate);
        } catch (Exception e) {
            log.warn("Failed to get USD to SYP exchange rate from database, using default: {}", e.getMessage());
            // Use default rate from ExchangeRateService
            response.setCurrentUSDToSYPRate(ExchangeRateService.getDefaultUsdToSypRate());
        }
        
        try {
            BigDecimal eurToSypRate = exchangeRateService.getExchangeRate(Currency.EUR, Currency.SYP);
            response.setCurrentEURToSYPRate(eurToSypRate);
        } catch (Exception e) {
            log.warn("Failed to get EUR to SYP exchange rate from database, using default: {}", e.getMessage());
            // Use default rate from ExchangeRateService
            response.setCurrentEURToSYPRate(ExchangeRateService.getDefaultEurToSypRate());
        }
    }

    /**
     * Calculate USD balance from SYP balance with fallback to default rate
     */
    private BigDecimal calculateUSDBalance(BigDecimal balanceInSYP) {
        try {
            // Try to get current exchange rate from service
            BigDecimal rate = exchangeRateService.getExchangeRate(Currency.SYP, Currency.USD);
            return balanceInSYP.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            // Fallback to default rate if service fails
            BigDecimal defaultRate = BigDecimal.ONE.divide(DEFAULT_USD_TO_SYP_RATE, 6, RoundingMode.HALF_UP);
            return balanceInSYP.multiply(defaultRate).setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    /**
     * Calculate EUR balance from SYP balance with fallback to default rate
     */
    private BigDecimal calculateEURBalance(BigDecimal balanceInSYP) {
        try {
            // Try to get current exchange rate from service
            BigDecimal rate = exchangeRateService.getExchangeRate(Currency.SYP, Currency.EUR);
            return balanceInSYP.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            // Fallback to default rate if service fails
            BigDecimal defaultRate = BigDecimal.ONE.divide(DEFAULT_EUR_TO_SYP_RATE, 6, RoundingMode.HALF_UP);
            return balanceInSYP.multiply(defaultRate).setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    @Transactional
    public MoneyBoxResponseDTO addTransaction(BigDecimal amount, String description, Currency currency) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        log.info("Adding manual transaction for pharmacy: {}, amount: {}, currency: {}, description: {}", 
                currentPharmacyId, amount, currency, description);
        
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        if (moneyBox.getStatus() != MoneyBoxStatus.OPEN) {
            throw new ConflictException("Money box is not open");
        }
        
        if (amount == null) {
            throw new ConflictException("Transaction amount cannot be null");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new ConflictException("Transaction amount cannot be zero");
        }
        
        // Use more descriptive transaction types for manual transactions
        TransactionType transactionType = (amount.compareTo(BigDecimal.ZERO) > 0) 
            ? TransactionType.CASH_DEPOSIT 
            : TransactionType.CASH_WITHDRAWAL;
        
        // Create transaction record FIRST using enhanced audit service
        // The audit service will handle balance calculation and moneyBox update
        String transactionDescription = (amount.compareTo(BigDecimal.ZERO) > 0) 
            ? "Manual cash deposit: " + (description != null ? description : "Cash added to money box")
            : "Manual cash withdrawal: " + (description != null ? description : "Cash removed from money box");
        
        MoneyBoxTransaction savedTransaction = enhancedAuditService.recordFinancialOperation(
            moneyBox.getId(),
            transactionType,
            amount,
            currency != null ? currency : Currency.SYP,
            transactionDescription,
            String.valueOf(moneyBox.getId()),
            "MANUAL_TRANSACTION",
            getCurrentUser().getId(),
            getCurrentUser().getClass().getSimpleName(),
            null, null, null,
            Map.of("pharmacyId", currentPharmacyId, "description", description != null ? description : "")
        );
        
        // Get updated moneyBox after transaction recording
        MoneyBox updatedMoneyBox = moneyBoxRepository.findById(moneyBox.getId())
            .orElseThrow(() -> new RuntimeException("MoneyBox not found after transaction"));
        
        log.info("Manual {} transaction added. New balance for pharmacy {}: {}", 
                transactionType, currentPharmacyId, updatedMoneyBox.getCurrentBalance());
        
        MoneyBoxResponseDTO response = MoneyBoxMapper.toResponseDTO(updatedMoneyBox);
        response.setTotalBalanceInUSD(calculateUSDBalance(updatedMoneyBox.getCurrentBalance()));
        response.setTotalBalanceInEUR(calculateEURBalance(updatedMoneyBox.getCurrentBalance()));
        
        // Set current exchange rates
        setExchangeRates(response);
        
        return response;
    }
    
    @Transactional
    public MoneyBoxResponseDTO addTransaction(BigDecimal amount, String description) {
        return addTransaction(amount, description, Currency.SYP);
    }
    
    @Transactional
    public MoneyBoxResponseDTO reconcileCash(BigDecimal actualCashCount, String notes) {
        // Validate input parameters
        if (actualCashCount == null) {
            throw new ConflictException("Actual cash count cannot be null");
        }
        
        if (actualCashCount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ConflictException("Actual cash count cannot be negative");
        }
        
        if (notes == null) {
            throw new ConflictException("Reconciliation notes cannot be null");
        }
        
        if (notes.trim().isEmpty()) {
            throw new ConflictException("Reconciliation notes cannot be empty");
        }
        
        Long currentPharmacyId = getCurrentUserPharmacyId();
        log.info("Reconciling cash for pharmacy: {}, actual count: {}", currentPharmacyId, actualCashCount);
        
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        BigDecimal balanceBefore = moneyBox.getCurrentBalance();
        BigDecimal difference = actualCashCount.subtract(balanceBefore);
        
        // Update reconciliation fields
        moneyBox.setReconciledBalance(actualCashCount);
        moneyBox.setLastReconciled(LocalDateTime.now());
        
        // If there's a difference, create adjustment transaction FIRST
        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            // Create adjustment transaction record using enhanced audit service
            // The audit service will handle balance calculation and moneyBox update
            enhancedAuditService.recordFinancialOperation(
                moneyBox.getId(),
                TransactionType.ADJUSTMENT,
                difference,
                Currency.SYP,
                notes != null ? notes : "Cash reconciliation adjustment",
                String.valueOf(moneyBox.getId()),
                "CASH_RECONCILIATION",
                getCurrentUser().getId(),
                getCurrentUser().getClass().getSimpleName(),
                null, null, null,
                Map.of("pharmacyId", currentPharmacyId, "actualCount", actualCashCount, "expectedCount", balanceBefore, "difference", difference)
            );
            
            // IMPORTANT: After the audit service runs, fetch the updated MoneyBox
            // The audit service has already updated the balance correctly
            MoneyBox updatedMoneyBox = moneyBoxRepository.findById(moneyBox.getId())
                .orElseThrow(() -> new RuntimeException("MoneyBox not found after transaction"));
            
            // Only update reconciliation fields, NOT the balance (audit service handled that)
            updatedMoneyBox.setReconciledBalance(actualCashCount);
            updatedMoneyBox.setLastReconciled(LocalDateTime.now());
            moneyBox = moneyBoxRepository.save(updatedMoneyBox);
        } else {
            // No difference, just save the reconciliation fields
            MoneyBox savedMoneyBox = moneyBoxRepository.save(moneyBox);
            moneyBox = savedMoneyBox;
        }
        log.info("Cash reconciled for pharmacy: {}", currentPharmacyId);
        
        MoneyBoxResponseDTO response = MoneyBoxMapper.toResponseDTO(moneyBox);
        response.setTotalBalanceInUSD(calculateUSDBalance(moneyBox.getCurrentBalance()));
        response.setTotalBalanceInEUR(calculateEURBalance(moneyBox.getCurrentBalance()));
        
        // Set current exchange rates
        setExchangeRates(response);
        
        return response;
    }
    
    public MoneyBoxSummary getPeriodSummary(LocalDateTime startDate, LocalDateTime endDate) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        // Get income transactions (positive amounts)
        BigDecimal totalIncome = transactionRepository.getTotalAmountByTypeAndPeriod(
            moneyBox.getId(), TransactionType.INCOME, startDate, endDate);
        
        // Get expense transactions (negative amounts)
        BigDecimal totalExpense = transactionRepository.getTotalAmountByTypeAndPeriod(
            moneyBox.getId(), TransactionType.EXPENSE, startDate, endDate);
        
        BigDecimal netAmount = totalIncome.add(totalExpense); // totalExpense is already negative
        
        return new MoneyBoxSummary(totalIncome, totalExpense.abs(), netAmount, startDate, endDate);
    }
    
    public List<MoneyBoxTransactionResponseDTO> getAllTransactions(LocalDateTime startDate, LocalDateTime endDate, String transactionType) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        List<MoneyBoxTransaction> transactions;
        
        if (startDate != null && endDate != null) {

            if (transactionType != null) {
                TransactionType type = TransactionType.valueOf(transactionType.toUpperCase());
                transactions = transactionRepository.findByMoneyBoxIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    moneyBox.getId(), startDate, endDate);
                // Filter by transaction type
                transactions = transactions.stream()
                    .filter(t -> t.getTransactionType() == type)
                    .collect(java.util.stream.Collectors.toList());
            } else {
                transactions = transactionRepository.findByMoneyBoxIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                    moneyBox.getId(), startDate, endDate);
            }
        } else if (transactionType != null) {
            // Filter only by transaction type
            TransactionType type = TransactionType.valueOf(transactionType.toUpperCase());
            transactions = transactionRepository.findByMoneyBoxIdAndTransactionTypeOrderByCreatedAtDesc(
                moneyBox.getId(), type);
        } else {
            // Get all transactions
            transactions = transactionRepository.findByMoneyBoxIdOrderByCreatedAtDesc(moneyBox.getId());
        }
        
        return transactions.stream()
            .map(transaction -> {
                // Get user email for each transaction
                String userEmail = getUserEmailById(transaction.getCreatedBy());
                return MoneyBoxMapper.toTransactionResponseDTO(transaction, userEmail);
            })
            .collect(java.util.stream.Collectors.toList());
    }

    // Paginated methods
    public PaginationDTO<MoneyBoxTransactionResponseDTO> getAllTransactionsPaginated(int page, int size) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MoneyBoxTransaction> transactionPage = transactionRepository.findByMoneyBoxIdOrderByCreatedAtDesc(moneyBox.getId(), pageable);
        
        List<MoneyBoxTransactionResponseDTO> responses = transactionPage.getContent().stream()
            .map(transaction -> {
                String userEmail = getUserEmailById(transaction.getCreatedBy());
                return MoneyBoxMapper.toTransactionResponseDTO(transaction, userEmail);
            })
            .collect(java.util.stream.Collectors.toList());
            
        return new PaginationDTO<>(responses, page, size, transactionPage.getTotalElements());
    }

    public PaginationDTO<MoneyBoxTransactionResponseDTO> getAllTransactionsPaginated(
            LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MoneyBoxTransaction> transactionPage = transactionRepository.findByMoneyBoxIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            moneyBox.getId(), startDate, endDate, pageable);
        
        List<MoneyBoxTransactionResponseDTO> responses = transactionPage.getContent().stream()
            .map(transaction -> {
                String userEmail = getUserEmailById(transaction.getCreatedBy());
                return MoneyBoxMapper.toTransactionResponseDTO(transaction, userEmail);
            })
            .collect(java.util.stream.Collectors.toList());
            
        return new PaginationDTO<>(responses, page, size, transactionPage.getTotalElements());
    }

    public PaginationDTO<MoneyBoxTransactionResponseDTO> getAllTransactionsPaginated(
            String transactionType, int page, int size) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        TransactionType type = TransactionType.valueOf(transactionType.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);
        Page<MoneyBoxTransaction> transactionPage = transactionRepository.findByMoneyBoxIdAndTransactionTypeOrderByCreatedAtDesc(
            moneyBox.getId(), type, pageable);
        
        List<MoneyBoxTransactionResponseDTO> responses = transactionPage.getContent().stream()
            .map(transaction -> {
                String userEmail = getUserEmailById(transaction.getCreatedBy());
                return MoneyBoxMapper.toTransactionResponseDTO(transaction, userEmail);
            })
            .collect(java.util.stream.Collectors.toList());
            
        return new PaginationDTO<>(responses, page, size, transactionPage.getTotalElements());
    }

    public PaginationDTO<MoneyBoxTransactionResponseDTO> getAllTransactionsPaginated(
            LocalDateTime startDate, LocalDateTime endDate, String transactionType, int page, int size) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        MoneyBox moneyBox = findMoneyBoxByPharmacyId(currentPharmacyId);
        
        TransactionType type = TransactionType.valueOf(transactionType.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);
        Page<MoneyBoxTransaction> transactionPage = transactionRepository.findByMoneyBoxIdAndTransactionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            moneyBox.getId(), type, startDate, endDate, pageable);
        
        List<MoneyBoxTransactionResponseDTO> responses = transactionPage.getContent().stream()
            .map(transaction -> {
                String userEmail = getUserEmailById(transaction.getCreatedBy());
                return MoneyBoxMapper.toTransactionResponseDTO(transaction, userEmail);
            })
            .collect(java.util.stream.Collectors.toList());
            
        return new PaginationDTO<>(responses, page, size, transactionPage.getTotalElements());
    }

    /**
     * Helper method to get user email by user ID
     */
    private String getUserEmailById(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            return userRepository.findById(userId)
                .map(user -> user.getEmail())
                .orElse(null);
        } catch (Exception e) {
            log.warn("Failed to fetch user email for ID {}: {}", userId, e.getMessage());
            return null;
        }
    }

    public CurrencyConversionResponseDTO convertCurrencyToSYP(BigDecimal amount, Currency fromCurrency) {
        try {
            BigDecimal convertedAmount = exchangeRateService.convertToSYP(amount, fromCurrency);
            BigDecimal exchangeRate = exchangeRateService.getExchangeRate(fromCurrency, Currency.SYP);
            
            return ExchangeRateMapper.toConversionResponse(
                amount, fromCurrency, convertedAmount, Currency.SYP, exchangeRate
            );
        } catch (Exception e) {
            log.warn("Failed to convert currency: {}. Using 1:1 rate.", e.getMessage());
            return ExchangeRateMapper.toConversionResponse(
                amount, fromCurrency, amount, Currency.SYP, BigDecimal.ONE, "FALLBACK"
            );
        }
    }

    public List<ExchangeRateResponseDTO> getCurrentExchangeRates() {
        return exchangeRateService.getAllActiveRates();
    }
    
    // Note: createTransactionRecord method removed - now using EnhancedMoneyBoxAuditService
    
    private MoneyBox findMoneyBoxByPharmacyId(Long pharmacyId) {
        return moneyBoxRepository.findByPharmacyId(pharmacyId)
                .orElseThrow(() -> new IllegalArgumentException("Money box not found for pharmacy: " + pharmacyId));
    }
    
    // Inner class for summary
    public static class MoneyBoxSummary {
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netAmount;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
        
        // Constructor, getters, setters...
        public MoneyBoxSummary() {}
        
        public MoneyBoxSummary(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal netAmount, 
                             LocalDateTime periodStart, LocalDateTime periodEnd) {
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.netAmount = netAmount;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
        }
        
        // Getters and setters
        public BigDecimal getTotalIncome() { return totalIncome; }
        public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
        
        public BigDecimal getTotalExpense() { return totalExpense; }
        public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
        
        public BigDecimal getNetAmount() { return netAmount; }
        public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
        
        public LocalDateTime getPeriodStart() { return periodStart; }
        public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }
        
        public LocalDateTime getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }
    }
}
