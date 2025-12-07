package com.Uqar.reports.service;

import com.Uqar.reports.dto.response.TopSoldProductsResponse;
import com.Uqar.reports.repository.AdminReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminReportService {

    private static final Logger logger = LoggerFactory.getLogger(AdminReportService.class);

    @Autowired
    private AdminReportRepository adminReportRepository;

    /**
     * Get top 10 sold products in a specific area
     * @param areaId The area ID to filter by
     * @param startDate Start date for the report period (optional)
     * @param endDate End date for the report period (optional)
     * @return List of top 10 sold products in the area
     */
    public List<TopSoldProductsResponse> getTopSoldProductsByArea(Long areaId, LocalDate startDate, LocalDate endDate) {
        try {
            logger.info("Fetching top sold products for area: {}, startDate: {}, endDate: {}", areaId, startDate, endDate);
            List<Map<String, Object>> results = adminReportRepository.findTopSoldProductsByArea(areaId, startDate, endDate);
            logger.info("Found {} results for area {}", results.size(), areaId);
            return mapToTopSoldProductsResponse(results);
        } catch (Exception e) {
            logger.error("Error fetching top sold products for area {}: {}", areaId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch top sold products for area: " + e.getMessage(), e);
        }
    }

    /**
     * Get top 10 sold products across all areas
     * @param startDate Start date for the report period (optional)
     * @param endDate End date for the report period (optional)
     * @return List of top 10 sold products across all areas
     */
    public List<TopSoldProductsResponse> getTopSoldProducts(LocalDate startDate, LocalDate endDate) {
        try {
            logger.info("Fetching top sold products for all areas, startDate: {}, endDate: {}", startDate, endDate);
            List<Map<String, Object>> results = adminReportRepository.findTopSoldProducts(startDate, endDate);
            logger.info("Found {} results for all areas", results.size());
            return mapToTopSoldProductsResponse(results);
        } catch (Exception e) {
            logger.error("Error fetching top sold products for all areas: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch top sold products: " + e.getMessage(), e);
        }
    }

    /**
     * Map database results to TopSoldProductsResponse DTOs
     * @param results List of Map results from database
     * @return List of TopSoldProductsResponse DTOs
     */
    private List<TopSoldProductsResponse> mapToTopSoldProductsResponse(List<Map<String, Object>> results) {
        return results.stream()
                .map(this::mapToTopSoldProductsResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map single database result to TopSoldProductsResponse DTO
     * @param result Map result from database
     * @return TopSoldProductsResponse DTO
     */
    private TopSoldProductsResponse mapToTopSoldProductsResponse(Map<String, Object> result) {
        Long productId = result.get("productId") != null ? ((Number) result.get("productId")).longValue() : null;
        String productName = (String) result.get("productName");
        String productCode = (String) result.get("productCode");
        String categoryName = (String) result.get("categoryName");
        String manufacturerName = (String) result.get("manufacturerName");
        Long totalQuantitySold = result.get("totalQuantitySold") != null ? ((Number) result.get("totalQuantitySold")).longValue() : 0L;
        BigDecimal totalRevenue = result.get("totalRevenue") != null ? BigDecimal.valueOf(((Number) result.get("totalRevenue")).doubleValue()) : BigDecimal.ZERO;
        Long areaId = result.get("areaId") != null ? ((Number) result.get("areaId")).longValue() : null;
        String areaName = (String) result.get("areaName");
        
        return new TopSoldProductsResponse(productId, productName, productCode, categoryName, 
                                         manufacturerName, totalQuantitySold, totalRevenue, areaId, areaName);
    }

    /**
     * Get total count of master products
     * @return Total count of master products
     */
    public Long getTotalMasterProductCount() {
        try {
            logger.info("Fetching total master product count");
            Long count = adminReportRepository.getTotalMasterProductCount();
            logger.info("Total master product count: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Error fetching total master product count: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch total master product count: " + e.getMessage(), e);
        }
    }

    /**
     * Get total count of pharmacies (excluding default pharmacy)
     * @return Total count of pharmacies minus 1
     */
    public Long getTotalPharmaciesCount() {
        try {
            logger.info("Fetching total pharmacies count (excluding default pharmacy)");
            Long count = adminReportRepository.getTotalPharmaciesCount();
            logger.info("Total pharmacies count (excluding default): {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Error fetching total pharmacies count: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch total pharmacies count: " + e.getMessage(), e);
        }
    }

    /**
     * Get total count of active users (excluding platform admin and default users)
     * @return Total count of active users minus platform admin and default users
     */
    public Long getTotalActiveUsersCount() {
        try {
            logger.info("Fetching total active users count (excluding platform admin and default users)");
            Long count = adminReportRepository.getTotalActiveUsersCount();
            logger.info("Total active users count (excluding platform admin and default): {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Error fetching total active users count: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch total active users count: " + e.getMessage(), e);
        }
    }
}
