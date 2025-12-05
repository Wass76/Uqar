package com.Uqar.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Pharmacy Product Request", example = """
{
  "tradeName": "Paracetamol",
  "scientificName": "Acetaminophen",
  "concentration": "500mg",
  "size": "20 tablets",
  "notes": "Pain relief medication",
  "tax": 5.0,
  "barcodes": ["1234567890123"],
  "requiresPrescription": false,
  "typeId": 1,
  "formId": 1,
  "manufacturerId": 1,
  "categoryIds": [1, 2],
  "translations": [
    {
      "tradeName": "باراسيتامول",
      "scientificName": "أسيتامينوفين",
      "lang": "ar"
    }
  ]
}
""")
public class PharmacyProductDTORequest {

    @Schema(description = "Trade name of the product", example = "Paracetamol")
    @NotBlank(message = "Trade name is required")
    private String tradeName;

    @Schema(description = "Scientific name of the product", example = "Acetaminophen")
    private String scientificName;

    @Schema(description = "Product concentration", example = "500mg")
    private String concentration;

    @Schema(description = "Product size/packaging", example = "20 tablets")
    @NotBlank(message = "size is required")
    private String size;

    // @NotBlank(message = "Purchase Price is required")
    // private float refPurchasePrice;

    // @NotBlank(message = "Selling Price is required")
    // private float refSellingPrice;

    @Schema(description = "Additional notes about the product", example = "Pain relief medication")
    private String notes;
    
    @Schema(description = "Tax percentage", example = "5.0")
    private float tax;

    @Schema(description = "Product barcodes", example = "[\"1234567890123\"]")
    @NotNull(message = "At least one barcode is required")
    private Set<String> barcodes;

    @Schema(description = "Whether the product requires prescription", example = "false")
    @Builder.Default
    private Boolean requiresPrescription = false;

    @Schema(description = "Product type ID", example = "1")
    private Long typeId;
    
    @Schema(description = "Product form ID", example = "1")
    @NotNull
    private Long formId;
    
    @Schema(description = "Manufacturer ID", example = "1")
    @NotNull
    private Long manufacturerId;

    @Schema(description = "Category IDs", example = "[1, 2]")
    private Set<Long> categoryIds;

    @Schema(description = "Product translations for different languages")
    private Set<PharmacyProductTranslationDTORequest> translations;
}
