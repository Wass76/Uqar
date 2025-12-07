package com.Uqar.product.service;


import com.Uqar.product.dto.ManufacturerDTORequest;
import com.Uqar.product.dto.ManufacturerDTOResponse;
import com.Uqar.product.dto.MultiLangDTOResponse;
import com.Uqar.product.entity.Manufacturer;
import com.Uqar.product.entity.ManufacturerTranslation;
import com.Uqar.product.mapper.ManufacturerMapper;
import com.Uqar.product.repo.ManufacturerRepo;
import com.Uqar.product.repo.ManufacturerTranslationRepo;
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
public class ManufacturerService {

    private final ManufacturerRepo manufacturerRepo;
    private final ManufacturerMapper manufacturerMapper;
    private final LanguageRepo languageRepo;
    private final ManufacturerTranslationRepo manufacturerTranslationRepo;

    public ManufacturerService(ManufacturerRepo manufacturerRepo,
                               ManufacturerMapper manufacturerMapper,
                               LanguageRepo languageRepo,
                               ManufacturerTranslationRepo manufacturerTranslationRepo) {
        this.manufacturerRepo = manufacturerRepo;
        this.manufacturerMapper = manufacturerMapper;
        this.languageRepo = languageRepo;
        this.manufacturerTranslationRepo = manufacturerTranslationRepo;
    }

    public List<ManufacturerDTOResponse> getManufacturers(String lang) {
        log.info("Getting manufacturers with lang: {}", lang);
        List<Manufacturer> manufacturers = manufacturerRepo.findAllWithTranslations();
        log.info("Found {} manufacturers", manufacturers.size());
        
        return manufacturers.stream()
                .map(manufacturer -> {
                    log.info("Processing manufacturer: {} with {} translations", manufacturer.getName(),
                            manufacturer.getTranslations() != null ? manufacturer.getTranslations().size() : 0);
                    return manufacturerMapper.toResponse(manufacturer, lang);
                })
                .toList();
    }

    public ManufacturerDTOResponse getByID(long id, String lang) {
        Manufacturer manufacturer = manufacturerRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Manufacturer with ID " + id + " not found"));
        return manufacturerMapper.toResponse(manufacturer, lang);
    }

    public ManufacturerDTOResponse insertManufacturer(ManufacturerDTORequest dto,
                                                      String lang) {
        if (manufacturerRepo.existsByName(dto.getName())) {
            throw new ConflictException("Manufacturer with name '" + dto.getName() + "' already exists");
        }
        
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName(dto.getName());
        Manufacturer savedManufacturer = manufacturerRepo.save(manufacturer);

        List<ManufacturerTranslation> translations = dto.getTranslations().stream()
            .map(t -> {
                Language language = languageRepo.findByCode(t.getLang())
                        .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                return new ManufacturerTranslation(t.getName(), savedManufacturer, language);
            })
            .collect(Collectors.toList());

        manufacturerTranslationRepo.saveAll(translations);
        savedManufacturer.setTranslations(new HashSet<>(translations));

        return manufacturerMapper.toResponse(savedManufacturer, lang);
    }

    public ManufacturerDTOResponse editManufacturer(Long id, ManufacturerDTORequest dto,
                                                    String lang) {
        return manufacturerRepo.findByIdWithTranslations(id).map(existing -> {
            if (!existing.getName().equals(dto.getName()) && manufacturerRepo.existsByName(dto.getName())) {
                throw new ConflictException("Manufacturer with name '" + dto.getName() + "' already exists");
            }

            existing.setName(dto.getName());
            Manufacturer saved = manufacturerRepo.save(existing);

            if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
                manufacturerTranslationRepo.deleteByManufacturer(saved);

                List<ManufacturerTranslation> translations = dto.getTranslations().stream()
                        .map(t -> {
                            Language language = languageRepo.findByCode(t.getLang())
                                    .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                            return new ManufacturerTranslation(t.getName(), saved, language);
                        })
                        .toList();

                manufacturerTranslationRepo.saveAll(translations);
            }

            // 🔁 إعادة تحميل الكائن من قاعدة البيانات بعد الحفظ والتحديث
            Manufacturer updated = manufacturerRepo.findByIdWithTranslations(saved.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Updated manufacturer not found"));

            return manufacturerMapper.toResponse(updated, lang);

        }).orElseThrow(() -> new EntityNotFoundException("Manufacturer with ID " + id + " not found"));
    }

    public void deleteManufacturer(Long id) {
        if (!manufacturerRepo.existsById(id)) {
            throw new EntityNotFoundException("Manufacturer with ID " + id + " not found");
        }
        manufacturerRepo.deleteById(id);
    }

    
    public List<MultiLangDTOResponse> getManufacturersMultiLang() {
        log.info("Getting manufacturers with multi-language support");
        List<Manufacturer> manufacturers = manufacturerRepo.findAllWithTranslations();
        log.info("Found {} manufacturers", manufacturers.size());
        
        return manufacturers.stream()
                .map(manufacturer -> {
                    log.info("Processing manufacturer: {} with {} translations", manufacturer.getName(),
                            manufacturer.getTranslations() != null ? manufacturer.getTranslations().size() : 0);
                    return manufacturerMapper.toMultiLangResponse(manufacturer);
                })
                .toList();
    }

        public MultiLangDTOResponse getByIDMultiLang(long id) {
        Manufacturer manufacturer = manufacturerRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Manufacturer with ID " + id + " not found"));
        return manufacturerMapper.toMultiLangResponse(manufacturer);
    }
}
