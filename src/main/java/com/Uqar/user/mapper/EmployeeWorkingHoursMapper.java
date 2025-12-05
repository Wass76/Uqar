package com.Uqar.user.mapper;

import com.Uqar.user.dto.EmployeeWorkingHoursDTO;
import com.Uqar.user.entity.EmployeeWorkingHours;
import com.Uqar.user.entity.WorkShift;

import java.util.List;
import java.util.stream.Collectors;

public class EmployeeWorkingHoursMapper {
    
    public static EmployeeWorkingHours toEntity(EmployeeWorkingHoursDTO dto) {
        if (dto == null) return null;
        
        EmployeeWorkingHours workingHours = new EmployeeWorkingHours();
        workingHours.setDayOfWeek(dto.getDayOfWeek());
        if (dto.getShifts() != null) {
            List<WorkShift> shifts = dto.getShifts().stream()
                    .map(WorkShiftMapper::toEntity)
                    .collect(Collectors.toList());
            workingHours.setShifts(shifts);
        }
        return workingHours;
    }
    
    public static EmployeeWorkingHoursDTO toDTO(EmployeeWorkingHours entity) {
        if (entity == null) return null;
        
        EmployeeWorkingHoursDTO dto = new EmployeeWorkingHoursDTO();
//        dto.setId(entity.getId());
        dto.setDayOfWeek(entity.getDayOfWeek());
        if (entity.getShifts() != null) {
            List<com.Uqar.user.dto.WorkShiftDTO> shifts = entity.getShifts().stream()
                    .map(WorkShiftMapper::toDTO)
                    .collect(Collectors.toList());
            dto.setShifts(shifts);
        }
        return dto;
    }
    
    public static List<EmployeeWorkingHours> toEntityList(List<EmployeeWorkingHoursDTO> dtos) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(EmployeeWorkingHoursMapper::toEntity)
                .collect(Collectors.toList());
    }
    
    public static List<EmployeeWorkingHoursDTO> toDTOList(List<EmployeeWorkingHours> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(EmployeeWorkingHoursMapper::toDTO)
                .collect(Collectors.toList());
    }
} 