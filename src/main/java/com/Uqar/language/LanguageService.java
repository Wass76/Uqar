package com.Uqar.language;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LanguageService {

    private final LanguageRepo languageRepo;

    public LanguageService(LanguageRepo languageRepo) {
        this.languageRepo = languageRepo;
    }

    public List<Language> gitAll() {
        return languageRepo.findAll();
    }

    public Language gitById(Long id) {
        return languageRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Language With id " + id + "not found"));
    }

    public void createLanguage(Language language) {
        languageRepo.save(language);
    }
    public Language editLanguage(Long id,Language language) {
        return languageRepo.findById(id).map(lang ->{
            lang.setName(language.getName());
            lang.setCode(language.getCode());
         return languageRepo.save(lang);
        }).orElseThrow(() -> new EntityNotFoundException("Language with ID " + id + " not found!"));
    }
    public void deleteLanguage(Long id) {
        if(!languageRepo.existsById(id)) {
            throw new EntityNotFoundException("Language with ID " + id + " not found!") ;
        }
        languageRepo.deleteById(id);
    }
}
