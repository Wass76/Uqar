package com.Uqar.user.controller;

import com.Uqar.user.dto.CustomerDebtDTORequest;
import com.Uqar.user.dto.CustomerDebtDTOResponse;
import com.Uqar.user.dto.PayDebtDTORequest;
import com.Uqar.user.dto.PayCustomerDebtsRequest;
import com.Uqar.user.dto.PayCustomerDebtsResponse;
import com.Uqar.user.service.CustomerDebtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer-debts")
@Tag(name = "Customer Debt Management", description = "APIs for managing customer debts and payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomerDebtController {

    private final CustomerDebtService customerDebtService;

   
    @PostMapping
    @Operation(summary = "Create new customer debt", description = "Creates a new debt for a customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully created debt"),
        @ApiResponse(responseCode = "400", description = "Invalid debt data"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CustomerDebtDTOResponse> createDebt(
            @Parameter(description = "Debt data", required = true)
            @RequestBody CustomerDebtDTORequest request) {
        CustomerDebtDTOResponse response = customerDebtService.createDebt(request);
        return ResponseEntity.ok(response);
    }

 
    @GetMapping("/{debtId}")
    @Operation(summary = "Get debt by ID", description = "Returns a specific debt by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved debt"),
        @ApiResponse(responseCode = "404", description = "Debt not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CustomerDebtDTOResponse> getDebtById(
            @Parameter(description = "Debt ID", example = "1") 
            @PathVariable Long debtId) {
        CustomerDebtDTOResponse debt = customerDebtService.getDebtById(debtId);
        return ResponseEntity.ok(debt);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get customer debts", description = "Returns all debts for a specific customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved customer debts"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CustomerDebtDTOResponse>> getCustomerDebts(
            @Parameter(description = "Customer ID", example = "1") 
            @PathVariable Long customerId) {
        List<CustomerDebtDTOResponse> debts = customerDebtService.getCustomerDebts(customerId);
        return ResponseEntity.ok(debts);
    }


    @GetMapping("/customer/{customerId}/status/{status}")
    @Operation(summary = "Get customer debts by status", description = "Returns customer debts filtered by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved debts"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CustomerDebtDTOResponse>> getCustomerDebtsByStatus(
            @Parameter(description = "Customer ID", example = "1") 
            @PathVariable Long customerId,
            @Parameter(description = "Debt status", example = "ACTIVE", 
                      schema = @Schema(allowableValues = {"ACTIVE", "PAID", "OVERDUE"})) 
            @PathVariable String status) {
        List<CustomerDebtDTOResponse> debts = customerDebtService.getCustomerDebtsByStatus(customerId, status);
        return ResponseEntity.ok(debts);
    }


 
    @PostMapping("/pay")
    @Operation(summary = "Pay debt", description = "Makes a payment towards a debt")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully processed payment"),
        @ApiResponse(responseCode = "400", description = "Invalid payment data"),
        @ApiResponse(responseCode = "404", description = "Debt not found"),
        @ApiResponse(responseCode = "409", description = "Debt already paid or payment exceeds debt amount"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CustomerDebtDTOResponse> payDebt(
            @Parameter(description = "Payment data", required = true)
            @RequestBody PayDebtDTORequest request) {
        CustomerDebtDTOResponse response = customerDebtService.payDebt(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{customerId}/autoPay")
    @Operation(summary = "Auto pay debt", description = "Automatically distributes payment across customer debts using FIFO strategy")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully processed payment"),
        @ApiResponse(responseCode = "400", description = "Invalid payment data"),
        @ApiResponse(responseCode = "404", description = "Debt not found"),
        @ApiResponse(responseCode = "409", description = "Debt already paid or payment exceeds debt amount"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PayCustomerDebtsResponse> autoPayDebts(
            @Parameter(description = "Payment data", required = true)
            @PathVariable Long customerId,
            @RequestBody PayCustomerDebtsRequest request) {
        PayCustomerDebtsResponse response = customerDebtService.autoPayDebt(customerId, request);
        return ResponseEntity.ok(response);
    }

 
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue debts", description = "Returns all overdue debts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved overdue debts"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CustomerDebtDTOResponse>> getOverdueDebts() {
        List<CustomerDebtDTOResponse> overdueDebts = customerDebtService.getOverdueDebts();
        return ResponseEntity.ok(overdueDebts);
    }

 
    // @GetMapping("/overdue/total")
    // @Operation(summary = "Get total overdue debts", description = "Returns the total amount of overdue debts")
    // @ApiResponses(value = {
    //     @ApiResponse(responseCode = "200", description = "Successfully retrieved total overdue amount"),
    //     @ApiResponse(responseCode = "500", description = "Internal server error")
    // })
    // public ResponseEntity<Float> getTotalOverdueDebts() {
    //     Float totalOverdue = customerDebtService.getTotalOverdueDebts();
    //     return ResponseEntity.ok(totalOverdue);
    // }

 
    @GetMapping("/status/{status}")
    @Operation(summary = "Get debts by status", description = "Returns all debts filtered by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved debts"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CustomerDebtDTOResponse>> getDebtsByStatus(
            @Parameter(description = "Debt status", example = "ACTIVE", 
                      schema = @Schema(allowableValues = {"ACTIVE", "PAID", "OVERDUE"})) 
            @PathVariable String status) {
        List<CustomerDebtDTOResponse> debts = customerDebtService.getDebtsByStatus(status);
        return ResponseEntity.ok(debts);
    }

 
    @GetMapping("/date-range")
    @Operation(summary = "Get debts by date range", description = "Returns debts created within a date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved debts"),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CustomerDebtDTOResponse>> getDebtsByDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2024-01-01") 
            @RequestParam LocalDate startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2024-12-31") 
            @RequestParam LocalDate endDate) {
        List<CustomerDebtDTOResponse> debts = customerDebtService.getDebtsByDateRange(startDate, endDate);
        return ResponseEntity.ok(debts);
    }

    
        // @GetMapping("/amount-range")
        // @Operation(summary = "Get debts by amount range", description = "Returns debts within a specific amount range")
        // public ResponseEntity<List<CustomerDebtDTOResponse>> getDebtsByAmountRange(
        //         @Parameter(description = "Minimum amount", example = "100.0") 
        //         @RequestParam Float minAmount,
        //         @Parameter(description = "Maximum amount", example = "1000.0") 
        //         @RequestParam Float maxAmount) {
        //     List<CustomerDebtDTOResponse> debts = customerDebtService.getDebtsByAmountRange(minAmount, maxAmount);
        //     return ResponseEntity.ok(debts);
        // }

   
    @PutMapping("/debt/{debtId}/status")
    @Operation(summary = "Update debt status", description = "Updates the status of a debt")
    public ResponseEntity<CustomerDebtDTOResponse> updateDebtStatus(
            @Parameter(description = "Debt ID", example = "1") 
            @PathVariable Long debtId,
            @Parameter(description = "New status", example = "PAID", 
                      schema = @Schema(allowableValues = {"ACTIVE", "PAID", "OVERDUE"})) 
            @RequestParam String status) {
        CustomerDebtDTOResponse debt = customerDebtService.updateDebtStatus(debtId, status);
        return ResponseEntity.ok(debt);
    }

    // @GetMapping("/statistics")
    // @Operation(summary = "Get debt statistics", description = "Returns comprehensive debt statistics")
    // public ResponseEntity<CustomerDebtService.DebtStatistics> getDebtStatistics() {
    //     CustomerDebtService.DebtStatistics statistics = customerDebtService.getDebtStatistics();
    //     return ResponseEntity.ok(statistics);
    // }

  
    @DeleteMapping("/debt/{debtId}")
    @Operation(summary = "Delete debt", description = "Deletes a debt record. Cannot delete paid debts.")
    public ResponseEntity<Void> deleteDebt(
            @Parameter(description = "Debt ID", example = "1") 
            @PathVariable Long debtId) {
        customerDebtService.deleteDebt(debtId);
        return ResponseEntity.ok().build();
    }
} 