package com.Uqar.moneybox.dto;

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
public class ExchangeRateResponseDTO {
    
    private Long id;
    private Currency fromCurrency;
    private Currency toCurrency;
    private BigDecimal rate;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private String source;
    private String notes;
}
