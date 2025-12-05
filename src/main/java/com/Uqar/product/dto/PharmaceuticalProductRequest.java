package com.Uqar.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

/**
 * DTO for pharmaceutical product creation request
 * Maps to the JSON structure required by productService
 */
public class PharmaceuticalProductRequest {

    @NotBlank(message = "الاسم التجاري مطلوب / Trade name is required")
    @JsonProperty("tradeName")
    private String tradeName;

    @NotBlank(message = "الاسم العلمي مطلوب / Scientific name is required")
    @JsonProperty("scientificName")
    private String scientificName;

    @JsonProperty("concentration")
    private String concentration;

    @JsonProperty("size")
    private String size;

    @NotNull(message = "سعر الشراء المرجعي مطلوب / Reference purchase price is required")
    @PositiveOrZero(message = "سعر الشراء يجب أن يكون موجب أو صفر / Purchase price must be positive or zero")
    @JsonProperty("refPurchasePrice")
    private float refPurchasePrice;

    @NotNull(message = "سعر البيع المرجعي مطلوب / Reference selling price is required")
    @PositiveOrZero(message = "سعر البيع يجب أن يكون موجب / Selling price must be positive")
    @JsonProperty("refSellingPrice")
    private float refSellingPrice;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("tax")
    private float tax = 15.0f; // Default tax rate

    @JsonProperty("barcode")
    private String barcode;

    @NotNull(message = "معرف الشكل الصيدلاني مطلوب / Form ID is required")
    @JsonProperty("formId")
    private Long formId;

    @NotNull(message = "معرف المصنع مطلوب / Manufacturer ID is required")
    @JsonProperty("manufacturerId")
    private Long manufacturerId;

    @JsonProperty("translations")
    private List<TranslationDto> translations;

    // Constructors
    public PharmaceuticalProductRequest() {}

    public PharmaceuticalProductRequest(String tradeName, String scientificName, String concentration, 
                                      String size, float refPurchasePrice, float refSellingPrice, 
                                      String notes, float tax, String barcode, Long formId, 
                                      Long manufacturerId, List<TranslationDto> translations) {
        this.tradeName = tradeName;
        this.scientificName = scientificName;
        this.concentration = concentration;
        this.size = size;
        this.refPurchasePrice = refPurchasePrice;
        this.refSellingPrice = refSellingPrice;
        this.notes = notes;
        this.tax = tax;
        this.barcode = barcode;
        this.formId = formId;
        this.manufacturerId = manufacturerId;
        this.translations = translations;
    }

    // Getters and Setters
    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getConcentration() {
        return concentration;
    }

    public void setConcentration(String concentration) {
        this.concentration = concentration;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public float getRefPurchasePrice() {
        return refPurchasePrice;
    }

    public void setRefPurchasePrice(float refPurchasePrice) {
        this.refPurchasePrice = refPurchasePrice;
    }

    public float getRefSellingPrice() {
        return refSellingPrice;
    }

    public void setRefSellingPrice(float refSellingPrice) {
        this.refSellingPrice = refSellingPrice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public float getTax() {
        return tax;
    }

    public void setTax(float tax) {
        this.tax = tax;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getFormId() {
        return formId;
    }

    public void setFormId(Long formId) {
        this.formId = formId;
    }

    public Long getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(Long manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public List<TranslationDto> getTranslations() {
        return translations;
    }

    public void setTranslations(List<TranslationDto> translations) {
        this.translations = translations;
    }

    @Override
    public String toString() {
        return "PharmaceuticalProductRequest{" +
                "tradeName='" + tradeName + '\'' +
                ", scientificName='" + scientificName + '\'' +
                ", concentration='" + concentration + '\'' +
                ", size='" + size + '\'' +
                ", refPurchasePrice=" + refPurchasePrice +
                ", refSellingPrice=" + refSellingPrice +
                ", notes='" + notes + '\'' +
                ", tax=" + tax +
                ", barcode='" + barcode + '\'' +
                ", formId=" + formId +
                ", manufacturerId=" + manufacturerId +
                ", translations=" + translations +
                '}';
    }

    /**
     * Inner class for translation data
     */
    public static class TranslationDto {
        @JsonProperty("tradeName")
        private String tradeName;

        @JsonProperty("scientificName")
        private String scientificName;

        @JsonProperty("lang")
        private String lang;

        // Constructors
        public TranslationDto() {}

        public TranslationDto(String tradeName, String scientificName, String lang) {
            this.tradeName = tradeName;
            this.scientificName = scientificName;
            this.lang = lang;
        }

        // Getters and Setters
        public String getTradeName() {
            return tradeName;
        }

        public void setTradeName(String tradeName) {
            this.tradeName = tradeName;
        }

        public String getScientificName() {
            return scientificName;
        }

        public void setScientificName(String scientificName) {
            this.scientificName = scientificName;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        @Override
        public String toString() {
            return "TranslationDto{" +
                    "tradeName='" + tradeName + '\'' +
                    ", scientificName='" + scientificName + '\'' +
                    ", lang='" + lang + '\'' +
                    '}';
        }
    }
}

