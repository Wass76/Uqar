package com.Teryaq.user.mapper;

import com.Teryaq.user.dto.AreaRequestDTO;
import com.Teryaq.user.dto.AreaResponseDTO;
import com.Teryaq.user.entity.Area;
import com.Teryaq.user.entity.AreaTranslation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AreaMapper {

    private static final Logger log = LoggerFactory.getLogger(AreaMapper.class);

    public static Area toEntity(AreaRequestDTO dto) {
        if (dto == null) return null;

        return Area.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .isActive(dto.getIsActive())
                .build();
    }

//    public static AreaResponseDTO toResponseDTO(Area entity) {
//        if (entity == null) return null;
//
//        Map<String, String> translations = new HashMap<>();
//        if (entity.getTranslations() != null) {
//            for (AreaTranslation translation : entity.getTranslations()) {
//                translations.put(translation.getLanguage(), translation.getTranslatedName());
//            }
//        }
//
//        return AreaResponseDTO.builder()
//                .id(entity.getId())
//                .name(entity.getName())
//                .localizedName(entity.getArabicName()) // Default to base name
//                .description(entity.getDescription())
//                .translations(translations)
//                .isActive(entity.getIsActive())
//                .build();
//    }

    public static void updateEntityFromDTO(Area entity, AreaRequestDTO dto) {
        if (entity == null || dto == null) return;

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setIsActive(dto.getIsActive());
    }

    public static String getTranslationByLanguage(Area area, String language) {
        if (area == null || area.getTranslations() == null) {
            log.info("There is no translation for this area {}", area.getName());
            return null;
        }
        return area.getTranslations().stream().map(areaTranslation -> {
//            if (areaTranslation.getLanguage().equals(language)) {
                log.info("the requested lang is: {}", areaTranslation.getLanguage());
                return areaTranslation.getTranslatedName();
//            }
//            else {
//                log.info("the requested lang is: {} But we didn't find it", areaTranslation.getLanguage() );
//                return area.getName();
//            }
        }).findFirst().orElse(null);

//        return area.getTranslations().stream()
//                .map(AreaTranslation::getTranslatedName)
//                .findFirst()
//                .orElse(null);
    }

    public static AreaResponseDTO toResponseDTO(Area entity) {
        if (entity == null) return null;

        Map<String, String> translations = new HashMap<>();
        if (entity.getTranslations() != null) {
            for (AreaTranslation translation : entity.getTranslations()) {
                translations.put(translation.getLanguage(), translation.getTranslatedName());
            }
        }

        // Get the translated name for the requested language
//        String translatedName
//                = getTranslationByLanguage(entity, language);

        return AreaResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName()) // Always keep base name
                .localizedName(entity.getArabicName()) // Default to base name
                .description(entity.getDescription())
                .translations(translations)
                .isActive(entity.getIsActive())
                .build();
    }
}
