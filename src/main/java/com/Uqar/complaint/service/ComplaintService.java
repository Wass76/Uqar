package com.Uqar.complaint.service;

import com.Uqar.complaint.dto.ComplaintRequestDTO;
import com.Uqar.complaint.dto.ComplaintResponseDTO;
import com.Uqar.complaint.dto.ComplaintUpdateRequestDTO;
import com.Uqar.complaint.entity.Complaint;
import com.Uqar.complaint.enums.ComplaintStatus;
import com.Uqar.complaint.mapper.ComplaintMapper;
import com.Uqar.complaint.repository.ComplaintRepository;
import com.Uqar.user.entity.User;
import com.Uqar.user.service.BaseSecurityService;
import com.Uqar.utils.exception.ResourceNotFoundException;
import com.Uqar.utils.exception.UnAuthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ComplaintService extends BaseSecurityService {
    
    private static final Logger logger = LoggerFactory.getLogger(ComplaintService.class);
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Autowired
    private ComplaintMapper complaintMapper;
    
    public ComplaintService(com.Uqar.user.repository.UserRepository userRepository) {
        super(userRepository);
    }
    
    @Transactional
    public ComplaintResponseDTO createComplaint(ComplaintRequestDTO requestDTO, HttpServletRequest httpRequest) {
        logger.info("Creating new complaint with title: {}", requestDTO.getTitle());
        
        User currentUser = getCurrentUser();
        Long pharmacyId = getCurrentUserPharmacyId();
        
        Complaint complaint = complaintMapper.toEntity(requestDTO, pharmacyId, currentUser.getId());
        
        // Set audit information
        setAuditInfo(complaint, httpRequest, currentUser);
        
        Complaint savedComplaint = complaintRepository.save(complaint);
        logger.info("Complaint created successfully with ID: {}", savedComplaint.getId());
        
        return complaintMapper.toResponseDTO(savedComplaint);
    }
    
    @Transactional(readOnly = true)
    public ComplaintResponseDTO getComplaintById(Long id) {
        logger.info("Retrieving complaint with ID: {}", id);
        
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));
        
        // Validate access - user can only access complaints from their pharmacy
        validatePharmacyAccess(complaint.getPharmacyId());
        
        return complaintMapper.toResponseDTO(complaint);
    }
    
    @Transactional(readOnly = true)
    public Page<ComplaintResponseDTO> getAllComplaintsForPharmacy(Pageable pageable) {
        Long pharmacyId = getCurrentUserPharmacyId();
        logger.info("Retrieving complaints for pharmacy ID: {} with pagination", pharmacyId);
        
        Page<Complaint> complaints = complaintRepository.findByPharmacyId(pharmacyId, pageable);
        return complaints.map(complaintMapper::toResponseDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<ComplaintResponseDTO> getComplaintsByStatus(ComplaintStatus status, Pageable pageable) {
        Long pharmacyId = getCurrentUserPharmacyId();
        logger.info("Retrieving complaints with status {} for pharmacy ID: {} with pagination", status, pharmacyId);
        
        Page<Complaint> complaints = complaintRepository.findByPharmacyIdAndStatus(pharmacyId, status, pageable);
        return complaints.map(complaintMapper::toResponseDTO);
    }
    
    @Transactional
    public ComplaintResponseDTO updateComplaint(Long id, ComplaintUpdateRequestDTO updateDTO, HttpServletRequest httpRequest) {
        logger.info("Updating complaint with ID: {}", id);
        
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));
        
        // Only platform admin or pharmacy manager can update complaints
        User currentUser = getCurrentUser();
        if (!isAdmin() && !hasRole("PHARMACY_MANAGER")) {
            throw new UnAuthorizedException("Only platform admin or pharmacy manager can update complaints");
        }
        
        // If not admin, validate pharmacy access
        if (!isAdmin()) {
            validatePharmacyAccess(complaint.getPharmacyId());
        }
        
        complaintMapper.updateEntity(complaint, updateDTO, currentUser.getId());
        
        // Update audit information
        complaint.setLastModifiedBy(currentUser.getId());
        complaint.setIpAddress(getClientIpAddress(httpRequest));
        complaint.setUserAgent(httpRequest.getHeader("User-Agent"));
        complaint.setSessionId(httpRequest.getSession().getId());
        complaint.setUserType(currentUser.getRole().getName());
        
        Complaint updatedComplaint = complaintRepository.save(complaint);
        logger.info("Complaint updated successfully with ID: {}", updatedComplaint.getId());
        
        return complaintMapper.toResponseDTO(updatedComplaint);
    }
    
    @Transactional
    public void deleteComplaint(Long id) {
        logger.info("Deleting complaint with ID: {}", id);
        
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + id));
        
        User currentUser = getCurrentUser();
        
        // Only creator or admin can delete
        if (!isAdmin() && !complaint.getCreatedBy().equals(currentUser.getId())) {
            throw new UnAuthorizedException("Only complaint creator or admin can delete complaints");
        }
        
        // If not admin, validate pharmacy access
        if (!isAdmin()) {
            validatePharmacyAccess(complaint.getPharmacyId());
        }
        
        complaintRepository.delete(complaint);
        logger.info("Complaint deleted successfully with ID: {}", id);
    }
    
    @Transactional(readOnly = true)
    public Map<ComplaintStatus, Long> getComplaintStatistics() {
        Long pharmacyId = getCurrentUserPharmacyId();
        logger.info("Retrieving complaint statistics for pharmacy ID: {}", pharmacyId);
        
        Map<ComplaintStatus, Long> statistics = new java.util.HashMap<>();
        for (ComplaintStatus status : ComplaintStatus.values()) {
            long count = complaintRepository.countByPharmacyIdAndStatus(pharmacyId, status);
            statistics.put(status, count);
        }
        return statistics;
    }
    
    @Transactional(readOnly = true)
    public List<ComplaintResponseDTO> getComplaintsNeedingResponse() {
        Long pharmacyId = getCurrentUserPharmacyId();
        logger.info("Retrieving complaints needing response for pharmacy ID: {}", pharmacyId);
        
        List<Complaint> complaints = complaintRepository.findComplaintsNeedingResponseByPharmacyId(pharmacyId);
        return complaints.stream()
                .map(complaintMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Set audit information for complaint
     */
    private void setAuditInfo(Complaint complaint, HttpServletRequest httpRequest, User currentUser) {
        complaint.setIpAddress(getClientIpAddress(httpRequest));
        complaint.setUserAgent(httpRequest.getHeader("User-Agent"));
        complaint.setSessionId(httpRequest.getSession().getId());
        complaint.setUserType(currentUser.getRole().getName());
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }
}