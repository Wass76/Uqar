package com.Uqar.sale.repo;

import com.Uqar.sale.entity.SaleInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleInvoiceRepository extends JpaRepository<SaleInvoice, Long> {
    

    List<SaleInvoice> findByPharmacyId(Long pharmacyId);
    
  
    List<SaleInvoice> findByPharmacyIdOrderByInvoiceDateDesc(Long pharmacyId);
    
   
    Page<SaleInvoice> findByPharmacyId(Long pharmacyId, Pageable pageable);
    
  
    Optional<SaleInvoice> findByIdAndPharmacyId(Long id, Long pharmacyId);
    
    List<SaleInvoice> findByPharmacyIdAndInvoiceDateBetween(Long pharmacyId, LocalDateTime start, LocalDateTime end);
}
