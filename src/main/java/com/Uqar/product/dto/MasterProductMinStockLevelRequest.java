package com.Uqar.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Master Product Minimum Stock Level Request", example = """
{
    "minStockLevel": 10
}
""")
public class MasterProductMinStockLevelRequest {

    @Schema(description = "Minimum stock level", example = "10")
    @Min(value = 0, message = "Minimum stock level must be greater than or equal to 0")
    private Integer minStockLevel;
}
