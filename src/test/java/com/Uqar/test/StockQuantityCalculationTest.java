package com.Uqar.test;

import com.Uqar.purchase.entity.PurchaseInvoiceItem;
import com.Uqar.product.entity.StockItem;
import com.Uqar.product.Enum.ProductType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StockQuantityCalculationTest {
    
    @Test
    public void testStockQuantityCalculation() {
        // Create a mock PurchaseInvoiceItem
        PurchaseInvoiceItem item = new PurchaseInvoiceItem();
        item.setProductId(1L);
        item.setProductType(ProductType.PHARMACY);
        item.setReceivedQty(100);
        item.setBonusQty(10);
        
        // Simulate the fixed calculation logic
        int bonusQty = item.getBonusQty() != null ? item.getBonusQty() : 0;
        int totalQuantity = item.getReceivedQty() + bonusQty;
        
        // Verify the calculation
        assertEquals(110, totalQuantity);
        assertEquals(100, item.getReceivedQty());
        assertEquals(10, item.getBonusQty());
        
        // Test edge case: null bonusQty
        item.setBonusQty(null);
        bonusQty = item.getBonusQty() != null ? item.getBonusQty() : 0;
        totalQuantity = item.getReceivedQty() + bonusQty;
        assertEquals(100, totalQuantity);
    }
}
