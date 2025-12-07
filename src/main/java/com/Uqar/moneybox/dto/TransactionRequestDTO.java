package com.Uqar.moneybox.dto;

import com.Uqar.moneybox.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {
    private Long moneyBoxId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String description;
    private String referenceId;
    private String referenceType;
    private String currency;
}
