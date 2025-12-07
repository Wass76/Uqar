package com.Uqar.reports.controller;

import com.Uqar.reports.dto.response.TopSoldProductsResponse;
import com.Uqar.reports.service.AdminReportService;
import com.Uqar.utils.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@CrossOrigin(origins = "*")
public class AdminReportController extends BaseController {

    @Autowired
    private AdminReportService adminReportService;

    /**
     * Get top 10 sold products in a specific area
     * @param areaId The area ID to filter by
     * @param startDate Start date for the report period (optional)
     * @param endDate End date for the report period (optional)
     * @return List of top 10 sold products in the area
     */
    @GetMapping("/top-sold-products-by-area")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<?> getTopSoldProductsByArea(
            @RequestParam Long areaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            // Validate areaId
            if (areaId == null || areaId <= 0) {
                return sendResponse("Invalid areaId: " + areaId, HttpStatus.BAD_REQUEST);
            }
            
            // Validate date range if both dates are provided
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                return sendResponse("Start date cannot be after end date", HttpStatus.BAD_REQUEST);
            }
            
            List<TopSoldProductsResponse> result = adminReportService.getTopSoldProductsByArea(areaId, startDate, endDate);
            return sendResponse(result, "Top sold products retrieved successfully", HttpStatus.OK);
        } catch (Exception e) {
            return sendResponse("Failed to retrieve top sold products: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get top 10 sold products across all areas
     * @param startDate Start date for the report period (optional)
     * @param endDate End date for the report period (optional)
     * @return List of top 10 sold products across all areas
     */
    @GetMapping("/top-sold-products")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<?> getTopSoldProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            // Validate date range if both dates are provided
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                return sendResponse("Start date cannot be after end date", HttpStatus.BAD_REQUEST);
            }
            
            List<TopSoldProductsResponse> result = adminReportService.getTopSoldProducts(startDate, endDate);
            return sendResponse(result, "Top sold products retrieved successfully", HttpStatus.OK);
        } catch (Exception e) {
            return sendResponse("Failed to retrieve top sold products: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get total count of master products in the system
     * @return Total count of master products
     */
    @GetMapping("/total-master-product-count")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<?> getTotalMasterProductCount() {
        try {
            Long count = adminReportService.getTotalMasterProductCount();
            return sendResponse(count, "Total master product count retrieved successfully", HttpStatus.OK);
        } catch (Exception e) {
            return sendResponse("Failed to retrieve total master product count: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get total count of pharmacies in the system (excluding default pharmacy)
     * @return Total count of pharmacies minus 1
     */
    @GetMapping("/total-pharmacies-count")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<?> getTotalPharmaciesCount() {
        try {
            Long count = adminReportService.getTotalPharmaciesCount();
            return sendResponse(count, "Total pharmacies count retrieved successfully", HttpStatus.OK);
        } catch (Exception e) {
            return sendResponse("Failed to retrieve total pharmacies count: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get total count of active users in the system (excluding platform admin and default users)
     * @return Total count of active users minus platform admin and default users
     */
    @GetMapping("/total-active-users-count")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<?> getTotalActiveUsersCount() {
        try {
            Long count = adminReportService.getTotalActiveUsersCount();
            return sendResponse(count, "Total active users count retrieved successfully", HttpStatus.OK);
        } catch (Exception e) {
            return sendResponse("Failed to retrieve total active users count: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
