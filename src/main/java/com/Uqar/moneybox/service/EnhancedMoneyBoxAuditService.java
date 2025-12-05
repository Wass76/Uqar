package com.Uqar.moneybox.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Uqar.moneybox.entity.MoneyBox;
import com.Uqar.moneybox.entity.MoneyBoxTransaction;
import com.Uqar.moneybox.enums.TransactionType;
import com.Uqar.moneybox.repository.MoneyBoxRepository;
import com.Uqar.moneybox.repository.MoneyBoxTransactionRepository;
import com.Uqar.user.Enum.Currency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Enhanced Money Box Audit Service
 * Leverages existing MoneyBoxTransaction for comprehensive financial auditing
 * This approach builds on the existing production-ready infrastructure
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedMoneyBoxAuditService {

    private final MoneyBoxTransactionRepository transactionRepository;
    private final MoneyBoxRepository moneyBoxRepository;
    private final ExchangeRateService exchangeRateService;

    /**
     * Record a comprehensive financial operation using existing MoneyBoxTransaction
     * This method enhances the existing transaction with additional audit data
     * 
     * @param moneyBoxId The ID of the MoneyBox
     * @param transactionType The type of transaction
     * @param originalAmount The original amount in original currency
     * @param originalCurrency The original currency
     * @param description Description of the transaction
     * @param referenceId Reference ID for the related entity
     * @param referenceType Reference type for the related entity
     * @param userId The user ID who performed the operation
     * @param userType The type of user (PHARMACIST, ADMIN, etc.)
     * @param ipAddress User's IP address
     * @param userAgent User's browser/client info
     * @param sessionId User session identifier
     * @param additionalData Additional metadata as Map
     * @return The saved MoneyBoxTransaction
     */
    @Transactional
    public MoneyBoxTransaction recordFinancialOperation(
            Long moneyBoxId,
            TransactionType transactionType,
            BigDecimal originalAmount,
            Currency originalCurrency,
            String description,
            String referenceId,
            String referenceType,
            Long userId,
            String userType,
            String ipAddress,
            String userAgent,
            String sessionId,
            Map<String, Object> additionalData) {
        
        try {
            // Convert amount to SYP for consistent storage (same logic as existing)
            BigDecimal convertedAmount = originalAmount;
            Currency convertedCurrency = originalCurrency;
            BigDecimal exchangeRate = BigDecimal.ONE;
            String conversionSource = "NO_CONVERSION";
            
            if (!Currency.SYP.equals(originalCurrency)) {
                try {
                    convertedAmount = exchangeRateService.convertToSYP(originalAmount, originalCurrency);
                    exchangeRate = exchangeRateService.getExchangeRate(originalCurrency, Currency.SYP);
                    convertedCurrency = Currency.SYP;
                    conversionSource = "EXCHANGE_RATE_SERVICE";
                    
                    log.info("Currency conversion: {} {} -> {} SYP (rate: {})", 
                            originalAmount, originalCurrency, convertedAmount, exchangeRate);
                } catch (Exception e) {
                    log.warn("Failed to convert currency for operation {}: {}", transactionType, e.getMessage());
                    conversionSource = "CONVERSION_FAILED";
                }
            }
            
            // Get current balance for balance tracking
            BigDecimal currentBalance = getCurrentMoneyBoxBalance(moneyBoxId);
            BigDecimal newBalance = calculateNewBalance(currentBalance, convertedAmount, transactionType);
            
            // Get MoneyBox entity
            MoneyBox moneyBox = moneyBoxRepository.findById(moneyBoxId)
                    .orElseThrow(() -> new RuntimeException("MoneyBox not found with ID: " + moneyBoxId));
            
            // Update the moneyBox balance BEFORE creating the transaction
            moneyBox.setCurrentBalance(newBalance);
            moneyBox = moneyBoxRepository.save(moneyBox);
            
            // Create enhanced MoneyBoxTransaction
            MoneyBoxTransaction transaction = new MoneyBoxTransaction();
            transaction.setMoneyBox(moneyBox);
            transaction.setTransactionType(transactionType);
            
            // Store the transaction amount with correct sign for display purposes
            BigDecimal transactionAmount;
            if (transactionType == TransactionType.OPENING_BALANCE) {
                // For opening balance transactions, store as positive (initial balance)
                transactionAmount = convertedAmount.abs();
            } else if (transactionType == TransactionType.CASH_DEPOSIT || transactionType == TransactionType.CASH_WITHDRAWAL) {
                // For manual transactions, store the amount as-is (already has correct sign)
                transactionAmount = convertedAmount;
            } else if (transactionType == TransactionType.ADJUSTMENT) {
                // For adjustment transactions, store the amount as-is (already has correct sign)
                transactionAmount = convertedAmount;
            } else if (isExpenseTransaction(transactionType)) {
                // For expense transactions, store as negative
                transactionAmount = convertedAmount.negate();
            } else {
                // For revenue transactions, store as positive
                transactionAmount = convertedAmount;
            }
            transaction.setAmount(transactionAmount);
            
            transaction.setBalanceBefore(currentBalance);
            transaction.setBalanceAfter(newBalance);
            transaction.setDescription(description);
            transaction.setReferenceId(referenceId);
            transaction.setReferenceType(referenceType);
            
            // Currency conversion details
            transaction.setOriginalCurrency(originalCurrency);
            transaction.setOriginalAmount(originalAmount);
            transaction.setConvertedCurrency(convertedCurrency);
            transaction.setConvertedAmount(convertedAmount);
            transaction.setExchangeRate(exchangeRate);
            transaction.setConversionTimestamp(LocalDateTime.now());
            transaction.setConversionSource(conversionSource);
            
            // Enhanced audit fields
            transaction.setOperationStatus("SUCCESS");
            transaction.setIpAddress(ipAddress);
            transaction.setUserAgent(userAgent);
            transaction.setSessionId(sessionId);
            transaction.setUserType(userType);
            transaction.setCreatedBy(userId);
            transaction.setAdditionalData(additionalData != null ? additionalData.toString() : null);
            
            MoneyBoxTransaction saved = transactionRepository.save(transaction);
            
            log.info("Financial operation recorded: {} - {} {} -> {} SYP", 
                    transactionType, originalAmount, originalCurrency, convertedAmount);
            
            return saved;
            
        } catch (Exception e) {
            log.error("Failed to record financial operation: {}", e.getMessage(), e);
            
            // Record failed operation
            try {
                MoneyBox moneyBox = moneyBoxRepository.findById(moneyBoxId)
                        .orElseThrow(() -> new RuntimeException("MoneyBox not found with ID: " + moneyBoxId));
                
                MoneyBoxTransaction failedTransaction = new MoneyBoxTransaction();
                failedTransaction.setMoneyBox(moneyBox);
                failedTransaction.setTransactionType(transactionType);
                failedTransaction.setAmount(BigDecimal.ZERO);
                failedTransaction.setDescription(description);
                failedTransaction.setReferenceId(referenceId);
                failedTransaction.setReferenceType(referenceType);
                failedTransaction.setOriginalCurrency(originalCurrency);
                failedTransaction.setOriginalAmount(originalAmount);
                failedTransaction.setConvertedCurrency(originalCurrency);
                failedTransaction.setConvertedAmount(BigDecimal.ZERO);
                failedTransaction.setOperationStatus("FAILED");
                failedTransaction.setErrorMessage(e.getMessage());
                failedTransaction.setCreatedBy(userId);
                failedTransaction.setUserType(userType);
                
                return transactionRepository.save(failedTransaction);
            } catch (Exception ex) {
                log.error("Failed to record failed operation: {}", ex.getMessage(), ex);
                throw new RuntimeException("Failed to record financial operation", ex);
            }
        }
    }

    /**
     * Get comprehensive financial summary for a Money Box
     * 
     * @param moneyBoxId The ID of the MoneyBox
     * @param startDate Start date for the summary period
     * @param endDate End date for the summary period
     * @return Map containing financial summary data
     */
    public Map<String, Object> getMoneyBoxFinancialSummary(Long moneyBoxId, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            // Get transactions in date range
            List<MoneyBoxTransaction> transactions = transactionRepository.findByMoneyBoxIdAndCreatedAtBetween(moneyBoxId, startDate, endDate);
            
            // Group by transaction type
            Map<TransactionType, List<MoneyBoxTransaction>> transactionsByType = transactions.stream()
                    .collect(Collectors.groupingBy(MoneyBoxTransaction::getTransactionType));
            
            Map<String, Map<String, Object>> operationSummary = new HashMap<>();
            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal totalExpenses = BigDecimal.ZERO;
            long totalOperations = transactions.size();
            long failedOperations = transactions.stream()
                    .mapToLong(t -> "FAILED".equals(t.getOperationStatus()) ? 1 : 0)
                    .sum();
            
            for (Map.Entry<TransactionType, List<MoneyBoxTransaction>> entry : transactionsByType.entrySet()) {
                TransactionType type = entry.getKey();
                List<MoneyBoxTransaction> typeTransactions = entry.getValue();
                
                BigDecimal totalAmount = typeTransactions.stream()
                        .map(MoneyBoxTransaction::getConvertedAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal avgAmount = totalAmount.divide(BigDecimal.valueOf(typeTransactions.size()), 2, RoundingMode.HALF_UP);
                
                Map<String, Object> typeData = new HashMap<>();
                typeData.put("count", typeTransactions.size());
                typeData.put("totalAmount", totalAmount);
                typeData.put("avgAmount", avgAmount);
                typeData.put("description", type.name());
                
                operationSummary.put(type.name(), typeData);
                
                // Categorize as revenue or expense
                if (isRevenueTransaction(type)) {
                    totalRevenue = totalRevenue.add(totalAmount);
                } else if (isExpenseTransaction(type)) {
                    totalExpenses = totalExpenses.add(totalAmount);
                }
            }
            
            // Calculate profit margin
            BigDecimal profit = totalRevenue.subtract(totalExpenses);
            BigDecimal profitMargin = BigDecimal.ZERO;
            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                profitMargin = profit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            }
            
            // Get currency breakdown
            Map<String, BigDecimal> currencyBreakdown = transactions.stream()
                    .filter(t -> t.getOriginalCurrency() != null)
                    .collect(Collectors.groupingBy(
                            t -> t.getOriginalCurrency().name(),
                            Collectors.reducing(BigDecimal.ZERO, 
                                    MoneyBoxTransaction::getOriginalAmount, 
                                    BigDecimal::add)
                    ));
            
            summary.put("moneyBoxId", moneyBoxId);
            summary.put("period", Map.of("start", startDate, "end", endDate));
            summary.put("totalOperations", totalOperations);
            summary.put("failedOperations", failedOperations);
            summary.put("successRate", totalOperations > 0 ? 
                    BigDecimal.valueOf(totalOperations - failedOperations)
                            .divide(BigDecimal.valueOf(totalOperations), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
            summary.put("totalRevenue", totalRevenue);
            summary.put("totalExpenses", totalExpenses);
            summary.put("profit", profit);
            summary.put("profitMargin", profitMargin);
            summary.put("operations", operationSummary);
            summary.put("currencyBreakdown", currencyBreakdown);
            summary.put("generatedAt", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Failed to generate financial summary: {}", e.getMessage(), e);
            summary.put("error", e.getMessage());
        }
        
        return summary;
    }

    /**
     * Get currency conversion analytics
     * 
     * @param moneyBoxId The ID of the MoneyBox
     * @param startDate Start date for the analytics period
     * @param endDate End date for the analytics period
     * @return Map containing currency conversion analytics
     */
    public Map<String, Object> getCurrencyConversionAnalytics(Long moneyBoxId, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            List<MoneyBoxTransaction> transactions = transactionRepository.findByMoneyBoxIdAndCreatedAtBetween(moneyBoxId, startDate, endDate);
            
            // Filter transactions with currency conversion
            List<MoneyBoxTransaction> conversionTransactions = transactions.stream()
                    .filter(t -> t.getOriginalCurrency() != null && 
                               t.getConvertedCurrency() != null &&
                               !t.getOriginalCurrency().equals(t.getConvertedCurrency()))
                    .collect(Collectors.toList());
            
            // Group by currency pair
            Map<String, List<MoneyBoxTransaction>> conversionsByPair = conversionTransactions.stream()
                    .collect(Collectors.groupingBy(t -> 
                            t.getOriginalCurrency().name() + "_" + t.getConvertedCurrency().name()));
            
            Map<String, Map<String, Object>> pairAnalytics = new HashMap<>();
            
            for (Map.Entry<String, List<MoneyBoxTransaction>> entry : conversionsByPair.entrySet()) {
                String currencyPair = entry.getKey();
                List<MoneyBoxTransaction> pairTransactions = entry.getValue();
                
                BigDecimal totalOriginalAmount = pairTransactions.stream()
                        .map(MoneyBoxTransaction::getOriginalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal totalConvertedAmount = pairTransactions.stream()
                        .map(MoneyBoxTransaction::getConvertedAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal avgExchangeRate = pairTransactions.stream()
                        .map(MoneyBoxTransaction::getExchangeRate)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(pairTransactions.size()), 6, RoundingMode.HALF_UP);
                
                BigDecimal minRate = pairTransactions.stream()
                        .map(MoneyBoxTransaction::getExchangeRate)
                        .filter(Objects::nonNull)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                
                BigDecimal maxRate = pairTransactions.stream()
                        .map(MoneyBoxTransaction::getExchangeRate)
                        .filter(Objects::nonNull)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                
                Map<String, Object> pairData = new HashMap<>();
                pairData.put("conversionCount", pairTransactions.size());
                pairData.put("totalOriginalAmount", totalOriginalAmount);
                pairData.put("totalConvertedAmount", totalConvertedAmount);
                pairData.put("avgExchangeRate", avgExchangeRate);
                pairData.put("minExchangeRate", minRate);
                pairData.put("maxExchangeRate", maxRate);
                pairData.put("firstConversion", pairTransactions.stream()
                        .map(MoneyBoxTransaction::getConversionTimestamp)
                        .filter(Objects::nonNull)
                        .min(LocalDateTime::compareTo)
                        .orElse(null));
                pairData.put("lastConversion", pairTransactions.stream()
                        .map(MoneyBoxTransaction::getConversionTimestamp)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null));
                
                pairAnalytics.put(currencyPair, pairData);
            }
            
            analytics.put("moneyBoxId", moneyBoxId);
            analytics.put("period", Map.of("start", startDate, "end", endDate));
            analytics.put("totalConversions", conversionTransactions.size());
            analytics.put("conversionPairs", pairAnalytics);
            analytics.put("generatedAt", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Failed to generate currency conversion analytics: {}", e.getMessage(), e);
            analytics.put("error", e.getMessage());
        }
        
        return analytics;
    }

    /**
     * Get audit trail for specific entity using existing reference fields
     * 
     * @param referenceType The reference type to search for
     * @param referenceId The reference ID to search for
     * @return List of MoneyBoxTransaction records for the entity
     */
    public List<MoneyBoxTransaction> getEntityAuditTrail(String referenceType, String referenceId) {
        return transactionRepository.findByReferenceIdAndReferenceType(referenceId, referenceType);
    }

    /**
     * Get failed operations analysis
     * 
     * @param moneyBoxId The ID of the MoneyBox
     * @param startDate Start date for the analysis period
     * @param endDate End date for the analysis period
     * @return Map containing failed operations analysis
     */
    public Map<String, Object> getFailedOperationsAnalysis(Long moneyBoxId, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            List<MoneyBoxTransaction> failedTransactions = transactionRepository.findByMoneyBoxIdAndOperationStatusAndCreatedAtBetween(
                    moneyBoxId, "FAILED", startDate, endDate);
            
            Map<TransactionType, Long> failuresByType = failedTransactions.stream()
                    .collect(Collectors.groupingBy(
                            MoneyBoxTransaction::getTransactionType,
                            Collectors.counting()
                    ));
            
            Map<String, Long> failuresByReferenceType = failedTransactions.stream()
                    .filter(t -> t.getReferenceType() != null)
                    .collect(Collectors.groupingBy(
                            MoneyBoxTransaction::getReferenceType,
                            Collectors.counting()
                    ));
            
            List<Map<String, Object>> failureDetails = failedTransactions.stream()
                    .map(t -> {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("transactionType", t.getTransactionType());
                        detail.put("referenceType", t.getReferenceType());
                        detail.put("referenceId", t.getReferenceId());
                        detail.put("errorMessage", t.getErrorMessage());
                        detail.put("timestamp", t.getCreatedAt());
                        detail.put("originalAmount", t.getOriginalAmount());
                        detail.put("originalCurrency", t.getOriginalCurrency());
                        return detail;
                    })
                    .collect(Collectors.toList());
            
            analysis.put("totalFailures", failedTransactions.size());
            analysis.put("failuresByType", failuresByType);
            analysis.put("failuresByReferenceType", failuresByReferenceType);
            analysis.put("failureDetails", failureDetails);
            analysis.put("period", Map.of("start", startDate, "end", endDate));
            
        } catch (Exception e) {
            log.error("Failed to generate failed operations analysis: {}", e.getMessage(), e);
            analysis.put("error", e.getMessage());
        }
        
        return analysis;
    }

    // Helper methods
    private BigDecimal getCurrentMoneyBoxBalance(Long moneyBoxId) {
        // Get the current balance directly from MoneyBox entity for accuracy
        MoneyBox moneyBox = moneyBoxRepository.findById(moneyBoxId)
                .orElseThrow(() -> new RuntimeException("MoneyBox not found with ID: " + moneyBoxId));
        
        return moneyBox.getCurrentBalance();
    }
    
    private BigDecimal calculateNewBalance(BigDecimal currentBalance, BigDecimal amount, TransactionType transactionType) {
        // For manual transactions (CASH_DEPOSIT/CASH_WITHDRAWAL), the amount already has the correct sign
        // For other transaction types, we need to determine the sign based on transaction type
        if (transactionType == TransactionType.OPENING_BALANCE) {
            // Opening balance: set to the amount (not add to current balance)
            return amount.abs();
        } else if (transactionType == TransactionType.CASH_DEPOSIT || transactionType == TransactionType.CASH_WITHDRAWAL) {
            // Manual transactions: amount already has correct sign
            return currentBalance.add(amount);
        } else if (transactionType == TransactionType.ADJUSTMENT) {
            // Adjustment transactions: amount already has the correct sign (positive for increase, negative for decrease)
            return currentBalance.add(amount);
        } else if (isRevenueTransaction(transactionType)) {
            // Revenue transactions: always add positive amount
            return currentBalance.add(amount.abs());
        } else if (isExpenseTransaction(transactionType)) {
            // Expense transactions: always subtract positive amount
            return currentBalance.subtract(amount.abs());
        }
        return currentBalance;
    }
    
    private boolean isRevenueTransaction(TransactionType transactionType) {
        return transactionType == TransactionType.SALE_PAYMENT ||
               transactionType == TransactionType.DEBT_PAYMENT || // ✅ ADDED: Debt payments are revenue
               transactionType == TransactionType.CASH_DEPOSIT ||
               transactionType == TransactionType.INCOME ||
               transactionType == TransactionType.TRANSFER_IN ||
               transactionType == TransactionType.OPENING_BALANCE;
    }
    
    private boolean isExpenseTransaction(TransactionType transactionType) {
        return transactionType == TransactionType.PURCHASE_PAYMENT ||
               transactionType == TransactionType.SALE_REFUND || // ✅ ADDED: Sale refunds are expenses (money goes out)
               transactionType == TransactionType.CASH_WITHDRAWAL ||
               transactionType == TransactionType.EXPENSE ||
               transactionType == TransactionType.TRANSFER_OUT ||
               transactionType == TransactionType.CLOSING_BALANCE;
    }
}