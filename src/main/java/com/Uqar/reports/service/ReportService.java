package com.Uqar.reports.service;

import com.Uqar.reports.dto.response.*;
import com.Uqar.reports.enums.Language;
import com.Uqar.reports.mapper.ReportMapper;
import com.Uqar.reports.repository.ReportRepository;
import com.Uqar.user.Enum.Currency;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.user.service.BaseSecurityService;
import com.Uqar.moneybox.service.ExchangeRateService;
import com.Uqar.product.entity.PharmacyProduct;
import com.Uqar.product.entity.MasterProduct;
import com.Uqar.product.repo.PharmacyProductRepo;
import com.Uqar.product.repo.MasterProductRepo;
import com.Uqar.language.LanguageRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Simplified Reports Service
 * Implements only the specific reports agreed upon with the business team:
 * 1. Monthly Purchase Report (daily breakdown)
 * 2. Daily Purchase Report
 * 3. Monthly Profit Report (daily breakdown)
 * 4. Daily Profit Report
 * 5. Most Sold Categories Monthly
 * 6. Top 10 Products Monthly
 */
@Slf4j
@Service
public class ReportService extends BaseSecurityService {
    
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final ExchangeRateService exchangeRateService;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final MasterProductRepo masterProductRepo;
    private final LanguageRepo languageRepo;
    
    public ReportService(UserRepository userRepository, ReportRepository reportRepository, ReportMapper reportMapper, 
                        ExchangeRateService exchangeRateService, PharmacyProductRepo pharmacyProductRepo, 
                        MasterProductRepo masterProductRepo, LanguageRepo languageRepo) {
        super(userRepository);
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
        this.exchangeRateService = exchangeRateService;
        this.pharmacyProductRepo = pharmacyProductRepo;
        this.masterProductRepo = masterProductRepo;
        this.languageRepo = languageRepo;
    }
    
    // ============================================================================
    // CURRENCY CONVERSION HELPER METHODS
    // ============================================================================
    
    /**
     * Convert amount to SYP (base currency) for consistent calculations
     */
    private BigDecimal convertToSYP(BigDecimal amount, Currency currency) {
        if (currency == Currency.SYP) {
            return amount;
        }
        return exchangeRateService.convertToSYP(amount, currency);
    }
    
    /**
     * Convert amount from SYP to target currency for display
     */
    private BigDecimal convertFromSYP(BigDecimal amount, Currency targetCurrency) {
        if (targetCurrency == Currency.SYP) {
            return amount;
        }
        Currency userCurrency = Currency.valueOf(targetCurrency.name());
        return exchangeRateService.convertFromSYP(amount, userCurrency);
    }
    
    /**
     * Process currency-aware profit data and convert to target currency
     * This method is specifically designed for profit reports
     */
    private Map<String, Object> processCurrencyAwareProfitData(List<Map<String, Object>> rawData, Currency targetCurrency) {
        return processCurrencyAwareProfitData(rawData, targetCurrency, null);
    }
    
    /**
     * Process currency-aware profit data and convert to target currency
     * This method is specifically designed for profit reports
     */
    private Map<String, Object> processCurrencyAwareProfitData(List<Map<String, Object>> rawData, Currency targetCurrency, LocalDate date) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        int totalInvoices = 0;
        BigDecimal sumForAverage = BigDecimal.ZERO;
        
        for (Map<String, Object> data : rawData) {
            // Handle null values safely
            Object currencyObj = data.get("currency");
            Object revenueObj = data.get("totalRevenue");
            Object profitObj = data.get("totalProfit");
            Object invoicesObj = data.get("totalInvoices");
            Object averageObj = data.get("averageRevenue");
            
            // Skip this data entry if essential fields are null
            if (currencyObj == null || revenueObj == null || profitObj == null || invoicesObj == null) {
                log.warn("Skipping profit data entry with null essential fields: {}", data);
                continue;
            }
            
            Currency currency = Currency.valueOf(currencyObj.toString());
            BigDecimal revenue = new BigDecimal(revenueObj.toString());
            BigDecimal profit = new BigDecimal(profitObj.toString());
            int invoices = ((Number) invoicesObj).intValue();
            
            // Convert to SYP for consistent calculation
            BigDecimal revenueInSYP = convertToSYP(revenue, currency);
            BigDecimal profitInSYP = convertToSYP(profit, currency);
            
            totalRevenue = totalRevenue.add(revenueInSYP);
            totalProfit = totalProfit.add(profitInSYP);
            totalInvoices += invoices;
            sumForAverage = sumForAverage.add(revenueInSYP);
        }
        
        // Convert final totals to target currency for display
        BigDecimal finalTotalRevenue = convertFromSYP(totalRevenue, targetCurrency);
        BigDecimal finalTotalProfit = convertFromSYP(totalProfit, targetCurrency);
        BigDecimal finalTotalCost = finalTotalRevenue.subtract(finalTotalProfit);
        BigDecimal averageRevenue = totalInvoices > 0 ? convertFromSYP(sumForAverage.divide(BigDecimal.valueOf(totalInvoices), 2, BigDecimal.ROUND_HALF_UP), targetCurrency) : BigDecimal.ZERO;
        
        // Calculate profit margin
        Double profitMargin = finalTotalRevenue.compareTo(BigDecimal.ZERO) > 0 ? 
            finalTotalProfit.divide(finalTotalRevenue, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0.0;
        
        Map<String, Object> result = Map.of(
            "totalInvoices", totalInvoices,
            "totalRevenue", finalTotalRevenue,
            "totalCost", finalTotalCost,
            "totalProfit", finalTotalProfit,
            "averageRevenue", averageRevenue,
            "profitMargin", profitMargin
        );
        
        // Add date if provided (for daily reports)
        if (date != null) {
            result = Map.of(
                "date", date,
                "totalInvoices", totalInvoices,
                "totalRevenue", finalTotalRevenue,
                "totalCost", finalTotalCost,
                "totalProfit", finalTotalProfit,
                "averageRevenue", averageRevenue,
                "profitMargin", profitMargin
            );
        }
        
        return result;
    }
    
    /**
     * Process currency-aware data and convert to target currency
     */
    private Map<String, Object> processCurrencyAwareData(List<Map<String, Object>> rawData, Currency targetCurrency) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalInvoices = 0;
        BigDecimal sumForAverage = BigDecimal.ZERO;
        
        for (Map<String, Object> data : rawData) {
            // Handle null values safely
            Object currencyObj = data.get("currency");
            Object amountObj = data.get("totalAmount");
            Object paidObj = data.get("totalPaid");
            Object invoicesObj = data.get("totalInvoices");
            
            // Skip this data entry if essential fields are null
            if (currencyObj == null || amountObj == null || paidObj == null || invoicesObj == null) {
                log.warn("Skipping data entry with null essential fields: {}", data);
                continue;
            }
            
            Currency currency = Currency.valueOf(currencyObj.toString());
            BigDecimal amount = new BigDecimal(amountObj.toString());
            BigDecimal paid = new BigDecimal(paidObj.toString());
            int invoices = ((Number) invoicesObj).intValue();
            
            // Convert to SYP for consistent calculation
            BigDecimal amountInSYP = convertToSYP(amount, currency);
            BigDecimal paidInSYP = convertToSYP(paid, currency);
            
            totalAmount = totalAmount.add(amountInSYP);
            totalPaid = totalPaid.add(paidInSYP);
            totalInvoices += invoices;
            sumForAverage = sumForAverage.add(amountInSYP);
            
            // Handle profit and revenue if present
            Object profitObj = data.get("totalProfit");
            if (profitObj != null) {
                BigDecimal profit = new BigDecimal(profitObj.toString());
                BigDecimal profitInSYP = convertToSYP(profit, currency);
                totalProfit = totalProfit.add(profitInSYP);
            }
            
            Object revenueObj = data.get("totalRevenue");
            if (revenueObj != null) {
                BigDecimal revenue = new BigDecimal(revenueObj.toString());
                BigDecimal revenueInSYP = convertToSYP(revenue, currency);
                totalRevenue = totalRevenue.add(revenueInSYP);
            }
        }
        
        // Convert final totals to target currency for display
        BigDecimal finalTotalAmount = convertFromSYP(totalAmount, targetCurrency);
        BigDecimal finalTotalPaid = convertFromSYP(totalPaid, targetCurrency);
        BigDecimal finalTotalProfit = convertFromSYP(totalProfit, targetCurrency);
        BigDecimal finalTotalRevenue = convertFromSYP(totalRevenue, targetCurrency);
        BigDecimal averageAmount = totalInvoices > 0 ? convertFromSYP(sumForAverage.divide(BigDecimal.valueOf(totalInvoices), 2, BigDecimal.ROUND_HALF_UP), targetCurrency) : BigDecimal.ZERO;
        
        Map<String, Object> result;
        if (totalProfit.compareTo(BigDecimal.ZERO) > 0) {
            result = Map.of(
                "totalInvoices", totalInvoices,
                "totalAmount", finalTotalAmount,
                "totalPaid", finalTotalPaid,
                "averageAmount", averageAmount,
                "totalProfit", finalTotalProfit,
                "totalRevenue", finalTotalRevenue
            );
        } else {
            result = Map.of(
                "totalInvoices", totalInvoices,
                "totalAmount", finalTotalAmount,
                "totalPaid", finalTotalPaid,
                "averageAmount", averageAmount
            );
        }
        
        return result;
    }
    
    /**
     * Process currency-aware daily data and convert to target currency
     * This method processes daily breakdown data (not summary data)
     */
    private List<Map<String, Object>> processCurrencyAwareDailyData(List<Map<String, Object>> rawData, Currency targetCurrency) {
        return rawData.stream()
            .map(data -> {
                // Handle null values safely
                Object currencyObj = data.get("currency");
                Object amountObj = data.get("totalAmount");
                Object paidObj = data.get("totalPaid");
                Object invoicesObj = data.get("totalInvoices");
                Object averageObj = data.get("averageAmount");
                Object dateObj = data.get("date");
                
                // Skip this data entry if essential fields are null
                if (currencyObj == null || amountObj == null || paidObj == null || invoicesObj == null || averageObj == null || dateObj == null) {
                    log.warn("Skipping daily data entry with null essential fields: {}", data);
                    return null;
                }
                
                Currency currency = Currency.valueOf(currencyObj.toString());
                BigDecimal amount = new BigDecimal(amountObj.toString());
                BigDecimal paid = new BigDecimal(paidObj.toString());
                BigDecimal average = new BigDecimal(averageObj.toString());
                int invoices = ((Number) invoicesObj).intValue();
                
                // Convert to SYP for consistent calculation
                BigDecimal amountInSYP = convertToSYP(amount, currency);
                BigDecimal paidInSYP = convertToSYP(paid, currency);
                BigDecimal averageInSYP = convertToSYP(average, currency);
                
                // Convert from SYP to target currency for display
                BigDecimal finalAmount = convertFromSYP(amountInSYP, targetCurrency);
                BigDecimal finalPaid = convertFromSYP(paidInSYP, targetCurrency);
                BigDecimal finalAverage = convertFromSYP(averageInSYP, targetCurrency);
                
                return Map.of(
                    "date", dateObj,
                    "totalInvoices", invoices,
                    "totalAmount", finalAmount,
                    "totalPaid", finalPaid,
                    "averageAmount", finalAverage,
                    "currency", targetCurrency.name()
                );
            })
            .filter(Objects::nonNull) // Remove null entries
            .collect(Collectors.toList());
    }
    
    /**
     * Process currency-aware daily profit data and convert to target currency
     * This method processes daily profit breakdown data (not summary data)
     */
    private List<Map<String, Object>> processCurrencyAwareDailyProfitData(List<Map<String, Object>> rawData, Currency targetCurrency) {
        return rawData.stream()
            .map(data -> {
                // Handle null values safely
                Object currencyObj = data.get("currency");
                Object revenueObj = data.get("totalRevenue");
                Object profitObj = data.get("totalProfit");
                Object invoicesObj = data.get("totalInvoices");
                Object averageObj = data.get("averageRevenue");
                Object dateObj = data.get("date");
                
                // Skip this data entry if essential fields are null
                if (currencyObj == null || revenueObj == null || profitObj == null || invoicesObj == null || averageObj == null || dateObj == null) {
                    log.warn("Skipping daily profit data entry with null essential fields: {}", data);
                    return null;
                }
                
                Currency currency = Currency.valueOf(currencyObj.toString());
                BigDecimal revenue = new BigDecimal(revenueObj.toString());
                BigDecimal profit = new BigDecimal(profitObj.toString());
                BigDecimal average = new BigDecimal(averageObj.toString());
                int invoices = ((Number) invoicesObj).intValue();
                
                // Convert to SYP for consistent calculation
                BigDecimal revenueInSYP = convertToSYP(revenue, currency);
                BigDecimal profitInSYP = convertToSYP(profit, currency);
                BigDecimal averageInSYP = convertToSYP(average, currency);
                
                // Convert from SYP to target currency for display
                BigDecimal finalRevenue = convertFromSYP(revenueInSYP, targetCurrency);
                BigDecimal finalProfit = convertFromSYP(profitInSYP, targetCurrency);
                BigDecimal finalAverage = convertFromSYP(averageInSYP, targetCurrency);
                
                return Map.of(
                    "date", dateObj,
                    "totalInvoices", invoices,
                    "totalRevenue", finalRevenue,
                    "totalProfit", finalProfit,
                    "averageRevenue", finalAverage,
                    "currency", targetCurrency.name()
                );
            })
            .filter(Objects::nonNull) // Remove null entries
            .collect(Collectors.toList());
    }
    
    /**
     * Process currency-aware items and convert to target currency
     * Note: Prices from database are already in SYP, so we only convert from SYP to target currency
     */
    private List<Map<String, Object>> processCurrencyAwareItems(List<Map<String, Object>> rawItems, Currency targetCurrency) {
        return rawItems.stream()
            .map(item -> {
                // Handle null values safely
                Object unitPriceObj = item.get("unitPrice");
                Object subTotalObj = item.get("subTotal");
                Object productNameObj = item.get("productName");
                Object quantityObj = item.get("quantity");
                Object supplierNameObj = item.get("supplierName");
                
                // Convert to BigDecimal with null safety
                BigDecimal unitPriceInSYP = unitPriceObj != null ? 
                    new BigDecimal(unitPriceObj.toString()) : BigDecimal.ZERO;
                BigDecimal subTotalInSYP = subTotalObj != null ? 
                    new BigDecimal(subTotalObj.toString()) : BigDecimal.ZERO;
                
                // Convert from SYP to target currency
                BigDecimal finalUnitPrice = convertFromSYP(unitPriceInSYP, targetCurrency);
                BigDecimal finalSubTotal = convertFromSYP(subTotalInSYP, targetCurrency);
                
                // Get actual product name instead of product ID
                String productName = productNameObj != null ? 
                    getProductName(productNameObj.toString()) : "Unknown Product";
                
                return Map.of(
                    "productName", productName,
                    "quantity", quantityObj != null ? quantityObj : 0,
                    "unitPrice", finalUnitPrice,
                    "subTotal", finalSubTotal,
                    "supplierName", supplierNameObj != null ? supplierNameObj.toString() : "Unknown Supplier",
                    "currency", targetCurrency.name()
                );
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Process currency-aware profit items and convert to target currency
     * Note: Prices from database are already in SYP, so we only convert from SYP to target currency
     */
    private List<Map<String, Object>> processCurrencyAwareProfitItems(List<Map<String, Object>> rawItems, Currency targetCurrency) {
        return rawItems.stream()
            .map(item -> {
                // Handle null values safely
                Object revenueObj = item.get("revenue");
                Object profitObj = item.get("profit");
                Object productNameObj = item.get("productName");
                Object quantityObj = item.get("quantity");
                
                // Convert to BigDecimal with null safety
                BigDecimal revenueInSYP = revenueObj != null ? 
                    new BigDecimal(revenueObj.toString()) : BigDecimal.ZERO;
                BigDecimal profitInSYP = profitObj != null ? 
                    new BigDecimal(profitObj.toString()) : BigDecimal.ZERO;
                
                // Convert from SYP to target currency
                BigDecimal finalRevenue = convertFromSYP(revenueInSYP, targetCurrency);
                BigDecimal finalProfit = convertFromSYP(profitInSYP, targetCurrency);
                
                // Calculate cost (revenue - profit)
                BigDecimal finalCost = finalRevenue.subtract(finalProfit);
                
                // Calculate profit margin
                Double profitMargin = finalRevenue.compareTo(BigDecimal.ZERO) > 0 ? 
                    finalProfit.divide(finalRevenue, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0.0;
                
                // Get actual product name, handling null cases
                String productName = getProductNameForProfitItem(item);
                
                return Map.of(
                    "productName", productName,
                    "quantity", quantityObj != null ? quantityObj : 0,
                    "revenue", finalRevenue,
                    "cost", finalCost,
                    "profit", finalProfit,
                    "profitMargin", profitMargin,
                    "currency", targetCurrency.name()
                );
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Process currency-aware product data and convert to target currency
     * Handles product report data with profit calculations
     */
    private List<Map<String, Object>> processCurrencyAwareProductData(List<Map<String, Object>> rawProducts, Currency targetCurrency) {
        return rawProducts.stream()
            .map(product -> {
                // Handle null values safely
                Object currencyObj = product.get("currency");
                Object revenueObj = product.get("totalRevenue");
                Object profitObj = product.get("totalProfit");
                Object averagePriceObj = product.get("averagePrice");
                Object productIdObj = product.get("productId");
                Object productTypeObj = product.get("productType");
                Object productNameObj = product.get("productName");
                Object quantityObj = product.get("totalQuantity");
                Object invoiceCountObj = product.get("invoiceCount");
                
                // Skip this product if essential fields are null
                if (currencyObj == null || revenueObj == null || productIdObj == null) {
                    log.warn("Skipping product with null essential fields: {}", product);
                    return null;
                }
                
                Currency currency = Currency.valueOf(currencyObj.toString());
                BigDecimal revenueInSYP = new BigDecimal(revenueObj.toString());
                BigDecimal profitInSYP = profitObj != null ? new BigDecimal(profitObj.toString()) : BigDecimal.ZERO;
                BigDecimal averagePriceInSYP = averagePriceObj != null ? new BigDecimal(averagePriceObj.toString()) : BigDecimal.ZERO;
                
                // Convert to SYP for consistent calculation
                BigDecimal revenueConverted = convertToSYP(revenueInSYP, currency);
                BigDecimal profitConverted = convertToSYP(profitInSYP, currency);
                BigDecimal averagePriceConverted = convertToSYP(averagePriceInSYP, currency);
                
                // Convert from SYP to target currency for display
                BigDecimal finalRevenue = convertFromSYP(revenueConverted, targetCurrency);
                BigDecimal finalProfit = convertFromSYP(profitConverted, targetCurrency);
                BigDecimal finalAveragePrice = convertFromSYP(averagePriceConverted, targetCurrency);
                
                // Calculate profit margin
                Double profitMargin = finalRevenue.compareTo(BigDecimal.ZERO) > 0 ? 
                    finalProfit.divide(finalRevenue, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0.0;
                
                // Get actual product name using the helper method with product type
                String actualProductName = getProductName(productIdObj.toString(), productTypeObj != null ? productTypeObj.toString() : null);
                
                return Map.of(
                    "productId", productIdObj,
                    "productType", productTypeObj != null ? productTypeObj : "UNKNOWN",
                    "productName", actualProductName,
                    "totalQuantity", quantityObj != null ? quantityObj : 0,
                    "totalRevenue", finalRevenue,
                    "averagePrice", finalAveragePrice,
                    "totalProfit", finalProfit,
                    "profitMargin", profitMargin,
                    "invoiceCount", invoiceCountObj != null ? invoiceCountObj : 0,
                    "currency", targetCurrency.name()
                );
            })
            .filter(Objects::nonNull) // Remove null entries
            .collect(Collectors.toList());
    }
    
    // ============================================================================
    // PURCHASE REPORTS
    // ============================================================================
    
    /**
     * Get Monthly Purchase Report with daily breakdown
     * Returns purchase data for each day in the specified month
     */
    public PurchaseReportResponse getMonthlyPurchaseReport(LocalDate startDate, LocalDate endDate, Currency currency, Language language) {
        Long pharmacyId = getCurrentUserPharmacyId();
        log.info("Generating monthly purchase report for pharmacy: {}, period: {} to {}", pharmacyId, startDate, endDate);
        
        try {
            // Get daily breakdown with currency information
            List<Map<String, Object>> dailyDataRawList = reportRepository.getMonthlyPurchaseDailyBreakdown(pharmacyId, startDate, endDate);
            
            // Get summary data with currency information
            List<Map<String, Object>> summaryRawList = reportRepository.getMonthlyPurchaseSummary(pharmacyId, startDate, endDate);
            
            // Process currency-aware data for both daily and summary
            Map<String, Object> summaryRaw = processCurrencyAwareData(summaryRawList, currency);
            List<Map<String, Object>> processedDailyData = processCurrencyAwareDailyData(dailyDataRawList, currency);
            
            // Convert to DTOs using mapper
            List<PurchaseReportResponse.DailyPurchaseData> dailyData = reportMapper.toDailyPurchaseDataList(processedDailyData);
            PurchaseReportResponse.PurchaseSummary summary = reportMapper.toPurchaseSummary(summaryRaw);
            
            // Build response
            PurchaseReportResponse response = new PurchaseReportResponse();
            response.setSuccess(true);
            response.setPharmacyId(pharmacyId);
            response.setStartDate(startDate);
            response.setEndDate(endDate);
            response.setCurrency(currency);
            response.setLanguage(language);
            response.setGeneratedAt(LocalDateTime.now());
            response.setDailyData(dailyData);
            response.setSummary(summary);
            
            return response;
                    
        } catch (Exception e) {
            log.error("Error generating monthly purchase report: {}", e.getMessage(), e);
            PurchaseReportResponse errorResponse = new PurchaseReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * Get Daily Purchase Report
     * Returns purchase data for a specific day
     */
    public DailyPurchaseReportResponse getDailyPurchaseReport(LocalDate date, Currency currency, Language language) {
        Long pharmacyId = getCurrentUserPharmacyId();
        log.info("Generating daily purchase report for pharmacy: {}, date: {}", pharmacyId, date);
        
        try {
            // Get daily purchase data with currency information
            List<Map<String, Object>> dailyDataRawList = reportRepository.getDailyPurchaseSummary(pharmacyId, date);
            
            // Process currency-aware data
            Map<String, Object> dailyDataRaw = processCurrencyAwareData(dailyDataRawList, currency);
            
            // Get purchase items for the day with currency information
            List<Map<String, Object>> purchaseItemsRaw = reportRepository.getDailyPurchaseItems(pharmacyId, date);
            
            // Process currency-aware items
            List<Map<String, Object>> processedItems = processCurrencyAwareItems(purchaseItemsRaw, currency);
            
            // Convert to DTOs using mapper
            DailyPurchaseReportResponse.DailyPurchaseData dailyData = reportMapper.toDailyPurchaseDataForDaily(dailyDataRaw);
            List<DailyPurchaseReportResponse.PurchaseItem> items = reportMapper.toDailyPurchaseItemList(processedItems);
            
            // Build response
            DailyPurchaseReportResponse response = new DailyPurchaseReportResponse();
            response.setSuccess(true);
            response.setPharmacyId(pharmacyId);
            response.setDate(date);
            response.setCurrency(currency);
            response.setLanguage(language);
            response.setGeneratedAt(LocalDateTime.now());
            response.setData(dailyData);
            response.setItems(items);
            
            return response;
                    
        } catch (Exception e) {
            log.error("Error generating daily purchase report: {}", e.getMessage(), e);
            DailyPurchaseReportResponse errorResponse = new DailyPurchaseReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            return errorResponse;
        }
    }
    
    // ============================================================================
    // PROFIT REPORTS
    // ============================================================================
    
    /**
     * Get Monthly Profit Report with daily breakdown
     * Returns profit data for each day in the specified month
     */
    public ProfitReportResponse getMonthlyProfitReport(LocalDate startDate, LocalDate endDate, Currency currency, Language language) {
        Long pharmacyId = getCurrentUserPharmacyId();
        log.info("Generating monthly profit report for pharmacy: {}, period: {} to {}", pharmacyId, startDate, endDate);
        
        try {
            // Get daily breakdown with currency information
            List<Map<String, Object>> dailyDataRawList = reportRepository.getMonthlyProfitDailyBreakdown(pharmacyId, startDate, endDate);
            
            // Get summary data with currency information
            List<Map<String, Object>> summaryRawList = reportRepository.getMonthlyProfitSummary(pharmacyId, startDate, endDate);
            
            // Process currency-aware data for both daily and summary
            Map<String, Object> summaryRaw = processCurrencyAwareProfitData(summaryRawList, currency);
            List<Map<String, Object>> processedDailyData = processCurrencyAwareDailyProfitData(dailyDataRawList, currency);
            
            // Convert to DTOs using mapper
            List<ProfitReportResponse.DailyProfitData> dailyData = reportMapper.toDailyProfitDataList(processedDailyData);
            ProfitReportResponse.ProfitSummary summary = reportMapper.toProfitSummary(summaryRaw);
            
            // Build response
            ProfitReportResponse response = new ProfitReportResponse();
            response.setSuccess(true);
            response.setPharmacyId(pharmacyId);
            response.setStartDate(startDate);
            response.setEndDate(endDate);
            response.setCurrency(currency);
            response.setLanguage(language);
            response.setGeneratedAt(LocalDateTime.now());
            response.setDailyData(dailyData);
            response.setSummary(summary);
            
            return response;
                    
        } catch (Exception e) {
            log.error("Error generating monthly profit report: {}", e.getMessage(), e);
            ProfitReportResponse errorResponse = new ProfitReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * Get Daily Profit Report
     * Returns profit data for a specific day
     */
    public ProfitReportResponse getDailyProfitReport(LocalDate date, Currency currency, Language language) {
        Long pharmacyId = getCurrentUserPharmacyId();
        log.info("Generating daily profit report for pharmacy: {}, date: {}", pharmacyId, date);
        
        try {
            // Get daily profit data with currency information
            List<Map<String, Object>> dailyDataRawList = reportRepository.getDailyProfitSummary(pharmacyId, date);
            
            // Process currency-aware profit data
            Map<String, Object> dailyDataRaw = processCurrencyAwareProfitData(dailyDataRawList, currency, date);
            
            // Get profit items for the day with currency information
            List<Map<String, Object>> profitItemsRaw = reportRepository.getDailyProfitItems(pharmacyId, date);
            
            // Process currency-aware profit items
            List<Map<String, Object>> processedItems = processCurrencyAwareProfitItems(profitItemsRaw, currency);
            
            // Convert to DTOs using mapper
            ProfitReportResponse.DailyProfitData dailyData = reportMapper.toDailyProfitData(dailyDataRaw);
            List<ProfitReportResponse.ProfitItem> items = reportMapper.toProfitItemList(processedItems);
            
            // Build response
            ProfitReportResponse response = new ProfitReportResponse();
            response.setSuccess(true);
            response.setPharmacyId(pharmacyId);
            response.setDate(date);
            response.setCurrency(currency);
            response.setLanguage(language);
            response.setGeneratedAt(LocalDateTime.now());
            response.setData(dailyData);
            response.setItems(items);
            
            return response;
                    
        } catch (Exception e) {
            log.error("Error generating daily profit report: {}", e.getMessage(), e);
            ProfitReportResponse errorResponse = new ProfitReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            return errorResponse;
        }
    }
    
    // ============================================================================
    // CATEGORY AND PRODUCT REPORTS
    // ============================================================================
    
    /**
     * Get Most Sold Categories Monthly
     * Returns the most sold categories in the pharmacy for the specified month
     */
    public CategoryReportResponse getMostSoldCategories(LocalDate startDate, LocalDate endDate, Language language) {
        Long pharmacyId = getCurrentUserPharmacyId();
        log.info("Generating most sold categories report for pharmacy: {}, period: {} to {}", pharmacyId, startDate, endDate);
        
        try {
            // Convert Language enum to Language entity ID
            Long languageId = getLanguageId(language);
            
            // Get categories from both MasterProduct and PharmacyProduct
            List<Map<String, Object>> masterCategories = reportRepository.getMostSoldCategoriesFromMasterProduct(pharmacyId, startDate, endDate, languageId);
            List<Map<String, Object>> pharmacyCategories = reportRepository.getMostSoldCategoriesFromPharmacyProduct(pharmacyId, startDate, endDate, languageId);
            
            // Combine and merge categories
            Map<String, Map<String, Object>> combinedCategories = new HashMap<>();
            
            // Process MasterProduct categories
            for (Map<String, Object> category : masterCategories) {
                String categoryName = (String) category.get("categoryName");
                if (categoryName != null) {
                    combinedCategories.put(categoryName, category);
                }
            }
            
            // Process PharmacyProduct categories and merge with existing ones
            for (Map<String, Object> category : pharmacyCategories) {
                String categoryName = (String) category.get("categoryName");
                if (categoryName != null) {
                    if (combinedCategories.containsKey(categoryName)) {
                        // Merge with existing category
                        Map<String, Object> existing = combinedCategories.get(categoryName);
                        Long totalQuantity = ((Number) existing.get("totalQuantity")).longValue() + ((Number) category.get("totalQuantity")).longValue();
                        Double totalRevenue = ((Number) existing.get("totalRevenue")).doubleValue() + ((Number) category.get("totalRevenue")).doubleValue();
                        Long invoiceCount = ((Number) existing.get("invoiceCount")).longValue() + ((Number) category.get("invoiceCount")).longValue();
                        
                        existing.put("totalQuantity", totalQuantity);
                        existing.put("totalRevenue", totalRevenue);
                        existing.put("invoiceCount", invoiceCount);
                    } else {
                        // Add new category
                        combinedCategories.put(categoryName, category);
                    }
                }
            }
            
            // Convert to list and sort by total quantity
            List<Map<String, Object>> categoriesRaw = new ArrayList<>(combinedCategories.values());
            categoriesRaw.sort((a, b) -> {
                Long qtyA = ((Number) a.get("totalQuantity")).longValue();
                Long qtyB = ((Number) b.get("totalQuantity")).longValue();
                return qtyB.compareTo(qtyA); // Descending order
            });
            
            // Convert to DTOs using mapper
            List<CategoryReportResponse.CategoryData> categories = reportMapper.toCategoryDataList(categoriesRaw);
            
            // Build response
            CategoryReportResponse response = new CategoryReportResponse();
            response.setSuccess(true);
            response.setPharmacyId(pharmacyId);
            response.setStartDate(startDate);
            response.setEndDate(endDate);
            response.setLanguage(language);
            response.setGeneratedAt(LocalDateTime.now());
            response.setCategories(categories);
            
            return response;
                    
        } catch (Exception e) {
            log.error("Error generating most sold categories report: {}", e.getMessage(), e);
            CategoryReportResponse errorResponse = new CategoryReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * Get Top 10 Products Monthly
     * Returns the top 10 most sold products in the pharmacy for the specified month
     * Includes currency conversion and profit calculations
     */
    public ProductReportResponse getTop10Products( LocalDate startDate, LocalDate endDate, Currency currency, Language language) {
        Long pharmacyId = getCurrentUserPharmacyId();
        log.info("Generating top 10 products report for pharmacy: {}, period: {} to {}", pharmacyId, startDate, endDate);

        try {
            // Get top products with currency information
            List<Map<String, Object>> productsRaw = reportRepository.getTop10Products(pharmacyId, startDate, endDate);
            
            // Process currency-aware product data
            List<Map<String, Object>> processedProducts = processCurrencyAwareProductData(productsRaw, currency);
            
            // Limit to top 10 and convert to DTOs using mapper
            List<ProductReportResponse.ProductData> products = reportMapper.toProductDataList(
                processedProducts.stream().limit(10).collect(Collectors.toList())
            );
            
            // Build response
            ProductReportResponse response = new ProductReportResponse();
            response.setSuccess(true);
            response.setPharmacyId(pharmacyId);
            response.setStartDate(startDate);
            response.setEndDate(endDate);
            response.setCurrency(currency);
            response.setLanguage(language);
            response.setGeneratedAt(LocalDateTime.now());
            response.setProducts(products);
            
            return response;
                    
        } catch (Exception e) {
            log.error("Error generating top 10 products report: {}", e.getMessage(), e);
            ProductReportResponse errorResponse = new ProductReportResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError(e.getMessage());
            return errorResponse;
        }
    }
    
    // ============================================================================
    // HELPER METHODS
    // ============================================================================
    
    /**
     * Get product name by product ID and product type
     * This method fetches the actual product name from the appropriate product table
     */
    private String getProductName(String productIdStr, String productType) {
        try {
            Long productId = Long.parseLong(productIdStr);
            
            if ("PHARMACY".equals(productType)) {
                java.util.Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                if (pharmacyProduct.isPresent()) {
                    return pharmacyProduct.get().getTradeName();
                }
            } else if ("MASTER".equals(productType)) {
                java.util.Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
                if (masterProduct.isPresent()) {
                    return masterProduct.get().getTradeName();
                }
            }
            
            // If not found with specific type, try both tables as fallback
            java.util.Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
            if (pharmacyProduct.isPresent()) {
                return pharmacyProduct.get().getTradeName();
            }
            
            java.util.Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
            if (masterProduct.isPresent()) {
                return masterProduct.get().getTradeName();
            }
            
            // If not found, return the product ID as fallback
            return "Product ID: " + productId;
            
        } catch (NumberFormatException e) {
            log.warn("Invalid product ID format: {}", productIdStr);
            return "Invalid Product ID";
        } catch (Exception e) {
            log.error("Error fetching product name for ID {}: {}", productIdStr, e.getMessage());
            return "Unknown Product";
        }
    }
    
    /**
     * Get product name by product ID only (fallback method)
     * This method fetches the actual product name from the appropriate product table
     */
    private String getProductName(String productIdStr) {
        return getProductName(productIdStr, null);
    }
    
    /**
     * Get product name for profit item, handling null productName from stockItem
     * This method tries to get the product name from the stockItem first, 
     * and if that's null, it fetches from product tables using productId and productType
     */
    private String getProductNameForProfitItem(Map<String, Object> item) {
        try {
            Object productNameObj = item.get("productName");
            Object productIdObj = item.get("productId");
            Object productTypeObj = item.get("productType");
            
            // If productName from stockItem is not null and not empty, use it
            if (productNameObj != null && !productNameObj.toString().trim().isEmpty()) {
                return productNameObj.toString();
            }
            
            // If productName is null, fetch from product tables using productId and productType
            if (productIdObj != null && productTypeObj != null) {
                try {
                    Long productId = Long.parseLong(productIdObj.toString());
                    String productType = productTypeObj.toString();
                    
                    if ("PHARMACY".equals(productType)) {
                        java.util.Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                        if (pharmacyProduct.isPresent()) {
                            return pharmacyProduct.get().getTradeName();
                        }
                    } else if ("MASTER".equals(productType)) {
                        java.util.Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
                        if (masterProduct.isPresent()) {
                            return masterProduct.get().getTradeName();
                        }
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid product ID format in profit item: {}", productIdObj);
                }
            }
            
            // If all else fails, return descriptive message
            return "Product Name Not Available";
            
        } catch (Exception e) {
            log.error("Error getting product name for profit item: {}", e.getMessage());
            return "Unknown Product";
        }
    }
    
    /**
     * Get language ID from Language enum
     * Converts the Language enum to the corresponding Language entity ID
     */
    private Long getLanguageId(Language language) {
        try {
            // Convert enum to string and find the corresponding Language entity
            String languageCode = language.name().toLowerCase();
            com.Uqar.language.Language languageEntity = languageRepo.findByCode(languageCode).orElse(null);
            if (languageEntity != null) {
                return languageEntity.getId();
            }
            
            // Fallback to English if not found
            com.Uqar.language.Language englishEntity = languageRepo.findByCode("en").orElse(null);
            if (englishEntity != null) {
                return englishEntity.getId();
            }
            
            // If no language found, return null (will use default category names)
            return null;
            
        } catch (Exception e) {
            log.warn("Error getting language ID for {}: {}", language, e.getMessage());
            return null;
        }
    }
}
