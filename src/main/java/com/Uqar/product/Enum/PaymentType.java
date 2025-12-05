package com.Uqar.product.Enum;

public enum PaymentType {
    CASH("نقدي", "Cash"),
    CREDIT("دين", "Credit");
    
    private final String arabicName;
    private final String englishName;
    
    PaymentType(String arabicName, String englishName) {
        this.arabicName = arabicName;
        this.englishName = englishName;
    }
    
    public String getArabicName() {
        return arabicName;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    public String getTranslatedName(String lang) {
        return "ar".equalsIgnoreCase(lang) ? arabicName : englishName;
    }
}
