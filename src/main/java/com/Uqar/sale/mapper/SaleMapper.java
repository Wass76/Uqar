package com.Uqar.sale.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.Uqar.product.entity.StockItem;
import com.Uqar.product.mapper.StockItemMapper;
import com.Uqar.product.service.CurrencyConversionService;
import com.Uqar.sale.dto.SaleInvoiceDTORequest;
import com.Uqar.sale.dto.SaleInvoiceDTOResponse;
import com.Uqar.sale.dto.SaleInvoiceItemDTORequest;
import com.Uqar.sale.dto.SaleInvoiceItemDTOResponse;
import com.Uqar.sale.entity.SaleInvoice;
import com.Uqar.sale.entity.SaleInvoiceItem;
import com.Uqar.user.Enum.Currency;
import com.Uqar.user.entity.Customer;
import com.Uqar.user.entity.Pharmacy;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SaleMapper {
    
    private final StockItemMapper stockItemMapper;
    private final CurrencyConversionService currencyConversionService;
    
    public SaleInvoice toEntity(SaleInvoiceDTORequest dto) {
        SaleInvoice invoice = new SaleInvoice();
        invoice.setPaymentType(dto.getPaymentType());
        invoice.setPaymentMethod(dto.getPaymentMethod());
        invoice.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : Currency.SYP);
        invoice.setDiscount(dto.getInvoiceDiscountValue() != null ? dto.getInvoiceDiscountValue() : 0);
        invoice.setDiscountType(dto.getInvoiceDiscountType());
        invoice.setPaidAmount(dto.getPaidAmount() != null ? dto.getPaidAmount() : 0);
        return invoice;
    }
    
    public SaleInvoice toEntityWithItems(SaleInvoiceDTORequest dto, List<SaleInvoiceItem> items) {
        SaleInvoice invoice = toEntity(dto);
        invoice.setItems(items);
        return invoice;
    }
    
    public SaleInvoice toEntityWithCustomerAndDate(SaleInvoiceDTORequest dto, Customer customer, Pharmacy pharmacy) {
        SaleInvoice invoice = toEntity(dto);
        invoice.setCustomer(customer);
        invoice.setPharmacy(pharmacy);
        invoice.setInvoiceDate(java.time.LocalDateTime.now());
        return invoice;
    }
    
    public SaleInvoiceItem toEntity(SaleInvoiceItemDTORequest dto, StockItem stockItem, Currency currency) {
        SaleInvoiceItem item = new SaleInvoiceItem();
        item.setStockItem(stockItem);
        
        // الحصول على عدد الأجزاء في العلبة
        Integer numberOfPartsPerBox = stockItemMapper.getNumberOfPartsPerBox(
            stockItem.getProductId(), 
            stockItem.getProductType()
        );
        
        // تحديد ما إذا كان بيع جزئي
        boolean isPartialSale = dto.getPartsToSell() != null && 
                                numberOfPartsPerBox != null && 
                                numberOfPartsPerBox > 1;
        
        // حساب السعر النهائي
        Float finalUnitPrice;
        Integer finalQuantity;
        
        if (isPartialSale) {
            // بيع جزئي: حساب السعر بناءً على الأجزاء
            // حفظ عدد الأجزاء المباعة
            item.setPartsSold(dto.getPartsToSell());
            
            // في البيع الجزئي: quantity = 0 لأننا سنتعامل مع remainingParts في SaleService
            // عدد العلبات المخصومة سيتم حسابه بناءً على remainingParts
            finalQuantity = 0;
            
            // حساب السعر: إذا تم إرسال partPrice يدوياً، استخدمه كسعر الجزء الواحد واضربه بعدد الأجزاء
            // وإلا احسبه من unitPrice (سعر العلبة) مع 20% ربح
            if (dto.getPartPrice() != null && dto.getPartPrice() > 0) {
                // الصيدلاني حدد partPrice يدوياً كسعر الجزء الواحد: اضربه بعدد الأجزاء
                finalUnitPrice = dto.getPartPrice() * dto.getPartsToSell();
            } else {
                // حساب تلقائي: نحتاج سعر العلبة أولاً
                Float baseBoxPrice;
                
                // استخدام unitPrice إذا تم إرساله، وإلا سعر البيع من المخزون
                if (dto.getUnitPrice() != null && dto.getUnitPrice() > 0) {
                    baseBoxPrice = dto.getUnitPrice();
                } else {
                    // استخدام سعر البيع من المخزون
                    Float sellingPrice = stockItemMapper.getProductSellingPrice(
                        stockItem.getProductId(), 
                        stockItem.getProductType()
                    );
                    
                    if (sellingPrice != null && sellingPrice > 0) {
                        baseBoxPrice = sellingPrice;
                    } else {
                        // Fallback to purchase price if no selling price is set
                        baseBoxPrice = stockItem.getActualPurchasePrice().floatValue();
                    }
                }
                
                // سعر الجزء الواحد (بدون هامش ربح) = سعر العلبة / عدد الأجزاء
                Float pricePerPart = baseBoxPrice / numberOfPartsPerBox;
                
                // سعر الأجزاء المباعة (مع 20% هامش ربح)
                Float totalPriceForParts = pricePerPart * dto.getPartsToSell() * 1.20f;
                
                finalUnitPrice = totalPriceForParts; // السعر الكلي للأجزاء المباعة
            }
            
            // تحويل السعر إلى العملة المطلوبة
            BigDecimal convertedPrice = currencyConversionService.getDisplayPrice(
                BigDecimal.valueOf(finalUnitPrice), 
                currency
            );
            finalUnitPrice = convertedPrice.floatValue();
        } else {
            // بيع علبة كاملة: partsSold = null
            item.setPartsSold(null);
            // بيع علبة كاملة: السعر العادي
            finalQuantity = dto.getQuantity(); // عدد علب للخصم
            
            // تحديد سعر العلبة: إذا تم إرسال unitPrice يدوياً، استخدمه. وإلا استخدم سعر المخزون
            Float baseBoxPrice;
            if (dto.getUnitPrice() != null && dto.getUnitPrice() > 0) {
                // إذا تم تحديد السعر يدوياً
                baseBoxPrice = dto.getUnitPrice();
            } else {
                // استخدام سعر البيع من المخزون
                Float sellingPrice = stockItemMapper.getProductSellingPrice(
                    stockItem.getProductId(), 
                    stockItem.getProductType()
                );
                
                if (sellingPrice != null && sellingPrice > 0) {
                    baseBoxPrice = sellingPrice;
                } else {
                    // Fallback to purchase price if no selling price is set
                    baseBoxPrice = stockItem.getActualPurchasePrice().floatValue();
                }
            }
            
            // تحويل سعر العلبة إلى العملة المطلوبة
            BigDecimal convertedPrice = currencyConversionService.getDisplayPrice(
                BigDecimal.valueOf(baseBoxPrice), 
                currency
            );
            finalUnitPrice = convertedPrice.floatValue();
        }
        
        item.setQuantity(finalQuantity);
        item.setUnitPrice(finalUnitPrice);
        
        return item;
    }
    
    public List<SaleInvoiceItem> toEntityList(List<SaleInvoiceItemDTORequest> dtos, List<StockItem> stockItems, Currency currency) {
        return dtos.stream()
            .map(dto -> {
                StockItem stockItem = stockItems.stream()
                    .filter(stock -> stock.getId().equals(dto.getStockItemId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Stock item not found with ID: " + dto.getStockItemId()));
                return toEntity(dto, stockItem, currency);
            })
            .collect(Collectors.toList());
    }

    public SaleInvoiceItemDTOResponse toResponse(SaleInvoiceItem item) {
        SaleInvoiceItemDTOResponse dto = new SaleInvoiceItemDTOResponse();
        dto.setId(item.getId());
        dto.setStockItemId(item.getStockItem() != null ? item.getStockItem().getId() : null);
        
        String productName = "Unknown Product";
        if (item.getStockItem() != null) {
            productName = stockItemMapper.getProductName(
                item.getStockItem().getProductId(), 
                item.getStockItem().getProductType()
            );
        }
        dto.setProductName(productName);
        
        dto.setQuantity(item.getQuantity());
        dto.setRefundedQuantity(item.getRefundedQuantity());
        dto.setAvailableForRefund(item.getQuantity() - item.getRefundedQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setSubTotal(item.getSubTotal());
        return dto;
    }

    public SaleInvoiceDTOResponse toResponse(SaleInvoice invoice) {
        SaleInvoiceDTOResponse dto = new SaleInvoiceDTOResponse();
        dto.setId(invoice.getId());
        dto.setCustomerId(invoice.getCustomer() != null ? invoice.getCustomer().getId() : null);
        dto.setCustomerName(invoice.getCustomer() != null ? invoice.getCustomer().getName() : "cash customer");
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setPaymentType(invoice.getPaymentType());
        dto.setPaymentMethod(invoice.getPaymentMethod());
        dto.setCurrency(invoice.getCurrency());
        dto.setDiscount(invoice.getDiscount());
        dto.setDiscountType(invoice.getDiscountType());
        dto.setPaidAmount(invoice.getPaidAmount());
        dto.setRemainingAmount(invoice.getRemainingAmount());
       // dto.setStatus(invoice.getStatus());
        dto.setPaymentStatus(invoice.getPaymentStatus());
        dto.setRefundStatus(invoice.getRefundStatus());
        if (invoice.getItems() != null) {
            List<SaleInvoiceItemDTOResponse> items = invoice.getItems().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
            dto.setItems(items);
        }
        return dto;
    }
} 