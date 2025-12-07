package com.Uqar.user.controller;

import com.Uqar.user.dto.EmployeeCreateRequestDTO;
import com.Uqar.user.dto.EmployeeUpdateRequestDTO;
import com.Uqar.user.dto.EmployeeResponseDTO;
import com.Uqar.user.dto.CreateWorkingHoursRequestDTO;
import com.Uqar.user.dto.UpsertWorkingHoursRequestDTO;
import com.Uqar.user.service.EmployeeService;

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
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Management", description = "APIs for managing pharmacy employees and their working hours")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Add new employee",
        description = "Creates a new employee in the pharmacy. Requires PHARMACY_MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created employee",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = EmployeeResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid employee data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<EmployeeResponseDTO> addEmployee(
            @Parameter(description = "Employee data", required = true)
            @Valid @RequestBody EmployeeCreateRequestDTO dto) {
        return ResponseEntity.ok(employeeService.addEmployee(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Get all employees in pharmacy",
        description = "Retrieves all employees in the current pharmacy. Requires PHARMACY_MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all employees",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = EmployeeResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployeesInPharmacy() {
        return ResponseEntity.ok(employeeService.getAllEmployeesInPharmacy());
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Get employee by ID",
        description = "Retrieves a specific employee by ID. Requires PHARMACY_MANAGER role and employee must belong to the same pharmacy."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved employee",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = EmployeeResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions or employee not in same pharmacy"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(
            @Parameter(description = "Employee ID", example = "1") @PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeService.getEmployeeByIdWithAuth(employeeId));
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Update employee",
        description = "Updates an existing employee's information. Password cannot be updated for security reasons. Requires PHARMACY_MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated employee",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = EmployeeResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid employee data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<EmployeeResponseDTO> updateEmployeeInPharmacy(
            @Parameter(description = "Employee ID", example = "1") @PathVariable Long employeeId, 
            @Parameter(description = "Updated employee data (password cannot be updated)", required = true)
            @Valid @RequestBody EmployeeUpdateRequestDTO dto) {
        return ResponseEntity.ok(employeeService.updateEmployeeInPharmacy(employeeId, dto));
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Delete employee",
        description = "Deletes an employee from the pharmacy. Requires PHARMACY_MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted employee"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteEmployeeInPharmacy(
            @Parameter(description = "Employee ID", example = "1") @PathVariable Long employeeId) {
        employeeService.deleteEmployeeInPharmacy(employeeId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{employeeId}/working-hours")
    @PreAuthorize("hasRole('PHARMACY_MANAGER')")
    @Operation(
        summary = "Create or update multiple working hours for employee",
        description = "Creates or updates working hours schedule for a specific employee. Accepts multiple working hours configurations for different days and shifts. Requires PHARMACY_MANAGER role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created/updated working hours",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Invalid working hours data"),
        @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> upsertWorkingHoursForEmployee(
            @Parameter(description = "Employee ID", example = "1") @PathVariable Long employeeId,
            @Parameter(description = "Multiple working hours configurations", required = true)
            @Valid @RequestBody UpsertWorkingHoursRequestDTO request) {
        return ResponseEntity.ok(employeeService.upsertWorkingHoursForEmployee(employeeId, request));
    }
} 