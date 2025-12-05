package com.Uqar.reports.controller;

import com.Uqar.reports.dto.response.*;
import com.Uqar.reports.enums.Language;
import com.Uqar.reports.service.ReportService;
import com.Uqar.user.Enum.Currency;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Reports Controller
 * Provides REST endpoints for the simplified reports agreed upon with the business team:
 * 1. Monthly Purchase Report (daily breakdown)
 * 2. Daily Purchase Report
 * 3. Monthly Profit Report (daily breakdown)
 * 4. Daily Profit Report
 * 5. Most Sold Categories Monthly
 * 6. Top 10 Products Monthly
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "Reports", description = "Simplified Reports API for Pharmacy Management System")
public class ReportController {
    
    private final ReportService reportService;
    
    // ============================================================================
    // PURCHASE REPORTS
    // ============================================================================
    
    @GetMapping("/purchase/monthly")
    @Operation(summary = "Monthly Purchase Report", description = "Get monthly purchase report with daily breakdown")
    public ResponseEntity<PurchaseReportResponse> getMonthlyPurchaseReport(
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2024-01-01")
            @RequestParam LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2024-01-31")
            @RequestParam LocalDate endDate,
            @Parameter(description = "Currency", example = "SYP")
            @RequestParam(defaultValue = "SYP") String currency,
            @Parameter(description = "Language", example = "EN")
            @RequestParam(defaultValue = "EN") String language) {
        log.info("Generating monthly purchase report for period: {} to {}", startDate, endDate);
        try {
            PurchaseReportResponse response = reportService.getMonthlyPurchaseReport(
                    startDate, endDate,     Currency.valueOf(currency), Language.valueOf(language));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating monthly purchase report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/purchase/daily")
    @Operation(summary = "Daily Purchase Report", description = "Get daily purchase report for a specific date")
    public ResponseEntity<DailyPurchaseReportResponse> getDailyPurchaseReport(
            @Parameter(description = "Date (YYYY-MM-DD)", example = "2024-01-15")
            @RequestParam LocalDate date,
            @Parameter(description = "Currency", example = "SYP")
            @RequestParam(defaultValue = "SYP") String currency,
            @Parameter(description = "Language", example = "EN")
            @RequestParam(defaultValue = "EN") String language) {
        log.info("Generating daily purchase report for date: {}", date);
        try {
            DailyPurchaseReportResponse response = reportService.getDailyPurchaseReport(
                    date, Currency.valueOf(currency), Language.valueOf(language));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating daily purchase report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ============================================================================
    // PROFIT REPORTS
    // ============================================================================
    
    @GetMapping("/profit/monthly")
    @Operation(summary = "Monthly Profit Report", description = "Get monthly profit report with daily breakdown")
    public ResponseEntity<ProfitReportResponse> getMonthlyProfitReport(
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2024-01-01")
            @RequestParam LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2024-01-31")
            @RequestParam LocalDate endDate,
            @Parameter(description = "Currency", example = "SYP")
            @RequestParam(defaultValue = "SYP") String currency,
            @Parameter(description = "Language", example = "EN")
            @RequestParam(defaultValue = "EN") String language) {
        log.info("Generating monthly profit report for period: {} to {}", startDate, endDate);
        try {
            ProfitReportResponse response = reportService.getMonthlyProfitReport(
                    startDate, endDate, Currency.valueOf(currency), Language.valueOf(language));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating monthly profit report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/profit/daily")
    @Operation(summary = "Daily Profit Report", description = "Get daily profit report for a specific date")
    public ResponseEntity<ProfitReportResponse> getDailyProfitReport(
            @Parameter(description = "Date (YYYY-MM-DD)", example = "2024-01-15")
            @RequestParam LocalDate date,
            @Parameter(description = "Currency", example = "SYP")
            @RequestParam(defaultValue = "SYP") String currency,
            @Parameter(description = "Language", example = "EN")
            @RequestParam(defaultValue = "EN") String language) {
        log.info("Generating daily profit report for date: {}", date);
        try {
            ProfitReportResponse response = reportService.getDailyProfitReport(
                    date, Currency.valueOf(currency), Language.valueOf(language));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating daily profit report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ============================================================================
    // CATEGORY AND PRODUCT REPORTS
    // ============================================================================
    
    @GetMapping("/categories/most-sold")
    @Operation(summary = "Most Sold Categories", description = "Get most sold categories monthly")
    public ResponseEntity<CategoryReportResponse> getMostSoldCategories(
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2024-01-01")
            @RequestParam LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2024-01-31")
            @RequestParam LocalDate endDate,
            @Parameter(description = "Language", example = "en")
            @RequestParam(defaultValue = "en") String language) {
        log.info("Generating most sold categories report for period: {} to {}", startDate, endDate);
        try {
            CategoryReportResponse response = reportService.getMostSoldCategories(
                    startDate, endDate, Language.valueOf(language));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating most sold categories report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/products/top-10")
    @Operation(summary = "Top 10 Products", description = "Get top 10 most sold products monthly")
    public ResponseEntity<ProductReportResponse> getTop10Products(
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2024-01-01")
            @RequestParam LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2024-01-31")
            @RequestParam LocalDate endDate,
            @Parameter(description = "Currency", example = "SYP")
            @RequestParam(defaultValue = "SYP") String currency,
            @Parameter(description = "Language", example = "en")
            @RequestParam(defaultValue = "en") String language) {
        log.info("Generating top 10 products report period: {} to {}", startDate, endDate);
        try {
            ProductReportResponse response = reportService.getTop10Products(
                     startDate, endDate, Currency.valueOf(currency), Language.valueOf(language));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating top 10 products report: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
