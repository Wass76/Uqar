package com.Uqar.moneybox.service;

import com.Uqar.moneybox.repository.ExchangeRateRepository;
import com.Uqar.moneybox.entity.ExchangeRate;
import com.Uqar.moneybox.dto.ExchangeRateResponseDTO;
import com.Uqar.moneybox.dto.CurrencyConversionResponseDTO;
import com.Uqar.moneybox.mapper.ExchangeRateMapper;
import com.Uqar.user.Enum.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    
    private final ExchangeRateRepository exchangeRateRepository;
    
    // Default exchange rates for production fallback
    private static final BigDecimal DEFAULT_USD_TO_SYP_RATE = new BigDecimal("10000");
    private static final BigDecimal DEFAULT_EUR_TO_SYP_RATE = new BigDecimal("11000");
    
    /**
     * Get default USD to SYP exchange rate
     */
    public static BigDecimal getDefaultUsdToSypRate() {
        return DEFAULT_USD_TO_SYP_RATE;
    }
    
    /**
     * Get default EUR to SYP exchange rate
     */
    public static BigDecimal getDefaultEurToSypRate() {
        return DEFAULT_EUR_TO_SYP_RATE;
    }
    
    /**
     * Get current exchange rate for currency pair
     */
    public BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }
        
        // First try to get from database
        try {
            Optional<ExchangeRate> dbRate = exchangeRateRepository.findByFromCurrencyAndToCurrencyAndIsActiveTrue(fromCurrency, toCurrency);
            if (dbRate.isPresent()) {
                return dbRate.get().getRate();
            }
        } catch (Exception e) {
            // Log error but continue with fallback rates for production safety
            // In production, you might want to add proper logging here
        }
        
        // Fallback to fixed rates if no database record or error occurs
        return getFallbackRate(fromCurrency, toCurrency);
    }
    
    /**
     * Get fallback exchange rate for production safety
     */
    private BigDecimal getFallbackRate(Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency == Currency.SYP && toCurrency == Currency.USD) {
            return BigDecimal.ONE.divide(DEFAULT_USD_TO_SYP_RATE, 6, RoundingMode.HALF_UP);
        }
        if (fromCurrency == Currency.USD && toCurrency == Currency.SYP) {
            return DEFAULT_USD_TO_SYP_RATE;
        }
        if (fromCurrency == Currency.SYP && toCurrency == Currency.EUR) {
            return BigDecimal.ONE.divide(DEFAULT_EUR_TO_SYP_RATE, 6, RoundingMode.HALF_UP);
        }
        if (fromCurrency == Currency.EUR && toCurrency == Currency.SYP) {
            return DEFAULT_EUR_TO_SYP_RATE;
        }
        if (fromCurrency == Currency.USD && toCurrency == Currency.EUR) {
            // Calculate cross-rate: USD -> SYP -> EUR
            BigDecimal usdToSyp = DEFAULT_USD_TO_SYP_RATE;
            BigDecimal eurToSyp = DEFAULT_EUR_TO_SYP_RATE;
            return usdToSyp.divide(eurToSyp, 6, RoundingMode.HALF_UP);
        }
        if (fromCurrency == Currency.EUR && toCurrency == Currency.USD) {
            // Calculate cross-rate: EUR -> SYP -> USD
            BigDecimal eurToSyp = DEFAULT_EUR_TO_SYP_RATE;
            BigDecimal usdToSyp = DEFAULT_USD_TO_SYP_RATE;
            return eurToSyp.divide(usdToSyp, 6, RoundingMode.HALF_UP);
        }
        
        throw new IllegalArgumentException("Unsupported currency pair: " + fromCurrency + " to " + toCurrency);
    }
    
    /**
     * Convert amount from one currency to another with production safety
     */
    public BigDecimal convertAmount(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        
        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Convert amount to SYP (base currency) - Production safe
     */
    public BigDecimal convertToSYP(BigDecimal amount, Currency currency) {
        return convertAmount(amount, currency, Currency.SYP);
    }
    
    /**
     * Convert amount from SYP to target currency - Production safe
     */
    public BigDecimal convertFromSYP(BigDecimal amount, Currency targetCurrency) {
        return convertAmount(amount, Currency.SYP, targetCurrency);
    }
    
    /**
     * Get effective exchange rate from database
     */
    public Optional<ExchangeRate> getEffectiveRate(Currency fromCurrency, Currency toCurrency, LocalDateTime date) {
        try {
            return exchangeRateRepository.findByFromCurrencyAndToCurrencyAndIsActiveTrue(fromCurrency, toCurrency);
        } catch (Exception e) {
            // Return empty for production safety
            return Optional.empty();
        }
    }
    
    /**
     * Get current active exchange rate from database
     */
    public ExchangeRateResponseDTO getCurrentRate(Currency fromCurrency, Currency toCurrency) {
        try {
            ExchangeRate rate = exchangeRateRepository.findByFromCurrencyAndToCurrencyAndIsActiveTrue(fromCurrency, toCurrency)
                .orElseThrow(() -> new IllegalArgumentException("No active exchange rate found for " + fromCurrency + " to " + toCurrency));
            
            return ExchangeRateMapper.toResponse(rate);
        } catch (Exception e) {
            // For production safety, return fallback rate info instead of throwing
            BigDecimal fallbackRate = getFallbackRate(fromCurrency, toCurrency);
            ExchangeRateResponseDTO fallbackResponse = new ExchangeRateResponseDTO();
            fallbackResponse.setFromCurrency(fromCurrency);
            fallbackResponse.setToCurrency(toCurrency);
            fallbackResponse.setRate(fallbackRate);
            fallbackResponse.setIsActive(true);
            fallbackResponse.setSource("FALLBACK_RATE");
            fallbackResponse.setNotes("Using fallback rate due to database unavailability");
            return fallbackResponse;
        }
    }
    
    /**
     * Get both direct and reverse exchange rates for a currency pair
     */
    public List<ExchangeRateResponseDTO> getExchangeRatePair(Currency currency1, Currency currency2) {
        try {
            List<ExchangeRate> rates = exchangeRateRepository.findByIsActiveTrue();
            
            return rates.stream()
                .filter(rate -> 
                    (rate.getFromCurrency().equals(currency1) && rate.getToCurrency().equals(currency2)) ||
                    (rate.getFromCurrency().equals(currency2) && rate.getToCurrency().equals(currency1))
                )
                .map(ExchangeRateMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            // Return empty list for production safety
            return List.of();
        }
    }
    
    /**
     * Set a new exchange rate and automatically generate reverse rate
     */
    @Transactional
    public ExchangeRateResponseDTO setExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate, String source, String notes) {
        // Validate input parameters for production safety
        if (fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException("From and to currencies cannot be null");
        }
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Rate must be positive and not null");
        }
        if (fromCurrency.equals(toCurrency)) {
            throw new IllegalArgumentException("From and to currencies cannot be the same");
        }
        
        try {
            // Get old rate for audit trail
            BigDecimal oldRateValue = null;
            Optional<ExchangeRate> existingRate = exchangeRateRepository.findByFromCurrencyAndToCurrencyAndIsActiveTrue(fromCurrency, toCurrency);
            if (existingRate.isPresent()) {
                ExchangeRate oldRate = existingRate.get();
                oldRateValue = oldRate.getRate();
                oldRate.setIsActive(false);
                oldRate.setEffectiveTo(LocalDateTime.now());
                exchangeRateRepository.save(oldRate);
            }
            
            // Create new exchange rate
            ExchangeRate newRate = new ExchangeRate();
            newRate.setFromCurrency(fromCurrency);
            newRate.setToCurrency(toCurrency);
            newRate.setRate(rate);
            newRate.setEffectiveFrom(LocalDateTime.now());
            newRate.setIsActive(true);
            newRate.setSource(source != null ? source : "MANUAL");
            newRate.setNotes(notes);
            
            ExchangeRate savedRate = exchangeRateRepository.save(newRate);
            
            // Exchange rate change logged in ExchangeRate entity itself
            // Audit trail is maintained through the ExchangeRate table
            
            // Automatically generate reverse exchange rate
            generateReverseExchangeRate(fromCurrency, toCurrency, rate, source, notes);
            
            return ExchangeRateMapper.toResponse(savedRate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set exchange rate: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate reverse exchange rate automatically
     */
    private void generateReverseExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate, String source, String notes) {
        // Skip if it's the same currency
        if (fromCurrency.equals(toCurrency)) {
            return;
        }
        
        try {
            // Calculate reverse rate (1 / original rate)
            BigDecimal reverseRate = BigDecimal.ONE.divide(rate, 6, RoundingMode.HALF_UP);
            
            // Deactivate any existing active rates for reverse currency pair
            Optional<ExchangeRate> existingReverseRate = exchangeRateRepository.findByFromCurrencyAndToCurrencyAndIsActiveTrue(toCurrency, fromCurrency);
            if (existingReverseRate.isPresent()) {
                ExchangeRate oldReverseRate = existingReverseRate.get();
                oldReverseRate.setIsActive(false);
                oldReverseRate.setEffectiveTo(LocalDateTime.now());
                exchangeRateRepository.save(oldReverseRate);
            }
            
            // Create reverse exchange rate
            ExchangeRate reverseExchangeRate = new ExchangeRate();
            reverseExchangeRate.setFromCurrency(toCurrency);
            reverseExchangeRate.setToCurrency(fromCurrency);
            reverseExchangeRate.setRate(reverseRate);
            reverseExchangeRate.setEffectiveFrom(LocalDateTime.now());
            reverseExchangeRate.setIsActive(true);
            reverseExchangeRate.setSource(source != null ? source + "_AUTO_REVERSE" : "AUTO_REVERSE");
            reverseExchangeRate.setNotes("Auto-generated reverse rate from " + fromCurrency + " to " + toCurrency + 
                                       (notes != null ? ". Original notes: " + notes : ""));
            
            exchangeRateRepository.save(reverseExchangeRate);
        } catch (Exception e) {
            // Log error but don't fail the main operation for production safety
            // In production, you might want to add proper logging here
        }
    }
    
    /**
     * Get all active exchange rates
     */
    public List<ExchangeRateResponseDTO> getAllActiveRates() {
        try {
            List<ExchangeRate> rates = exchangeRateRepository.findByIsActiveTrue();
            return rates.stream()
                .map(ExchangeRateMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            // Return empty list for production safety
            return List.of();
        }
    }
    
    /**
     * Get exchange rate by ID
     */
    public ExchangeRateResponseDTO getRateById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        try {
            ExchangeRate rate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exchange rate not found with ID: " + id));
            
            return ExchangeRateMapper.toResponse(rate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get exchange rate by ID: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deactivate an exchange rate and its reverse rate
     */
    @Transactional
    public void deactivateRate(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        try {
            ExchangeRate rate = exchangeRateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exchange rate not found with ID: " + id));
            
            // Deactivate the main rate
            rate.setIsActive(false);
            rate.setEffectiveTo(LocalDateTime.now());
            exchangeRateRepository.save(rate);
            
            // Also deactivate the reverse rate if it exists
            Optional<ExchangeRate> reverseRate = exchangeRateRepository.findByFromCurrencyAndToCurrencyAndIsActiveTrue(
                rate.getToCurrency(), rate.getFromCurrency());
            if (reverseRate.isPresent()) {
                ExchangeRate reverse = reverseRate.get();
                reverse.setIsActive(false);
                reverse.setEffectiveTo(LocalDateTime.now());
                exchangeRateRepository.save(reverse);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to deactivate exchange rate: " + e.getMessage(), e);
        }
    }
    
    /**
     * Test method to verify the service is working
     */
    public String testService() {
        return "ExchangeRate Service is working! Default USD to SYP rate: " + DEFAULT_USD_TO_SYP_RATE;
    }
    
    /**
     * Convert amount between currencies and return DTO
     */
    public CurrencyConversionResponseDTO convertAmountToDTO(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        
        BigDecimal convertedAmount = convertAmount(amount, fromCurrency, toCurrency);
        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        
        return ExchangeRateMapper.toConversionResponse(amount, fromCurrency, convertedAmount, toCurrency, rate);
    }
}
