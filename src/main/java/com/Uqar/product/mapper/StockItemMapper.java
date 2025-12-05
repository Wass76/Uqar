package com.Uqar.product.mapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.Uqar.product.Enum.ProductType;
import com.Uqar.product.dto.StockItemDTOResponse;
import com.Uqar.product.dto.StockItemDetailDTOResponse;
import com.Uqar.product.dto.StockItemWithProductInfoDTOResponse;
import com.Uqar.product.dto.StockProductOverallDTOResponse;
import com.Uqar.product.dto.StockReportDTOResponse;
import com.Uqar.product.entity.Category;
import com.Uqar.product.entity.MasterProduct;
import com.Uqar.product.entity.PharmacyProduct;
import com.Uqar.product.entity.PharmacyProductBarcode;
import com.Uqar.product.entity.StockItem;
import com.Uqar.product.repo.MasterProductRepo;
import com.Uqar.product.repo.PharmacyProductRepo;
import com.Uqar.product.repo.StockItemRepo;
import com.Uqar.product.service.CurrencyConversionService;
import com.Uqar.purchase.repository.PurchaseOrderItemRepo;
import com.Uqar.user.Enum.Currency;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockItemMapper {
    
    private final PharmacyProductRepo pharmacyProductRepo;
    private final MasterProductRepo masterProductRepo;
    private final PurchaseOrderItemRepo purchaseOrderItemRepo;
    private final StockItemRepo stockItemRepo;
    private final CurrencyConversionService currencyConversionService;
    
    public StockItemDTOResponse toResponse(StockItem stockItem) {
        return toResponse(stockItem, null);
    }
    
    public StockItemDTOResponse toResponse(StockItem stockItem, Currency currency) {
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = stockItem.getExpiryDate();
        
        Boolean isExpired = expiryDate != null && expiryDate.isBefore(today);
        Boolean isExpiringSoon = expiryDate != null && 
            expiryDate.isAfter(today) && 
            expiryDate.isBefore(today.plusDays(30));
        
        Integer daysUntilExpiry = null;
        if (expiryDate != null && expiryDate.isAfter(today)) {
            daysUntilExpiry = (int) ChronoUnit.DAYS.between(today, expiryDate);
        }
        
        String productName = getProductName(
            stockItem.getProductId(), stockItem.getProductType());
        
        Integer total = getTotalFromPurchaseOrder(stockItem);
        
        String supplier = getSupplierName(stockItem);
        List<String> categories = getCategories(stockItem.getProductId(), stockItem.getProductType());
        // Use minStockLevel from StockItem directly, fallback to product if null
        // Integer minStockLevel = stockItem.getMinStockLevel() != null ? 
        //     stockItem.getMinStockLevel() : 
        //     getMinStockLevel(stockItem.getProductId(), stockItem.getProductType());

        List<String> barcodes = getBarcodes(stockItem.getProductId(), stockItem.getProductType());
        
        Float sellingPrice = getProductSellingPrice(stockItem.getProductId(), stockItem.getProductType());
        
        StockItemDTOResponse.StockItemDTOResponseBuilder builder = StockItemDTOResponse.builder()
                .id(stockItem.getId())
                .productId(stockItem.getProductId())
                .productName(productName)
                .productType(stockItem.getProductType())
                .barcodes(barcodes)
                .quantity(stockItem.getQuantity())
                .bonusQty(stockItem.getBonusQty())
                .total(total)
                .supplier(supplier)
                .categories(categories)
                .expiryDate(stockItem.getExpiryDate())
                .batchNo(stockItem.getBatchNo())
                .actualPurchasePrice(Math.round(stockItem.getActualPurchasePrice() * 100.0) / 100.0)
                .sellingPrice(sellingPrice != null ? Math.round(sellingPrice * 100.0f) / 100.0f : null)
                .dateAdded(stockItem.getDateAdded())
                .addedBy(stockItem.getCreatedBy() != null ? stockItem.getCreatedBy() : stockItem.getAddedBy())
                .purchaseInvoiceId(stockItem.getPurchaseInvoice() != null ? 
                    stockItem.getPurchaseInvoice().getId() : null)
                .isExpired(isExpired)
                .isExpiringSoon(isExpiringSoon)
                .daysUntilExpiry(daysUntilExpiry)
                .pharmacyId(stockItem.getPharmacy().getId())
                .purchaseInvoiceNumber(stockItem.getPurchaseInvoice() != null ? 
                    stockItem.getPurchaseInvoice().getInvoiceNumber() : null);
        
        // Apply currency conversion if currency is specified
        if (currency != null && !Currency.SYP.equals(currency)) {
            applyCurrencyConversion(builder, sellingPrice, stockItem.getActualPurchasePrice(), currency);
        }
        
        return builder.build();
    }
    
    private void applyCurrencyConversion(StockItemDTOResponse.StockItemDTOResponseBuilder builder, 
                                        Float sellingPrice, Double actualPurchasePrice, Currency currency) {
        builder.requestedCurrency(currency.name())
               .pricesConverted(true);
        
        // Convert selling price
        if (sellingPrice != null && sellingPrice > 0) {
            var convertedPrice = currencyConversionService.convertPriceFromSYP(
                java.math.BigDecimal.valueOf(sellingPrice), currency);
            if (convertedPrice != null) {
                builder.sellingPriceUSD(convertedPrice.getDisplayPrice().floatValue())
                       .exchangeRateSYPToUSD(convertedPrice.getExchangeRate().doubleValue())
                       .conversionTimestampSYPToUSD(convertedPrice.getConversionTimestamp())
                       .rateSource(convertedPrice.getRateSource());
            }
        }
        
        // Convert actual purchase price
        if (actualPurchasePrice != null && actualPurchasePrice > 0) {
            var convertedPrice = currencyConversionService.convertPriceFromSYP(
                java.math.BigDecimal.valueOf(actualPurchasePrice), currency);
            if (convertedPrice != null) {
                builder.actualPurchasePriceUSD(convertedPrice.getDisplayPrice().doubleValue())
                       .exchangeRateSYPToUSD(convertedPrice.getExchangeRate().doubleValue())
                       .conversionTimestampSYPToUSD(convertedPrice.getConversionTimestamp())
                       .rateSource(convertedPrice.getRateSource());
            }
        }
    }
    
    public List<StockItemDTOResponse> toResponseList(List<StockItem> stockItems) {
        return stockItems.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public StockReportDTOResponse toReportResponse(List<StockItem> stockItems, 
                                                   ProductType productType) {
        List<StockItemDTOResponse> stockItemResponses = toResponseList(stockItems);
        
        Integer totalQuantity = stockItems.stream()
                .mapToInt(StockItem::getQuantity)
                .sum();
        
        Double totalValue = stockItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getActualPurchasePrice())
                .sum();
        
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        
        Long expiredItems = stockItems.stream()
                .filter(item -> item.getExpiryDate() != null && item.getExpiryDate().isBefore(today))
                .count();
        
        Long expiringSoonItems = stockItems.stream()
                .filter(item -> item.getExpiryDate() != null && 
                    item.getExpiryDate().isAfter(today) && 
                    item.getExpiryDate().isBefore(thirtyDaysFromNow))
                .count();
        
        return StockReportDTOResponse.builder()
                .productType(productType)
                .totalItems(stockItems.size())
                .totalQuantity(totalQuantity)
                .totalValue(totalValue)
                .expiredItems(expiredItems)
                .expiringSoonItems(expiringSoonItems)
                .stockItems(stockItemResponses)
                .build();
    }
    
    public StockItemWithProductInfoDTOResponse toStockItemWithProductInfoDTO(StockItem stockItem) {
        return StockItemWithProductInfoDTOResponse.builder()
                .id(stockItem.getId())
                .productId(stockItem.getProductId())
                .productType(stockItem.getProductType())
                .quantity(stockItem.getQuantity())
                .actualPurchasePrice(stockItem.getActualPurchasePrice())
                .expiryDate(stockItem.getExpiryDate())
                .dateAdded(stockItem.getDateAdded())
                .productName(getProductName(stockItem.getProductId(), stockItem.getProductType()))
                .batchNo(stockItem.getBatchNo())
                .bonusQty(stockItem.getBonusQty())
                .addedBy(stockItem.getAddedBy())
                .purchaseInvoiceId(stockItem.getPurchaseInvoice() != null ? stockItem.getPurchaseInvoice().getId() : null)
                .requiresPrescription(isProductRequiresPrescription(stockItem.getProductId(), stockItem.getProductType()))
                .build();
    }
    
    public List<StockItemWithProductInfoDTOResponse> toStockItemWithProductInfoDTOList(List<StockItem> stockItems) {
        return stockItems.stream()
                .map(this::toStockItemWithProductInfoDTO)
                .collect(Collectors.toList());
    }
    
    public StockItemDetailDTOResponse toDetailResponse(StockItem stockItem) {
        
        String productName = getProductName(
            stockItem.getProductId(), stockItem.getProductType());
        
        String supplier = getSupplierName(stockItem);
        List<String> categories = getCategories(stockItem.getProductId(), stockItem.getProductType());
       // Integer minStockLevel = getMinStockLevel(stockItem.getProductId(), stockItem.getProductType());
        
        List<String> barcodes = getBarcodes(stockItem.getProductId(), stockItem.getProductType());
        
        Double totalValue = stockItem.getQuantity() * stockItem.getActualPurchasePrice();

        Boolean isRequiresPrescription = isProductRequiresPrescription(stockItem.getProductId(), stockItem.getProductType());
        
        return StockItemDetailDTOResponse.builder()
                .id(stockItem.getId())
                .productName(productName)
                .batchNumber(stockItem.getBatchNo())
                .productType(stockItem.getProductType())
                .barcodes(barcodes)
                .currentStock(stockItem.getQuantity())
                .actualPurchasePrice(stockItem.getActualPurchasePrice())
                .totalValue(totalValue)
                .categories(categories)
                .supplier(supplier)
                .expiryDate(stockItem.getExpiryDate())
                .requiresPrescription(isRequiresPrescription)
                .build();
    }

    private String getSupplierName(StockItem stockItem) {
        try {
            if (stockItem.getPurchaseInvoice() != null && 
                stockItem.getPurchaseInvoice().getSupplier() != null) {
                return stockItem.getPurchaseInvoice().getSupplier().getName();
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private List<String> getCategories(Long productId, ProductType productType) {
        try {
            if (productType == ProductType.PHARMACY) {
                Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                if (pharmacyProduct.isPresent() && pharmacyProduct.get().getCategories() != null) {
                    return pharmacyProduct.get().getCategories().stream()
                            .map(Category::getName)
                            .collect(Collectors.toList());
                }
            } else if (productType == ProductType.MASTER) {
                Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
                if (masterProduct.isPresent() && masterProduct.get().getCategories() != null) {
                    return masterProduct.get().getCategories().stream()
                            .map(Category::getName)
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
        }
        return List.of();
    }
    
    public Integer getMinStockLevel(Long productId, ProductType productType) {
        try {
            List<StockItem> stockItems = stockItemRepo.findByProductIdAndProductTypeOrderByDateAddedDesc(productId, productType);
            if (!stockItems.isEmpty()) {
                Integer stockItemMinLevel = stockItems.get(0).getMinStockLevel();
                if (stockItemMinLevel != null) {
                    return stockItemMinLevel; // Return from StockItem if available
                }
            }
            
            if (productType == ProductType.PHARMACY) {
                Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                if (pharmacyProduct.isPresent()) {
                    return pharmacyProduct.get().getMinStockLevel();
                }
            } else if (productType == ProductType.MASTER) {
                Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
                if (masterProduct.isPresent()) {
                    return masterProduct.get().getMinStockLevel();
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    public String getProductName(Long productId, ProductType productType) {
        return getProductName(productId, productType, "en"); // Default to English
    }
    
    public String getProductName(Long productId, ProductType productType, String lang) {
        try {
            if (productType == ProductType.PHARMACY) {
                Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                if (pharmacyProduct.isPresent()) {
                    PharmacyProduct product = pharmacyProduct.get();
                    // Try to get translated name first
                    String translatedName = getTranslatedPharmacyProductName(product, lang);
                    return translatedName != null ? translatedName : product.getTradeName();
                }
            } else if (productType == ProductType.MASTER) {
                Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
                if (masterProduct.isPresent()) {
                    MasterProduct product = masterProduct.get();
                    // Try to get translated name first
                    String translatedName = getTranslatedMasterProductName(product, lang);
                    return translatedName != null ? translatedName : product.getTradeName();
                }
            }
        } catch (Exception e) {
        }
        return "Unknown Product";
    }
    
    private String getTranslatedPharmacyProductName(PharmacyProduct product, String lang) {
        return product.getTranslations().stream()
            .filter(t -> lang.equalsIgnoreCase(t.getLanguage().getCode()))
            .findFirst()
            .map(translation -> translation.getTradeName())
            .orElse(null);
    }
    
    private String getTranslatedMasterProductName(MasterProduct product, String lang) {
        return product.getTranslations().stream()
            .filter(t -> lang.equalsIgnoreCase(t.getLanguage().getCode()))
            .findFirst()
            .map(translation -> translation.getTradeName())
            .orElse(null);
    }
    
    /**
     * الحصول على الاسم العربي والإنجليزي معاً
     */
    public Map<String, String> getProductNames(Long productId, ProductType productType) {
        Map<String, String> names = new HashMap<>();
        names.put("ar", "Unknown Product");
        names.put("en", "Unknown Product");
        
        try {
            if (productType == ProductType.PHARMACY) {
                Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                if (pharmacyProduct.isPresent()) {
                    PharmacyProduct product = pharmacyProduct.get();
                    String arabicName = getTranslatedPharmacyProductName(product, "ar");
                    String englishName = getTranslatedPharmacyProductName(product, "en");
                    names.put("ar", arabicName != null ? arabicName : product.getTradeName());
                    names.put("en", englishName != null ? englishName : product.getTradeName());
                }
            } else if (productType == ProductType.MASTER) {
                Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
                if (masterProduct.isPresent()) {
                    MasterProduct product = masterProduct.get();
                    String arabicName = getTranslatedMasterProductName(product, "ar");
                    String englishName = getTranslatedMasterProductName(product, "en");
                    names.put("ar", arabicName != null ? arabicName : product.getTradeName());
                    names.put("en", englishName != null ? englishName : product.getTradeName());
                }
            }
        } catch (Exception e) {
            // Keep default values
        }
        
        return names;
    }
    
    public boolean isProductRequiresPrescription(Long productId, ProductType productType) {
        try {
            if (productType == ProductType.PHARMACY) {
                Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                if (pharmacyProduct.isPresent()) {
                    return pharmacyProduct.get().getRequiresPrescription();
                }
            } else if (productType == ProductType.MASTER) {
                Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
                if (masterProduct.isPresent()) {
                    return masterProduct.get().getRequiresPrescription();
                }
            }
        } catch (Exception e) {
        }
        return false;   
    }
    
    public Float getProductSellingPrice(Long productId, ProductType productType) {
        try {
            if (productType == ProductType.PHARMACY) {
                Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                if (pharmacyProduct.isPresent()) {
                    return pharmacyProduct.get().getRefSellingPrice();
                }
            } else if (productType == ProductType.MASTER) {
                Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
                if (masterProduct.isPresent()) {
                    return masterProduct.get().getRefSellingPrice();
                }
            }
        } catch (Exception e) {
        }
        return 0f;
    }

    // private Integer getTotalFromPurchaseInvoice(StockItem stockItem) {
    //     try {
    //         if (stockItem.getPurchaseInvoice() != null) {
    //             Optional<com.Uqar.purchase.entity.PurchaseInvoiceItem> invoiceItem = 
    //                 stockItem.getPurchaseInvoice().getItems().stream()
    //                     .filter(item -> item.getProductId().equals(stockItem.getProductId()) &&
    //                                   item.getProductType() == stockItem.getProductType() &&
    //                                   (item.getBatchNo() != null && item.getBatchNo().equals(stockItem.getBatchNo())))
    //                     .findFirst();
                
    //             if (invoiceItem.isPresent()) {
    //                 double price = invoiceItem.get().getInvoicePrice() != null ? invoiceItem.get().getInvoicePrice() : 0.0;
    //                 int quantity = invoiceItem.get().getReceivedQty() != null ? invoiceItem.get().getReceivedQty() : 0;
        //             return (int) (price * quantity);
        //         }
        //     }
        // } catch (Exception e) {
        // }
    //    return 0;
    //}

    private Integer getTotalFromPurchaseOrder(StockItem stockItem) {
        try {
            List<com.Uqar.purchase.entity.PurchaseOrderItem> orderItems = 
                purchaseOrderItemRepo.findByProductIdAndProductType(stockItem.getProductId(), stockItem.getProductType());
            
            if (!orderItems.isEmpty()) {
                com.Uqar.purchase.entity.PurchaseOrderItem orderItem = orderItems.get(0);
                
                if (orderItem.getPurchaseOrder() != null) {
                    return orderItem.getPurchaseOrder().getTotal() != null ? 
                           orderItem.getPurchaseOrder().getTotal().intValue() : 0;
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }

    private List<String> getBarcodes(Long productId, ProductType productType) {
        List<String> barcodes = new ArrayList<>();
        
        try {
            if (productType == ProductType.PHARMACY) {
                Optional<PharmacyProduct> pharmacyProduct = pharmacyProductRepo.findById(productId);
                if (pharmacyProduct.isPresent()) {
                    barcodes = pharmacyProduct.get().getBarcodes().stream()
                            .map(PharmacyProductBarcode::getBarcode)
                            .collect(Collectors.toList());
                }
            } else if (productType == ProductType.MASTER) {
                Optional<MasterProduct> masterProduct = masterProductRepo.findById(productId);
                if (masterProduct.isPresent() && masterProduct.get().getBarcode() != null) {
                    barcodes.add(masterProduct.get().getBarcode());
                }
            }
        } catch (Exception e) {
        }
        
        return barcodes;
    }
    
    public StockProductOverallDTOResponse toProductSummary(Long productId, ProductType productType, List<StockItem> stockItems, Long pharmacyId) {
        return toProductSummaryWithLang(productId, productType, stockItems, pharmacyId, true, "en"); // Always enable dual currency, default English
    }
    
    public StockProductOverallDTOResponse toProductSummary(Long productId, ProductType productType, List<StockItem> stockItems, Long pharmacyId, boolean dualCurrency) {
        return toProductSummaryWithLang(productId, productType, stockItems, pharmacyId, dualCurrency, "en"); // Default English
    }
    
    public StockProductOverallDTOResponse toProductSummaryWithLang(Long productId, ProductType productType, List<StockItem> stockItems, Long pharmacyId, boolean dualCurrency, String lang) {
        if (stockItems.isEmpty()) {
            return null;
        }
        
        Integer totalQuantity = stockItems.stream().mapToInt(StockItem::getQuantity).sum();
        Integer totalBonusQuantity = stockItems.stream().mapToInt(item -> item.getBonusQty() != null ? item.getBonusQty() : 0).sum();
        Double actualPurchasePrice = stockItems.stream()
            .mapToDouble(item -> item.getActualPurchasePrice() * item.getQuantity())
            .sum() / totalQuantity;
        // Round to 2 decimal places
        actualPurchasePrice = Math.round(actualPurchasePrice * 100.0) / 100.0;
        
        Double totalValue = totalQuantity * actualPurchasePrice;
        // Round to 2 decimal places
        totalValue = Math.round(totalValue * 100.0) / 100.0;
        
        // الحصول على الاسم حسب اللغة المطلوبة
        String productName = getProductName(productId, productType, lang);
        
        // الحصول على الاسم العربي والإنجليزي معاً
        Map<String, String> productNames = getProductNames(productId, productType);
        String productNameAr = productNames.get("ar");
        String productNameEn = productNames.get("en");
        
        List<String> categories = getCategories(productId, productType);
        Float sellingPrice = getProductSellingPrice(productId, productType);
        Integer minStockLevel = getMinStockLevel(productId, productType);
        List<String> barcodes = getBarcodes(productId, productType);
        
        LocalDate today = LocalDate.now();
        Boolean hasExpiredItems = stockItems.stream()
            .anyMatch(item -> item.getExpiryDate() != null && item.getExpiryDate().isBefore(today));
        Boolean hasExpiringSoonItems = stockItems.stream()
            .anyMatch(item -> item.getExpiryDate() != null && 
                item.getExpiryDate().isAfter(today) && 
                item.getExpiryDate().isBefore(today.plusDays(30)));
        
        LocalDate earliestExpiryDate = stockItems.stream()
            .map(StockItem::getExpiryDate)
            .filter(Objects::nonNull)
            .min(LocalDate::compareTo)
            .orElse(null);
        
        LocalDate latestExpiryDate = stockItems.stream()
            .map(StockItem::getExpiryDate)
            .filter(Objects::nonNull)
            .max(LocalDate::compareTo)
            .orElse(null);
        
        StockProductOverallDTOResponse.StockProductOverallDTOResponseBuilder builder = StockProductOverallDTOResponse.builder()
            .id(stockItems.get(0).getId())
            .productId(productId)
            .productName(productName)
            .productNameAr(productNameAr)
            .productNameEn(productNameEn)
            .productType(productType)
            .barcodes(barcodes)
            .totalQuantity(totalQuantity)
            .totalBonusQuantity(totalBonusQuantity)
            .actualPurchasePrice(actualPurchasePrice)   
            .totalValue(totalValue)
            .categories(categories)
            .sellingPrice(sellingPrice)
            .minStockLevel(minStockLevel)
            .hasExpiredItems(hasExpiredItems)
            .hasExpiringSoonItems(hasExpiringSoonItems)
            .earliestExpiryDate(earliestExpiryDate)
            .latestExpiryDate(latestExpiryDate)
            .numberOfBatches(stockItems.size())
            .pharmacyId(pharmacyId);
        
        // Apply dual currency conversion if requested
        if (dualCurrency) {
            applyDualCurrencyConversion(builder, sellingPrice, actualPurchasePrice, totalValue);
        }
        
        return builder.build();
    }
    
    private void applyDualCurrencyConversion(StockProductOverallDTOResponse.StockProductOverallDTOResponseBuilder builder,
                                           Float sellingPrice, Double actualPurchasePrice, Double totalValue) {
        builder.dualCurrencyDisplay(true);
        
        // Convert selling price to USD
        if (sellingPrice != null && sellingPrice > 0) {
            var convertedPrice = currencyConversionService.convertPriceFromSYP(sellingPrice, Currency.USD);
            if (convertedPrice != null) {
                builder.sellingPriceUSD(convertedPrice.getDisplayPrice().floatValue())
                       .exchangeRateSYPToUSD(convertedPrice.getExchangeRate().doubleValue())
                       .conversionTimestampSYPToUSD(convertedPrice.getConversionTimestamp())
                       .rateSource(convertedPrice.getRateSource());
            }
        }
        
        // Convert average purchase price to USD
        if (actualPurchasePrice != null && actualPurchasePrice > 0) {
            var convertedPrice = currencyConversionService.convertPriceFromSYP(actualPurchasePrice, Currency.USD);
            if (convertedPrice != null) {
                builder.actualPurchasePriceUSD(convertedPrice.getDisplayPrice().doubleValue())
                       .exchangeRateSYPToUSD(convertedPrice.getExchangeRate().doubleValue())
                       .conversionTimestampSYPToUSD(convertedPrice.getConversionTimestamp())
                       .rateSource(convertedPrice.getRateSource());
            }
        }
        
        // Convert total value to USD
        if (totalValue != null && totalValue > 0) {
            var convertedPrice = currencyConversionService.convertPriceFromSYP(totalValue, Currency.USD);
            if (convertedPrice != null) {
                builder.totalValueUSD(convertedPrice.getDisplayPrice().doubleValue());
                builder.exchangeRateSYPToUSD(convertedPrice.getExchangeRate().doubleValue())
                       .conversionTimestampSYPToUSD(convertedPrice.getConversionTimestamp())
                       .rateSource(convertedPrice.getRateSource());
            }
        }
    }
} 