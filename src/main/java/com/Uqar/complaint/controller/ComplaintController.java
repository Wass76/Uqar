package com.Uqar.complaint.controller;

import com.Uqar.complaint.dto.ComplaintRequestDTO;
import com.Uqar.complaint.dto.ComplaintResponseDTO;
import com.Uqar.complaint.dto.ComplaintUpdateRequestDTO;
import com.Uqar.complaint.enums.ComplaintStatus;
import com.Uqar.complaint.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/complaints")
@Tag(name = "Complaint Management", description = "APIs for managing pharmacy complaints")
@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ComplaintController {
    
    private static final Logger logger = LoggerFactory.getLogger(ComplaintController.class);
    
    private final ComplaintService complaintService;
    
    @PostMapping
    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE')")
    @Operation(
        summary = "Create a new complaint",
        description = "Allows pharmacy managers and employees to create a new complaint. " +
                    "The pharmacy ID will be automatically filled based on the current user's pharmacy."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Complaint created successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ComplaintResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User not authorized to create complaints")
    })
    public ResponseEntity<ComplaintResponseDTO> createComplaint(
            @Valid @RequestBody ComplaintRequestDTO requestDTO,
            HttpServletRequest httpRequest) {
        
        logger.info("Creating new complaint with title: {}", requestDTO.getTitle());
        ComplaintResponseDTO responseDTO = complaintService.createComplaint(requestDTO, httpRequest);
        return ResponseEntity.ok(responseDTO);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') or hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get complaint by ID",
        description = "Retrieves a specific complaint by its ID. Users can only access complaints from their own pharmacy."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Complaint retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ComplaintResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Complaint not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User not authorized to access this complaint")
    })
    public ResponseEntity<ComplaintResponseDTO> getComplaintById(
            @Parameter(description = "Complaint ID", required = true)
            @PathVariable Long id) {
        
        logger.info("Retrieving complaint with ID: {}", id);
        ComplaintResponseDTO responseDTO = complaintService.getComplaintById(id);
        return ResponseEntity.ok(responseDTO);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') or hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get all complaints for pharmacy",
        description = "Retrieves all complaints for the current user's pharmacy with optional pagination."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Complaints retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<ComplaintResponseDTO>> getAllComplaints(
            @Parameter(description = "Page number (0-based)", example = "0")
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (1-100)", example = "10")
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size)  {
        logger.info("Retrieving all complaints with pagination");
        Pageable pageable = PageRequest.of(page, size);
        Page<ComplaintResponseDTO> complaints = complaintService.getAllComplaintsForPharmacy(pageable);
        return ResponseEntity.ok(complaints);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') or hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get complaints by status",
        description = "Retrieves complaints filtered by status for the current user's pharmacy."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Complaints retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<ComplaintResponseDTO>> getComplaintsByStatus(
            @Parameter(description = "Complaint status", required = true)
            @PathVariable ComplaintStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.info("Retrieving complaints with status: {}", status);
        Page<ComplaintResponseDTO> complaints = complaintService.getComplaintsByStatus(status, pageable);
        return ResponseEntity.ok(complaints.getContent());
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Update complaint status and response",
        description = "Allows pharmacy managers and platform admins to update complaint status and add responses."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Complaint updated successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ComplaintResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Complaint not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User not authorized to update complaints")
    })
    public ResponseEntity<ComplaintResponseDTO> updateComplaint(
            @Parameter(description = "Complaint ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ComplaintUpdateRequestDTO updateDTO,
            HttpServletRequest httpRequest) {
        
        logger.info("Updating complaint with ID: {}", id);
        ComplaintResponseDTO responseDTO = complaintService.updateComplaint(id, updateDTO, httpRequest);
        return ResponseEntity.ok(responseDTO);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Delete complaint",
        description = "Allows complaint creators, pharmacy managers, and platform admins to delete complaints."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Complaint deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Complaint not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User not authorized to delete this complaint")
    })
    public ResponseEntity<Void> deleteComplaint(
            @Parameter(description = "Complaint ID", required = true)
            @PathVariable Long id) {
        
        logger.info("Deleting complaint with ID: {}", id);
        complaintService.deleteComplaint(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get complaint statistics",
        description = "Retrieves complaint statistics (count by status) for the current user's pharmacy."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<ComplaintStatus, Long>> getComplaintStatistics() {
        logger.info("Retrieving complaint statistics");
        Map<ComplaintStatus, Long> statistics = complaintService.getComplaintStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/needing-response")
    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PLATFORM_ADMIN')")
    @Operation(
        summary = "Get complaints needing response",
        description = "Retrieves complaints that are pending or in progress and need management response."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Complaints retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<ComplaintResponseDTO>> getComplaintsNeedingResponse() {
        logger.info("Retrieving complaints needing response");
        List<ComplaintResponseDTO> complaints = complaintService.getComplaintsNeedingResponse();
        return ResponseEntity.ok(complaints);
    }
}