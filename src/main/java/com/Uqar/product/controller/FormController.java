package com.Uqar.product.controller;


import com.Uqar.product.dto.FormDTORequest;
import com.Uqar.product.dto.FormDTOResponse;
import com.Uqar.product.dto.MultiLangDTOResponse;
import com.Uqar.product.service.FormService;
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
@RequestMapping("api/v1/Forms")
@Tag(name = "Product Form Management", description = "APIs for managing product forms")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class FormController {

    private final FormService formService;

    public FormController(FormService formService) {
        this.formService = formService;
    }

    @GetMapping 
    @Operation(
        summary = "Get all product forms",
        description = "Retrieves all product forms with language support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all product forms",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = FormDTOResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> geForms(
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(formService.getForms(lang));
    }

    @GetMapping("{id}")
    @Operation(
        summary = "Get product form by ID",
        description = "Retrieves a specific product form by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product form",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = FormDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product form not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "Product form ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(formService.getByID(id, lang));
    }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Create new product form",
        description = "Creates a new product form. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created product form",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = FormDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid product form data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createForm(
            @Parameter(description = "Product form data", required = true)
            @Valid @RequestBody FormDTORequest dto,
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        FormDTOResponse response = formService.insertForm(dto, lang);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Update product form",
        description = "Updates an existing product form. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated product form",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = FormDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid product form data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Product form not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateForm(
            @Parameter(description = "Product form ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Updated product form data", required = true)
            @Valid @RequestBody FormDTORequest Form,
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(formService.editForm(id, Form, lang));
    }   

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Delete product form",
        description = "Deletes a product form. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted product form"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Product form not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteForm(
            @Parameter(description = "Product form ID", example = "1") @PathVariable Long id) {
        formService.deleteForm(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/multi-lang")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get all product forms with multi-language support",
        description = "Retrieves all product forms with both Arabic and English translations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all product forms with multi-language support",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getFormsMultiLang() {
        return ResponseEntity.ok(formService.getFormsMultiLang());
    }

    @GetMapping("/multi-lang/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get product form by ID with multi-language support",
        description = "Retrieves a specific product form by ID with both Arabic and English translations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product form with multi-language support",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product form not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })  
    public ResponseEntity<?> getByIdMultiLang(
            @Parameter(description = "Product form ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(formService.getByIDMultiLang(id));
    }  
}
