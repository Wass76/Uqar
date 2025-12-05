package com.Uqar.complaint.mapper;

import com.Uqar.complaint.dto.ComplaintRequestDTO;
import com.Uqar.complaint.dto.ComplaintResponseDTO;
import com.Uqar.complaint.dto.ComplaintUpdateRequestDTO;
import com.Uqar.complaint.entity.Complaint;
import org.springframework.stereotype.Component;

@Component
public class ComplaintMapper {
    
    /**
     * Convert ComplaintRequestDTO to Complaint entity
     */
    public Complaint toEntity(ComplaintRequestDTO dto, Long pharmacyId, Long createdBy) {
        Complaint complaint = new Complaint();
        complaint.setTitle(dto.getTitle());
        complaint.setDescription(dto.getDescription());
        complaint.setPharmacyId(pharmacyId);
        complaint.setCreatedBy(createdBy);
        complaint.setAdditionalData(dto.getAdditionalData());
        return complaint;
    }
    
    /**
     * Convert Complaint entity to ComplaintResponseDTO
     */
    public ComplaintResponseDTO toResponseDTO(Complaint complaint) {
        ComplaintResponseDTO dto = new ComplaintResponseDTO();
        dto.setId(complaint.getId());
        dto.setTitle(complaint.getTitle());
        dto.setDescription(complaint.getDescription());
        dto.setPharmacyId(complaint.getPharmacyId());
        dto.setCreatedBy(complaint.getCreatedBy());
        dto.setStatus(complaint.getStatus());
        dto.setResponse(complaint.getResponse());
        dto.setRespondedBy(complaint.getRespondedBy());
        dto.setRespondedAt(complaint.getRespondedAt());
        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setUpdatedAt(complaint.getUpdatedAt());
        dto.setUpdatedBy(complaint.getLastModifiedBy());
        dto.setAdditionalData(complaint.getAdditionalData());
        return dto;
    }
    
    /**
     * Update Complaint entity with ComplaintUpdateRequestDTO
     */
    public void updateEntity(Complaint complaint, ComplaintUpdateRequestDTO dto, Long updatedBy) {
        complaint.setStatus(dto.getStatus());
        complaint.setResponse(dto.getResponse());
        complaint.setLastModifiedBy(updatedBy);
        complaint.setAdditionalData(dto.getAdditionalData());
        
        // If status is being changed to RESOLVED or CLOSED, set responded fields
        if (dto.getStatus() == com.Uqar.complaint.enums.ComplaintStatus.RESOLVED || 
            dto.getStatus() == com.Uqar.complaint.enums.ComplaintStatus.CLOSED) {
            complaint.setRespondedBy(updatedBy);
            complaint.setRespondedAt(java.time.LocalDateTime.now());
        }
    }
}
