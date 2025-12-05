package com.Uqar.reports.dto.response;

import com.Uqar.reports.enums.Language;
import com.Uqar.reports.enums.ReportType;
import com.Uqar.user.Enum.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Simplified Report Response DTO
 * Generic response structure for all reports
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    
    private boolean success;
    private String message;
    private ReportType reportType;
    private Long pharmacyId;
    private LocalDateTime generatedAt;
    private Currency currency;
    private Language language;
    
    // Generic data container
    private Map<String, Object> data;
    
    // For reports with daily breakdown
    private List<Map<String, Object>> dailyData;
    
    // For reports with summary
    private Map<String, Object> summary;
    
    // For reports with items/details
    private List<Map<String, Object>> items;
    
    // For category reports
    private List<Map<String, Object>> categories;
    
    // For product reports
    private List<Map<String, Object>> products;
    
    // Error information
    private String error;
    private String errorCode;
}
