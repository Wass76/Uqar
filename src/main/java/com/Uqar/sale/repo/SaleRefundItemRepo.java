package com.Uqar.sale.repo;

import com.Uqar.sale.entity.SaleRefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRefundItemRepo extends JpaRepository<SaleRefundItem, Long> {
    
    List<SaleRefundItem> findBySaleRefundId(Long saleRefundId);
    
    List<SaleRefundItem> findBySaleInvoiceItemId(Long saleInvoiceItemId);
}
