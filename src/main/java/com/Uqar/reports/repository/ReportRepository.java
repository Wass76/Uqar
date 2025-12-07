package com.Uqar.reports.repository;

import com.Uqar.sale.entity.SaleInvoice;
import com.Uqar.sale.entity.SaleInvoiceItem;
import com.Uqar.purchase.entity.PurchaseInvoiceItem;
import com.Uqar.product.entity.PharmacyProduct;
import com.Uqar.product.entity.MasterProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Simplified Reports Repository
 * Implements only the specific queries agreed upon with the business team:
 * 1. Monthly Purchase Report (daily breakdown)
 * 2. Daily Purchase Report
 * 3. Monthly Profit Report (daily breakdown)
 * 4. Daily Profit Report
 * 5. Most Sold Categories Monthly
 * 6. Top 10 Products Monthly
 */
@Repository
public interface ReportRepository extends JpaRepository<SaleInvoice, Long> {
    
    // ============================================================================
    // PURCHASE REPORTS QUERIES
    // ============================================================================
    
    /**
     * Get monthly purchase daily breakdown
     * Returns purchase data for each day in the specified month
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "DATE(pi.createdAt) as date, " +
           "COUNT(pi) as totalInvoices, " +
           "SUM(pi.total) as totalAmount, " +
           "SUM(pi.total) as totalPaid, " +
           "AVG(pi.total) as averageAmount, " +
           "pi.currency as currency " +
           "FROM PurchaseInvoice pi " +
           "WHERE pi.pharmacy.id = :pharmacyId " +
           "AND DATE(pi.createdAt) BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(pi.createdAt), pi.currency " +
           "ORDER BY date")
    List<Map<String, Object>> getMonthlyPurchaseDailyBreakdown(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Get monthly purchase summary
     * Returns summary data for the entire month
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "COUNT(pi) as totalInvoices, " +
           "SUM(pi.total) as totalAmount, " +
           "SUM(pi.total) as totalPaid, " +
           "AVG(pi.total) as averageAmount, " +
           "pi.currency as currency " +
           "FROM PurchaseInvoice pi " +
           "WHERE pi.pharmacy.id = :pharmacyId " +
           "AND DATE(pi.createdAt) BETWEEN :startDate AND :endDate " +
           "GROUP BY pi.currency")
    List<Map<String, Object>> getMonthlyPurchaseSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Get daily purchase summary
     * Returns purchase data for a specific day
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "COUNT(pi) as totalInvoices, " +
           "SUM(pi.total) as totalAmount, " +
           "SUM(pi.total) as totalPaid, " +
           "AVG(pi.total) as averageAmount, " +
           "pi.currency as currency " +
           "FROM PurchaseInvoice pi " +
           "WHERE pi.pharmacy.id = :pharmacyId " +
           "AND DATE(pi.createdAt) = :date " +
           "GROUP BY pi.currency")
    List<Map<String, Object>> getDailyPurchaseSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("date") LocalDate date);
    
    /**
     * Get daily purchase items
     * Returns purchase items for a specific day
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "pii.productId as productName, " +
           "pii.receivedQty as quantity, " +
           "pii.invoicePrice as unitPrice, " +
           "(pii.receivedQty * pii.invoicePrice) as subTotal, " +
           "pi.supplier.name as supplierName, " +
           "pi.currency as currency " +
           "FROM PurchaseInvoiceItem pii " +
           "JOIN pii.purchaseInvoice pi " +
           "WHERE pi.pharmacy.id = :pharmacyId " +
           "AND DATE(pi.createdAt) = :date " +
           "ORDER BY (pii.receivedQty * pii.invoicePrice) DESC")
    List<Map<String, Object>> getDailyPurchaseItems(
            @Param("pharmacyId") Long pharmacyId,
            @Param("date") LocalDate date);
    
    // ============================================================================
    // PROFIT REPORTS QUERIES
    // ============================================================================
    
    /**
     * Get monthly profit daily breakdown
     * Returns profit data for each day in the specified month
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "DATE(si.invoiceDate) as date, " +
           "COUNT(si) as totalInvoices, " +
           "SUM(si.totalAmount) as totalRevenue, " +
           "SUM(CASE " +
           "  WHEN sii.stockItem.actualPurchasePrice IS NULL THEN sii.subTotal " +
           "  ELSE (sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) " +
           "END) as totalProfit, " +
           "AVG(si.totalAmount) as averageRevenue, " +
           "si.currency as currency " +
           "FROM SaleInvoice si " +
           "JOIN si.items sii " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "GROUP BY DATE(si.invoiceDate), si.currency " +
           "ORDER BY date")
    List<Map<String, Object>> getMonthlyProfitDailyBreakdown(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Get monthly profit summary
     * Returns summary data for the entire month
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "COUNT(si) as totalInvoices, " +
           "SUM(si.totalAmount) as totalRevenue, " +
           "SUM(sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) as totalProfit, " +
           "AVG(si.totalAmount) as averageRevenue, " +
           "si.currency as currency " +
           "FROM SaleInvoice si " +
           "JOIN si.items sii " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "GROUP BY si.currency")
    List<Map<String, Object>> getMonthlyProfitSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Get daily profit summary
     * Returns profit data for a specific day
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "COUNT(si) as totalInvoices, " +
           "SUM(si.totalAmount) as totalRevenue, " +
           "SUM(CASE " +
           "  WHEN sii.stockItem.actualPurchasePrice IS NULL THEN sii.subTotal " +
           "  ELSE (sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) " +
           "END) as totalProfit, " +
           "AVG(si.totalAmount) as averageRevenue, " +
           "si.currency as currency " +
           "FROM SaleInvoice si " +
           "JOIN si.items sii " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) = :date " +
           "AND si.status = 'SOLD' " +
           "GROUP BY si.currency")
    List<Map<String, Object>> getDailyProfitSummary(
            @Param("pharmacyId") Long pharmacyId,
            @Param("date") LocalDate date);
    
    /**
     * Get daily profit items
     * Returns profit items for a specific day
     * Note: Currency conversion will be handled in the service layer
     */
    @Query("SELECT " +
           "sii.stockItem.productId as productId, " +
           "sii.stockItem.productType as productType, " +
           "sii.stockItem.productName as productName, " +
           "sii.quantity as quantity, " +
           "sii.subTotal as revenue, " +
           "CASE " +
           "  WHEN sii.stockItem.actualPurchasePrice IS NULL THEN sii.subTotal " +
           "  ELSE (sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) " +
           "END as profit, " +
           "si.currency as currency " +
           "FROM SaleInvoiceItem sii " +
           "JOIN sii.saleInvoice si " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) = :date " +
           "AND si.status = 'SOLD' " +
           "ORDER BY " +
           "CASE " +
           "  WHEN sii.stockItem.actualPurchasePrice IS NULL THEN sii.subTotal " +
           "  ELSE (sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) " +
           "END DESC")
    List<Map<String, Object>> getDailyProfitItems(
            @Param("pharmacyId") Long pharmacyId,
            @Param("date") LocalDate date);
    
    // ============================================================================
    // CATEGORY AND PRODUCT REPORTS QUERIES
    // ============================================================================
    
    /**
     * Get most sold categories for MasterProduct
     * Returns categories from MasterProduct sales with language support
     */
    @Query("SELECT " +
           "COALESCE(ct.name, c.name) as categoryName, " +
           "SUM(sii.quantity) as totalQuantity, " +
           "SUM(sii.subTotal) as totalRevenue, " +
           "COUNT(DISTINCT si) as invoiceCount " +
           "FROM SaleInvoiceItem sii " +
           "JOIN sii.saleInvoice si " +
           "JOIN sii.stockItem st " +
           "JOIN MasterProduct mp ON st.productId = mp.id " +
           "JOIN mp.categories c " +
           "LEFT JOIN CategoryTranslation ct ON (ct.category.id = c.id AND (:languageId IS NULL OR ct.language.id = :languageId)) " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "AND st.productType = com.Uqar.product.Enum.ProductType.MASTER " +
           "GROUP BY COALESCE(ct.name, c.name) " +
           "ORDER BY totalQuantity DESC")
    List<Map<String, Object>> getMostSoldCategoriesFromMasterProduct(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("languageId") Long languageId);
    
    /**
     * Get most sold categories for PharmacyProduct
     * Returns categories from PharmacyProduct sales with language support
     */
    @Query("SELECT " +
           "COALESCE(ct.name, c.name) as categoryName, " +
           "SUM(sii.quantity) as totalQuantity, " +
           "SUM(sii.subTotal) as totalRevenue, " +
           "COUNT(DISTINCT si) as invoiceCount " +
           "FROM SaleInvoiceItem sii " +
           "JOIN sii.saleInvoice si " +
           "JOIN sii.stockItem st " +
           "JOIN PharmacyProduct pp ON st.productId = pp.id " +
           "JOIN pp.categories c " +
           "LEFT JOIN CategoryTranslation ct ON (ct.category.id = c.id AND (:languageId IS NULL OR ct.language.id = :languageId)) " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "AND st.productType = com.Uqar.product.Enum.ProductType.PHARMACY " +
           "GROUP BY COALESCE(ct.name, c.name) " +
           "ORDER BY totalQuantity DESC")
    List<Map<String, Object>> getMostSoldCategoriesFromPharmacyProduct(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("languageId") Long languageId);
    
    /**
     * Get most sold categories monthly
     * Returns the most sold categories in the pharmacy for the specified month
     * Uses JPQL to handle entity relationships properly
     */
    @Query("SELECT " +
           "COALESCE(c.name, 'Uncategorized') as categoryName, " +
           "SUM(sii.quantity) as totalQuantity, " +
           "SUM(sii.subTotal) as totalRevenue, " +
           "COUNT(DISTINCT si) as invoiceCount " +
           "FROM SaleInvoiceItem sii " +
           "JOIN sii.saleInvoice si " +
           "JOIN sii.stockItem st " +
           "LEFT JOIN MasterProduct mp ON st.productId = mp.id " +
           "LEFT JOIN PharmacyProduct pp ON st.productId = pp.id " +
           "LEFT JOIN mp.categories c " +
           "LEFT JOIN pp.categories c2 " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "AND (st.productType = com.Uqar.product.Enum.ProductType.MASTER OR st.productType = com.Uqar.product.Enum.ProductType.PHARMACY) " +
           "GROUP BY COALESCE(c.name, c2.name, 'Uncategorized') " +
           "ORDER BY totalQuantity DESC")
    List<Map<String, Object>> getMostSoldCategories(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * Get top 10 products monthly
     * Returns the top 10 most sold products in the pharmacy for the specified month
     * Includes profit calculations and currency information
     */
    @Query("SELECT " +
           "sii.stockItem.productId as productId, " +
           "sii.stockItem.productType as productType, " +
           "sii.stockItem.productName as productName, " +
           "SUM(sii.quantity) as totalQuantity, " +
           "SUM(sii.subTotal) as totalRevenue, " +
           "AVG(sii.unitPrice) as averagePrice, " +
           "SUM(CASE " +
           "  WHEN sii.stockItem.actualPurchasePrice IS NULL THEN sii.subTotal " +
           "  ELSE (sii.subTotal - (sii.quantity * sii.stockItem.actualPurchasePrice)) " +
           "END) as totalProfit, " +
           "COUNT(DISTINCT si) as invoiceCount, " +
           "si.currency as currency " +
           "FROM SaleInvoiceItem sii " +
           "JOIN sii.saleInvoice si " +
           "WHERE si.pharmacy.id = :pharmacyId " +
           "AND DATE(si.invoiceDate) BETWEEN :startDate AND :endDate " +
           "AND si.status = 'SOLD' " +
           "GROUP BY sii.stockItem.productId, sii.stockItem.productType, sii.stockItem.productName, si.currency " +
           "ORDER BY totalQuantity DESC")
    List<Map<String, Object>> getTop10Products(
            @Param("pharmacyId") Long pharmacyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
