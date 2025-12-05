package com.Uqar.product.repo;

import com.Uqar.product.entity.Type;
import com.Uqar.product.entity.TypeTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeTranslationRepo extends JpaRepository<TypeTranslation, Long> {
    boolean existsByName(String name);
    void deleteByType(Type type);
}
