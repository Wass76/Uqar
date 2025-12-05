package com.Uqar.product.repo;

import com.Uqar.product.entity.Form;
import com.Uqar.product.entity.FormTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormTranslationRepo extends JpaRepository<FormTranslation, Long> {
    void deleteByForm(Form form);
}
