package com.Uqar.purchase.repository;

import com.Uqar.purchase.entity.PurchaseInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseInvoiceItemRepo extends JpaRepository<PurchaseInvoiceItem, Long> {
} 