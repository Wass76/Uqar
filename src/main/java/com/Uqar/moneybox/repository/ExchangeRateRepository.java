package com.Uqar.moneybox.repository;

import com.Uqar.moneybox.entity.ExchangeRate;
import com.Uqar.user.Enum.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    
    Optional<ExchangeRate> findByFromCurrencyAndToCurrencyAndIsActiveTrue(Currency fromCurrency, Currency toCurrency);
    
    List<ExchangeRate> findByFromCurrencyAndIsActiveTrue(Currency fromCurrency);
    
    List<ExchangeRate> findByToCurrencyAndIsActiveTrue(Currency toCurrency);
    
    List<ExchangeRate> findByIsActiveTrue();
    
    @Query("SELECT e FROM ExchangeRate e WHERE e.isActive = true AND (e.fromCurrency = :currency OR e.toCurrency = :currency)")
    List<ExchangeRate> findActiveRatesByCurrency(@Param("currency") Currency currency);
    
    @Query("SELECT COUNT(e) FROM ExchangeRate e WHERE e.isActive = true AND e.fromCurrency = :fromCurrency AND e.toCurrency = :toCurrency")
    Long countActiveRates(@Param("fromCurrency") Currency fromCurrency, @Param("toCurrency") Currency toCurrency);
}
