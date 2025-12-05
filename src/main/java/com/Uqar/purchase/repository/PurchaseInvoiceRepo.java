package com.Uqar.purchase.repository;

import com.Uqar.purchase.entity.PurchaseInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseInvoiceRepo extends JpaRepository<PurchaseInvoice, Long> {
    
    /**
     * Find all purchase invoices by pharmacy ID
     */
    List<PurchaseInvoice> findByPharmacyId(Long pharmacyId);
    
    /**
     * Find all purchase invoices by pharmacy ID with pagination
     */
    Page<PurchaseInvoice> findByPharmacyId(Long pharmacyId, Pageable pageable);
    
    /**
     * Find purchase invoice by ID and pharmacy ID
     */
    java.util.Optional<PurchaseInvoice> findByIdAndPharmacyId(Long id, Long pharmacyId);
    
    // New methods for filtering by time range
    @Query("SELECT pi FROM PurchaseInvoice pi WHERE pi.pharmacy.id = :pharmacyId AND pi.createdAt BETWEEN :startDate AND :endDate")
    Page<PurchaseInvoice> findByPharmacyIdAndCreatedAtBetween(
        @Param("pharmacyId") Long pharmacyId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    // New methods for filtering by supplier
    Page<PurchaseInvoice> findByPharmacyIdAndSupplierId(Long pharmacyId, Long supplierId, Pageable pageable);
    
    /**
     * Count purchase invoices by pharmacy ID and supplier ID
     */
    long countByPharmacyIdAndSupplierId(Long pharmacyId, Long supplierId);
} 