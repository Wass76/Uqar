package com.Uqar.user.controller;

import com.Uqar.user.dto.AuthenticationRequest;
import com.Uqar.user.dto.PharmacyResponseDTO;
import com.Uqar.user.dto.UserAuthenticationResponse;
import com.Uqar.user.service.PharmacyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/pharmacy")
@Tag(name = "Pharmacy Management", description = "APIs for managing pharmacy operations and authentication")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class PharmacyController {

    @Autowired
    private PharmacyService pharmacyService;

    @PostMapping("/login")
    @Operation(
        summary = "Pharmacy user login",
        description = "Authenticates pharmacy users (managers, pharmacists, trainees) and returns a JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UserAuthenticationResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "429", description = "Too many login attempts"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserAuthenticationResponse> pharmacyLogin(
            @Valid @RequestBody AuthenticationRequest request, 
            HttpServletRequest httpServletRequest) {
        UserAuthenticationResponse response = pharmacyService.pharmacyLogin(request, httpServletRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete-registration")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Complete pharmacy registration",
        description = "Completes the pharmacy registration process with additional information including address, contact details, and manager information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registration completed successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PharmacyResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid registration data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> completeRegistration(
            @Parameter(description = "New password for the pharmacy manager", required = true) 
            @RequestParam String newPassword,
            @Parameter(description = "Pharmacy address/location", required = false) 
            @RequestParam(required = false) String location,
            @Parameter(description = "Manager's first name", required = false) 
            @RequestParam(required = false) String managerFirstName,
            @Parameter(description = "Manager's last name", required = false) 
            @RequestParam(required = false) String managerLastName,
            @Parameter(description = "Pharmacy phone number", required = false) 
            @RequestParam(required = false) String pharmacyPhone,
            @Parameter(description = "Pharmacy email address", required = false) 
            @RequestParam(required = false) String pharmacyEmail,
            @Parameter(description = "Pharmacy opening hours", required = false)
            @RequestParam(required = false) String openingHours,
            @Parameter(description = "Area ID where the pharmacy is located", required = false)
            @RequestParam(required = false) Long areaId
    ) {
        PharmacyResponseDTO pharmacy = pharmacyService.completeRegistration(newPassword, location, managerFirstName, managerLastName, pharmacyPhone, pharmacyEmail, openingHours, areaId);
        return ResponseEntity.ok(pharmacy);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get all pharmacies",
        description = "Retrieves a list of all pharmacies in the system. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all pharmacies",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PharmacyResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<PharmacyResponseDTO>> getAllPharmacies() {
        List<PharmacyResponseDTO> pharmacies = pharmacyService.getAllPharmacies();
        return ResponseEntity.ok(pharmacies);
    }
    
    @GetMapping("/{pharmacyId}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Get pharmacy by ID",
        description = "Retrieves a specific pharmacy by ID. Requires PHARMACY_MANAGER role and pharmacy must belong to the current user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved pharmacy",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PharmacyResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions or pharmacy not accessible"),
        @ApiResponse(responseCode = "404", description = "Pharmacy not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PharmacyResponseDTO> getPharmacyById(
            @Parameter(description = "Pharmacy ID", example = "1") @PathVariable Long pharmacyId) {
        PharmacyResponseDTO pharmacy = pharmacyService.getPharmacyByIdWithAuth(pharmacyId);
        return ResponseEntity.ok(pharmacy);
    }
    
    @PostMapping("/update-active-status")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Update active status for all pharmacies",
        description = "Updates the isActive status for all pharmacies based on their registration completion. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated pharmacy active status"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> updateAllPharmacyActiveStatus() {
        pharmacyService.updateAllPharmacyActiveStatus();
        return ResponseEntity.ok("Successfully updated active status for all pharmacies");
    }
} 