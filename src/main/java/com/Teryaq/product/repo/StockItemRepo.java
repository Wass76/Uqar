package com.Teryaq.product.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.entity.StockItem;

public interface StockItemRepo extends JpaRepository<StockItem, Long> {
    
    List<StockItem> findByProductId(Long productId);
    
    List<StockItem> findByPharmacyId(Long pharmacyId);
    
    List<StockItem> findByProductIdAndPharmacyId(Long productId, Long pharmacyId);
    
    List<StockItem> findByProductIdAndProductTypeAndPharmacyId(Long productId, ProductType productType, Long pharmacyId);
    
    @Query("SELECT COUNT(si) FROM StockItem si WHERE si.productId = :productId AND si.productType = :productType AND si.pharmacy.id = :pharmacyId")
    Long countByProductIdAndProductTypeAndPharmacyId(@Param("productId") Long productId, @Param("productType") ProductType productType, @Param("pharmacyId") Long pharmacyId);
    
    List<StockItem> findByProductTypeAndPharmacyId(ProductType productType, Long pharmacyId);
    
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockItem s WHERE s.productId = :productId AND s.pharmacy.id = :pharmacyId AND s.quantity > 0 AND s.productType = :productType")
    Integer getTotalQuantity(@Param("productId") Long productId, @Param("pharmacyId") Long pharmacyId, @Param("productType") ProductType productType);
    
    @Query("SELECT s FROM StockItem s WHERE s.expiryDate < :date AND s.pharmacy.id = :pharmacyId AND s.quantity > 0")
    List<StockItem> findExpiredItems(@Param("date") LocalDate date, @Param("pharmacyId") Long pharmacyId);
    
    @Query("SELECT s FROM StockItem s WHERE s.expiryDate > :startDate AND s.expiryDate <= :endDate AND s.pharmacy.id = :pharmacyId AND s.quantity > 0")
    List<StockItem> findItemsExpiringSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("pharmacyId") Long pharmacyId);
    
    @Query("SELECT s FROM StockItem s WHERE s.productId = :productId AND s.pharmacy.id = :pharmacyId AND s.quantity > :minQuantity AND s.expiryDate > :date ORDER BY s.createdAt ASC")
    List<StockItem> findByProductIdAndQuantity(
        @Param("productId") Long productId, 
        @Param("pharmacyId") Long pharmacyId, 
        @Param("minQuantity") Integer minQuantity, 
        @Param("date") LocalDate date);
    
    @Query("""
        SELECT si FROM StockItem si
        LEFT JOIN si.purchaseInvoice pi
        WHERE si.pharmacy.id = :pharmacyId
          AND (
            LOWER(si.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(si.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR EXISTS (
                SELECT 1 FROM PharmacyProduct pp 
                WHERE pp.id = si.productId 
                AND si.productType = com.Teryaq.product.Enum.ProductType.PHARMACY
                AND (
                    LOWER(pp.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR EXISTS (
                        SELECT 1 FROM PharmacyProductBarcode ppb 
                        WHERE ppb.product.id = pp.id 
                        AND LOWER(ppb.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
                    OR EXISTS (
                        SELECT 1 FROM PharmacyProductTranslation ppt 
                        WHERE ppt.product.id = pp.id 
                        AND (
                            LOWER(ppt.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(ppt.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        )
                    )
                )
            )
            OR EXISTS (
                SELECT 1 FROM MasterProduct mp 
                WHERE mp.id = si.productId 
                AND si.productType = com.Teryaq.product.Enum.ProductType.MASTER
                AND (
                    LOWER(mp.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(mp.barcode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR EXISTS (
                        SELECT 1 FROM MasterProductTranslation mpt 
                        WHERE mpt.product.id = mp.id 
                        AND (
                            LOWER(mpt.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                            OR LOWER(mpt.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        )
                    )
                )
            )
          )
        """)
        List<StockItem> searchStockItems(
            @Param("keyword") String keyword,
            @Param("pharmacyId") Long pharmacyId);
    
    List<StockItem> findByBarcodeAndPharmacyId(String barcode, Long pharmacyId);
    
    List<StockItem> findByProductNameContainingIgnoreCaseAndPharmacyId(String productName, Long pharmacyId);
    

    
    @Query("""
        SELECT DISTINCT si.productId, si.productType
        FROM StockItem si
        WHERE si.pharmacy.id = :pharmacyId
        ORDER BY si.productType, si.productId
        """)
    List<Object[]> findUniqueProductsByPharmacyId(@Param("pharmacyId") Long pharmacyId);
    
    @Query("""
        SELECT DISTINCT si.productId, si.productType
        FROM StockItem si
        WHERE si.pharmacy.id = :pharmacyId
        ORDER BY si.productType, si.productId
        """)
    List<Object[]> findUniqueProductsCombined(@Param("pharmacyId") Long pharmacyId);
    
    @Query("SELECT COUNT(si) > 0 FROM StockItem si WHERE si.productId = :productId AND si.productType = :productType")
    Boolean existsByProductIdAndProductType(@Param("productId") Long productId, @Param("productType") ProductType productType);
    
    @Query("SELECT si FROM StockItem si WHERE si.productId = :productId AND si.productType = :productType ORDER BY si.createdAt DESC")
    List<StockItem> findByProductIdAndProductTypeOrderByDateAddedDesc(@Param("productId") Long productId, @Param("productType") ProductType productType);
}
            