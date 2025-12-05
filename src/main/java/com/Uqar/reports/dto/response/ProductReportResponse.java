package com.Uqar.reports.dto.response;

import com.Uqar.product.Enum.ProductType;
import com.Uqar.reports.enums.Language;
import com.Uqar.user.Enum.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Product Report Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReportResponse {
    
    private boolean success;
    private Long pharmacyId;
    private LocalDateTime generatedAt;
    private Language language;
    private Currency currency;
    
    // Report period
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Report data
    private List<ProductData> products;
    
    // Error information
    private String error;
    
    // Inner classes
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductData {
        private Long productId;
        private ProductType productType;
        private String productName;
        private Long totalSales;
        private Double totalRevenue;
        private Integer totalQuantity;
        private Double averagePrice;
        private Double profit;
        private Double profitMargin;
    }
}
