package com.Uqar.product.repo;

import com.Uqar.product.entity.MasterProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MasterProductRepo extends JpaRepository<MasterProduct, Long> {
    @Query("""
SELECT DISTINCT p FROM MasterProduct p
LEFT JOIN p.translations pt
WHERE (
    LOWER(p.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
    LOWER(p.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
    LOWER(p.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
    (pt.language.code = :lang AND (
        LOWER(pt.tradeName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(pt.scientificName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    ))
)
""")
    Page<MasterProduct> search(
            @Param("keyword") String keyword,
            @Param("lang") String lang,
            Pageable pageable);


    Optional<MasterProduct> findByBarcode(@Param("barcode") String barcode);

    @Query("SELECT DISTINCT p FROM MasterProduct p LEFT JOIN FETCH p.translations tr LEFT JOIN FETCH tr.language LEFT JOIN FETCH p.categories WHERE p.id = :id")
    Optional<MasterProduct> findByIdWithTranslations(@Param("id") Long id);
}
