package com.Uqar.moneybox.mapper;

import com.Uqar.moneybox.entity.ExchangeRate;
import com.Uqar.moneybox.dto.ExchangeRateResponseDTO;
import com.Uqar.moneybox.dto.CurrencyConversionResponseDTO;
import com.Uqar.user.Enum.Currency;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class ExchangeRateMapper {
    
    /**
     * Map ExchangeRate entity to ExchangeRateResponseDTO
     */
    public static ExchangeRateResponseDTO toResponse(ExchangeRate exchangeRate) {
        if (exchangeRate == null) {
            return null;
        }
        
        ExchangeRateResponseDTO dto = new ExchangeRateResponseDTO();
        dto.setId(exchangeRate.getId());
        dto.setFromCurrency(exchangeRate.getFromCurrency());
        dto.setToCurrency(exchangeRate.getToCurrency());
        dto.setRate(exchangeRate.getRate());
        dto.setIsActive(exchangeRate.getIsActive());
        dto.setCreatedAt(exchangeRate.getCreatedAt());
        dto.setEffectiveFrom(exchangeRate.getEffectiveFrom());
        dto.setEffectiveTo(exchangeRate.getEffectiveTo());
        dto.setSource(exchangeRate.getSource());
        dto.setNotes(exchangeRate.getNotes());
        
        return dto;
    }
    
    /**
     * Map ExchangeRate entity to ExchangeRateResponseDTO with custom fields
     */
    public static ExchangeRateResponseDTO toResponse(ExchangeRate exchangeRate, String customSource, String customNotes) {
        if (exchangeRate == null) {
            return null;
        }
        
        ExchangeRateResponseDTO dto = new ExchangeRateResponseDTO();
        dto.setId(exchangeRate.getId());
        dto.setFromCurrency(exchangeRate.getFromCurrency());
        dto.setToCurrency(exchangeRate.getToCurrency());
        dto.setRate(exchangeRate.getRate());
        dto.setIsActive(exchangeRate.getIsActive());
        dto.setCreatedAt(exchangeRate.getCreatedAt());
        dto.setEffectiveFrom(exchangeRate.getEffectiveFrom());
        dto.setEffectiveTo(exchangeRate.getEffectiveTo());
        dto.setSource(customSource != null ? customSource : exchangeRate.getSource());
        dto.setNotes(customNotes != null ? customNotes : exchangeRate.getNotes());
        
        return dto;
    }
    
    /**
     * Create CurrencyConversionResponseDTO from conversion details
     */
    public static CurrencyConversionResponseDTO toConversionResponse(
            BigDecimal originalAmount, 
            Currency fromCurrency, 
            BigDecimal convertedAmount, 
            Currency toCurrency, 
            BigDecimal exchangeRate) {
        
        CurrencyConversionResponseDTO dto = new CurrencyConversionResponseDTO();
        dto.setOriginalAmount(originalAmount);
        dto.setFromCurrency(fromCurrency);
        dto.setConvertedAmount(convertedAmount);
        dto.setToCurrency(toCurrency);
        dto.setExchangeRate(exchangeRate);
        dto.setConversionTime(LocalDateTime.now());
        dto.setRateSource("EXCHANGE_RATE_SERVICE");
        
        return dto;
    }
    
    /**
     * Create CurrencyConversionResponseDTO with custom source
     */
    public static CurrencyConversionResponseDTO toConversionResponse(
            BigDecimal originalAmount, 
            Currency fromCurrency, 
            BigDecimal convertedAmount, 
            Currency toCurrency, 
            BigDecimal exchangeRate,
            String rateSource) {
        
        CurrencyConversionResponseDTO dto = new CurrencyConversionResponseDTO();
        dto.setOriginalAmount(originalAmount);
        dto.setFromCurrency(fromCurrency);
        dto.setConvertedAmount(convertedAmount);
        dto.setToCurrency(toCurrency);
        dto.setExchangeRate(exchangeRate);
        dto.setConversionTime(LocalDateTime.now());
        dto.setRateSource(rateSource != null ? rateSource : "EXCHANGE_RATE_SERVICE");
        
        return dto;
    }
}
