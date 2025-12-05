package com.Uqar.user.controller;

import com.Uqar.user.dto.AuthenticationRequest;
import com.Uqar.user.dto.PharmacyCreateRequestDTO;
import com.Uqar.user.dto.PharmacyResponseDTO;
import com.Uqar.user.dto.UserAuthenticationResponse;
import com.Uqar.user.service.PharmacyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "APIs for platform administration and pharmacy creation")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class AdminController {
    @Autowired
    private PharmacyService pharmacyService;

    @PostMapping("/login")
    @Operation(
        summary = "Admin login",
        description = "Authenticates a platform admin and returns a JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UserAuthenticationResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "429", description = "Too many login attempts"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<UserAuthenticationResponse> adminLogin(
            @Valid @RequestBody AuthenticationRequest request, 
            HttpServletRequest httpServletRequest) {
        UserAuthenticationResponse response = pharmacyService.adminLogin(request, httpServletRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pharmacies")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Create new pharmacy",
        description = "Creates a new pharmacy in the system. Requires PLATFORM_ADMIN role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created pharmacy",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PharmacyResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid pharmacy data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createPharmacy(@Valid @RequestBody PharmacyCreateRequestDTO dto) {
        PharmacyResponseDTO pharmacy = pharmacyService.createPharmacy(dto);
        return ResponseEntity.ok(pharmacy);
    }
} 