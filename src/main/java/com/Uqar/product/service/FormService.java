package com.Uqar.product.service;


import com.Uqar.product.dto.FormDTORequest;
import com.Uqar.product.dto.FormDTOResponse;
import com.Uqar.product.dto.MultiLangDTOResponse;
import com.Uqar.product.entity.Form;
import com.Uqar.product.entity.FormTranslation;
import com.Uqar.product.mapper.FormMapper;
import com.Uqar.product.repo.FormRepo;
import com.Uqar.product.repo.FormTranslationRepo;
import com.Uqar.language.Language;
import com.Uqar.language.LanguageRepo;
import com.Uqar.utils.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
public class FormService {

    private final FormRepo formRepo;
    private final FormMapper formMapper;
    private final LanguageRepo languageRepo;
    private final FormTranslationRepo formTranslationRepo;

    public FormService(FormRepo formRepo, FormMapper formMapper, 
                      LanguageRepo languageRepo, FormTranslationRepo formTranslationRepo) {
        this.formRepo = formRepo;
        this.formMapper = formMapper;
        this.languageRepo = languageRepo;
        this.formTranslationRepo = formTranslationRepo;
    }

    public List<FormDTOResponse> getForms(String lang) {
        log.info("Getting forms with lang: {}", lang);
        List<Form> forms = formRepo.findAllWithTranslations();
        log.info("Found {} forms", forms.size());
        
        return forms.stream()
                .map(form -> {
                    log.info("Processing form: {} with {} translations", form.getName(),
                            form.getTranslations() != null ? form.getTranslations().size() : 0);
                    return formMapper.toResponse(form, lang);
                })
                .toList();
    }

    public FormDTOResponse getByID(long id, String lang) {
        Form form = formRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Form with ID " + id + " not found"));
        return formMapper.toResponse(form, lang);
    }

    public FormDTOResponse insertForm(FormDTORequest dto, String lang) {
        if (formRepo.existsByName(dto.getName())) {
            throw new ConflictException("Form with name '" + dto.getName() + "' already exists");
        }
        
        Form form = new Form();
        form.setName(dto.getName());
        Form savedForm = formRepo.save(form);

        List<FormTranslation> translations = dto.getTranslations().stream()
            .map(t -> {
                Language language = languageRepo.findByCode(t.getLang())
                        .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                return new FormTranslation(t.getName(), savedForm, language);
            })
            .collect(Collectors.toList());

        formTranslationRepo.saveAll(translations);
        savedForm.setTranslations(new HashSet<>(translations));

        return formMapper.toResponse(savedForm, lang);
    }

    public FormDTOResponse editForm(Long id, FormDTORequest dto, String lang) {
        return formRepo.findByIdWithTranslations(id).map(existing -> {
            if (!existing.getName().equals(dto.getName()) && formRepo.existsByName(dto.getName())) {
                throw new ConflictException("Form with name '" + dto.getName() + "' already exists");
            }

            existing.setName(dto.getName());
            Form saved = formRepo.save(existing);

            if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
                formTranslationRepo.deleteByForm(saved);

                List<FormTranslation> translations = dto.getTranslations().stream()
                        .map(t -> {
                            Language language = languageRepo.findByCode(t.getLang())
                                    .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                            return new FormTranslation(t.getName(), saved, language);
                        })
                        .toList();

                formTranslationRepo.saveAll(translations);
            }

            // 🔁 إعادة تحميل الكائن من قاعدة البيانات بعد الحفظ والتحديث
            Form updated = formRepo.findByIdWithTranslations(saved.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Updated form not found"));

            return formMapper.toResponse(updated, lang);

        }).orElseThrow(() -> new EntityNotFoundException("Form with ID " + id + " not found"));
    }

    public void deleteForm(Long id) {
        if (!formRepo.existsById(id)) {
            throw new EntityNotFoundException("Form with ID " + id + " not found");
        }
        formRepo.deleteById(id);
    }

    
    public List<MultiLangDTOResponse> getFormsMultiLang() {
        log.info("Getting forms with multi-language support");
        List<Form> forms = formRepo.findAllWithTranslations();
        log.info("Found {} forms", forms.size());
        
        return forms.stream()
                .map(form -> {
                    log.info("Processing form: {} with {} translations", form.getName(),
                            form.getTranslations() != null ? form.getTranslations().size() : 0);
                    return formMapper.toMultiLangResponse(form);
                })
                .toList();
    }

        public MultiLangDTOResponse getByIDMultiLang(long id) {
        Form form = formRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Form with ID " + id + " not found"));
        return formMapper.toMultiLangResponse(form);
    }
}
