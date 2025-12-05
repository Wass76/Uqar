package com.Uqar.user.controller;

import com.Uqar.user.dto.SupplierDTORequest;
import com.Uqar.user.dto.SupplierDTOResponse;
import com.Uqar.user.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/suppliers")
@Tag(name = "Supplier Management", description = "APIs for managing suppliers and their information")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @Operation(
        summary = "Create new supplier",
        description = "Creates a new supplier in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created supplier",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SupplierDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid supplier data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SupplierDTOResponse> create(
            @Parameter(description = "Supplier data", required = true)
            @Valid @RequestBody SupplierDTORequest request) {
        return ResponseEntity.ok(supplierService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update supplier",
        description = "Updates an existing supplier's information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated supplier",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SupplierDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid supplier data"),
        @ApiResponse(responseCode = "404", description = "Supplier not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SupplierDTOResponse> update(
            @Parameter(description = "Supplier ID", example = "1") @PathVariable Long id, 
            @Parameter(description = "Updated supplier data", required = true)
            @Valid @RequestBody SupplierDTORequest request) {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete supplier",
        description = "Deletes a supplier from the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully deleted supplier"),
        @ApiResponse(responseCode = "404", description = "Supplier not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Supplier ID", example = "1") @PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get supplier by ID",
        description = "Retrieves a specific supplier by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved supplier",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SupplierDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Supplier not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SupplierDTOResponse> getById(
            @Parameter(description = "Supplier ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @GetMapping
    @Operation(
        summary = "Get all suppliers",
        description = "Retrieves a list of all suppliers in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all suppliers",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SupplierDTOResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<SupplierDTOResponse>> listAll() {
        return ResponseEntity.ok(supplierService.listAll());
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search suppliers by name",
        description = "Searches for suppliers by name (partial match)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved matching suppliers",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = SupplierDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid search parameter"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<SupplierDTOResponse>> searchByName(
            @Parameter(description = "Supplier name to search for", example = "ABC Pharma") 
            @RequestParam String name) {
        return ResponseEntity.ok(supplierService.searchByName(name));
    }
} 