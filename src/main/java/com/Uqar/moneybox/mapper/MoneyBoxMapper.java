package com.Uqar.moneybox.mapper;

import com.Uqar.moneybox.dto.MoneyBoxRequestDTO;
import com.Uqar.moneybox.dto.MoneyBoxResponseDTO;
import com.Uqar.moneybox.dto.MoneyBoxTransactionResponseDTO;
import com.Uqar.moneybox.entity.MoneyBox;
import com.Uqar.moneybox.entity.MoneyBoxTransaction;
import com.Uqar.moneybox.enums.MoneyBoxStatus;
import com.Uqar.moneybox.service.ExchangeRateService;
import com.Uqar.user.Enum.Currency;
import com.Uqar.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MoneyBoxMapper {
    
    // Default exchange rates for production fallback
    private static final BigDecimal DEFAULT_USD_TO_SYP_RATE = new BigDecimal("10000");
    private static final BigDecimal DEFAULT_EUR_TO_SYP_RATE = new BigDecimal("11000");
    
    public static MoneyBox toEntity(MoneyBoxRequestDTO request) {
        MoneyBox moneyBox = new MoneyBox();
        moneyBox.setInitialBalance(request.getInitialBalance());
        moneyBox.setCurrentBalance(request.getInitialBalance());
        moneyBox.setStatus(MoneyBoxStatus.OPEN); // Set default status to OPEN
        moneyBox.setCurrency("SYP"); // Always set to SYP for consistency
        return moneyBox;
    }
    
    public static MoneyBoxResponseDTO toResponseDTO(MoneyBox moneyBox) {
        return MoneyBoxResponseDTO.builder()
                .id(moneyBox.getId())
                .pharmacyId(moneyBox.getPharmacyId())
                .currentBalance(moneyBox.getCurrentBalance())
                .initialBalance(moneyBox.getInitialBalance())
                .lastReconciled(moneyBox.getLastReconciled())
                .reconciledBalance(moneyBox.getReconciledBalance())
                .status(moneyBox.getStatus())
                .currency(moneyBox.getCurrency())
                .baseCurrency(Currency.SYP)
                .totalBalanceInSYP(moneyBox.getCurrentBalance())
                .totalBalanceInUSD(null) // Will be set by service layer
                .totalBalanceInEUR(null) // Will be set by service layer
                .createdAt(moneyBox.getCreatedAt())
                .updatedAt(moneyBox.getUpdatedAt())
                .build();
    }
    
    public MoneyBoxResponseDTO toResponseDTOWithTransactions(MoneyBox moneyBox, 
                                                                  List<MoneyBoxTransaction> transactions) {
        MoneyBoxResponseDTO response = toResponseDTO(moneyBox);
        
        if (transactions != null && !transactions.isEmpty()) {
            response.setRecentTransactions(transactions.stream()
                    .map(MoneyBoxMapper::toTransactionResponseDTO)
                    .collect(Collectors.toList()));
            response.setTotalTransactionCount(transactions.size());
        }
        
        return response;
    }
    
    public static MoneyBoxTransactionResponseDTO toTransactionResponseDTO(MoneyBoxTransaction transaction) {
        return toTransactionResponseDTO(transaction, null);
    }
    
    public static MoneyBoxTransactionResponseDTO toTransactionResponseDTO(MoneyBoxTransaction transaction, String userEmail) {
        return MoneyBoxTransactionResponseDTO.builder()
                .id(transaction.getId())
                .moneyBoxId(transaction.getMoneyBox().getId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .referenceId(transaction.getReferenceId())
                .referenceType(transaction.getReferenceType())
                .originalCurrency(transaction.getOriginalCurrency())
                .originalAmount(transaction.getOriginalAmount())
                .convertedCurrency(transaction.getConvertedCurrency())
                .convertedAmount(transaction.getConvertedAmount())
                .exchangeRate(transaction.getExchangeRate())
                .conversionTimestamp(transaction.getConversionTimestamp())
                .conversionSource(transaction.getConversionSource())
                .createdAt(transaction.getCreatedAt())
                .createdBy(transaction.getCreatedBy())
                .createdByUserEmail(userEmail)
                .build();
    }
    
    public static List<MoneyBoxTransactionResponseDTO> toTransactionResponseDTOList(List<MoneyBoxTransaction> transactions) {
        if (transactions == null) {
            return List.of();
        }
        
        return transactions.stream()
                .map(MoneyBoxMapper::toTransactionResponseDTO)
                .collect(Collectors.toList());
    }
}
