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
@Schema(description = "Product Form Translation Request", example = """
{
  "name": "قرص",
  "lang": "ar"
}
""")
public class FormTranslationDTORequest {

    @Schema(description = "Product form name in the target language", example = "قرص")
    private String name;
    
    @Schema(description = "Language code for the translation", example = "ar")
    private String lang;

}