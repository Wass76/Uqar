package com.Uqar.reports.repository;

import com.Uqar.sale.entity.SaleInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface AdminReportRepository extends JpaRepository<SaleInvoice, Long> {

    /**
     * Find top 10 sold products in a specific area
     * @param areaId The area ID to filter by
     * @param startDate Start date for the report period (optional)
     * @param endDate End date for the report period (optional)
     * @return List of top 10 sold products in the area
     */
    @Query(value = "SELECT " +
           "sii.stock_item_id as productId, " +
           "sii.stock_item_product_name as productName, " +
           "sii.stock_item_barcode as productCode, " +
           "'N/A' as categoryName, " +
           "'N/A' as manufacturerName, " +
           "SUM(sii.quantity) as totalQuantitySold, " +
           "SUM(sii.sub_total) as totalRevenue, " +
           "p.area_id as areaId, " +
           "a.name as areaName " +
           "FROM sale_invoice_items sii " +
           "JOIN sale_invoices si ON sii.sale_invoice_id = si.id " +
           "JOIN pharmacies p ON si.pharmacy_id = p.id " +
           "JOIN areas a ON p.area_id = a.id " +
           "WHERE p.area_id = :areaId " +
           "AND (:startDate IS NULL OR DATE(si.invoice_date) >= :startDate) " +
           "AND (:endDate IS NULL OR DATE(si.invoice_date) <= :endDate) " +
           "AND si.status = 'SOLD' " +
           "GROUP BY sii.stock_item_id, sii.stock_item_product_name, sii.stock_item_barcode, " +
           "p.area_id, a.name " +
           "ORDER BY totalQuantitySold DESC " +
           "LIMIT 10", nativeQuery = true)
    List<Map<String, Object>> findTopSoldProductsByArea(
            @Param("areaId") Long areaId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find top 10 sold products across all areas
     * @param startDate Start date for the report period (optional)
     * @param endDate End date for the report period (optional)
     * @return List of top 10 sold products across all areas
     */
    @Query(value = "SELECT " +
           "sii.stock_item_id as productId, " +
           "sii.stock_item_product_name as productName, " +
           "sii.stock_item_barcode as productCode, " +
           "'N/A' as categoryName, " +
           "'N/A' as manufacturerName, " +
           "SUM(sii.quantity) as totalQuantitySold, " +
           "SUM(sii.sub_total) as totalRevenue, " +
           "p.area_id as areaId, " +
           "a.name as areaName " +
           "FROM sale_invoice_items sii " +
           "JOIN sale_invoices si ON sii.sale_invoice_id = si.id " +
           "JOIN pharmacies p ON si.pharmacy_id = p.id " +
           "JOIN areas a ON p.area_id = a.id " +
           "WHERE (:startDate IS NULL OR DATE(si.invoice_date) >= :startDate) " +
           "AND (:endDate IS NULL OR DATE(si.invoice_date) <= :endDate) " +
           "AND si.status = 'SOLD' " +
           "GROUP BY sii.stock_item_id, sii.stock_item_product_name, sii.stock_item_barcode, " +
           "p.area_id, a.name " +
           "ORDER BY totalQuantitySold DESC " +
           "LIMIT 10", nativeQuery = true)
    List<Map<String, Object>> findTopSoldProducts(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get total count of master products
     * @return Total count of master products
     */
    @Query("SELECT COUNT(m) FROM MasterProduct m")
    Long getTotalMasterProductCount();

    /**
     * Get total count of pharmacies (excluding default pharmacy with id = 1)
     * @return Total count of pharmacies minus 1
     */
    @Query("SELECT COUNT(p) - 1 FROM Pharmacy p")
    Long getTotalPharmaciesCount();

    /**
     * Get total count of active employees (excluding platform admin and default users)
     * @return Total count of active employees minus platform admin and default users
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = com.Uqar.user.Enum.UserStatus.ACTIVE")
    Long getTotalActiveUsersCount();
}
