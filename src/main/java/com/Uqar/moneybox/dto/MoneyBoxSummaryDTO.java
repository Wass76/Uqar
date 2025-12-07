package com.Uqar.moneybox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoneyBoxSummaryDTO {
    private Long pharmacyId;
    private BigDecimal currentBalance;
    private BigDecimal initialBalance;
    private LocalDateTime lastReconciled;
    private BigDecimal reconciledBalance;
    
    // Period summary
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal totalSales;
    private BigDecimal totalPurchases;
    private BigDecimal totalExpenses;
    private BigDecimal totalIncome;
    private BigDecimal netCashFlow;
    
    // Transaction counts
    private Long salesTransactionCount;
    private Long purchaseTransactionCount;
    private Long expenseTransactionCount;
    private Long incomeTransactionCount;
    
    // Currency information
    private String currency;
    
    // Reconciliation status
    private boolean isReconciled;
    private BigDecimal reconciliationDifference;
}
