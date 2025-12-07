package com.Uqar.purchase.controller;

import com.Uqar.purchase.dto.PurchaseOrderDTORequest;
import com.Uqar.purchase.dto.PurchaseOrderDTOResponse;
import com.Uqar.product.dto.PaginationDTO;
import com.Uqar.purchase.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Uqar.product.Enum.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Order Management", description = "APIs for managing purchase orders")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    @Operation(
        summary = "Create new purchase order",
        description = "Creates a new purchase order with the given request"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created purchase order",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PurchaseOrderDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid purchase order data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PurchaseOrderDTOResponse> create(
            @Parameter(description = "Purchase order data", required = true)
            @Valid @RequestBody PurchaseOrderDTORequest request, 
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseOrderService.create(request, language));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get purchase order by ID",
        description = "Retrieves a specific purchase order by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved purchase order",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PurchaseOrderDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Purchase order not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PurchaseOrderDTOResponse> getById(
            @Parameter(description = "Purchase order ID", example = "1") @PathVariable Long id, 
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseOrderService.getById(id, language));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Edit purchase order",
        description = "Updates an existing purchase order with the given request. Cannot edit completed or cancelled orders."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated purchase order",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PurchaseOrderDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid purchase order data"),
        @ApiResponse(responseCode = "404", description = "Purchase order not found"),
        @ApiResponse(responseCode = "409", description = "Purchase order cannot be edited (completed or cancelled)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PurchaseOrderDTOResponse> edit(
            @Parameter(description = "Purchase order ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Updated purchase order data", required = true)
            @Valid @RequestBody PurchaseOrderDTORequest request, 
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseOrderService.edit(id, request, language));
    }


    @GetMapping
    @Operation(
        summary = "Get paginated purchase orders",
        description = "Retrieves purchase orders with pagination support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated purchase orders",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PaginationDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaginationDTO<PurchaseOrderDTOResponse>> listAllPaginated(
            @Parameter(description = "Page number (0-based)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseOrderService.listAllPaginated(page, size, language));
    }



    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get paginated purchase orders by status",
        description = "Retrieves paginated purchase orders filtered by status"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated purchase orders by status",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PaginationDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaginationDTO<PurchaseOrderDTOResponse>> getByStatusPaginated(
            @Parameter(description = "Order status", example = "PENDING", 
                      schema = @Schema(allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELLED"})) 
            @PathVariable OrderStatus status,
            @Parameter(description = "Page number (0-based)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseOrderService.getByStatusPaginated(status, page, size, language));
    }

    @GetMapping("/time-range")
    @Operation(
        summary = "Get paginated purchase orders by time range",
        description = "Retrieves paginated purchase orders filtered by creation time range"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated purchase orders by time range",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PaginationDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaginationDTO<PurchaseOrderDTOResponse>> getByTimeRangePaginated(
            @Parameter(description = "Start date", example = "2024-03-01")
            @RequestParam LocalDate startDate,
            @Parameter(description = "End date", example = "2027-03-01")
            @RequestParam LocalDate endDate,
            @Parameter(description = "Page number (0-based)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        // Convert LocalDate to LocalDateTime for the full day range
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return ResponseEntity.ok(purchaseOrderService.getByTimeRangePaginated(startDateTime, endDateTime, page, size, language));
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(
        summary = "Get paginated purchase orders by supplier",
        description = "Retrieves paginated purchase orders filtered by supplier ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated purchase orders by supplier",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PaginationDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid supplier ID"),
        @ApiResponse(responseCode = "404", description = "Supplier not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaginationDTO<PurchaseOrderDTOResponse>> getBySupplierPaginated(
            @Parameter(description = "Supplier ID", example = "1") @PathVariable Long supplierId,
            @Parameter(description = "Page number (0-based)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(defaultValue = "ar") String language) {
        return ResponseEntity.ok(purchaseOrderService.getBySupplierPaginated(supplierId, page, size, language));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Cancel purchase order",
        description = "Cancels a purchase order. Cannot cancel already cancelled or completed orders."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully cancelled purchase order"),
        @ApiResponse(responseCode = "404", description = "Purchase order not found"),
        @ApiResponse(responseCode = "409", description = "Purchase order cannot be cancelled (already cancelled or completed)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> cancel(
            @Parameter(description = "Purchase order ID", example = "1") @PathVariable Long id) {
        purchaseOrderService.cancel(id);
        return ResponseEntity.ok().build();
    }
} 