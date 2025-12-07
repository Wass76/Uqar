package com.Uqar.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class AreaRequestDTO {

    @NotBlank(message = "Area name cannot be blank")
    private String name;

    private String description;

    private Map<String, String> translations; // language -> translated name

    private Boolean isActive = true;
}
