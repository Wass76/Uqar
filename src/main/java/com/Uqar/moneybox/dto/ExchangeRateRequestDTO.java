package com.Uqar.moneybox.dto;

import com.Uqar.user.Enum.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateRequestDTO {
    
    @NotNull(message = "From currency is required")
    private Currency fromCurrency;
    
    @NotNull(message = "To currency is required")
    private Currency toCurrency;
    
    @NotNull(message = "Exchange rate is required")
    @DecimalMin(value = "0.000001", message = "Exchange rate must be greater than 0")
    private BigDecimal rate;
    
    @Size(max = 100, message = "Source must not exceed 100 characters")
    private String source;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
