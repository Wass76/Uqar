package com.Uqar.moneybox.service;

import com.Uqar.moneybox.entity.MoneyBox;
import com.Uqar.moneybox.entity.MoneyBoxTransaction;
import com.Uqar.moneybox.enums.TransactionType;
import com.Uqar.moneybox.repository.MoneyBoxRepository;
import com.Uqar.moneybox.repository.MoneyBoxTransactionRepository;
import com.Uqar.user.Enum.Currency;
import com.Uqar.moneybox.service.EnhancedMoneyBoxAuditService;
import com.Uqar.utils.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseIntegrationService {
    
    private final MoneyBoxRepository moneyBoxRepository;
    private final MoneyBoxTransactionRepository transactionRepository;
    private final ExchangeRateService exchangeRateService;
    private final EnhancedMoneyBoxAuditService enhancedAuditService;
    
    /**
     * Records a purchase payment in the money box with automatic currency conversion to SYP
     * This method is designed to be called within a transaction from the purchase service
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordPurchasePayment(Long pharmacyId, Long purchaseId, BigDecimal amount, Currency currency) {
        log.info("Recording purchase payment: pharmacy={}, purchase={}, amount={}, currency={}", 
                pharmacyId, purchaseId, amount, currency);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Use enhanced audit service to record the transaction
            enhancedAuditService.recordFinancialOperation(
                moneyBox.getId(),
                TransactionType.PURCHASE_PAYMENT,
                amount, // Positive amount - the service will handle the sign based on transaction type
                currency,
                "Purchase payment for purchase ID: " + purchaseId + 
                (currency != Currency.SYP ? " (Converted from " + currency + ")" : ""),
                String.valueOf(purchaseId),
                "PURCHASE",
                null, // User ID - would need to be passed from service
                null, // User type - would need to be passed from service
                null, null, null, // IP, User Agent, Session ID
                Map.of("pharmacyId", pharmacyId, "paymentMethod", "CASH")
            );
            
            log.info("Purchase payment recorded successfully using enhanced audit service. Amount: {} {}", 
                    amount, currency);
        } catch (Exception e) {
            log.error("Failed to record purchase payment for purchase {}: {}", purchaseId, e.getMessage(), e);
            throw new RuntimeException("Failed to record purchase payment in MoneyBox", e);
        }
    }
    
    /**
     * Records a purchase refund in the money box with automatic currency conversion to SYP
     * This method is designed to be called within a transaction from the purchase service
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordPurchaseRefund(Long pharmacyId, Long purchaseId, BigDecimal amount, Currency currency) {
        log.info("Recording purchase refund: pharmacy={}, purchase={}, amount={}, currency={}", 
                pharmacyId, purchaseId, amount, currency);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Use enhanced audit service to record the transaction
            enhancedAuditService.recordFinancialOperation(
                moneyBox.getId(),
                TransactionType.INCOME, // Treat refund as income
                amount,
                currency,
                "Refund for purchase ID: " + purchaseId + 
                (currency != Currency.SYP ? " (Converted from " + currency + ")" : ""),
                String.valueOf(purchaseId),
                "PURCHASE_REFUND",
                null, // User ID - would need to be passed from service
                null, // User type - would need to be passed from service
                null, null, null, // IP, User Agent, Session ID
                Map.of("pharmacyId", pharmacyId, "refundType", "PURCHASE")
            );
            
            log.info("Purchase refund recorded successfully using enhanced audit service. Amount: {} {}", 
                    amount, currency);
        } catch (Exception e) {
            log.error("Failed to record purchase refund for purchase {}: {}", purchaseId, e.getMessage(), e);
            throw new RuntimeException("Failed to record purchase refund in MoneyBox", e);
        }
    }
    
    /**
     * Gets total purchase amount for a period in SYP (converted from all currencies)
     */
    public BigDecimal getPurchaseAmountForPeriod(Long pharmacyId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            BigDecimal totalPurchases = transactionRepository.getTotalAmountByTypeAndPeriod(
                    moneyBox.getId(), TransactionType.PURCHASE_PAYMENT, startDate, endDate);
            
            return totalPurchases != null ? totalPurchases.abs() : BigDecimal.ZERO; // Return absolute value
        } catch (Exception e) {
            log.error("Failed to get purchase amount for period: pharmacy={}, start={}, end={}", 
                     pharmacyId, startDate, endDate, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Records an expense payment with automatic currency conversion to SYP
     * This method is designed to be called within a transaction
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordExpense(Long pharmacyId, String expenseDescription, BigDecimal amount, Currency currency) {
        log.info("Recording expense: pharmacy={}, description={}, amount={}, currency={}", 
                pharmacyId, expenseDescription, amount, currency);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Use enhanced audit service to record the transaction
            enhancedAuditService.recordFinancialOperation(
                moneyBox.getId(),
                TransactionType.EXPENSE,
                amount, // Positive amount - the service will handle the sign based on transaction type
                currency,
                expenseDescription + (currency != Currency.SYP ? " (Converted from " + currency + ")" : ""),
                null, // No specific reference ID for general expenses
                "EXPENSE",
                null, // User ID - would need to be passed from service
                null, // User type - would need to be passed from service
                null, null, null, // IP, User Agent, Session ID
                Map.of("pharmacyId", pharmacyId, "expenseType", "GENERAL")
            );
            
            log.info("Expense recorded successfully using enhanced audit service. Amount: {} {}", 
                    amount, currency);
        } catch (Exception e) {
            log.error("Failed to record expense: pharmacy={}, description={}, amount={}", 
                     pharmacyId, expenseDescription, amount, e.getMessage(), e);
            throw new RuntimeException("Failed to record expense in MoneyBox", e);
        }
    }
    
    /**
     * Records income with automatic currency conversion to SYP
     * This method is designed to be called within a transaction
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRED)
    public void recordIncome(Long pharmacyId, String incomeDescription, BigDecimal amount, Currency currency) {
        log.info("Recording income: pharmacy={}, description={}, amount={}, currency={}", 
                pharmacyId, incomeDescription, amount, currency);
        
        try {
            MoneyBox moneyBox = findMoneyBoxByPharmacyId(pharmacyId);
            
            // Use enhanced audit service to record the transaction
            enhancedAuditService.recordFinancialOperation(
                moneyBox.getId(),
                TransactionType.INCOME,
                amount,
                currency,
                incomeDescription + (currency != Currency.SYP ? " (Converted from " + currency + ")" : ""),
                null, // No specific reference ID for general income
                "INCOME",
                null, // User ID - would need to be passed from service
                null, // User type - would need to be passed from service
                null, null, null, // IP, User Agent, Session ID
                Map.of("pharmacyId", pharmacyId, "incomeType", "GENERAL")
            );
            
            log.info("Income recorded successfully using enhanced audit service. Amount: {} {}", 
                    amount, currency);
        } catch (Exception e) {
            log.error("Failed to record income: pharmacy={}, description={}, amount={}", 
                     pharmacyId, incomeDescription, amount, e.getMessage(), e);
            throw new RuntimeException("Failed to record income in MoneyBox", e);
        }
    }
    
    private MoneyBox findMoneyBoxByPharmacyId(Long pharmacyId) {
        return moneyBoxRepository.findByPharmacyId(pharmacyId)
                .orElseThrow(() -> new ConflictException("Money box not found for pharmacy: " + pharmacyId));
    }
}
