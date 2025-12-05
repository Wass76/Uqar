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
public class MoneyBoxRequestDTO {
    
    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", message = "Initial balance must be greater than or equal to 0")
    private BigDecimal initialBalance;
    
    @NotNull(message = "Currency is required")
//    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters (e.g., SYP)")
    private Currency currency;
}
