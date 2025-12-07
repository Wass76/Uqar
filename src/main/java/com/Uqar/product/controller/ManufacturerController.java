package com.Uqar.product.controller;


import com.Uqar.product.dto.ManufacturerDTORequest;
import com.Uqar.product.dto.ManufacturerDTOResponse;
import com.Uqar.product.dto.MultiLangDTOResponse;
import com.Uqar.product.service.ManufacturerService;
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
@RequestMapping("api/v1/manufacturers")
@Tag(name = "Manufacturer Management", description = "APIs for managing product manufacturers")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    public ManufacturerController(ManufacturerService manufacturerService) {
        this.manufacturerService = manufacturerService;
    }

    @GetMapping
    @Operation(
        summary = "Get all manufacturers",
        description = "Retrieves all manufacturers with language support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all manufacturers",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ManufacturerDTOResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getManufacturers(
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(manufacturerService.getManufacturers(lang));
    }

    @GetMapping("{id}")
    @Operation(
        summary = "Get manufacturer by ID",
        description = "Retrieves a specific manufacturer by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved manufacturer",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ManufacturerDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Manufacturer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "Manufacturer ID", example = "1") @PathVariable Long id, 
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(manufacturerService.getByID(id, lang));
    }

    @PostMapping
    @PreAuthorize("hasRole('PLATFORM_ADMIN') || hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Create new manufacturer",
        description = "Creates a new manufacturer. Requires PLATFORM_ADMIN or PHARMACY_MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created manufacturer",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ManufacturerDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid manufacturer data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createManufacturer(
            @Parameter(description = "Manufacturer data", required = true)
            @Valid @RequestBody ManufacturerDTORequest dto,
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        ManufacturerDTOResponse response = manufacturerService.insertManufacturer(dto, lang);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Update manufacturer",
        description = "Updates an existing manufacturer. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated manufacturer",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ManufacturerDTOResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid manufacturer data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Manufacturer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateManufacturer(
            @Parameter(description = "Manufacturer ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Updated manufacturer data", required = true)
            @Valid @RequestBody ManufacturerDTORequest manufacturer,
            @Parameter(description = "Language code", example = "en") 
            @RequestParam(name = "lang", defaultValue = "ar") String lang) {
        return ResponseEntity.ok(manufacturerService.editManufacturer(id, manufacturer, lang));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Delete manufacturer",
        description = "Deletes a manufacturer. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted manufacturer"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Manufacturer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteManufacturer(
            @Parameter(description = "Manufacturer ID", example = "1") @PathVariable Long id) {
        manufacturerService.deleteManufacturer(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @GetMapping("/multi-lang")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get all product manufacturers with multi-language support",
        description = "Retrieves all product manufacturers with both Arabic and English translations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all product manufacturers with multi-language support",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getManufacturersMultiLang() {
        return ResponseEntity.ok(manufacturerService.getManufacturersMultiLang());
    }

    @GetMapping("/multi-lang/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get product manufacturer by ID with multi-language support",
        description = "Retrieves a specific product manufacturer by ID with both Arabic and English translations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product manufacturer with multi-language support",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = MultiLangDTOResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product manufacturer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })  
    public ResponseEntity<?> getByIdMultiLang(
            @Parameter(description = "Product manufacturer ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(manufacturerService.getByIDMultiLang(id));
    }
}
