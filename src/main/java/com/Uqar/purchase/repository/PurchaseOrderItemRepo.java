package com.Uqar.purchase.repository;

import com.Uqar.purchase.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import com.Uqar.product.Enum.ProductType;
import java.util.List;

public interface PurchaseOrderItemRepo extends JpaRepository<PurchaseOrderItem, Long> {

    List<PurchaseOrderItem> findByProductIdAndProductType(Long productId, ProductType productType);
} 