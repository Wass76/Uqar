package com.Uqar.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Data
@Schema(description = "Work shift information")
public class WorkShiftDTO {
    @JsonFormat(pattern = "HH:mm")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Schema(description = "Start time of the shift", example = "09:00")
    private LocalTime startTime;
    
    @JsonFormat(pattern = "HH:mm")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @Schema(description = "End time of the shift", example = "17:00")
    private LocalTime endTime;
    
    @Schema(description = "Description of the shift", example = "Regular Shift")
    private String description;
} 