package com.Uqar.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Request to create or update multiple working hours for an employee")
public class UpsertWorkingHoursRequestDTO {
    @Schema(description = "List of working hours requests for different days and shifts", 
            example = "[{\"daysOfWeek\": [\"MONDAY\", \"TUESDAY\"], \"shifts\": [{\"startTime\": \"09:00\", \"endTime\": \"17:00\", \"description\": \"Morning Shift\"}]}, {\"daysOfWeek\": [\"WEDNESDAY\", \"THURSDAY\"], \"shifts\": [{\"startTime\": \"14:00\", \"endTime\": \"22:00\", \"description\": \"Evening Shift\"}]}]")
    @jakarta.validation.constraints.NotEmpty(message = "Working hours requests cannot be empty")
    private List<CreateWorkingHoursRequestDTO> workingHoursRequests;
}
