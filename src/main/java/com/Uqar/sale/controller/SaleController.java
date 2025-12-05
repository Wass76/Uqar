package com.Uqar.sale.controller;

import com.Uqar.sale.dto.SaleInvoiceDTORequest;
import com.Uqar.sale.dto.SaleInvoiceDTOResponse;
import com.Uqar.sale.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.List;
import com.Uqar.sale.dto.SaleRefundDTORequest;
import com.Uqar.sale.dto.SaleRefundDTOResponse;

@RestController
@RequestMapping("/api/v1/sales")
@Tag(name = "Sale Management", description = "APIs for managing sales and invoices")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class SaleController {
    
    private final SaleService saleService;
   
    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') or hasRole('PHARMACY_TRAINEE') ")
    @Operation(
        summary = "Create a new sale invoice", 
        description = "Creates a new sale invoice with the given request. Requires EMPLOYEE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created sale invoice",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SaleInvoiceDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid sale data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Product or customer not found"),
        @ApiResponse(responseCode = "409", description = "Insufficient stock"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<SaleInvoiceDTOResponse> createSale(
            @Parameter(description = "Sale invoice request data", required = true)
            @Valid @RequestBody SaleInvoiceDTORequest request) {
        SaleInvoiceDTOResponse response = saleService.createSaleInvoice(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') or hasRole('PHARMACY_TRAINEE') ")
    @Operation(
        summary = "Get all sale invoices", 
        description = "Retrieves all sale invoices for the current pharmacy. Requires EMPLOYEE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved sale invoices",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SaleInvoiceDTOResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<SaleInvoiceDTOResponse>> getAllSales() {
        List<SaleInvoiceDTOResponse> response = saleService.getAllSales();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') or hasRole('PHARMACY_TRAINEE') ")
    @Operation(
        summary = "Get a sale invoice by ID", 
        description = "Retrieves a sale invoice by its ID. Requires EMPLOYEE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved sale invoice",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SaleInvoiceDTOResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Sale invoice not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SaleInvoiceDTOResponse> getSaleById(
            @Parameter(description = "Sale invoice ID", example = "1") 
            @Min(1) @PathVariable Long id) {
        SaleInvoiceDTOResponse response = saleService.getSaleById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') or hasRole('PHARMACY_TRAINEE') ")
    @Operation(
        summary = "Cancel a sale invoice", 
        description = "Cancels a sale invoice and restores stock quantities. Requires EMPLOYEE role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully cancelled sale invoice"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Sale invoice not found"),
        @ApiResponse(responseCode = "409", description = "Sale invoice already cancelled"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelSale(
            @Parameter(description = "Sale invoice ID", example = "1") 
            @Min(1) @PathVariable Long id) {
        saleService.cancelSale(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') or hasRole('PHARMACY_TRAINEE') ")
    @Operation(
        summary = "Process sale refund", 
        description = "Process a refund for a sale invoice. Can be full invoice or partial items refund."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully processed refund",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SaleRefundDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid refund request"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Sale invoice not found"),
        @ApiResponse(responseCode = "409", description = "Sale invoice already refunded or cannot be refunded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{id}/refund")
    public ResponseEntity<SaleRefundDTOResponse> processRefund(
            @Parameter(description = "Sale invoice ID", example = "1") 
            @Min(1) @PathVariable Long id,
            @Parameter(description = "Refund request data", required = true)
            @Valid @RequestBody SaleRefundDTORequest request) {
        SaleRefundDTOResponse response = saleService.processRefund(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') or hasRole('PHARMACY_TRAINEE') ")
    @Operation(
        summary = "Get refunds by sale invoice ID", 
        description = "Retrieves all refunds for a specific sale invoice."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved refunds",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SaleRefundDTOResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}/refunds")
    public ResponseEntity<List<SaleRefundDTOResponse>> getRefundsBySaleId(
            @Parameter(description = "Sale invoice ID", example = "1") 
            @Min(1) @PathVariable Long id) {
        List<SaleRefundDTOResponse> response = saleService.getRefundsBySaleId(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') or hasRole('PHARMACY_TRAINEE') ")
    @Operation(
        summary = "Get all refunds", 
        description = "Retrieves all refunds for the current pharmacy."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved refunds",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SaleRefundDTOResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/refunds")
    public ResponseEntity<List<SaleRefundDTOResponse>> getAllRefunds() {
        List<SaleRefundDTOResponse> response = saleService.getAllRefunds();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') or hasRole('PHARMACY_TRAINEE') ")
    @Operation(
        summary = "Get refund details with debt and cash information", 
        description = "Retrieves detailed refund information including debt reduction and cash refund details."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved refund details",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SaleRefundDTOResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Refund not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/refunds/{refundId}/details")
    public ResponseEntity<Object> getRefundDetails(
            @Parameter(description = "Refund ID", example = "1") 
            @Min(1) @PathVariable Long refundId) {
        Object response = saleService.getRefundDetails(refundId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') or hasRole('PHARMACY_TRAINEE') ")
    @Operation(
        summary = "Get refunds by date range", 
        description = "Retrieves refunds between two dates for the current pharmacy."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved refunds",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SaleRefundDTOResponse.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/refunds/date-range")
    public ResponseEntity<List<SaleRefundDTOResponse>> getRefundsByDateRange(
        @Parameter(description = "Start date", example = "2024-01-01")
        @RequestParam("startDate") LocalDate startDate,
        @Parameter(description = "End date", example = "2024-01-31")
        @RequestParam("endDate") LocalDate endDate) {
        List<SaleRefundDTOResponse> response = saleService.getRefundsByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    // @GetMapping("/search-by-date")
    // @Operation(
    //     summary = "Search sale invoices by specific date",
    //     description = "Search for sale invoices on a specific date"
    // )
    // public ResponseEntity<List<SaleInvoiceDTOResponse>> searchSaleInvoiceByDate(
    //     @Parameter(description = "Search date", example = "2025-01-15")
    //     @RequestParam("date") LocalDate createdAt) {
    //     List<SaleInvoiceDTOResponse> response = saleService.searchSaleInvoiceByDate(createdAt);
    //     return ResponseEntity.ok(response);
    // }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') or hasRole('PHARMACY_TRAINEE') ")
    @GetMapping("/searchByDateRange")
    @Operation(
        summary = "Search sale invoices by date range",
        description = "Search for sale invoices between two dates"
    )
    public ResponseEntity<List<SaleInvoiceDTOResponse>> searchSaleInvoiceByDateRange(
        @Parameter(description = "Start date", example = "2025-01-01")
        @RequestParam("startDate") LocalDate startDate,
        @Parameter(description = "End date", example = "2025-01-31")
        @RequestParam("endDate") LocalDate endDate) {
        List<SaleInvoiceDTOResponse> response = saleService.searchSaleInvoiceByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }
} 