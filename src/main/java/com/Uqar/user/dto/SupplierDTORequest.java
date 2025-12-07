package com.Uqar.user.dto;

import com.Uqar.user.Enum.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Supplier Request", example = """
{
  "name": "ABC Pharmaceuticals",
  "phone": "+1234567890",
  "address": "123 Main St, City, Country",
  "preferredCurrency": "USD"
}
""")
public class SupplierDTORequest {
    
    @Schema(description = "Supplier name", example = "ABC Pharmaceuticals")
    private String name;
    
    @Schema(description = "Supplier phone number", example = "+1234567890")
    private String phone;
    
    @Schema(description = "Supplier address", example = "123 Main St, City, Country")
    private String address;
    
    @Schema(description = "Preferred currency for transactions", example = "USD", 
            allowableValues = {"USD", "EUR", "GBP", "SAR", "AED"})
    private Currency preferredCurrency;
} 