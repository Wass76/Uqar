package com.Teryaq.product.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.dto.InventoryAdjustmentRequest;
import com.Teryaq.product.dto.StockItemDTOResponse;
import com.Teryaq.product.dto.StockItemEditRequest;
import com.Teryaq.product.dto.StockProductOverallDTOResponse;
import com.Teryaq.product.service.CurrencyConversionService;
import com.Teryaq.product.service.StockService;
import com.Teryaq.user.Enum.Currency;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "Stock Managment", description = "Managing Stock on the pharmacy")
public class StockManagementController {

    private final StockService stockService;
    private final CurrencyConversionService currencyConversionService;
    
    @Transactional
    @PutMapping("/{stockItemId}/edit")
    @Operation(summary = "edit stock quantity and expiry date", description = "edit stock quantity and expiry date together")
    public ResponseEntity<StockItemDTOResponse> adjustStockQuantityAndExpiryDate(
            @PathVariable Long stockItemId,
            @Valid @RequestBody StockItemEditRequest request) {
        
        StockItemDTOResponse result = stockService.editStockQuantityAndExpiryDate(
            stockItemId,
            request.getQuantity(),
            request.getExpiryDate(),
            request.getMinStockLevel(),
            request.getReasonCode(),
            request.getAdditionalNotes()
        );
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/search")
    @Operation(
        summary = "Search for Stock Products", 
        description = "Search for unique products in stock by product name, barcode, or trade name. Returns each product once with aggregated stock information. Always displays prices in both SYP and USD currencies."
    )
    public ResponseEntity<List<StockProductOverallDTOResponse>> advancedStockSearch(
            @Parameter(description = "Search keyword for product name, barcode, or trade name", example = "paracetamol")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Language code for product names", example = "ar")
            @RequestParam(defaultValue = "en") String lang) {
        
        List<StockProductOverallDTOResponse> stockProducts = stockService.stockItemSearch(keyword, lang);
        
        // Always apply dual currency conversion
        stockProducts.forEach(product -> {
            product.setDualCurrencyDisplay(true);
            
            // Convert selling price to USD
            if (product.getSellingPrice() != null && product.getSellingPrice() > 0) {
                var convertedPrice = currencyConversionService.convertPriceFromSYP(product.getSellingPrice(), Currency.USD);
                if (convertedPrice != null) {
                    product.setSellingPriceUSD(convertedPrice.getDisplayPrice().floatValue());
                    product.setExchangeRateSYPToUSD(convertedPrice.getExchangeRate().doubleValue());
                    product.setConversionTimestampSYPToUSD(convertedPrice.getConversionTimestamp());
                    product.setRateSource(convertedPrice.getRateSource());
                }
            }
            
            // Convert average purchase price to USD
            if (product.getActualPurchasePrice() != null && product.getActualPurchasePrice() > 0) {
                var convertedPrice = currencyConversionService.convertPriceFromSYP(product.getActualPurchasePrice(), Currency.USD);
                if (convertedPrice != null) {
                    product.setActualPurchasePriceUSD(convertedPrice.getDisplayPrice().doubleValue());
                    product.setExchangeRateSYPToUSD(convertedPrice.getExchangeRate().doubleValue());
                    product.setConversionTimestampSYPToUSD(convertedPrice.getConversionTimestamp());
                    product.setRateSource(convertedPrice.getRateSource());
                }
            }
            
            // Convert total value to USD
            if (product.getTotalValue() != null && product.getTotalValue() > 0) {
                var convertedPrice = currencyConversionService.convertPriceFromSYP(product.getTotalValue(), Currency.USD);
                if (convertedPrice != null) {
                    product.setTotalValueUSD(convertedPrice.getDisplayPrice().doubleValue());
                    product.setExchangeRateSYPToUSD(convertedPrice.getExchangeRate().doubleValue());
                    product.setConversionTimestampSYPToUSD(convertedPrice.getConversionTimestamp());
                    product.setRateSource(convertedPrice.getRateSource());
                }
            }
        });
        
        return ResponseEntity.ok(stockProducts);
    }
    

    @DeleteMapping("/{stockItemId}")
    @Operation(summary = "delete stock item", description = "delete a specific stock item")
    public ResponseEntity<Void> deleteStockItem(@PathVariable Long stockItemId) {
        stockService.deleteStockItem(stockItemId);
        return ResponseEntity.noContent().build();
    }
    // @GetMapping("/expired")
    // @Operation(summary = "expired products", description = "get all expired products")
    // public ResponseEntity<List<StockItem>> getExpiredItems() {
    //     List<StockItem> stockItems = stockService.getExpiredItems();
    //     return ResponseEntity.ok(stockItems);
    // }
    
    // @GetMapping("/expiring-soon")
    // @Operation(summary = "expiring-soon products", description = "get all expiring-soon products during 30 days")
    // public ResponseEntity<List<StockItem>> getItemsExpiringSoon() {
    //     List<StockItem> stockItems = stockService.getItemsExpiringSoon();
    //     return ResponseEntity.ok(stockItems);
    // }
        
    // @GetMapping("/report/stock-summary")
    // @Operation(summary = "stock-summary", description = "stock-summary with statistces")
    // public ResponseEntity<Map<String, Object>> getStockSummary() {
    //     Map<String, Object> summary = stockService.getStockSummary();
    //     return ResponseEntity.ok(summary);
    // }
    
    // @GetMapping("/report/stock-value")
    // @Operation(summary = "stock-value", description = "")
    // public ResponseEntity<Map<String, Object>> getStockValue() {
    //     Map<String, Object> stockValue = stockService.getStockValue();
    //     return ResponseEntity.ok(stockValue);
    // }
    
    
    // @GetMapping("/report/{productType}")
    // @Operation(summary = "report by product type", description = "get report by product type")
    // public ResponseEntity<Map<String, Object>> getStockReportByProductType(@PathVariable ProductType productType) {
    //     Map<String, Object> report = stockService.getStockReportByProductType(productType);
    //     return ResponseEntity.ok(report);
    // }
    
    // @GetMapping("/report/comprehensive")
    // @Operation(summary = "comprehensive report for stock", description = "get comprehensive report for stock")
    // public ResponseEntity<Map<String, Object>> getComprehensiveStockReport() {
    //     Map<String, Object> report = stockService.getComprehensiveStockReport();
    //     return ResponseEntity.ok(report);
    // }
    
    @GetMapping("/products/Overall")
    @Operation(
        summary = "Get stock products Overall", 
        description = "Get Overall of all products in stock (each product once with aggregated data)."
    )
    public ResponseEntity<List<StockProductOverallDTOResponse>> getStockProductsOverall() {
        
        List<StockProductOverallDTOResponse> productsOverall = stockService.getAllStockProductsOverall();
        
        return ResponseEntity.ok(productsOverall);
    }
    
    @GetMapping("/product/{productId}/details")
    @Operation(summary = "Get product stock details", description = "Get detailed stock information for a specific product.")
    public ResponseEntity<Map<String, Object>> getProductStockDetails(
            @PathVariable Long productId,
            @RequestParam ProductType productType) {
        Map<String, Object> productDetails = stockService.getProductStockDetails(productId, productType);
        return ResponseEntity.ok(productDetails);
    }

    @PostMapping("/adjustment/add")
    @Operation(
        summary = "Add stock items without purchase invoice",
        description = "Add medicines to inventory without creating a purchase invoice. " +
                      "Useful for inventory adjustments, physical counts, or error corrections."
    )
    @PreAuthorize("hasRole('PHARMACY_MANAGER') or hasRole('PHARMACY_EMPLOYEE')")
    public ResponseEntity<StockItemDTOResponse> addStockWithoutInvoice(
            @Valid @RequestBody InventoryAdjustmentRequest request) {
        
        StockItemDTOResponse result = stockService.addStockWithoutInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    
} 