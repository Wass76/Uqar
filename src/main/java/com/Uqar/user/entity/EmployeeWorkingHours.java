package com.Uqar.user.entity;

import com.Uqar.utils.entity.AuditedEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWorkingHours extends AuditedEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "working_hours_id")
    private List<WorkShift> shifts = new ArrayList<>();
    
    // Helper methods to properly manage the shifts collection
    public void setShifts(List<WorkShift> newShifts) {
        if (this.shifts == null) {
            this.shifts = new ArrayList<>();
        }
        
        // Clear existing shifts
        this.shifts.clear();
        
        // Add new shifts
        if (newShifts != null) {
            for (WorkShift shift : newShifts) {
                this.shifts.add(shift);
            }
        }
    }
    
    public void updateShifts(List<WorkShift> newShifts) {
        if (this.shifts == null) {
            this.shifts = new ArrayList<>();
        }
        
        // Remove all existing shifts
        this.shifts.clear();
        
        // Add new shifts
        if (newShifts != null) {
            this.shifts.addAll(newShifts);
        }
    }
    
    public void replaceShifts(List<WorkShift> newShifts) {
        // Create a new ArrayList to avoid Hibernate collection reference issues
        List<WorkShift> updatedShifts = new ArrayList<>();
        
        if (newShifts != null) {
            updatedShifts.addAll(newShifts);
        }
        
        // Set the new collection
        this.shifts = updatedShifts;
    }
    
    public void addShift(WorkShift shift) {
        if (this.shifts == null) {
            this.shifts = new ArrayList<>();
        }
        this.shifts.add(shift);
    }
    
    public void removeShift(WorkShift shift) {
        if (this.shifts != null) {
            this.shifts.remove(shift);
        }
    }
    
    @Override
    protected String getSequenceName() {
        return "employee_working_hours_id_seq";
    }
} 