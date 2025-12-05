package com.Uqar.sale.repo;

import com.Uqar.sale.entity.SaleRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRefundRepo extends JpaRepository<SaleRefund, Long> {
    
    List<SaleRefund> findByPharmacyIdOrderByRefundDateDesc(Long pharmacyId);
    
    List<SaleRefund> findBySaleInvoiceIdAndPharmacyId(Long saleInvoiceId, Long pharmacyId);
    
    @Query("SELECT sr FROM SaleRefund sr WHERE sr.pharmacy.id = :pharmacyId AND sr.refundDate BETWEEN :startDate AND :endDate")
    List<SaleRefund> findByPharmacyIdAndRefundDateBetween(
        @Param("pharmacyId") Long pharmacyId,
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate
    );
    
    boolean existsBySaleInvoiceIdAndPharmacyId(Long saleInvoiceId, Long pharmacyId);
    
    // تابع للبحث عن مرتجع حسب ID والصيدلية
    @Query("SELECT sr FROM SaleRefund sr WHERE sr.id = :refundId AND sr.pharmacy.id = :pharmacyId")
    java.util.Optional<SaleRefund> findByIdAndPharmacyId(@Param("refundId") Long refundId, @Param("pharmacyId") Long pharmacyId);
}
