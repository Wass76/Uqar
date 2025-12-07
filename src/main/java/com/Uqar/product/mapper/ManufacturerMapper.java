package com.Uqar.product.mapper;

import com.Uqar.product.dto.ManufacturerDTOResponse;
import com.Uqar.product.dto.ManufacturerDTORequest;
import com.Uqar.product.dto.MultiLangDTOResponse;
import com.Uqar.product.entity.ManufacturerTranslation;
import com.Uqar.product.entity.Manufacturer;
import org.springframework.stereotype.Component;

@Component
public class ManufacturerMapper {

    public ManufacturerDTOResponse toResponse(Manufacturer manufacturer, String lang) {
        if (manufacturer == null) return null;

        String sanitizedlang = lang == null ? "en" : lang.trim().toLowerCase();
        


        String translatedName = manufacturer.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(sanitizedlang))
                .map(ManufacturerTranslation::getName)
                .findFirst()
                .orElse(manufacturer.getName());
                


        return ManufacturerDTOResponse.builder()
                .id(manufacturer.getId())
                .name(translatedName)
                .build();
    }

    public Manufacturer toEntity(ManufacturerDTORequest dto) {
        if (dto == null) return null;

        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName(dto.getName());
        return manufacturer;
    }

    public MultiLangDTOResponse toMultiLangResponse(Manufacturer manufacturer) {
        if (manufacturer == null) return null;

        String nameAr = manufacturer.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "ar".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(ManufacturerTranslation::getName)
                .findFirst()
                .orElse(manufacturer.getName());

        String nameEn = manufacturer.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "en".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(ManufacturerTranslation::getName)
                .findFirst()
                .orElse(manufacturer.getName());

        return MultiLangDTOResponse.builder()
                .id(manufacturer.getId())
                .nameAr(nameAr)
                .nameEn(nameEn)
                .build();
    }
}
