package com.Uqar.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.DayOfWeek;
import java.util.List;

@Data
@Schema(description = "Request to create working hours for multiple days")
public class CreateWorkingHoursRequestDTO {
    @Schema(description = "List of days to apply the same working hours", 
            example = "[\"MONDAY\", \"TUESDAY\", \"WEDNESDAY\", \"THURSDAY\", \"FRIDAY\"]")
    private List<DayOfWeek> daysOfWeek; // List of days to apply the same working hours
    
    @Schema(description = "The shifts to apply to all specified days")
    private List<WorkShiftDTO> shifts;   // The shifts to apply to all specified days
} 