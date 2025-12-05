package com.Uqar.moneybox.repository;

import com.Uqar.moneybox.entity.MoneyBoxTransaction;
import com.Uqar.moneybox.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MoneyBoxTransactionRepository extends JpaRepository<MoneyBoxTransaction, Long> {
    
    List<MoneyBoxTransaction> findByMoneyBoxIdOrderByCreatedAtDesc(Long moneyBoxId);
    
    List<MoneyBoxTransaction> findByMoneyBoxIdAndTransactionTypeOrderByCreatedAtDesc(
            Long moneyBoxId, TransactionType transactionType);
    
    List<MoneyBoxTransaction> findByMoneyBoxIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long moneyBoxId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Pagination methods
    Page<MoneyBoxTransaction> findByMoneyBoxIdOrderByCreatedAtDesc(Long moneyBoxId, Pageable pageable);
    
    Page<MoneyBoxTransaction> findByMoneyBoxIdAndTransactionTypeOrderByCreatedAtDesc(
            Long moneyBoxId, TransactionType transactionType, Pageable pageable);
    
    Page<MoneyBoxTransaction> findByMoneyBoxIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long moneyBoxId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT t FROM MoneyBoxTransaction t WHERE t.moneyBox.id = :moneyBoxId AND t.transactionType = :transactionType AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<MoneyBoxTransaction> findByMoneyBoxIdAndTransactionTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            @Param("moneyBoxId") Long moneyBoxId, 
            @Param("transactionType") TransactionType transactionType,
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            Pageable pageable);
    
    @Query("SELECT SUM(t.amount) FROM MoneyBoxTransaction t WHERE t.moneyBox.id = :moneyBoxId AND t.transactionType = :transactionType")
    BigDecimal getTotalAmountByType(@Param("moneyBoxId") Long moneyBoxId, @Param("transactionType") TransactionType transactionType);
    
    @Query("SELECT SUM(t.amount) FROM MoneyBoxTransaction t WHERE t.moneyBox.id = :moneyBoxId AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByPeriod(@Param("moneyBoxId") Long moneyBoxId, 
                                     @Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(t.amount) FROM MoneyBoxTransaction t WHERE t.moneyBox.id = :moneyBoxId AND t.transactionType = :transactionType AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByTypeAndPeriod(@Param("moneyBoxId") Long moneyBoxId, 
                                            @Param("transactionType") TransactionType transactionType,
                                            @Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM MoneyBoxTransaction t WHERE t.moneyBox.id = :moneyBoxId AND t.transactionType = :transactionType")
    Long countTransactionsByType(@Param("moneyBoxId") Long moneyBoxId, @Param("transactionType") TransactionType transactionType);
    
    List<MoneyBoxTransaction> findByReferenceIdAndReferenceType(String referenceId, String referenceType);
    
    // Enhanced audit query methods
    List<MoneyBoxTransaction> findByMoneyBoxIdAndCreatedAtBetween(Long moneyBoxId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Note: Using existing findByReferenceIdAndReferenceType instead of findByEntityTypeAndEntityId
    // List<MoneyBoxTransaction> findByReferenceIdAndReferenceType(String referenceId, String referenceType); // Already exists
    
    List<MoneyBoxTransaction> findByMoneyBoxIdAndOperationStatusAndCreatedAtBetween(
            Long moneyBoxId, String operationStatus, LocalDateTime startDate, LocalDateTime endDate);
    
    List<MoneyBoxTransaction> findByCreatedByAndCreatedAtBetween(
            Long createdBy, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT t FROM MoneyBoxTransaction t WHERE t.moneyBox.id = :moneyBoxId AND t.originalCurrency = :currency AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<MoneyBoxTransaction> findByMoneyBoxIdAndOriginalCurrencyAndCreatedAtBetween(
            @Param("moneyBoxId") Long moneyBoxId, 
            @Param("currency") com.Uqar.user.Enum.Currency currency,
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM MoneyBoxTransaction t WHERE t.moneyBox.id = :moneyBoxId AND t.operationStatus = :status AND t.createdAt BETWEEN :startDate AND :endDate")
    Long countTransactionsByStatusAndPeriod(
            @Param("moneyBoxId") Long moneyBoxId, 
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t.transactionType, COUNT(t), SUM(t.convertedAmount) FROM MoneyBoxTransaction t WHERE t.moneyBox.id = :moneyBoxId AND t.createdAt BETWEEN :startDate AND :endDate GROUP BY t.transactionType")
    List<Object[]> getTransactionSummaryByType(
            @Param("moneyBoxId") Long moneyBoxId, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    // Method to get the latest transaction for balance calculation
    @Query("SELECT t FROM MoneyBoxTransaction t WHERE t.moneyBox.id = :moneyBoxId ORDER BY t.createdAt DESC LIMIT 1")
    java.util.Optional<MoneyBoxTransaction> findTopByMoneyBoxIdOrderByCreatedAtDesc(@Param("moneyBoxId") Long moneyBoxId);
}
