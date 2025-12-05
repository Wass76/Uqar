package com.Uqar.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor  
@Builder
public class CategoryTranslationDTOResponse {

    private String name;
    private String languageName;
        
}