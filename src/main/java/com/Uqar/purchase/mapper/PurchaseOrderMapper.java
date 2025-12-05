package com.Uqar.purchase.mapper;

import com.Uqar.product.Enum.OrderStatus;
import com.Uqar.product.Enum.ProductType;
import com.Uqar.purchase.dto.PurchaseOrderDTORequest;
import com.Uqar.purchase.dto.PurchaseOrderDTOResponse;
import com.Uqar.purchase.dto.PurchaseOrderItemDTORequest;
import com.Uqar.purchase.dto.PurchaseOrderItemDTOResponse;
import com.Uqar.purchase.entity.PurchaseOrder;
import com.Uqar.purchase.entity.PurchaseOrderItem;
import com.Uqar.product.entity.PharmacyProduct;
import com.Uqar.product.entity.MasterProduct;
import com.Uqar.user.entity.Supplier;
import com.Uqar.user.entity.Pharmacy;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;
import com.Uqar.product.entity.PharmacyProductTranslation;
import com.Uqar.product.entity.MasterProductTranslation;

@Component
public class PurchaseOrderMapper {
    public PurchaseOrder toEntity(PurchaseOrderDTORequest dto, Supplier supplier, Pharmacy pharmacy, List<PurchaseOrderItem> items) {
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(supplier);
        order.setPharmacy(pharmacy);
        order.setCurrency(dto.getCurrency());
        order.setStatus(OrderStatus.PENDING);
        order.setItems(items.stream().peek(i -> i.setPurchaseOrder(order)).collect(Collectors.toList()));
        double total = items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        order.setTotal(total);
        return order;
    }

    public PurchaseOrderDTOResponse toResponse(PurchaseOrder order, List<PharmacyProduct> pharmacyProducts, List<MasterProduct> masterProducts, String language) {
        PurchaseOrderDTOResponse dto = new PurchaseOrderDTOResponse();
        dto.setId(order.getId());
        dto.setSupplierId(order.getSupplier().getId());
        dto.setSupplierName(order.getSupplier().getName());
        dto.setCurrency(order.getCurrency());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setCreatedBy(order.getCreatedBy());
        dto.setItems(order.getItems().stream().map(item -> {
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

    public PurchaseOrderDTOResponse toResponse(PurchaseOrder order, List<PharmacyProduct> pharmacyProducts, List<MasterProduct> masterProducts) {
        PurchaseOrderDTOResponse dto = new PurchaseOrderDTOResponse();
        dto.setId(order.getId());
        dto.setSupplierId(order.getSupplier().getId());
        dto.setSupplierName(order.getSupplier().getName());
        dto.setCurrency(order.getCurrency());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setCreatedBy(order.getCreatedBy());
        dto.setItems(order.getItems().stream().map(item -> {
            String productName = null;
            PharmacyProduct pharmacyProduct = null;
            MasterProduct masterProduct = null;
            
            if (item.getProductType() == ProductType.PHARMACY) {
                pharmacyProduct = pharmacyProducts.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst().orElse(null);
                productName = pharmacyProduct != null ? pharmacyProduct.getTradeName() : "N/A";
            } else if (item.getProductType() == ProductType.MASTER) {
                masterProduct = masterProducts.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst().orElse(null);
                productName = masterProduct != null ? masterProduct.getTradeName() : "N/A";
            }
            return toItemResponse(item, productName, pharmacyProduct, masterProduct);
        }).toList());
        return dto;
    }

    public PurchaseOrderItem toItemEntity(PurchaseOrderItemDTORequest dto, String barcode, Double price) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setProductId(dto.getProductId());
        item.setProductType(dto.getProductType());
        item.setQuantity(dto.getQuantity());
        item.setPrice(price);
        item.setBarcode(barcode);
        return item;
    }

    public PurchaseOrderItemDTOResponse toItemResponse(PurchaseOrderItem item, String productName, PharmacyProduct pharmacyProduct, MasterProduct masterProduct) {
        PurchaseOrderItemDTOResponse dto = new PurchaseOrderItemDTOResponse();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(productName);
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setBarcode(item.getBarcode());
        dto.setProductType(item.getProductType());
        
        // Set refSellingPrice and minStockLevel based on product type
        if (item.getProductType() == ProductType.PHARMACY && pharmacyProduct != null) {
            dto.setRefSellingPrice((double) pharmacyProduct.getRefSellingPrice());
            dto.setMinStockLevel(pharmacyProduct.getMinStockLevel());
        } else if (item.getProductType() == ProductType.MASTER && masterProduct != null) {
            dto.setRefSellingPrice((double) masterProduct.getRefSellingPrice());
            // TODO: Implement minStockLevel for MASTER type products after fixing related issues
            dto.setMinStockLevel(null);
        } else {
            dto.setRefSellingPrice(null);
            dto.setMinStockLevel(null);
        }
        
        return dto;
    }
} 