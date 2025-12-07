package com.Uqar.product.dto;

import com.Uqar.user.Enum.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyAwarePriceDTO {
    
    /**
     * Original price in SYP (base currency)
     */
    private BigDecimal priceInSYP;
    
    /**
     * Converted price in requested currency
     */
    private BigDecimal priceInRequestedCurrency;
    
    /**
     * The currency requested by the user
     */
    private Currency requestedCurrency;
    
    /**
     * Exchange rate used for conversion
     */
    private BigDecimal exchangeRate;
    
    /**
     * Timestamp when conversion was performed
     */
    private String conversionTimestamp;
    
    /**
     * Source of the exchange rate (database or fallback)
     */
    private String rateSource;
    
    /**
     * Get the appropriate price based on whether conversion was requested
     */
    public BigDecimal getDisplayPrice() {
        return priceInRequestedCurrency != null ? priceInRequestedCurrency : priceInSYP;
    }
    
    /**
     * Check if price was converted to a different currency
     */
    public boolean isConverted() {
        return requestedCurrency != null && !Currency.SYP.equals(requestedCurrency);
    }
}
