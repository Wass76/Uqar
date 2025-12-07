package com.Uqar.product.controller;


import com.Uqar.product.dto.MProductDTORequest;
import com.Uqar.product.dto.ProductMultiLangDTOResponse;
import com.Uqar.product.dto.MasterProductMinStockLevelRequest;
import com.Uqar.product.service.MasterProductService;
import java.util.List;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;


@RestController
@RequestMapping("api/v1/master_products")
@Tag(name = "Master Product Management", description = "APIs for managing master products")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class MasterProductController {

    private final MasterProductService masterProductService;

    public MasterProductController(MasterProductService masterProductService) {
        this.masterProductService = masterProductService;
    }

    @GetMapping
    @Operation(
        summary = "Get all master products",
        description = "Retrieves all master products with enhanced pagination (same as purchase invoices)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved master products",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAllMasterProducts(
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang,
            @Parameter(description = "Page number (0-based)", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt") 
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", example = "desc", 
                      schema = @Schema(allowableValues = {"asc", "desc"})) 
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(masterProductService.getMasterProductPaginated(lang, page, size));
    }

    @GetMapping("{id}")
    @Operation(
        summary = "Get master product by ID",
        description = "Retrieves a specific master product by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved master product",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Master product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getMasterProductById(
            @Parameter(description = "Master product ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(masterProductService.getByID(id, lang));
    }

    // @PostMapping("/search")
    // public ResponseEntity<?> searchProducts(@RequestBody SearchDTORequest requestDTO ,
    //                                         Pageable pageable) {
    //     return ResponseEntity.ok( masterProductService.search(requestDTO , pageable));
    // }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Create new master product",
        description = "Creates a new master product. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created master product",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid master product data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createMasterProduct(
            @Parameter(description = "Master product data", required = true)
            @Valid @RequestBody MProductDTORequest masterProduct,
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
       return ResponseEntity.ok(masterProductService.insertMasterProduct(masterProduct, lang));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Create multiple master products",
        description = "Creates multiple master products in bulk. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created master products",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid master product data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createMasterProductsBulk(
            @Parameter(description = "List of master product data", required = true)
            @Valid @RequestBody List<MProductDTORequest> masterProducts,
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
       return ResponseEntity.ok(masterProductService.insertMasterProductsBulk(masterProducts, lang));
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Update master product",
        description = "Updates an existing master product. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated master product",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid master product data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Master product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateMasterProductById(
            @Parameter(description = "Master product ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Updated master product data", required = true)
            @Valid @RequestBody MProductDTORequest masterProduct, 
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(masterProductService.editMasterProduct(id, masterProduct, lang));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Delete master product",
        description = "Deletes a master product. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted master product"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Master product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteMasterProductById(
            @Parameter(description = "Master product ID", example = "1") @PathVariable Long id) {
        masterProductService.deleteMasterProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/multi-lang")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get all master products with multi-language support",
        description = "Retrieves all master products with both Arabic and English translations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all master products with multi-language support",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ProductMultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getMasterProductsMultiLang() {
        return ResponseEntity.ok(masterProductService.getMasterProductsMultiLang());
    }

    @GetMapping("/multi-lang/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get master product by ID with multi-language support",
        description = "Retrieves a specific master product by ID with both Arabic and English translations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved master product with multi-language support",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ProductMultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Master product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getMasterProductByIdMultiLang(
            @Parameter(description = "Master product ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(masterProductService.getMasterProductByIdMultiLang(id));
    }

    @PatchMapping("/{id}/min-stock-level")
    @Operation(
        summary = "Update master product minimum stock level",
        description = "Updates the minimum stock level for a master product. Available for pharmacy employees."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated minimum stock level",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid minimum stock level value"),
        @ApiResponse(responseCode = "404", description = "Master product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateMasterProductMinStockLevel(
            @Parameter(description = "Master product ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Minimum stock level data", required = true)
            @Valid @RequestBody MasterProductMinStockLevelRequest request,
            @Parameter(description = "Language code", example = "ar") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(masterProductService.updateMasterProductMinStockLevel(id, request.getMinStockLevel(), lang));
    }
}


