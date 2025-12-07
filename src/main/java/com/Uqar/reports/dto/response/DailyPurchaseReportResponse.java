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
 * Daily Purchase Report Response DTO
 * Specific response structure for daily purchase reports
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPurchaseReportResponse {
    
    private boolean success;
    private Long pharmacyId;
    private LocalDateTime generatedAt;
    private Currency currency;
    private Language language;
    
    // Daily report specific fields
    private LocalDate date;
    private DailyPurchaseData data;
    private List<PurchaseItem> items;
    
    // Error information
    private String error;
    
    // Inner classes
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPurchaseData {
        private LocalDate date;
        private Long totalInvoices;
        private Double totalAmount;
        private Double totalPaid;
        private Double averageAmount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseItem {
        private String productName;
        private Integer quantity;
        private Double unitPrice;
        private Double subTotal;
        private String supplierName;
    }
}
