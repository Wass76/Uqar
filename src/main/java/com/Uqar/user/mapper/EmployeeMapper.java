package com.Uqar.user.mapper;

import com.Uqar.user.dto.EmployeeCreateRequestDTO;
import com.Uqar.user.dto.EmployeeUpdateRequestDTO;
import com.Uqar.user.dto.EmployeeResponseDTO;
import com.Uqar.user.dto.EmployeeWorkingHoursDTO;
import com.Uqar.user.entity.Employee;
import com.Uqar.user.entity.EmployeeWorkingHours;

import java.util.List;
import java.util.stream.Collectors;

public class EmployeeMapper {
    
    // Map from CreateRequestDTO to new Employee entity
    public static Employee toEntity(EmployeeCreateRequestDTO dto) {
        if (dto == null) return null;
        Employee employee = new Employee();
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setPassword(dto.getPassword());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setStatus(dto.getStatus());
        employee.setDateOfHire(dto.getDateOfHire());
        // Don't set working hours here - will be handled in service after employee is saved
        // email and pharmacy will be set in service
        return employee;
    }

    // Update existing Employee entity from UpdateRequestDTO
    public static void updateEntity(Employee employee, EmployeeUpdateRequestDTO dto) {
        if (dto == null || employee == null) return;
        
        if (dto.getFirstName() != null) {
            employee.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            employee.setLastName(dto.getLastName());
        }
        if (dto.getPhoneNumber() != null) {
            employee.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getStatus() != null) {
            employee.setStatus(dto.getStatus());
        }
        if (dto.getDateOfHire() != null) {
            employee.setDateOfHire(dto.getDateOfHire());
        }
    }

    // Map from Employee entity to ResponseDTO
    public static EmployeeResponseDTO toResponseDTO(Employee entity) {
        if (entity == null) return null;
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setStatus(entity.getStatus());
        dto.setDateOfHire(entity.getDateOfHire());
        dto.setRoleName(entity.getRole() != null ? entity.getRole().getName() : null);
        dto.setPharmacyId(entity.getPharmacy() != null ? entity.getPharmacy().getId() : null);
        
        // Set working hours if available
        if (entity.getEmployeeWorkingHoursList() != null) {
            dto.setWorkingHours(EmployeeWorkingHoursMapper.toDTOList(entity.getEmployeeWorkingHoursList()));
        }
        
        return dto;
    }
    
    // Create working hours from DTO list
    public static List<EmployeeWorkingHours> createWorkingHoursFromDTO(Employee employee, List<EmployeeWorkingHoursDTO> workingHoursDTOs) {
        if (workingHoursDTOs == null || workingHoursDTOs.isEmpty()) {
            return null;
        }
        
        return workingHoursDTOs.stream()
                .map(dto -> {
                    EmployeeWorkingHours workingHours = EmployeeWorkingHoursMapper.toEntity(dto);
                    workingHours.setEmployee(employee);
                    return workingHours;
                })
                .collect(Collectors.toList());
    }
} 