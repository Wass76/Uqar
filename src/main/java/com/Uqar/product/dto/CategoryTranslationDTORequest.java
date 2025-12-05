package com.Uqar.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor  
@Builder
@Schema(description = "Category Translation Request", example = """
{
  "name": "مسكنات الألم",
  "lang": "ar"
}
""")
public class CategoryTranslationDTORequest {

    @Schema(description = "Category name in the target language", example = "مسكنات الألم")
    private String name;
    
    @Schema(description = "Language code for the translation", example = "ar")
    private String lang;
    
}