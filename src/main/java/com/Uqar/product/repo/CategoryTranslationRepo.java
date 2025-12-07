package com.Uqar.product.repo;

import com.Uqar.product.entity.Category;
import com.Uqar.product.entity.CategoryTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryTranslationRepo extends JpaRepository<CategoryTranslation, Long> {
    void deleteByCategory(Category category);
}
