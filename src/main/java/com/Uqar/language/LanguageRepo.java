package com.Uqar.language;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LanguageRepo extends JpaRepository<Language,Long> {
    Optional<Language> findByCode(String code);
}
