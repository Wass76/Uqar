package com.Uqar.sale.mapper;

import com.Uqar.sale.dto.*;
import com.Uqar.sale.entity.*;
import com.Uqar.product.entity.StockItem;
import com.Uqar.product.mapper.StockItemMapper;
import com.Uqar.product.service.CurrencyConversionService;
import com.Uqar.user.entity.Customer;
import com.Uqar.user.entity.Pharmacy;
import com.Uqar.user.Enum.Currency;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
        item.setQuantity(dto.getQuantity());
        
        if (dto.getUnitPrice() != null) {
            // إذا تم تحديد السعر يدوياً، نطبق تحويل العملة عليه
            BigDecimal convertedPrice = currencyConversionService.getDisplayPrice(
                BigDecimal.valueOf(dto.getUnitPrice()), 
                currency
            );
            item.setUnitPrice(convertedPrice.floatValue());
        } else {
            // استخدام سعر البيع من المخزون وتطبيق تحويل العملة
            Float sellingPrice = stockItemMapper.getProductSellingPrice(
                stockItem.getProductId(), 
                stockItem.getProductType()
            );
            
            if (sellingPrice != null && sellingPrice > 0) {
                BigDecimal convertedPrice = currencyConversionService.getDisplayPrice(
                    BigDecimal.valueOf(sellingPrice), 
                    currency
                );
                item.setUnitPrice(convertedPrice.floatValue());
            } else {
                // Fallback to purchase price if no selling price is set
                BigDecimal convertedPrice = currencyConversionService.getDisplayPrice(
                    BigDecimal.valueOf(stockItem.getActualPurchasePrice()), 
                    currency
                );
                item.setUnitPrice(convertedPrice.floatValue());
            }
        }
        
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