package com.Teryaq.product.mapper;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.dto.ProductSearchDTOResponse;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.repo.StockItemRepo;
import com.Teryaq.product.service.CurrencyConversionService;
import com.Teryaq.user.Enum.Currency;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductSearchMapper {
    
    private final StockItemRepo stockItemRepo;
    private final CurrencyConversionService currencyConversionService;
    
    public ProductSearchDTOResponse convertMasterProductToUnifiedDTO(MasterProduct product, String lang, Long pharmacyId) {
        return convertMasterProductToUnifiedDTO(product, lang, pharmacyId, true); // Always enable dual currency
    }
    
    public ProductSearchDTOResponse convertMasterProductToUnifiedDTO(MasterProduct product, String lang, Long pharmacyId, boolean dualCurrency) {
        // الحصول على الترجمة حسب اللغة المطلوبة
        String translatedTradeName = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(lang))
                .findFirst()
                .map(t -> t.getTradeName())
                .orElse(product.getTradeName())
                : product.getTradeName();

        String translatedScientificName = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(lang))
                .findFirst()
                .map(t -> t.getScientificName())
                .orElse(product.getScientificName())
                : product.getScientificName();
        
        // الحصول على الاسم العربي والإنجليزي دائماً
        String tradeNameAr = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase("ar"))
                .findFirst()
                .map(t -> t.getTradeName())
                .orElse(product.getTradeName())
                : product.getTradeName();
        
        String tradeNameEn = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase("en"))
                .findFirst()
                .map(t -> t.getTradeName())
                .orElse(product.getTradeName())
                : product.getTradeName();
        
        String scientificNameAr = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase("ar"))
                .findFirst()
                .map(t -> t.getScientificName())
                .orElse(product.getScientificName())
                : product.getScientificName();
        
        String scientificNameEn = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase("en"))
                .findFirst()
                .map(t -> t.getScientificName())
                .orElse(product.getScientificName())
                : product.getScientificName();

        String translatedNotes = product.getNotes();
        
        Integer quantity = getProductQuantity(product.getId(), ProductType.MASTER, pharmacyId);

        return ProductSearchDTOResponse.builder()
                .id(product.getId())
                .tradeName(translatedTradeName)
                .tradeNameAr(tradeNameAr)
                .tradeNameEn(tradeNameEn)
                .scientificName(translatedScientificName)
                .scientificNameAr(scientificNameAr)
                .scientificNameEn(scientificNameEn)
                .barcodes(product.getBarcode() != null ? Set.of(product.getBarcode()) : new HashSet<>())
                .productTypeName(ProductType.MASTER.getTranslatedName(lang))                
                .requiresPrescription(product.getRequiresPrescription())
                .concentration(product.getConcentration())
                .size(product.getSize())
                .refPurchasePrice(product.getRefPurchasePrice())
                .refSellingPrice(product.getRefSellingPrice())
                .minStockLevel(product.getMinStockLevel())
                .dualCurrencyDisplay(dualCurrency)
                .refPurchasePriceUSD(dualCurrency && product.getRefPurchasePrice() > 0 ? 
                    currencyConversionService.convertPriceFromSYP(product.getRefPurchasePrice(), Currency.USD).getDisplayPrice().doubleValue() : null)
                .refSellingPriceUSD(dualCurrency && product.getRefSellingPrice() > 0 ? 
                    currencyConversionService.convertPriceFromSYP(product.getRefSellingPrice(), Currency.USD).getDisplayPrice().doubleValue() : null)
                .exchangeRateSYPToUSD(dualCurrency ? 
                    currencyConversionService.convertPriceFromSYP(1.0f, Currency.USD).getExchangeRate().doubleValue() : null)
                .notes(translatedNotes)
                .tax(product.getTax())
                .quantity(quantity)
                .pharmacyId(pharmacyId)
                .pharmacyName(null) 
                .typeId(product.getType() != null ? product.getType().getId() : null)
                .formId(product.getForm() != null ? product.getForm().getId() : null)
                .manufacturerId(product.getManufacturer() != null ? product.getManufacturer().getId() : null)
                .categoryIds(product.getCategories() != null ? 
                    product.getCategories().stream()
                        .map(category -> category.getId())
                        .collect(java.util.stream.Collectors.toSet()) 
                    : new HashSet<>())
                .type(product.getType() != null ? 
                    product.getType().getTranslations().stream()
                        .filter(t -> lang.equalsIgnoreCase(t.getLanguage().getCode()))
                        .findFirst()
                        .map(com.Teryaq.product.entity.TypeTranslation::getName)
                        .orElse(product.getType().getName())
                    : null)
                .form(product.getForm() != null ? 
                    product.getForm().getTranslations().stream()
                        .filter(t -> lang.equalsIgnoreCase(t.getLanguage().getCode()))
                        .findFirst()
                        .map(com.Teryaq.product.entity.FormTranslation::getName)
                        .orElse(product.getForm().getName())
                    : null)
                .manufacturer(product.getManufacturer() != null ? 
                    product.getManufacturer().getTranslations().stream()
                        .filter(t -> lang.equalsIgnoreCase(t.getLanguage().getCode()))
                        .findFirst()
                        .map(com.Teryaq.product.entity.ManufacturerTranslation::getName)
                        .orElse(product.getManufacturer().getName())
                    : null)
                .categories(product.getCategories() != null ? 
                    product.getCategories().stream()
                        .map(category -> category.getName())
                        .collect(java.util.stream.Collectors.toSet()) 
                    : new HashSet<>())
                .build();
    }
    
    public ProductSearchDTOResponse convertPharmacyProductToUnifiedDTO(PharmacyProduct product, String lang, Long pharmacyId) {
        return convertPharmacyProductToUnifiedDTO(product, lang, pharmacyId, true); // Always enable dual currency
    }
    
    public ProductSearchDTOResponse convertPharmacyProductToUnifiedDTO(PharmacyProduct product, String lang, Long pharmacyId, boolean dualCurrency) {
        // الحصول على الترجمة حسب اللغة المطلوبة
        String translatedTradeName = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(lang))
                .findFirst()
                .map(t -> t.getTradeName())
                .orElse(product.getTradeName())
                : product.getTradeName();

        String translatedScientificName = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(lang))
                .findFirst()
                .map(t -> t.getScientificName())
                .orElse(product.getScientificName())
                : product.getScientificName();
        
        // الحصول على الاسم العربي والإنجليزي دائماً
        String tradeNameAr = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase("ar"))
                .findFirst()
                .map(t -> t.getTradeName())
                .orElse(product.getTradeName())
                : product.getTradeName();
        
        String tradeNameEn = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase("en"))
                .findFirst()
                .map(t -> t.getTradeName())
                .orElse(product.getTradeName())
                : product.getTradeName();
        
        String scientificNameAr = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase("ar"))
                .findFirst()
                .map(t -> t.getScientificName())
                .orElse(product.getScientificName())
                : product.getScientificName();
        
        String scientificNameEn = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase("en"))
                .findFirst()
                .map(t -> t.getScientificName())
                .orElse(product.getScientificName())
                : product.getScientificName();

        String translatedNotes = product.getNotes();
        
        Integer quantity = getProductQuantity(product.getId(), ProductType.PHARMACY, pharmacyId);

        return ProductSearchDTOResponse.builder()
                .id(product.getId())
                .tradeName(translatedTradeName)
                .tradeNameAr(tradeNameAr)
                .tradeNameEn(tradeNameEn)
                .scientificName(translatedScientificName)
                .scientificNameAr(scientificNameAr)
                .scientificNameEn(scientificNameEn)
                .barcodes(product.getBarcodes() != null ? 
                    product.getBarcodes().stream()
                        .map(barcode -> barcode.getBarcode())
                        .collect(java.util.stream.Collectors.toSet()) 
                    : new HashSet<>())
                .productTypeName(ProductType.PHARMACY.getTranslatedName(lang))
                .requiresPrescription(product.getRequiresPrescription())
                .concentration(product.getConcentration())
                .size(product.getSize())
                .refPurchasePrice(Math.round(product.getRefPurchasePrice() * 100.0f) / 100.0f)
                .refSellingPrice(Math.round(product.getRefSellingPrice() * 100.0f) / 100.0f)
                .minStockLevel(product.getMinStockLevel())
                .dualCurrencyDisplay(dualCurrency)
                .refPurchasePriceUSD(dualCurrency && product.getRefPurchasePrice() > 0 ? 
                    currencyConversionService.convertPriceFromSYP(product.getRefPurchasePrice(), Currency.USD).getDisplayPrice().doubleValue() : null)
                .refSellingPriceUSD(dualCurrency && product.getRefSellingPrice() > 0 ? 
                    currencyConversionService.convertPriceFromSYP(product.getRefSellingPrice(), Currency.USD).getDisplayPrice().doubleValue() : null)
                .exchangeRateSYPToUSD(dualCurrency ? 
                    currencyConversionService.convertPriceFromSYP(1.0f, Currency.USD).getExchangeRate().doubleValue() : null)
                .notes(translatedNotes)
                .tax(product.getTax())
                .quantity(quantity)
                .pharmacyId(product.getPharmacy() != null ? product.getPharmacy().getId() : null)
                .pharmacyName(product.getPharmacy() != null ? product.getPharmacy().getName() : null)
                .typeId(product.getType() != null ? product.getType().getId() : null)
                .formId(product.getForm() != null ? product.getForm().getId() : null)
                .manufacturerId(product.getManufacturer() != null ? product.getManufacturer().getId() : null)
                .categoryIds(product.getCategories() != null ? 
                    product.getCategories().stream()
                        .map(category -> category.getId())
                        .collect(java.util.stream.Collectors.toSet()) 
                    : new HashSet<>())
                .type(product.getType() != null ? 
                    product.getType().getTranslations().stream()
                        .filter(t -> lang.equalsIgnoreCase(t.getLanguage().getCode()))
                        .findFirst()
                        .map(com.Teryaq.product.entity.TypeTranslation::getName)
                        .orElse(product.getType().getName())
                    : null)
                .form(product.getForm() != null ? 
                    product.getForm().getTranslations().stream()
                        .filter(t -> lang.equalsIgnoreCase(t.getLanguage().getCode()))
                        .findFirst()
                        .map(com.Teryaq.product.entity.FormTranslation::getName)
                        .orElse(product.getForm().getName())
                    : null)
                .manufacturer(product.getManufacturer() != null ? 
                    product.getManufacturer().getTranslations().stream()
                        .filter(t -> lang.equalsIgnoreCase(t.getLanguage().getCode()))
                        .findFirst()
                        .map(com.Teryaq.product.entity.ManufacturerTranslation::getName)
                        .orElse(product.getManufacturer().getName())
                    : null)
                .categories(product.getCategories() != null ? 
                    product.getCategories().stream()
                        .map(category -> category.getName())
                        .collect(java.util.stream.Collectors.toSet()) 
                    : new HashSet<>())
                .build();
    }
    
    
    private Integer getProductQuantity(Long productId, ProductType productType, Long pharmacyId) {
        try {
            Integer totalQuantity = stockItemRepo.getTotalQuantity(productId, pharmacyId, productType);
            return totalQuantity != null ? totalQuantity : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
