package com.Uqar.reports.dto.response;

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
 * Profit Report Response DTOs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitReportResponse {
    
    private boolean success;
    private Long pharmacyId;
    private LocalDateTime generatedAt;
    private Currency currency;
    private Language language;
    
    // For monthly reports
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DailyProfitData> dailyData;
    private ProfitSummary summary;
    
    // For daily reports
    private LocalDate date;
    private DailyProfitData data;
    private List<ProfitItem> items;
    
    // Error information
    private String error;
    
    // Inner classes
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyProfitData {
        private LocalDate date;
        private Long totalInvoices;
        private Double totalRevenue;
        private Double totalCost;
        private Double totalProfit;
        private Double profitMargin;
        private Double averageRevenue;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfitSummary {
        private Long totalInvoices;
        private Double totalRevenue;
        private Double totalCost;
        private Double totalProfit;
        private Double profitMargin;
        private Double averageProfit;
        private Double averageRevenue;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfitItem {
        private String productName;
        private Integer quantity;
        private Double revenue;
        private Double cost;
        private Double profit;
        private Double profitMargin;
    }
}
