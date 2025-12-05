package com.Uqar.moneybox.dto;

import com.Uqar.moneybox.enums.TransactionType;
import com.Uqar.user.Enum.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneyBoxTransactionResponseDTO {
    
    private Long id;
    private Long moneyBoxId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;
    private String referenceId;
    private String referenceType;
    
    // Currency conversion information
    private Currency originalCurrency;
    private BigDecimal originalAmount;
    private Currency convertedCurrency;
    private BigDecimal convertedAmount;
    private BigDecimal exchangeRate;
    private LocalDateTime conversionTimestamp;
    private String conversionSource;
    
    private LocalDateTime createdAt;
    private Long createdBy; // New field for user ID
    private String createdByUserEmail; // New field for user email
}
