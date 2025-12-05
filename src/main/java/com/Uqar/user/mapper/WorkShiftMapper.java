package com.Uqar.user.mapper;

import com.Uqar.user.dto.WorkShiftDTO;
import com.Uqar.user.entity.WorkShift;

import java.util.List;
import java.util.stream.Collectors;

public class WorkShiftMapper {
    
    public static WorkShift toEntity(WorkShiftDTO dto) {
        if (dto == null) return null;
        
        WorkShift workShift = new WorkShift();
        workShift.setStartTime(dto.getStartTime());
        workShift.setEndTime(dto.getEndTime());
        workShift.setDescription(dto.getDescription());
        return workShift;
    }
    
    public static WorkShiftDTO toDTO(WorkShift entity) {
        if (entity == null) return null;
        
        WorkShiftDTO dto = new WorkShiftDTO();
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setDescription(entity.getDescription());
        return dto;
    }
    
    public static List<WorkShift> toEntityList(List<WorkShiftDTO> dtos) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(WorkShiftMapper::toEntity)
                .collect(Collectors.toList());
    }
    
    public static List<WorkShiftDTO> toDTOList(List<WorkShift> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(WorkShiftMapper::toDTO)
                .collect(Collectors.toList());
    }
} 