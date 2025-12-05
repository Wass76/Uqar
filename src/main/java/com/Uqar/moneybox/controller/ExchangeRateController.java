package com.Uqar.moneybox.controller;

import com.Uqar.moneybox.dto.ExchangeRateRequestDTO;
import com.Uqar.moneybox.dto.ExchangeRateResponseDTO;
import com.Uqar.moneybox.dto.CurrencyConversionResponseDTO;
import com.Uqar.moneybox.service.ExchangeRateService;
import com.Uqar.user.Enum.Currency;
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
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/exchange-rates")
@RequiredArgsConstructor
@Tag(name = "Exchange Rate Management", description = "APIs for managing currency exchange rates")
@SecurityRequirement(name = "BearerAuth")
@CrossOrigin("*")
public class ExchangeRateController {
    
    private final ExchangeRateService exchangeRateService;
    
    @GetMapping("/current/{fromCurrency}/{toCurrency}")
    @Operation(summary = "Get current exchange rate", description = "Get the current active exchange rate for a currency pair")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exchange rate retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ExchangeRateResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Exchange rate not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ExchangeRateResponseDTO> getCurrentRate(
            @Parameter(description = "Source currency", example = "USD") @PathVariable Currency fromCurrency,
            @Parameter(description = "Target currency", example = "SYP") @PathVariable Currency toCurrency) {
        
        ExchangeRateResponseDTO response = exchangeRateService.getCurrentRate(fromCurrency, toCurrency);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/pair/{currency1}/{currency2}")
    @Operation(summary = "Get exchange rate pair", description = "Get both direct and reverse exchange rates for a currency pair")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exchange rate pair retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ExchangeRateResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<ExchangeRateResponseDTO>> getExchangeRatePair(
            @Parameter(description = "First currency", example = "USD") @PathVariable Currency currency1,
            @Parameter(description = "Second currency", example = "SYP") @PathVariable Currency currency2) {
        
        List<ExchangeRateResponseDTO> response = exchangeRateService.getExchangeRatePair(currency1, currency2);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/convert")
    @Operation(summary = "Convert amount between currencies", description = "Convert an amount from one currency to another using current rates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Amount converted successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = CurrencyConversionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid currency pair"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CurrencyConversionResponseDTO> convertAmount(
            @Parameter(description = "Amount to convert", example = "100.00") @RequestParam BigDecimal amount,
            @Parameter(description = "Source currency", example = "USD") @RequestParam Currency fromCurrency,
            @Parameter(description = "Target currency", example = "SYP") @RequestParam Currency toCurrency) {
        
        CurrencyConversionResponseDTO response = exchangeRateService.convertAmountToDTO(amount, fromCurrency, toCurrency);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(summary = "Set exchange rate", description = "Set a new exchange rate for a currency pair and automatically generate reverse rate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exchange rate set successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ExchangeRateResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ExchangeRateResponseDTO> setExchangeRate(@Valid @RequestBody ExchangeRateRequestDTO request) {
        ExchangeRateResponseDTO response = exchangeRateService.setExchangeRate(
            request.getFromCurrency(),
            request.getToCurrency(),
            request.getRate(),
            request.getSource(),
            request.getNotes()
        );
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get all active exchange rates", description = "Get all currently active exchange rates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active rates retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ExchangeRateResponseDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<ExchangeRateResponseDTO>> getAllActiveRates() {
        List<ExchangeRateResponseDTO> response = exchangeRateService.getAllActiveRates();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get exchange rate by ID", description = "Get exchange rate details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exchange rate retrieved successfully",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ExchangeRateResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Exchange rate not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ExchangeRateResponseDTO> getRateById(
            @Parameter(description = "Exchange rate ID", example = "1") @PathVariable Long id) {
        
        ExchangeRateResponseDTO response = exchangeRateService.getRateById(id);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate exchange rate", description = "Deactivate an exchange rate and its reverse rate (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Exchange rate deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Exchange rate not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deactivateRate(
            @Parameter(description = "Exchange rate ID", example = "1") @PathVariable Long id) {
        
        exchangeRateService.deactivateRate(id);
        return ResponseEntity.ok().build();
    }
}
