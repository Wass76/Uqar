package com.Uqar.moneybox.repository;

import com.Uqar.moneybox.entity.MoneyBox;
import com.Uqar.moneybox.enums.MoneyBoxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MoneyBoxRepository extends JpaRepository<MoneyBox, Long> {
    
    Optional<MoneyBox> findByPharmacyId(Long pharmacyId);
    
    List<MoneyBox> findByStatus(MoneyBoxStatus status);
    
    @Query("SELECT m FROM MoneyBox m WHERE m.pharmacyId = :pharmacyId AND m.status = :status")
    Optional<MoneyBox> findByPharmacyIdAndStatus(@Param("pharmacyId") Long pharmacyId, @Param("status") MoneyBoxStatus status);
    
    @Query("SELECT SUM(m.currentBalance) FROM MoneyBox m WHERE m.status = :status")
    Double getTotalBalanceByStatus(@Param("status") MoneyBoxStatus status);
    
    @Query("SELECT COUNT(m) FROM MoneyBox m WHERE m.pharmacyId = :pharmacyId")
    Long countByPharmacyId(@Param("pharmacyId") Long pharmacyId);
}
