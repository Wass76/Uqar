package com.Uqar.product.controller;


import com.Uqar.product.dto.TypeDTORequest;
import com.Uqar.product.service.TypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import com.Uqar.product.dto.MultiLangDTOResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("api/v1/types")
@Tag(name = "Product Type Management", description = "APIs for managing product types")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class TypeController {

    private final TypeService typeService;

    public TypeController(TypeService typeService) {
        this.typeService = typeService;
    }

    @GetMapping
    @Operation(
        summary = "Get all product types",
        description = "Retrieves all product types with language support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all product types",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAll(
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(typeService.getTypes(lang));
    }

    @GetMapping("{id}")
    @Operation(
        summary = "Get product type by ID",
        description = "Retrieves a specific product type by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product type",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Product type not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "Product type ID", example = "1") @PathVariable Long id,  
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(typeService.getByID(id, lang));
    }

    
    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Create new product type",
        description = "Creates a new product type. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created product type",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid product type data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createType(
            @Parameter(description = "Product type data", required = true)
            @Valid @RequestBody TypeDTORequest type,
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(typeService.insertType(type, lang));
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Update product type",
        description = "Updates an existing product type. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated product type",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid product type data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Product type not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateType(
            @Parameter(description = "Product type ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Updated product type data", required = true)
            @Valid @RequestBody TypeDTORequest type,
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(typeService.editType(id, type, lang));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Delete product type",
        description = "Deletes a product type. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted product type"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Product type not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteType(
            @Parameter(description = "Product type ID", example = "1") @PathVariable Long id) {
        typeService.deleteType(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/multi-lang")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get all product types with multi-language support",
        description = "Retrieves all product types with both Arabic and English translations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all product types with multi-language support",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getTypesMultiLang() {
        return ResponseEntity.ok(typeService.getTypesMultiLang());
    }

    @GetMapping("/multi-lang/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get product type by ID with multi-language support",
        description = "Retrieves a specific product type by ID with both Arabic and English translations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product type with multi-language support",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product type not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })  
    public ResponseEntity<?> getByIdMultiLang(
            @Parameter(description = "Product type ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(typeService.getByIDMultiLang(id));
    }
}