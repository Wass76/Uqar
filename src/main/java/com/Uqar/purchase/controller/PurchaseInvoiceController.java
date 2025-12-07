package com.Uqar.purchase.controller;

import com.Uqar.purchase.dto.PurchaseInvoiceDTORequest;
import com.Uqar.purchase.dto.PurchaseInvoiceDTOResponse;
import com.Uqar.product.dto.PaginationDTO;
import com.Uqar.purchase.service.PurchaseInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/purchase-invoices")
@RequiredArgsConstructor
@Tag(name = "Purchase Invoice Management", description = "APIs for managing purchase invoices")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class PurchaseInvoiceController {
    private final PurchaseInvoiceService purchaseInvoiceService;

    @PostMapping
    @Operation(
        summary = "Create new purchase invoice",
        description = "Creates a new purchase invoice with the given request"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created purchase invoice",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PurchaseInvoiceDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid purchase invoice data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PurchaseInvoiceDTOResponse> create(
            @Parameter(description = "Purchase invoice data", required = true)
            @Valid @RequestBody PurchaseInvoiceDTORequest request, 
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.create(request, language));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get purchase invoice by ID",
        description = "Retrieves a specific purchase invoice by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved purchase invoice",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PurchaseInvoiceDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Purchase invoice not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PurchaseInvoiceDTOResponse> getById(
            @Parameter(description = "Purchase invoice ID", example = "1") 
            @Min(1) @PathVariable Long id, 
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.getById(id, language));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Edit purchase invoice",
        description = "Updates an existing purchase invoice with the given request"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated purchase invoice",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PurchaseInvoiceDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid purchase invoice data"),
        @ApiResponse(responseCode = "404", description = "Purchase invoice not found"),
        @ApiResponse(responseCode = "409", description = "Purchase invoice cannot be edited"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PurchaseInvoiceDTOResponse> edit(
            @Parameter(description = "Purchase invoice ID", example = "1") 
            @Min(1) @PathVariable Long id,
            @Parameter(description = "Updated purchase invoice data", required = true)
            @Valid @RequestBody PurchaseInvoiceDTORequest request, 
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.edit(id, request, language));
    }


    @GetMapping
    @Operation(
        summary = "Get paginated purchase invoices",
        description = "Retrieves purchase invoices with pagination support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated purchase invoices",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PaginationDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaginationDTO<PurchaseInvoiceDTOResponse>> listAllPaginated(
            @Parameter(description = "Page number (0-based)", example = "0") 
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (1-100)", example = "10") 
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.listAllPaginated(page, size, language));
    }

    @GetMapping("/time-range")
    @Operation(
        summary = "Get paginated purchase invoices by time range",
        description = "Retrieves paginated purchase invoices filtered by creation time range"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated purchase invoices by time range",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PaginationDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaginationDTO<PurchaseInvoiceDTOResponse>> getByTimeRangePaginated(
            @Parameter(description = "Start date and time (ISO format)", example = "2024-01-01T00:00:00") 
            @RequestParam String startDate,
            @Parameter(description = "End date and time (ISO format)", example = "2024-12-31T23:59:59") 
            @RequestParam String endDate,
            @Parameter(description = "Page number (0-based)", example = "0") 
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (1-100)", example = "10") 
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDate);
        java.time.LocalDateTime end = java.time.LocalDateTime.parse(endDate);
        return ResponseEntity.ok(purchaseInvoiceService.getByTimeRangePaginated(start, end, page, size, language));
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(
        summary = "Get paginated purchase invoices by supplier",
        description = "Retrieves paginated purchase invoices filtered by supplier ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated purchase invoices by supplier",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PaginationDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid supplier ID"),
        @ApiResponse(responseCode = "404", description = "Supplier not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaginationDTO<PurchaseInvoiceDTOResponse>> getBySupplierPaginated(
            @Parameter(description = "Supplier ID", example = "1") @PathVariable Long supplierId,
            @Parameter(description = "Page number (0-based)", example = "0") 
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (1-100)", example = "10") 
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseInvoiceService.getBySupplierPaginated(supplierId, page, size, language));
    }
} 