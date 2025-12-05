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
@Schema(description = "Product Type Request", example = """
{
  "name": "Medicine",
  "lang": "en",
  "translations": [
    {
      "name": "دواء",
      "lang": "ar"
    }
  ]
}
""")
public class TypeDTORequest {

    @Schema(description = "Product type name", example = "Medicine")
    private String name;
    
    @Schema(description = "Language code", example = "en")
    private String lang;

    @Schema(description = "Product type translations for different languages")
    private Set<TypeTranslationDTORequest> translations;

}
