package com.Uqar.product.entity;

import java.time.LocalDate;

import com.Uqar.product.Enum.InventoryAdjustmentReason;
import com.Uqar.product.Enum.ProductType;
import com.Uqar.purchase.entity.PurchaseInvoice;
import com.Uqar.user.entity.Pharmacy;
import com.Uqar.utils.entity.AuditedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "stock_item")
@NoArgsConstructor
@AllArgsConstructor
public class StockItem extends AuditedEntity {
    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private ProductType productType; // 'MASTER' or 'PHARMACY'

    @Column
    private String productName;

    @Column
    private String barcode;

    @Column(nullable = false)
    private Integer quantity;

    @Column
    private Integer bonusQty;

    @Column
    private Integer minStockLevel;

    @Column
    private LocalDate expiryDate;

    @Column
    private String batchNo;

    @Column
    private String invoiceNumber;

    @Column(nullable = false)
    private Double actualPurchasePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_invoice_id")
    private PurchaseInvoice purchaseInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 50)
    private InventoryAdjustmentReason reason; // سبب الإضافة (للتعديلات بدون فاتورة)

    @Column(length = 2000)
    private String notes; // ملاحظات إضافية 

    
     // عدد الأجزاء المتبقية من العلبة الحالية (للبيع الجزئي)
    @Column(name = "remaining_parts")
    private Integer remainingParts;

    @Override
    protected String getSequenceName() {
        return "stock_item_id_seq";
    }
} 