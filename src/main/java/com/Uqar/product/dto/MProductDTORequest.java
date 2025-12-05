package com.Uqar.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Product Request", example = """
{
  "tradeName": "Omeprazole",
  "scientificName": "Omeprazole Magnesium",
  "concentration": "20mg",
  "size": "14 capsules",
  "refPurchasePrice": 150.0,
  "refSellingPrice": 300.0,
  "notes": "مثبط مضخة البروتون - يستخدم لعلاج قرحة المعدة والارتجاع المريئي",
  "tax": 15.0,
  "barcode": "3210987654321",
  "requiresPrescription": true,
  "typeId": 1,
  "formId": 2,
  "manufacturerId": 1,
  "categoryIds": [1, 2],
  "translations": [
    {
      "tradeName": "أوميبرازول",
      "scientificName": "أوميبرازول ماغنيسيوم",
      "lang": "ar"
    }
  ]
}
""")
public class MProductDTORequest {


    @NotBlank(message = "Trade name is required")
    private String tradeName;

    @NotBlank(message = "Scientific name is required")
    private String scientificName;

    @NotBlank(message = "Concentration is required")
    private String concentration;

    @NotBlank(message = "size is required")
    private String size;

    @NotNull(message = "Purchase Price is required")
    @Min(value = 0, message = "Purchase Price must be greater than or equal to 0")
    private float refPurchasePrice;

    @NotNull(message = "Selling Price is required")
    @Min(value = 0, message = "Selling Price must be greater than or equal to 0")
    private float refSellingPrice;

    private String notes;
    
    @Min(value = 0, message = "Tax must be greater than or equal to 0")
    private float tax;

    @NotBlank(message = "Barcode is required")
    private String barcode;

    @Builder.Default
    private Boolean requiresPrescription = false;

    private Long typeId;
    private Long formId;
    private Long manufacturerId;

    private Set<Long> categoryIds;


    private Set<MProductTranslationDTORequest> translations;
}
