package com.Uqar.user.repository;

import com.Uqar.user.entity.AreaTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreaTranslationRepository extends JpaRepository<AreaTranslation, Long> {

    List<AreaTranslation> findByAreaId(Long areaId);

    List<AreaTranslation> findByLanguage(String language);

    Optional<AreaTranslation> findByAreaIdAndLanguage(Long areaId, String language);

    List<AreaTranslation> findByAreaIdAndLanguageIn(Long areaId, List<String> languages);

    List<AreaTranslation> findByLanguageAndTranslatedName(String language, String translatedName);
}
