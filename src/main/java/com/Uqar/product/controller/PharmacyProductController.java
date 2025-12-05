package com.Uqar.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Uqar.product.dto.PharmacyProductDTORequest;
import com.Uqar.product.dto.PharmacyProductIdsMaultiLangDTOResponse;
import com.Uqar.product.dto.ProductMultiLangDTOResponse;
import com.Uqar.product.service.PharmacyProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@RestController
@RequestMapping("api/v1/pharmacy_products")
@Tag(name = "Pharmacy Product Management", description = "APIs for managing pharmacy-specific products")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class PharmacyProductController {

    private final PharmacyProductService pharmacyProductService;

    public PharmacyProductController(PharmacyProductService pharmacyProductService) {
        this.pharmacyProductService = pharmacyProductService;
    }
    

    @GetMapping
    @Operation(
        summary = "Get all pharmacy products",
        description = "Retrieves all pharmacy products with enhanced pagination (same as purchase invoices)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved pharmacy products",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllPharmacyProducts(
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang,
            @Parameter(description = "Page number (0-based)", example = "0") 
                                                  @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") 
                                                  @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") 
                                                  @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc", 
                      schema = @Schema(allowableValues = {"asc", "desc"})) 
                                                  @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(pharmacyProductService.getPharmacyProductPaginated(lang, page, size));
    }



    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @GetMapping("pharmacy/{pharmacyId}")
    @Operation(
        summary = "Get pharmacy products by pharmacy ID",
        description = "Retrieves all products for a specific pharmacy with enhanced pagination. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved pharmacy products",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Pharmacy not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getPharmacyProductsByPharmacyId(
            @Parameter(description = "Pharmacy ID", example = "1") @PathVariable Long pharmacyId,
            @Parameter(description = "Language code", example = "en") 
                                                           @RequestParam(name = "lang", defaultValue = "ar") String lang,
            @Parameter(description = "Page number (0-based)", example = "0") 
                                                           @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10") 
                                                           @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") 
                                                           @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc", 
                      schema = @Schema(allowableValues = {"asc", "desc"})) 
                                                           @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(pharmacyProductService.getPharmacyProductByPharmacyIdPaginated(pharmacyId, lang, page, size));
    }



    @GetMapping("{id}")
    @Operation(
        summary = "Get pharmacy product by ID",
        description = "Retrieves a specific pharmacy product by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved pharmacy product",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Pharmacy product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getPharmacyProductById(
            @Parameter(description = "Pharmacy product ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Language code", example = "en") 
                                                     @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(pharmacyProductService.getByID(id, lang));
    }

    @PostMapping
    @Operation(
        summary = "Create new pharmacy product",
        description = "Creates a new pharmacy product"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created pharmacy product",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid pharmacy product data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createPharmacyProduct(
            @Parameter(description = "Pharmacy product data", required = true)
            @Valid @RequestBody PharmacyProductDTORequest pharmacyProduct, 
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(pharmacyProductService.insertPharmacyProduct(pharmacyProduct, lang));
    }

    @PutMapping("{id}")
    @Operation(
        summary = "Update pharmacy product",
        description = "Updates an existing pharmacy product"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated pharmacy product",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid pharmacy product data"),
        @ApiResponse(responseCode = "404", description = "Pharmacy product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updatePharmacyProductById(
            @Parameter(description = "Pharmacy product ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Updated pharmacy product data", required = true)
                                                        @RequestBody PharmacyProductDTORequest pharmacyProduct, 
            @Parameter(description = "Language code", example = "en") 
                                                        @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(pharmacyProductService.editPharmacyProduct(id, pharmacyProduct, lang));
    }



    @DeleteMapping("{id}")
    @Operation(
        summary = "Delete pharmacy product",
        description = "Deletes a pharmacy product"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted pharmacy product"),
        @ApiResponse(responseCode = "404", description = "Pharmacy product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deletePharmacyProductById(
            @Parameter(description = "Pharmacy product ID", example = "1") @PathVariable Long id) {
        pharmacyProductService.deletePharmacyProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/multi-lang")
    @Operation(
        summary = "Get all pharmacy products with multi-language support",
        description = "Retrieves all pharmacy products with both Arabic and English translations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all pharmacy products with multi-language support",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ProductMultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getPharmacyProductsMultiLang() {
        return ResponseEntity.ok(pharmacyProductService.getPharmacyProductsMultiLang());
    }

    @GetMapping("/multi-lang/{id}")
    @Operation(
        summary = "Get pharmacy product by ID with multi-language support",
        description = "Retrieves a specific pharmacy product by ID with both Arabic and English translations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved pharmacy product with multi-language support",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ProductMultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Pharmacy product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getPharmacyProductByIdMultiLang(
            @Parameter(description = "Pharmacy product ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(pharmacyProductService.getPharmacyProductByIdMultiLang(id));
    }

    @GetMapping("/multi-lang-with-ids")
    @Operation(
        summary = "Get all pharmacy products with multi-language support and IDs",
        description = "Retrieves all pharmacy products with both Arabic and English translations, including all related entity IDs"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all pharmacy products with multi-language support and IDs",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PharmacyProductIdsMaultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<PharmacyProductIdsMaultiLangDTOResponse>> getPharmacyProductsWithIdsMultiLang() {
        return ResponseEntity.ok(pharmacyProductService.getPharmacyProductsWithIdsMultiLang());
    }

    @GetMapping("/multi-lang-with-ids/{id}")
    @Operation(
        summary = "Get pharmacy product by ID with multi-language support and IDs",
        description = "Retrieves a specific pharmacy product by ID with both Arabic and English translations, including all related entity IDs"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved pharmacy product with multi-language support and IDs",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PharmacyProductIdsMaultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Pharmacy product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PharmacyProductIdsMaultiLangDTOResponse> getPharmacyProductByIdWithIdsMultiLang(
            @Parameter(description = "Pharmacy product ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(pharmacyProductService.getPharmacyProductByIdWithIdsMultiLang(id));
    }
}
