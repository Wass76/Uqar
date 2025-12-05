package com.Uqar.complaint.dto;

import com.Uqar.complaint.enums.ComplaintStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for complaint data")
public class ComplaintResponseDTO {
    
    @Schema(description = "Unique identifier of the complaint", example = "1")
    private Long id;
    
    @Schema(description = "Title of the complaint", example = "System Performance Issue")
    private String title;
    
    @Schema(description = "Detailed description of the complaint", example = "The system is running very slowly during peak hours")
    private String description;
    
    @Schema(description = "ID of the pharmacy", example = "1")
    private Long pharmacyId;
    
    @Schema(description = "ID of the user who created the complaint", example = "1")
    private Long createdBy;
    
    @Schema(description = "Current status of the complaint", example = "PENDING")
    private ComplaintStatus status;
    
    @Schema(description = "Response from management", example = "We are investigating this issue")
    private String response;
    
    @Schema(description = "ID of the user who responded", example = "2")
    private Long respondedBy;
    
    @Schema(description = "Timestamp when the complaint was responded to")
    private LocalDateTime respondedAt;
    
    @Schema(description = "Timestamp when the complaint was created")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the complaint was last updated")
    private LocalDateTime updatedAt;
    
    @Schema(description = "ID of the user who last updated the complaint", example = "1")
    private Long updatedBy;
    
    @Schema(description = "Additional data in JSON format", example = "{\"priority\": \"high\", \"category\": \"technical\"}")
    private String additionalData;
}
