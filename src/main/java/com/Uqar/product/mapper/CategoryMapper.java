package com.Uqar.product.mapper;

import com.Uqar.product.dto.CategoryDTORequest;
import com.Uqar.product.dto.CategoryDTOResponse;
import com.Uqar.product.dto.MultiLangDTOResponse;
import com.Uqar.product.entity.Category;
import com.Uqar.product.entity.CategoryTranslation;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDTOResponse toResponse(Category category, String lang) {
        if (category == null) return null;

        String sanitizedlang = lang == null ? "en" : lang.trim().toLowerCase();
        


        String translatedName = category.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
                .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(sanitizedlang))
                .map(CategoryTranslation::getName)
                .findFirst()
                .orElse(category.getName());
                


        return CategoryDTOResponse.builder()
                .id(category.getId())
                .name(translatedName)
                .build();
    }


    public Category toEntity(CategoryDTORequest dto) {
        if (dto == null) return null;

        Category category = new Category();
        category.setName(dto.getName());
        return category;
    }

    public MultiLangDTOResponse toMultiLangResponse(Category category) {
        if (category == null) return null;

        String nameAr = category.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "ar".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(CategoryTranslation::getName)
                .findFirst()
                .orElse(category.getName());

        String nameEn = category.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "en".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(CategoryTranslation::getName)
                .findFirst()
                .orElse(category.getName());

        return MultiLangDTOResponse.builder()
                .id(category.getId())
                .nameAr(nameAr)
                .nameEn(nameEn)
                .build();
    }

}
