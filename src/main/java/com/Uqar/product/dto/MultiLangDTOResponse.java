package com.Uqar.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MultiLangDTOResponse {
    private Long id;
    private String nameAr;
    private String nameEn;
} 