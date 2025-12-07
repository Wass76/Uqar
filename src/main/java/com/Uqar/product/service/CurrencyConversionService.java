package com.Uqar.product.service;

import com.Uqar.moneybox.service.ExchangeRateService;
import com.Uqar.product.dto.CurrencyAwarePriceDTO;
import com.Uqar.user.Enum.Currency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyConversionService {
    
    private final ExchangeRateService exchangeRateService;
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Convert a price from SYP to the requested currency
     * 
     * @param priceInSYP The price in SYP (base currency)
     * @param requestedCurrency The currency to convert to
     * @return CurrencyAwarePriceDTO with both original and converted prices
     */
    public CurrencyAwarePriceDTO convertPriceFromSYP(BigDecimal priceInSYP, Currency requestedCurrency) {
        if (priceInSYP == null) {
            return null;
        }
        
        // If no currency specified or SYP requested, return original price
        if (requestedCurrency == null || Currency.SYP.equals(requestedCurrency)) {
            return CurrencyAwarePriceDTO.builder()
                    .priceInSYP(priceInSYP)
                    .priceInRequestedCurrency(priceInSYP)
                    .requestedCurrency(Currency.SYP)
                    .exchangeRate(BigDecimal.ONE)
                    .conversionTimestamp(LocalDateTime.now().format(TIMESTAMP_FORMATTER))
                    .rateSource("SYP_BASE")
                    .build();
        }
        
        try {
            // Convert from SYP to requested currency
            BigDecimal convertedPrice = exchangeRateService.convertAmount(
                priceInSYP, 
                Currency.SYP, 
                requestedCurrency
            );
            
            // Get the exchange rate used
            BigDecimal exchangeRate = exchangeRateService.getExchangeRate(Currency.SYP, requestedCurrency);
            
            return CurrencyAwarePriceDTO.builder()
                    .priceInSYP(priceInSYP)
                    .priceInRequestedCurrency(convertedPrice)
                    .requestedCurrency(requestedCurrency)
                    .exchangeRate(exchangeRate)
                    .conversionTimestamp(LocalDateTime.now().format(TIMESTAMP_FORMATTER))
                    .rateSource("EXCHANGE_RATE_SERVICE")
                    .build();
                    
        } catch (Exception e) {
            log.warn("Failed to convert price from SYP to {}: {}. Returning original price.", 
                    requestedCurrency, e.getMessage());
            
            // Return original price if conversion fails
            return CurrencyAwarePriceDTO.builder()
                    .priceInSYP(priceInSYP)
                    .priceInRequestedCurrency(priceInSYP)
                    .requestedCurrency(Currency.SYP)
                    .exchangeRate(BigDecimal.ONE)
                    .conversionTimestamp(LocalDateTime.now().format(TIMESTAMP_FORMATTER))
                    .rateSource("CONVERSION_FAILED_FALLBACK")
                    .build();
        }
    }
    
    /**
     * Convert a price from SYP to the requested currency (Float version for backward compatibility)
     */
    public CurrencyAwarePriceDTO convertPriceFromSYP(Float priceInSYP, Currency requestedCurrency) {
        if (priceInSYP == null) {
            return null;
        }
        return convertPriceFromSYP(BigDecimal.valueOf(priceInSYP), requestedCurrency);
    }
    
    /**
     * Convert a price from SYP to the requested currency (Double version for backward compatibility)
     */
    public CurrencyAwarePriceDTO convertPriceFromSYP(Double priceInSYP, Currency requestedCurrency) {
        if (priceInSYP == null) {
            return null;
        }
        return convertPriceFromSYP(BigDecimal.valueOf(priceInSYP), requestedCurrency);
    }
    
    /**
     * Convert multiple prices from SYP to the requested currency
     * 
     * @param pricesInSYP Array of prices in SYP
     * @param requestedCurrency The currency to convert to
     * @return Array of CurrencyAwarePriceDTO
     */
    public CurrencyAwarePriceDTO[] convertMultiplePricesFromSYP(BigDecimal[] pricesInSYP, Currency requestedCurrency) {
        if (pricesInSYP == null) {
            return null;
        }
        
        CurrencyAwarePriceDTO[] convertedPrices = new CurrencyAwarePriceDTO[pricesInSYP.length];
        for (int i = 0; i < pricesInSYP.length; i++) {
            convertedPrices[i] = convertPriceFromSYP(pricesInSYP[i], requestedCurrency);
        }
        return convertedPrices;
    }
    
    /**
     * Get the display price for a given currency (helper method)
     */
    public BigDecimal getDisplayPrice(BigDecimal priceInSYP, Currency requestedCurrency) {
        CurrencyAwarePriceDTO priceDTO = convertPriceFromSYP(priceInSYP, requestedCurrency);
        return priceDTO != null ? priceDTO.getDisplayPrice() : priceInSYP;
    }
    
    /**
     * Get the display price for a given currency (Float version)
     */
    public BigDecimal getDisplayPrice(Float priceInSYP, Currency requestedCurrency) {
        CurrencyAwarePriceDTO priceDTO = convertPriceFromSYP(priceInSYP, requestedCurrency);
        return priceDTO != null ? priceDTO.getDisplayPrice() : BigDecimal.valueOf(priceInSYP);
    }
}
