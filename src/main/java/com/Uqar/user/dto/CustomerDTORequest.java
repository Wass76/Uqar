package com.Uqar.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Customer Request", example = """
{
  "name": "John Doe",
  "phoneNumber": "1234567890",
  "address": "123 Main St, City",
  "notes": "Regular customer"
}
""")
public class CustomerDTORequest {
 
    @Schema(description = "Name of the customer", example = "John Doe")
    @NotBlank(message = "customer name is required")
    private String name;

    @Schema(description = "Phone number (10 digits)", example = "1234567890")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @Schema(description = "Customer address", example = "123 Main St, City")
    private String address;

    @Schema(description = "Additional notes about the customer", example = "Regular customer")
    private String notes;

    // @Builder.Default
    // private boolean isActive = true;
}
