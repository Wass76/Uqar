package com.Uqar.sale.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Uqar.sale.entity.SaleInvoiceItem;

@Repository
public interface SaleInvoiceItemRepository extends JpaRepository<SaleInvoiceItem, Long> {
    // حذف جميع عناصر فاتورة بيع معينة
    void deleteBySaleInvoiceId(Long saleInvoiceId);
    
    // التحقق من وجود عناصر مبيعات مرتبطة بـ StockItem
    @Query("SELECT sii.stockItem.id FROM SaleInvoiceItem sii WHERE sii.stockItem.id IN :stockItemIds")
    List<Long> findStockItemIdsReferencedInSales(@Param("stockItemIds") List<Long> stockItemIds);
    
    // التحقق من وجود عنصر مبيعات مرتبط بـ StockItem محدد
    boolean existsByStockItemId(Long stockItemId);
} 