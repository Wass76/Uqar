package com.Uqar.reports.mapper;

import com.Uqar.reports.dto.response.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapper for converting raw database query results to DTOs
 * Follows the project's mapper pattern
 */
@Component
public class ReportMapper {
    
    // ============================================================================
    // PURCHASE REPORT MAPPERS
    // ============================================================================
    
    public PurchaseReportResponse.DailyPurchaseData toDailyPurchaseData(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        return PurchaseReportResponse.DailyPurchaseData.builder()
                .date(convertToLocalDate(raw.get("date")))
                .totalInvoices(getLongValue(raw.get("totalInvoices"), 0L))
                .totalAmount(getDoubleValue(raw.get("totalAmount"), 0.0))
                .totalPaid(getDoubleValue(raw.get("totalPaid"), 0.0))
                .averageAmount(getDoubleValue(raw.get("averageAmount"), 0.0))
                .build();
    }
    
    public List<PurchaseReportResponse.DailyPurchaseData> toDailyPurchaseDataList(List<Map<String, Object>> rawList) {
        if (rawList == null) {
            return new ArrayList<>();
        }
        List<PurchaseReportResponse.DailyPurchaseData> result = new ArrayList<>();
        for (Map<String, Object> raw : rawList) {
            result.add(toDailyPurchaseData(raw));
        }
        return result;
    }
    
    public PurchaseReportResponse.PurchaseSummary toPurchaseSummary(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        return PurchaseReportResponse.PurchaseSummary.builder()
                .totalInvoices(getLongValue(raw.get("totalInvoices"), 0L))
                .totalAmount(getDoubleValue(raw.get("totalAmount"), 0.0))
                .totalPaid(getDoubleValue(raw.get("totalPaid"), 0.0))
                .averageAmount(getDoubleValue(raw.get("averageAmount"), 0.0))
                .totalItems(getDoubleValue(raw.get("totalItems"), 0.0))
                .build();
    }
    
    public PurchaseReportResponse.PurchaseItem toPurchaseItem(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        return PurchaseReportResponse.PurchaseItem.builder()
                .productName(getStringValue(raw.get("productName"), "Unknown"))
                .quantity(getIntegerValue(raw.get("quantity"), 0))
                .unitPrice(getDoubleValue(raw.get("unitPrice"), 0.0))
                .subTotal(getDoubleValue(raw.get("subTotal"), 0.0))
                .supplierName(getStringValue(raw.get("supplierName"), "Unknown"))
                .build();
    }
    
    public List<PurchaseReportResponse.PurchaseItem> toPurchaseItemList(List<Map<String, Object>> rawList) {
        if (rawList == null) {
            return new ArrayList<>();
        }
        List<PurchaseReportResponse.PurchaseItem> result = new ArrayList<>();
        for (Map<String, Object> raw : rawList) {
            result.add(toPurchaseItem(raw));
        }
        return result;
    }
    
    // ============================================================================
    // DAILY PURCHASE REPORT MAPPERS
    // ============================================================================
    
    public DailyPurchaseReportResponse.DailyPurchaseData toDailyPurchaseDataForDaily(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        return DailyPurchaseReportResponse.DailyPurchaseData.builder()
                .date(convertToLocalDate(raw.get("date")))
                .totalInvoices(getLongValue(raw.get("totalInvoices"), 0L))
                .totalAmount(getDoubleValue(raw.get("totalAmount"), 0.0))
                .totalPaid(getDoubleValue(raw.get("totalPaid"), 0.0))
                .averageAmount(getDoubleValue(raw.get("averageAmount"), 0.0))
                .build();
    }
    
    public DailyPurchaseReportResponse.PurchaseItem toDailyPurchaseItem(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        return DailyPurchaseReportResponse.PurchaseItem.builder()
                .productName(getStringValue(raw.get("productName"), "Unknown"))
                .quantity(getIntegerValue(raw.get("quantity"), 0))
                .unitPrice(getDoubleValue(raw.get("unitPrice"), 0.0))
                .subTotal(getDoubleValue(raw.get("subTotal"), 0.0))
                .supplierName(getStringValue(raw.get("supplierName"), "Unknown"))
                .build();
    }
    
    public List<DailyPurchaseReportResponse.PurchaseItem> toDailyPurchaseItemList(List<Map<String, Object>> rawList) {
        if (rawList == null) {
            return new ArrayList<>();
        }
        List<DailyPurchaseReportResponse.PurchaseItem> result = new ArrayList<>();
        for (Map<String, Object> raw : rawList) {
            result.add(toDailyPurchaseItem(raw));
        }
        return result;
    }
    
    // ============================================================================
    // PROFIT REPORT MAPPERS
    // ============================================================================
    
    public ProfitReportResponse.DailyProfitData toDailyProfitData(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        return ProfitReportResponse.DailyProfitData.builder()
                .date(convertToLocalDate(raw.get("date")))
                .totalInvoices(getLongValue(raw.get("totalInvoices"), 0L))
                .totalRevenue(getDoubleValue(raw.get("totalRevenue"), 0.0))
                .totalCost(getDoubleValue(raw.get("totalCost"), 0.0))
                .totalProfit(getDoubleValue(raw.get("totalProfit"), 0.0))
                .profitMargin(getDoubleValue(raw.get("profitMargin"), 0.0))
                .averageRevenue(getDoubleValue(raw.get("averageRevenue"), 0.0))
                .build();
    }
    
    public List<ProfitReportResponse.DailyProfitData> toDailyProfitDataList(List<Map<String, Object>> rawList) {
        if (rawList == null) {
            return new ArrayList<>();
        }
        List<ProfitReportResponse.DailyProfitData> result = new ArrayList<>();
        for (Map<String, Object> raw : rawList) {
            result.add(toDailyProfitData(raw));
        }
        return result;
    }
    
    public ProfitReportResponse.ProfitSummary toProfitSummary(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        return ProfitReportResponse.ProfitSummary.builder()
                .totalInvoices(getLongValue(raw.get("totalInvoices"), 0L))
                .totalRevenue(getDoubleValue(raw.get("totalRevenue"), 0.0))
                .totalCost(getDoubleValue(raw.get("totalCost"), 0.0))
                .totalProfit(getDoubleValue(raw.get("totalProfit"), 0.0))
                .profitMargin(getDoubleValue(raw.get("profitMargin"), 0.0))
                .averageProfit(getDoubleValue(raw.get("averageProfit"), 0.0))
                .averageRevenue(getDoubleValue(raw.get("averageRevenue"), 0.0))
                .build();
    }
    
    public ProfitReportResponse.ProfitItem toProfitItem(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        return ProfitReportResponse.ProfitItem.builder()
                .productName(getStringValue(raw.get("productName"), "Unknown"))
                .quantity(getIntegerValue(raw.get("quantity"), 0))
                .revenue(getDoubleValue(raw.get("revenue"), 0.0))
                .cost(getDoubleValue(raw.get("cost"), 0.0))
                .profit(getDoubleValue(raw.get("profit"), 0.0))
                .profitMargin(getDoubleValue(raw.get("profitMargin"), 0.0))
                .build();
    }
    
    public List<ProfitReportResponse.ProfitItem> toProfitItemList(List<Map<String, Object>> rawList) {
        if (rawList == null) {
            return new ArrayList<>();
        }
        List<ProfitReportResponse.ProfitItem> result = new ArrayList<>();
        for (Map<String, Object> raw : rawList) {
            result.add(toProfitItem(raw));
        }
        return result;
    }
    
    // ============================================================================
    // CATEGORY REPORT MAPPERS
    // ============================================================================
    
    public CategoryReportResponse.CategoryData toCategoryData(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        return CategoryReportResponse.CategoryData.builder()
                .categoryName(getStringValue(raw.get("categoryName"), "Unknown"))
                .totalQuantity(getLongValue(raw.get("totalQuantity"), 0L))
                .totalRevenue(getDoubleValue(raw.get("totalRevenue"), 0.0))
                .invoiceCount(getLongValue(raw.get("invoiceCount"), 0L))
                .averageQuantity(getDoubleValue(raw.get("averageQuantity"), 0.0))
                .averageRevenue(getDoubleValue(raw.get("averageRevenue"), 0.0))
                .build();
    }
    
    public List<CategoryReportResponse.CategoryData> toCategoryDataList(List<Map<String, Object>> rawList) {
        if (rawList == null) {
            return new ArrayList<>();
        }
        List<CategoryReportResponse.CategoryData> result = new ArrayList<>();
        for (Map<String, Object> raw : rawList) {
            result.add(toCategoryData(raw));
        }
        return result;
    }
    
    // ============================================================================
    // PRODUCT REPORT MAPPERS
    // ============================================================================
    
    public ProductReportResponse.ProductData toProductData(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }
        return ProductReportResponse.ProductData.builder()
                .productId(getLongValue(raw.get("productId"), 0L))
                .productType(convertToProductType(raw.get("productType")))
                .productName(getStringValue(raw.get("productName"), "Unknown"))
                .totalSales(getLongValue(raw.get("invoiceCount"), 0L)) // Using invoiceCount as totalSales
                .totalRevenue(getDoubleValue(raw.get("totalRevenue"), 0.0))
                .totalQuantity(getIntegerValue(raw.get("totalQuantity"), 0))
                .averagePrice(getDoubleValue(raw.get("averagePrice"), 0.0))
                .profit(getDoubleValue(raw.get("totalProfit"), 0.0))
                .profitMargin(getDoubleValue(raw.get("profitMargin"), 0.0))
                .build();
    }
    
    public List<ProductReportResponse.ProductData> toProductDataList(List<Map<String, Object>> rawList) {
        if (rawList == null) {
            return new ArrayList<>();
        }
        List<ProductReportResponse.ProductData> result = new ArrayList<>();
        for (Map<String, Object> raw : rawList) {
            result.add(toProductData(raw));
        }
        return result;
    }
    
    // ============================================================================
    // UTILITY METHODS
    // ============================================================================
    
    private LocalDate convertToLocalDate(Object dateObj) {
        if (dateObj == null) {
            return null;
        }
        if (dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        }
        if (dateObj instanceof LocalDateTime) {
            return ((LocalDateTime) dateObj).toLocalDate();
        }
        if (dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate();
        }
        if (dateObj instanceof java.util.Date) {
            return new java.sql.Date(((java.util.Date) dateObj).getTime()).toLocalDate();
        }
        if (dateObj instanceof String) {
            try {
                return LocalDate.parse((String) dateObj);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    private Double getDoubleValue(Object value, Double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    private Long getLongValue(Object value, Long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    private Integer getIntegerValue(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    private String getStringValue(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }
    
    /**
     * Safely converts object to ProductType
     */
    private com.Uqar.product.Enum.ProductType convertToProductType(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof com.Uqar.product.Enum.ProductType) {
            return (com.Uqar.product.Enum.ProductType) value;
        }
        if (value instanceof String) {
            try {
                return com.Uqar.product.Enum.ProductType.valueOf((String) value);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
}
