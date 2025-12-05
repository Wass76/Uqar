package com.Uqar.purchase.repository;

import com.Uqar.purchase.entity.PurchaseOrder;
import com.Uqar.product.Enum.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseOrderRepo extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByStatus(OrderStatus status);
    Page<PurchaseOrder> findByStatus(OrderStatus status, Pageable pageable);
    List<PurchaseOrder> findByPharmacyId(Long pharmacyId);
    Page<PurchaseOrder> findByPharmacyId(Long pharmacyId, Pageable pageable);
    List<PurchaseOrder> findByPharmacyIdAndStatus(Long pharmacyId, OrderStatus status);
    Page<PurchaseOrder> findByPharmacyIdAndStatus(Long pharmacyId, OrderStatus status, Pageable pageable);
    
    // New methods for filtering by time range
    @Query("SELECT po FROM PurchaseOrder po WHERE po.pharmacy.id = :pharmacyId AND po.createdAt BETWEEN :startDate AND :endDate")
    Page<PurchaseOrder> findByPharmacyIdAndCreatedAtBetween(
        @Param("pharmacyId") Long pharmacyId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    // New methods for filtering by supplier
    Page<PurchaseOrder> findByPharmacyIdAndSupplierId(Long pharmacyId, Long supplierId, Pageable pageable);

    /**
     * Count purchase invoices by pharmacy ID and supplier ID
     */
    long countByPharmacyIdAndSupplierId(Long pharmacyId, Long supplierId);

} 