package com.Uqar.product.mapper;

import com.Uqar.product.dto.MProductTranslationDTORequest;
import com.Uqar.product.dto.MProductTranslationDTOResponse;
import com.Uqar.product.entity.MasterProductTranslation;
import org.springframework.stereotype.Component;

@Component
public class MasterProductTranslationMapper {
    
    public MProductTranslationDTOResponse toResponse(MasterProductTranslation translation) {
        return MProductTranslationDTOResponse.builder()
                .tradeName(translation.getTradeName())
                .scientificName(translation.getScientificName())
                .languageName(translation.getLanguage().getName())
                .build();
    }
    
    public MasterProductTranslation toEntity(MProductTranslationDTORequest dto) {
        MasterProductTranslation translation = new MasterProductTranslation();
        translation.setTradeName(dto.getTradeName());
        translation.setScientificName(dto.getScientificName());
        return translation;
    }
}
