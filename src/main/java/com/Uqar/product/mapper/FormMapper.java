package com.Uqar.product.mapper;

import com.Uqar.product.dto.FormDTORequest;
import com.Uqar.product.dto.FormDTOResponse;
import com.Uqar.product.dto.MultiLangDTOResponse;
import com.Uqar.product.entity.Form;
import com.Uqar.product.entity.FormTranslation;
import org.springframework.stereotype.Component;

@Component
public class FormMapper {


   public FormDTOResponse toResponse(Form form, String lang) {
       if (form == null) return null;

       String sanitizedlang = lang == null ? "en" : lang.trim().toLowerCase();
       
       

       String translatedName = form.getTranslations().stream()
               .filter(t -> t.getLanguage() != null && t.getLanguage().getCode() != null)
               .filter(t -> t.getLanguage().getCode().trim().equalsIgnoreCase(sanitizedlang))
               .map(FormTranslation::getName)
               .findFirst()
               .orElse(form.getName());
               
       

     return FormDTOResponse.builder()
             .id(form.getId())
             .name(translatedName)
             .build();
 }

    public Form toEntity(FormDTORequest dto) {
        if (dto == null) return null;

        Form form = new Form();
        form.setName(dto.getName());
        return form;
    }

    public MultiLangDTOResponse toMultiLangResponse(Form form) {
        if (form == null) return null;

        String nameAr = form.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "ar".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(FormTranslation::getName)
                .findFirst()
                .orElse(form.getName());

        String nameEn = form.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "en".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(FormTranslation::getName)
                .findFirst()
                .orElse(form.getName());

        return MultiLangDTOResponse.builder()
                .id(form.getId())
                .nameAr(nameAr)
                .nameEn(nameEn)
                .build();
    }

}
