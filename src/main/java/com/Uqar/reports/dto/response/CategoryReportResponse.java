package com.Uqar.reports.dto.response;

import com.Uqar.reports.enums.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Category Report Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryReportResponse {
    
    private boolean success;
    private Long pharmacyId;
    private LocalDateTime generatedAt;
    private Language language;
    
    // Report period
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Report data
    private List<CategoryData> categories;
    
    // Error information
    private String error;
    
    // Inner classes
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryData {
        private String categoryName;
        private Long totalQuantity;
        private Double totalRevenue;
        private Long invoiceCount;
        private Double averageQuantity;
        private Double averageRevenue;
    }
}
