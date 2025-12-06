package com.Teryaq.product.Enum;

public enum InventoryAdjustmentReason {
    INVENTORY_COUNT("Inventory Count", "جرد المخزون"),
    PHYSICAL_COUNT_ADJUSTMENT("Physical Count Adjustment", "تعديل الجرد الفعلي");
    
    private final String englishName;
    private final String arabicName;
    
    InventoryAdjustmentReason(String englishName, String arabicName) {
        this.englishName = englishName;
        this.arabicName = arabicName;
    }
    
    public String getTranslatedName(String lang) {
        return "ar".equalsIgnoreCase(lang) ? arabicName : englishName;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    public String getArabicName() {
        return arabicName;
    }
}

