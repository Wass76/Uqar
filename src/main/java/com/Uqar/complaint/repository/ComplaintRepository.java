package com.Uqar.complaint.repository;

import com.Uqar.complaint.entity.Complaint;
import com.Uqar.complaint.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    
    /**
     * Find complaints by pharmacy ID
     */
    List<Complaint> findByPharmacyId(Long pharmacyId);
    
    /**
     * Find complaints by pharmacy ID with pagination
     */
    Page<Complaint> findByPharmacyId(Long pharmacyId, Pageable pageable);
    
    /**
     * Find complaints by creator ID
     */
    List<Complaint> findByCreatedBy(Long createdBy);
    
    /**
     * Find complaints by creator ID with pagination
     */
    Page<Complaint> findByCreatedBy(Long createdBy, Pageable pageable);
    
    /**
     * Find complaints by status
     */
    List<Complaint> findByStatus(ComplaintStatus status);
    
    /**
     * Find complaints by status with pagination
     */
    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);
    
    /**
     * Find complaints by pharmacy ID and status
     */
    List<Complaint> findByPharmacyIdAndStatus(Long pharmacyId, ComplaintStatus status);
    
    /**
     * Find complaints by pharmacy ID and status with pagination
     */
    Page<Complaint> findByPharmacyIdAndStatus(Long pharmacyId, ComplaintStatus status, Pageable pageable);
    
    /**
     * Find complaints created between two dates
     */
    @Query("SELECT c FROM Complaint c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Complaint> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find complaints by pharmacy ID created between two dates
     */
    @Query("SELECT c FROM Complaint c WHERE c.pharmacyId = :pharmacyId AND c.createdAt BETWEEN :startDate AND :endDate")
    List<Complaint> findByPharmacyIdAndCreatedAtBetween(@Param("pharmacyId") Long pharmacyId,
                                                       @Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count complaints by status
     */
    long countByStatus(ComplaintStatus status);
    
    /**
     * Count complaints by pharmacy ID and status
     */
    long countByPharmacyIdAndStatus(Long pharmacyId, ComplaintStatus status);
    
    /**
     * Find complaints that need response (PENDING or IN_PROGRESS)
     */
    @Query("SELECT c FROM Complaint c WHERE c.status IN ('PENDING', 'IN_PROGRESS')")
    List<Complaint> findComplaintsNeedingResponse();
    
    /**
     * Find complaints that need response by pharmacy ID
     */
    @Query("SELECT c FROM Complaint c WHERE c.pharmacyId = :pharmacyId AND c.status IN ('PENDING', 'IN_PROGRESS')")
    List<Complaint> findComplaintsNeedingResponseByPharmacyId(@Param("pharmacyId") Long pharmacyId);
}
