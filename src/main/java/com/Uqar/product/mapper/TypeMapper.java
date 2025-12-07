package com.Uqar.product.mapper;

import com.Uqar.product.dto.TypeDTOResponse;
import com.Uqar.product.dto.TypeDTORequest;
import com.Uqar.product.dto.MultiLangDTOResponse;
import com.Uqar.product.entity.TypeTranslation;
import com.Uqar.product.entity.Type;
import org.springframework.stereotype.Component;


@Component
public class TypeMapper {


    public TypeDTOResponse toResponse(Type type, String lang) {
        if (type == null) return null;
    
        String sanitizedlang = lang == null ? "en" : lang.trim().toLowerCase();
    
        String translatedName = type.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(sanitizedlang))
                .map(TypeTranslation::getName)
                .findFirst()
                .orElse(type.getName());
        
        return TypeDTOResponse.builder()
                .id(type.getId())
                .name(translatedName)
                .build();
    }
    
    

    public Type toEntity(TypeDTOResponse dto) {
        if (dto == null) return null;

        Type type = new Type();
        type.setName(dto.getName());
        return type;
    }

    public Type toEntity(TypeDTORequest dto) {
        if (dto == null) return null;

        Type type = new Type();
        type.setName(dto.getName());
        return type;
    }

    public MultiLangDTOResponse toMultiLangResponse(Type type) {
        if (type == null) return null;

        String nameAr = type.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "ar".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(TypeTranslation::getName)
                .findFirst()
                .orElse(type.getName());

        String nameEn = type.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "en".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(TypeTranslation::getName)
                .findFirst()
                .orElse(type.getName());

        return MultiLangDTOResponse.builder()
                .id(type.getId())
                .nameAr(nameAr)
                .nameEn(nameEn)
                .build();
    }
}
