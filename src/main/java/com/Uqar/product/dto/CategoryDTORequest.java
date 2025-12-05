package com.Uqar.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Category Request", example = """
{
  "name": "Pain Relief",
  "lang": "en",
  "translations": [
    {
      "name": "مسكنات الألم",
      "lang": "ar"
    }
  ]
}
""")
public class CategoryDTORequest {

    @Schema(description = "Category name", example = "Pain Relief")
    private String name;
    
    @Schema(description = "Language code", example = "en")
    private String lang;

    @Schema(description = "Category translations for different languages")
    private Set<CategoryTranslationDTORequest> translations;
}   
