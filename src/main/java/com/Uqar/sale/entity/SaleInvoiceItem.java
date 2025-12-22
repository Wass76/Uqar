package com.Uqar.sale.entity;

//import com.Uqar.product.Enum.DiscountType;
import com.Uqar.product.entity.StockItem;
import com.Uqar.utils.entity.AuditedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "sale_invoice_items")
public class SaleInvoiceItem extends AuditedEntity{
 
    @ManyToOne
    @JoinColumn(name = "sale_invoice_id")
    private SaleInvoice saleInvoice;

    @ManyToOne
    @JoinColumn(name = "stock_item_id")
    private StockItem stockItem;

    private Integer quantity;

    private Float unitPrice;

    // private Float discount;
    
    // @Enumerated(EnumType.STRING)
    // private DiscountType discountType;

    private Float subTotal;

    // تتبع الكمية المرتجعة من هذا العنصر
    @Column(nullable = false)
    private Integer refundedQuantity = 0;

     // عدد الأجزاء المباعة (للبيع الجزئي)
    @Column(name = "parts_sold")
    private Integer partsSold;

    @Override
    protected String getSequenceName() {
        return "sale_invoice_item_id_seq";
    }
} 