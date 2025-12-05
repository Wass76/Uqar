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
@Schema(description = "Product Form Request", example = """
{
  "name": "Tablet",
  "lang": "en",
  "translations": [
    {
      "name": "قرص",
      "lang": "ar"
    }
  ]
}
""")
public class FormDTORequest {

    @Schema(description = "Product form name", example = "Tablet")
    private String name;
    
    @Schema(description = "Language code", example = "en")
    private String lang;

    @Schema(description = "Product form translations for different languages")
    private Set<FormTranslationDTORequest> translations;

}
