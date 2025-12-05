package com.Uqar.complaint.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a complaint")
public class ComplaintRequestDTO {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Title of the complaint", example = "System Performance Issue", required = true)
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @Schema(description = "Detailed description of the complaint", example = "The system is running very slowly during peak hours", required = true)
    private String description;
    
    @Schema(description = "Additional data in JSON format", example = "{\"priority\": \"high\", \"category\": \"technical\"}")
    private String additionalData;
}
