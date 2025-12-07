package com.Uqar.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Manufacturer Request", example = """
{
  "name": "Pfizer",
  "lang": "en",
  "translations": [
    {
      "name": "فايزر",
      "lang": "ar"
    }
  ]
}
""")
public class ManufacturerDTORequest {

    @Schema(description = "Manufacturer name", example = "Pfizer")
    private String name;
    
    @Schema(description = "Language code", example = "en")
    private String lang;

    @Schema(description = "Manufacturer translations for different languages")
    private Set<ManufacturerTranslationDTORequest> translations;

}
