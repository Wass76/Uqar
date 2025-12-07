package com.Uqar.complaint.dto;

import com.Uqar.complaint.enums.ComplaintStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating complaint status")
public class ComplaintUpdateRequestDTO {
    
    @NotNull(message = "Status is required")
    @Schema(description = "New status of the complaint", example = "IN_PROGRESS", required = true)
    private ComplaintStatus status;
    
    @Schema(description = "Response from management", example = "We are investigating this issue")
    private String response;
    
    @Schema(description = "Additional data in JSON format", example = "{\"priority\": \"high\", \"category\": \"technical\"}")
    private String additionalData;
}
