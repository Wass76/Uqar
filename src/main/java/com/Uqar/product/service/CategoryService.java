package com.Uqar.product.service;

import com.Uqar.product.dto.CategoryDTORequest;
import com.Uqar.product.dto.CategoryDTOResponse;
import com.Uqar.product.dto.MultiLangDTOResponse;
import com.Uqar.product.entity.Category;
import com.Uqar.product.entity.CategoryTranslation;
import com.Uqar.product.mapper.CategoryMapper;
import com.Uqar.product.repo.CategoryRepo;
import com.Uqar.product.repo.CategoryTranslationRepo;
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
public class CategoryService {

    private final CategoryRepo categoryRepo;
    private final CategoryMapper categoryMapper;
    private final LanguageRepo languageRepo;
    private final CategoryTranslationRepo categoryTranslationRepo;

    public CategoryService(CategoryRepo categoryRepo, CategoryMapper categoryMapper, 
                         LanguageRepo languageRepo, CategoryTranslationRepo categoryTranslationRepo) {
        this.categoryRepo = categoryRepo;
        this.categoryMapper = categoryMapper;
        this.languageRepo = languageRepo;
        this.categoryTranslationRepo = categoryTranslationRepo;
    }

    public List<CategoryDTOResponse> getCategories(String lang) {
        log.info("Getting categories with lang: {}", lang);
        List<Category> categories = categoryRepo.findAllWithTranslations();
        log.info("Found {} categories", categories.size());
        
        return categories.stream()
                .map(category -> {
                    log.info("Processing category: {} with {} translations", category.getName(),
                            category.getTranslations() != null ? category.getTranslations().size() : 0);
                    return categoryMapper.toResponse(category, lang);
                })
                .toList();
    }

    public CategoryDTOResponse getByID(long id, String lang) {
        Category category = categoryRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found"));
        return categoryMapper.toResponse(category, lang);
    }

    public CategoryDTOResponse insertCategory(CategoryDTORequest dto, String lang) {
        if (categoryRepo.existsByName(dto.getName())) {
            throw new ConflictException("Category with name '" + dto.getName() + "' already exists");
        }
        
        Category category = new Category();
        category.setName(dto.getName());
        Category savedCategory = categoryRepo.save(category);

        List<CategoryTranslation> translations = dto.getTranslations().stream()
            .map(t -> {
                Language language = languageRepo.findByCode(t.getLang())
                        .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                return new CategoryTranslation(t.getName(), savedCategory, language);
            })
            .collect(Collectors.toList());

        categoryTranslationRepo.saveAll(translations);
        savedCategory.setTranslations(new HashSet<>(translations));

        return categoryMapper.toResponse(savedCategory, lang);
    }

    public CategoryDTOResponse editCategory(Long id, CategoryDTORequest dto, String lang) {
        return categoryRepo.findByIdWithTranslations(id).map(existing -> {
            if (!existing.getName().equals(dto.getName()) && categoryRepo.existsByName(dto.getName())) {
                throw new ConflictException("Category with name '" + dto.getName() + "' already exists");
            }

            existing.setName(dto.getName());
            Category saved = categoryRepo.save(existing);

            if (dto.getTranslations() != null && !dto.getTranslations().isEmpty()) {
                categoryTranslationRepo.deleteByCategory(saved);

                List<CategoryTranslation> translations = dto.getTranslations().stream()
                        .map(t -> {
                            Language language = languageRepo.findByCode(t.getLang())
                                    .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                            return new CategoryTranslation(t.getName(), saved, language);
                        })
                        .toList();

                categoryTranslationRepo.saveAll(translations);
            }

            // 🔁 إعادة تحميل الكائن من قاعدة البيانات بعد الحفظ والتحديث
            Category updated = categoryRepo.findByIdWithTranslations(saved.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Updated category not found"));

            return categoryMapper.toResponse(updated, lang);

        }).orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found"));
    }

    public void deleteCategory(Long id) {
        if (!categoryRepo.existsById(id)) {
            throw new EntityNotFoundException("Category with ID " + id + " not found");
        }
        categoryRepo.deleteById(id);
    }

    public List<MultiLangDTOResponse> getCategoriesMultiLang() {
        log.info("Getting categories with multi-language support");
        List<Category> categories = categoryRepo.findAllWithTranslations();
        log.info("Found {} categories", categories.size());
        
        return categories.stream()
                .map(category -> {
                    log.info("Processing category: {} with {} translations", category.getName(),
                            category.getTranslations() != null ? category.getTranslations().size() : 0);
                    return categoryMapper.toMultiLangResponse(category);
                })
                .toList();
    }

        public MultiLangDTOResponse getByIDMultiLang(long id) {
        Category category = categoryRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with ID " + id + " not found"));
        return categoryMapper.toMultiLangResponse(category);
    }
}
