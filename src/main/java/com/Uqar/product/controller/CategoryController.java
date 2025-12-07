package com.Uqar.product.controller;


import com.Uqar.product.dto.CategoryDTORequest;
import com.Uqar.product.dto.CategoryDTOResponse;
import com.Uqar.product.dto.MultiLangDTOResponse;
import com.Uqar.product.service.CategoryService;
import org.springframework.http.HttpStatus;
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


@RestController
@RequestMapping("api/v1/categories")
@Tag(name = "Product Category Management", description = "APIs for managing product categories")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(
        summary = "Get all product categories",
        description = "Retrieves all product categories with language support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all product categories",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = CategoryDTOResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getCategories(
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(categoryService.getCategories(lang));
    }

    @GetMapping("{id}")
    @Operation(
        summary = "Get product category by ID",
        description = "Retrieves a specific product category by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product category",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = CategoryDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "Product category ID", example = "1") @PathVariable Long id, 
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(categoryService.getByID(id, lang));
    }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Create new product category",
        description = "Creates a new product category. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created product category",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = CategoryDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid product category data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createCategory(
            @Parameter(description = "Product category data", required = true)
            @Valid @RequestBody CategoryDTORequest dto,
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        CategoryDTOResponse response = categoryService.insertCategory(dto, lang);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Update product category",
        description = "Updates an existing product category. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated product category",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = CategoryDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid product category data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Product category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateCategory(
            @Parameter(description = "Product category ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Updated product category data", required = true)
            @Valid @RequestBody CategoryDTORequest category,
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(categoryService.editCategory(id, category, lang));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Delete product category",
        description = "Deletes a product category. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted product category"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Product category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Product category ID", example = "1") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/multi-lang")
    @Operation(
        summary = "Get all product categories with multi-language support",
        description = "Retrieves all product categories with both Arabic and English translations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all product categories with multi-language support",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getCategoriesMultiLang() {
        return ResponseEntity.ok(categoryService.getCategoriesMultiLang());
    }

    @GetMapping("/multi-lang/{id}")
    @Operation(
        summary = "Get product category by ID with multi-language support",
        description = "Retrieves a specific product category by ID with both Arabic and English translations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product category with multi-language support",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product category not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })  
    public ResponseEntity<?> getByIdMultiLang(
            @Parameter(description = "Product category ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getByIDMultiLang(id));
    }
}
