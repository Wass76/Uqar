package com.Uqar.moneybox.controller;

import com.Uqar.moneybox.dto.MoneyBoxRequestDTO;
import com.Uqar.moneybox.dto.MoneyBoxResponseDTO;
import com.Uqar.moneybox.dto.MoneyBoxTransactionResponseDTO;
import com.Uqar.moneybox.dto.CurrencyConversionResponseDTO;
import com.Uqar.moneybox.dto.ExchangeRateResponseDTO;
import com.Uqar.moneybox.service.MoneyBoxService;
import com.Uqar.moneybox.service.ExchangeRateService;
import com.Uqar.user.Enum.Currency;
import com.Uqar.product.dto.PaginationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/moneybox")
@RequiredArgsConstructor
@Tag(name = "Money Box Management", description = "APIs for managing pharmacy money box with multi-currency support")
@CrossOrigin(origins = "*")
public class MoneyBoxController {
    
    private final MoneyBoxService moneyBoxService;
    private final ExchangeRateService exchangeRateService;

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') ")
    @PostMapping
    @Operation(summary = "Create a new money box", description = "Creates a new money box for the current pharmacy with automatic currency conversion to SYP")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Money box created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Pharmacy already has a money box"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MoneyBoxResponseDTO> createMoneyBox(@Valid @RequestBody MoneyBoxRequestDTO request) {
        MoneyBoxResponseDTO response = moneyBoxService.createMoneyBox(request);
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') ")
    @GetMapping
    @Operation(summary = "Get current pharmacy money box", description = "Retrieves the money box information for the current pharmacy")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Money box retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Money box not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MoneyBoxResponseDTO> getMoneyBox() {
        MoneyBoxResponseDTO response = moneyBoxService.getMoneyBoxByCurrentPharmacy();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') ")
    @PostMapping("/transactions")
    @Operation(summary = "Add manual transaction", description = "Adds a manual transaction to the money box with automatic currency conversion to SYP")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid transaction data"),
        @ApiResponse(responseCode = "409", description = "Money box is not open"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MoneyBoxResponseDTO> addTransaction(
            @Parameter(description = "Transaction amount", required = true) 
            @RequestParam @NotNull(message = "Transaction amount is required") 
            @DecimalMin(value = "-999999.99", message = "Transaction amount must be within valid range")
            BigDecimal amount,
            @Parameter(description = "Transaction description", required = true) 
            @RequestParam @NotNull(message = "Transaction description is required") 
            @NotBlank(message = "Transaction description cannot be empty") 
            @Size(min = 1, max = 500, message = "Transaction description must be between 1 and 500 characters") 
            String description) {
        
        MoneyBoxResponseDTO response = moneyBoxService.addTransaction(amount, description);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE')")
    @PostMapping("/transactions/syp")
    @Operation(summary = "Add transaction in SYP", description = "Adds a manual transaction to the money box in SYP (legacy endpoint)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid transaction data"),
        @ApiResponse(responseCode = "409", description = "Money box is not open"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MoneyBoxResponseDTO> addTransactionInSYP(
            @Parameter(description = "Transaction amount", required = true) 
            @RequestParam @NotNull(message = "Transaction amount is required") 
            @DecimalMin(value = "-999999.99", message = "Transaction amount must be within valid range")
            BigDecimal amount,
            @Parameter(description = "Transaction description", required = true) 
            @RequestParam @NotNull(message = "Transaction description is required") 
            @NotBlank(message = "Transaction description cannot be empty") 
            @Size(min = 1, max = 500, message = "Transaction description must be between 1 and 500 characters") 
            String description) {
        
        MoneyBoxResponseDTO response = moneyBoxService.addTransaction(amount, description);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') ")
    @PostMapping("/reconcile")
    @Operation(summary = "Reconcile cash", description = "Reconciles the money box with actual cash count")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cash reconciled successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid reconciliation data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MoneyBoxResponseDTO> reconcileCash(
            @Parameter(description = "Actual cash count", required = true) 
            @RequestParam @NotNull(message = "Actual cash count is required") 
            @DecimalMin(value = "0.0", message = "Actual cash count must be non-negative") 
            BigDecimal actualCashCount,
            @Parameter(description = "Reconciliation notes", required = true) 
            @RequestParam @NotNull(message = "Reconciliation notes are required") 
            @NotBlank(message = "Reconciliation notes cannot be empty") 
            @Size(min = 1, max = 1000, message = "Reconciliation notes must be between 1 and 1000 characters") 
            String notes) {
        
        MoneyBoxResponseDTO response = moneyBoxService.reconcileCash(actualCashCount, notes);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') ")
    @GetMapping("/summary")
    @Operation(summary = "Get period summary", description = "Gets money box summary for a specific time period")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MoneyBoxService.MoneyBoxSummary> getPeriodSummary(
            @Parameter(description = "Start date (ISO format)") @RequestParam String startDate,
            @Parameter(description = "End date (ISO format)") @RequestParam String endDate) {
        
        // Implementation would parse the date strings and call the service
        // For now, return a placeholder response
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') ")
    @GetMapping("/currency/convert")
    @Operation(summary = "Convert currency to SYP", description = "Converts an amount from any currency to SYP using current exchange rates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Currency converted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid conversion request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CurrencyConversionResponseDTO> convertToSYP(
            @Parameter(description = "Amount to convert", required = true) 
            @RequestParam @NotNull(message = "Amount to convert is required") 
            @DecimalMin(value = "0.01", message = "Amount must be greater than 0") 
            BigDecimal amount,
            @Parameter(description = "Source currency", required = true) 
            @RequestParam @NotNull(message = "Source currency is required") 
            Currency fromCurrency) {
        
        CurrencyConversionResponseDTO response = moneyBoxService.convertCurrencyToSYP(amount, fromCurrency);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') ")
    @GetMapping("/currency/rates")
    @Operation(summary = "Get current exchange rates", description = "Gets current exchange rates for all supported currencies")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exchange rates retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<ExchangeRateResponseDTO>> getCurrentRates() {
        List<ExchangeRateResponseDTO> rates = moneyBoxService.getCurrentExchangeRates();
        return ResponseEntity.ok(rates);
    }

    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE') ")
    @GetMapping("/transactions")
    @Operation(summary = "Get all money box transactions", description = "Retrieves paginated transactions for the current pharmacy money box with dual currency amounts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = PaginationDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
        @ApiResponse(responseCode = "404", description = "Money box not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<PaginationDTO<MoneyBoxTransactionResponseDTO>> getAllTransactions(
            @Parameter(description = "Page number (0-based)", example = "0") 
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (1-100)", example = "10") 
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Start date", example = "2024-01-01") 
            @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "End date", example = "2024-01-31") 
            @RequestParam(required = false) LocalDate endDate,
            @Parameter(description = "Transaction type (optional)") 
            @RequestParam(required = false) String transactionType) {
        
        PaginationDTO<MoneyBoxTransactionResponseDTO> transactions;
        
        if (startDate != null && endDate != null && transactionType != null) {
            // Convert LocalDate to LocalDateTime for the full day range
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            transactions = moneyBoxService.getAllTransactionsPaginated(startDateTime, endDateTime, transactionType, page, size);
        } else if (startDate != null && endDate != null) {
            // Convert LocalDate to LocalDateTime for the full day range
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            transactions = moneyBoxService.getAllTransactionsPaginated(startDateTime, endDateTime, page, size);
        } else if (transactionType != null) {
            transactions = moneyBoxService.getAllTransactionsPaginated(transactionType, page, size);
        } else {
            transactions = moneyBoxService.getAllTransactionsPaginated(page, size);
        }
        
        return ResponseEntity.ok(transactions);
    }

    // TODO: Implement currency conversion report endpoint
    // @GetMapping("/reports/currency-conversion")
    // @Operation(summary = "Get currency conversion report", description = "Shows all invoices with their dual currency amounts and conversion details")
    // public ResponseEntity<CurrencyConversionReportResponse> getCurrencyConversionReport(
    //     @Parameter(description = "Start date (ISO format)") @RequestParam String startDate,
    //     @Parameter(description = "End date (ISO format)") @RequestParam String endDate,
    //     @Parameter(description = "Currency filter (optional)") @RequestParam(required = false) Currency currency) {
    //     // Implementation will show all invoices with dual currency amounts
    //     // Including purchase invoices, sale invoices with their original and SYP equivalent amounts
    //     // Plus exchange rates used and conversion timestamps
    // }
}
