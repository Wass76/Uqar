package com.Uqar.product.service;

import com.Uqar.product.dto.TypeDTORequest;
import com.Uqar.product.dto.TypeDTOResponse;
import com.Uqar.product.dto.MultiLangDTOResponse;
import com.Uqar.product.entity.TypeTranslation;
import com.Uqar.product.entity.Type;
import com.Uqar.product.mapper.TypeMapper;
import com.Uqar.product.repo.TypeRepo;
import com.Uqar.product.repo.TypeTranslationRepo;
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
public class TypeService {

    private final TypeRepo typeRepo;
    private final TypeMapper typeMapper;
    private final LanguageRepo languageRepo;
    private final TypeTranslationRepo typeTranslationRepo;

    public TypeService(TypeRepo typeRepo, TypeMapper typeMapper, LanguageRepo languageRepo, TypeTranslationRepo typeTranslationRepo) {
        this.typeRepo = typeRepo;
        this.typeMapper = typeMapper;
        this.languageRepo = languageRepo;
        this.typeTranslationRepo = typeTranslationRepo;
    }

    public List<TypeDTOResponse> getTypes(String lang) {
        log.info("Getting types with lang: {}", lang);
        
        List<Type> types = typeRepo.findAllWithTranslations();
        log.info("Found {} types", types.size());
    
        return types.stream()
                .map(type -> {
                    log.info("Processing type: {} with {} translations", type.getName(),
                            type.getTranslations() != null ? type.getTranslations().size() : 0);
    
                    return typeMapper.toResponse(type, lang);
                })
                .toList();
    }
    
    public TypeDTOResponse getByID(long id, String lang) {
        Type type = typeRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Type with ID " + id + " not found"));
        return typeMapper.toResponse(type, lang);
    }

    public TypeDTOResponse insertType(TypeDTORequest dto, String lang) {
    
            if (typeRepo.existsByName(dto.getName())) {
                throw new ConflictException("Type with name '" + dto.getName() + "' already exists");
            }
    
            Type type = new Type();
            type.setName(dto.getName());
            Type savedType = typeRepo.save(type);
    
            List<TypeTranslation> translations = dto.getTranslations().stream()
                .map(t -> {
                    Language language = languageRepo.findByCode(t.getLang())
                            .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                    return new TypeTranslation(t.getName(), savedType, language);
                })
                .collect(Collectors.toList());
    
            typeTranslationRepo.saveAll(translations);
    
            savedType.setTranslations(new HashSet<>(translations));
    
            return typeMapper.toResponse(savedType, lang);
            
    }

    
    
    public TypeDTOResponse editType(Long id, TypeDTORequest dto, String lang) {
    return typeRepo.findByIdWithTranslations(id).map(existing -> {
        if (!existing.getName().equals(dto.getName()) && typeRepo.existsByName(dto.getName())) {
            throw new ConflictException("Type with name '" + dto.getName() + "' already exists");
        }

        existing.setName(dto.getName());
        Type saved = typeRepo.save(existing);

        if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
            typeTranslationRepo.deleteByType(saved);

            List<TypeTranslation> translations = dto.getTranslations().stream()
                    .map(t -> {
                        Language language = languageRepo.findByCode(t.getLang())
                                .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                        return new TypeTranslation(t.getName(), saved, language);
                    })
                    .toList();

            typeTranslationRepo.saveAll(translations);
        }

        // 🔁 إعادة تحميل الكائن من قاعدة البيانات بعد الحفظ والتحديث
        Type updated = typeRepo.findByIdWithTranslations(saved.getId())
                .orElseThrow(() -> new EntityNotFoundException("Updated type not found"));

        return typeMapper.toResponse(updated, lang);

    }).orElseThrow(() -> new EntityNotFoundException("Type with ID " + id + " not found"));
}

    

    public void deleteType(Long id) {
        if (!typeRepo.existsById(id)) {
            throw new EntityNotFoundException("Type with ID " + id + " not found");
        }
        typeRepo.deleteById(id);
    }

    
    public List<MultiLangDTOResponse> getTypesMultiLang() {
        log.info("Getting types with multi-language support");
        List<Type> types = typeRepo.findAllWithTranslations();
        log.info("Found {} types", types.size());
        
        return types.stream()
                .map(type -> {
                    log.info("Processing type: {} with {} translations", type.getName(),
                            type.getTranslations() != null ? type.getTranslations().size() : 0);
                    return typeMapper.toMultiLangResponse(type);
                })
                .toList();
    }

        public MultiLangDTOResponse getByIDMultiLang(long id) {
        Type type = typeRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Type with ID " + id + " not found"));
        return typeMapper.toMultiLangResponse(type);
    }
}
