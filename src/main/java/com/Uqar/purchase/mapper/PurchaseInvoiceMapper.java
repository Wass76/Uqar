package com.Uqar.purchase.mapper;

import com.Uqar.purchase.dto.PurchaseInvoiceDTORequest;
import com.Uqar.purchase.dto.PurchaseInvoiceDTOResponse;
import com.Uqar.purchase.dto.PurchaseInvoiceItemDTORequest;
import com.Uqar.purchase.dto.PurchaseInvoiceItemDTOResponse;
import com.Uqar.purchase.entity.PurchaseInvoice;
import com.Uqar.purchase.entity.PurchaseInvoiceItem;
import com.Uqar.product.entity.PharmacyProduct;
import com.Uqar.product.entity.MasterProduct;
import com.Uqar.user.entity.Supplier;
import com.Uqar.product.entity.PharmacyProductTranslation;
import com.Uqar.product.entity.MasterProductTranslation;
import com.Uqar.product.repo.StockItemRepo;
import com.Uqar.moneybox.service.ExchangeRateService;
import com.Uqar.user.Enum.Currency;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.Uqar.product.Enum.ProductType;

@Component
public class PurchaseInvoiceMapper {
    
    @Autowired
    private StockItemRepo stockItemRepo;
    
    @Autowired
    private ExchangeRateService exchangeRateService;
    
    public PurchaseInvoice toEntity(PurchaseInvoiceDTORequest dto, Supplier supplier, List<PurchaseInvoiceItem> items) {
        PurchaseInvoice invoice = new PurchaseInvoice();
        invoice.setPurchaseOrder(null); // Set in service if needed
        invoice.setSupplier(supplier);
        invoice.setCurrency(dto.getCurrency());
        invoice.setInvoiceNumber(dto.getInvoiceNumber());
        invoice.setItems(items.stream().peek(i -> i.setPurchaseInvoice(invoice)).collect(Collectors.toList()));
        // Note: pharmacy will be set in the service layer
        return invoice;
    }

    public PurchaseInvoiceDTOResponse toResponse(PurchaseInvoice invoice, List<PharmacyProduct> pharmacyProducts, List<MasterProduct> masterProducts, String language) {
        PurchaseInvoiceDTOResponse dto = new PurchaseInvoiceDTOResponse();
        dto.setId(invoice.getId());
        dto.setPurchaseOrderId(invoice.getPurchaseOrder() != null ? invoice.getPurchaseOrder().getId() : null);
        dto.setSupplierId(invoice.getSupplier().getId());
        dto.setSupplierName(invoice.getSupplier().getName());
        dto.setCurrency(invoice.getCurrency());
        dto.setTotal(invoice.getTotal());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setCreatedBy(invoice.getCreatedBy());
        dto.setItems(invoice.getItems().stream().map(item -> {
            String productName = null;
            PharmacyProduct pharmacyProduct = null;
            MasterProduct masterProduct = null;
            
            if (item.getProductType() == ProductType.PHARMACY) {
                pharmacyProduct = pharmacyProducts.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst().orElse(null);
                if (pharmacyProduct != null) {
                    // Try to get translated name
                    productName = pharmacyProduct.getTranslations().stream()
                        .filter(t -> t.getLanguage().getCode().equals(language))
                        .findFirst()
                        .map(PharmacyProductTranslation::getTradeName)
                        .orElse(pharmacyProduct.getTradeName()); // Fallback to default
                } else {
                    productName = "N/A";
                }
            } else if (item.getProductType() == ProductType.MASTER) {
                masterProduct = masterProducts.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst().orElse(null);
                if (masterProduct != null) {
                    // Try to get translated name
                    productName = masterProduct.getTranslations().stream()
                        .filter(t -> t.getLanguage().getCode().equals(language))
                        .findFirst()
                        .map(MasterProductTranslation::getTradeName)
                        .orElse(masterProduct.getTradeName()); // Fallback to default
                } else {
                    productName = "N/A";
                }
            }
            return toItemResponse(item, productName, pharmacyProduct, masterProduct);
        }).toList());
        return dto;
    }

    public PurchaseInvoiceItem toItemEntity(PurchaseInvoiceItemDTORequest dto) {
        PurchaseInvoiceItem item = new PurchaseInvoiceItem();
        item.setProductId(dto.getProductId());
        item.setProductType(dto.getProductType());
        item.setReceivedQty(dto.getReceivedQty());
        item.setBonusQty(dto.getBonusQty() != null ? dto.getBonusQty() : 0); // Default to 0 if null
        item.setInvoicePrice(dto.getInvoicePrice());
        item.setBatchNo(dto.getBatchNo());
        item.setExpiryDate(dto.getExpiryDate());
        return item;
    }

    public PurchaseInvoiceItemDTOResponse toItemResponse(PurchaseInvoiceItem item, String productName, PharmacyProduct pharmacyProduct, MasterProduct masterProduct) {
        PurchaseInvoiceItemDTOResponse dto = new PurchaseInvoiceItemDTOResponse();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(productName);
        dto.setProductType(item.getProductType().name());
        dto.setReceivedQty(item.getReceivedQty());
        dto.setBonusQty(item.getBonusQty());
        
        // Convert prices from SYP to the invoice currency for display
        Currency invoiceCurrency = item.getPurchaseInvoice().getCurrency();
        Double invoicePriceInRequestedCurrency = convertPriceFromSYP(item.getInvoicePrice(), invoiceCurrency);
        Double actualPriceInRequestedCurrency = convertPriceFromSYP(item.getActualPrice(), invoiceCurrency);
        
        dto.setInvoicePrice(invoicePriceInRequestedCurrency);
        dto.setActualPrice(actualPriceInRequestedCurrency);
        dto.setBatchNo(item.getBatchNo());
        dto.setExpiryDate(item.getExpiryDate());
        
        // Set refSellingPrice and minStockLevel based on product type
        if (item.getProductType() == ProductType.PHARMACY && pharmacyProduct != null) {
            // Convert selling price from SYP to the invoice currency for display
            Double sellingPriceInSYP = (double) pharmacyProduct.getRefSellingPrice();
            Double sellingPriceInRequestedCurrency = convertSellingPriceFromSYP(sellingPriceInSYP, item.getPurchaseInvoice().getCurrency());
            dto.setRefSellingPrice(sellingPriceInRequestedCurrency);
            
            // Use minStockLevel from StockItem if available, otherwise from product
            dto.setMinStockLevel(getMinStockLevelFromStockItem(item.getProductId(), item.getProductType()) != null ? 
                getMinStockLevelFromStockItem(item.getProductId(), item.getProductType()) : 
                pharmacyProduct.getMinStockLevel());
        } else if (item.getProductType() == ProductType.MASTER && masterProduct != null) {
            // Convert selling price from SYP to the invoice currency for display
            Double sellingPriceInSYP = (double) masterProduct.getRefSellingPrice();
            Double sellingPriceInRequestedCurrency = convertSellingPriceFromSYP(sellingPriceInSYP, item.getPurchaseInvoice().getCurrency());
            dto.setRefSellingPrice(sellingPriceInRequestedCurrency);
            
            // Use minStockLevel from StockItem if available, otherwise from product
            dto.setMinStockLevel(getMinStockLevelFromStockItem(item.getProductId(), item.getProductType()) != null ? 
                getMinStockLevelFromStockItem(item.getProductId(), item.getProductType()) : 
                masterProduct.getMinStockLevel());
        } else {
            dto.setRefSellingPrice(null);
            dto.setMinStockLevel(null);
        }
        
        return dto;
    }
    
    // Helper method to get minStockLevel from StockItem
    private Integer getMinStockLevelFromStockItem(Long productId, ProductType productType) {
        try {
            // Find the most recent StockItem for this product
            List<com.Uqar.product.entity.StockItem> stockItems = stockItemRepo.findByProductIdAndProductTypeOrderByDateAddedDesc(productId, productType);
            if (!stockItems.isEmpty()) {
                return stockItems.get(0).getMinStockLevel();
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    /**
     * Convert selling price from SYP to the requested currency for display
     */
    private Double convertSellingPriceFromSYP(Double sellingPriceInSYP, Currency targetCurrency) {
        if (targetCurrency == Currency.SYP) {
            return sellingPriceInSYP;
        }
        
        BigDecimal sellingPriceBigDecimal = BigDecimal.valueOf(sellingPriceInSYP);
        BigDecimal sellingPriceInTargetCurrency = exchangeRateService.convertFromSYP(sellingPriceBigDecimal, targetCurrency);
        
        return sellingPriceInTargetCurrency.doubleValue();
    }
    
    /**
     * Convert any price from SYP to the requested currency for display
     */
    private Double convertPriceFromSYP(Double priceInSYP, Currency targetCurrency) {
        if (targetCurrency == Currency.SYP) {
            return priceInSYP;
        }
        
        BigDecimal priceBigDecimal = BigDecimal.valueOf(priceInSYP);
        BigDecimal priceInTargetCurrency = exchangeRateService.convertFromSYP(priceBigDecimal, targetCurrency);
        
        return priceInTargetCurrency.doubleValue();
    }
} 