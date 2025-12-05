package com.Uqar.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class AreaResponseDTO {

    private Long id;
    private String name; // Base name (English)
    private String localizedName; // Name in requested language
    private String description;
    private Map<String, String> translations; // language -> translated name
    private Boolean isActive;
}
