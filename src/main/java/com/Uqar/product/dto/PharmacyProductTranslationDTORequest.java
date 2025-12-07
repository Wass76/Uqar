package com.Uqar.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Pharmacy Product Translation Request", example = """
{
  "tradeName": "باراسيتامول",
  "scientificName": "أسيتامينوفين",
  "lang": "ar"
}
""")
public class PharmacyProductTranslationDTORequest {

    @Schema(description = "Trade name in the target language", example = "باراسيتامول")
    @NotBlank(message = "Trade name is required")
    private String tradeName;

    @Schema(description = "Scientific name in the target language", example = "أسيتامينوفين")
    @NotBlank(message = "Scientific name is required")
    private String scientificName;

    @Schema(description = "Language code for the translation", example = "ar")
    private String lang;
} 