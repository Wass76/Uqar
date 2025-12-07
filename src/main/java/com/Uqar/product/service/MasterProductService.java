package com.Uqar.product.service;

import com.Uqar.product.dto.*;
import com.Uqar.product.Enum.ProductType;
import com.Uqar.product.repo.StockItemRepo;
import com.Uqar.product.entity.MasterProduct;
import com.Uqar.product.entity.MasterProductTranslation;
import com.Uqar.product.mapper.MasterProductMapper;
import com.Uqar.product.repo.MasterProductRepo;
import com.Uqar.product.repo.MasterProductTranslationRepo;
import com.Uqar.product.repo.PharmacyProductRepo;
import com.Uqar.utils.exception.ConflictException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Uqar.language.LanguageRepo;
import com.Uqar.language.Language;
import com.Uqar.user.service.BaseSecurityService;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.user.entity.Employee;
import com.Uqar.utils.exception.UnAuthorizedException;
import com.Uqar.user.entity.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Transactional
public class MasterProductService extends BaseSecurityService {

    private final MasterProductRepo masterProductRepo;
    private final MasterProductMapper masterProductMapper;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final MasterProductTranslationRepo masterProductTranslationRepo;
    private final LanguageRepo languageRepo;
    private final StockItemRepo stockItemRepo;

    public MasterProductService(MasterProductRepo masterProductRepo,
                               MasterProductMapper masterProductMapper,
                               PharmacyProductRepo pharmacyProductRepo,
                               MasterProductTranslationRepo masterProductTranslationRepo,
                               LanguageRepo languageRepo,
                               StockItemRepo stockItemRepo,
                               UserRepository userRepository) {
        super(userRepository);
        this.masterProductRepo = masterProductRepo;
        this.masterProductMapper = masterProductMapper;
        this.pharmacyProductRepo = pharmacyProductRepo;
        this.masterProductTranslationRepo = masterProductTranslationRepo;
        this.languageRepo = languageRepo;
        this.stockItemRepo = stockItemRepo;
    }


    public PaginationDTO<MProductDTOResponse> getMasterProductPaginated(String lang, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MasterProduct> productPage = masterProductRepo.findAll(pageable);
        
        List<MProductDTOResponse> responses = productPage.getContent().stream()
                .map(product -> masterProductMapper.toResponse(product, lang))
                .collect(Collectors.toList());
        
        return new PaginationDTO<>(responses, page, size, productPage.getTotalElements());
    }
    
    public Page<MProductDTOResponse> getMasterProduct(String lang , Pageable pageable) {
        return masterProductRepo.findAll(pageable).map(
                product -> masterProductMapper.toResponse(product, lang)
        );
    }

    public MProductDTOResponse getByID(long id, String lang) {
        MasterProduct product = masterProductRepo.findByIdWithTranslations(id)
                .orElseThrow(() -> new EntityNotFoundException("Master Product with ID " + id + " not found"));
        return masterProductMapper.toResponse(product, lang);
    }


    public PaginationDTO<MProductDTOResponse> searchPaginated(SearchDTORequest requestDTO, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MasterProduct> productPage = masterProductRepo.search(
                requestDTO.getKeyword(),
                requestDTO.getLang(),
                pageable
        );
        
        List<MProductDTOResponse> responses = productPage.getContent().stream()
                .map(product -> masterProductMapper.toResponse(product, requestDTO.getLang()))
                .collect(Collectors.toList());
        
        return new PaginationDTO<>(responses, page, size, productPage.getTotalElements());
    }
    
    public Page<MProductDTOResponse> search(SearchDTORequest requestDTO , Pageable pageable) {
        Page<MasterProduct> products = masterProductRepo.search(
                requestDTO.getKeyword(),
                requestDTO.getLang(),
                pageable
        );
        return products.map(product -> masterProductMapper.toResponse(product, requestDTO.getLang()));
    }

    public MProductDTOResponse insertMasterProduct(MProductDTORequest requestDTO, String lang) {
        if(masterProductRepo.findByBarcode(requestDTO.getBarcode()).isPresent()) {
            throw new ConflictException("Barcode already exists");
        }
        MasterProduct product = masterProductMapper.toEntity(requestDTO);
        MasterProduct saved = masterProductRepo.save(product);

        // حفظ الترجمات إذا وجدت
        if (requestDTO.getTranslations() != null && !requestDTO.getTranslations().isEmpty()) {
            Set<MasterProductTranslation> translations = requestDTO.getTranslations().stream()
                .map(t -> {
                    Language language = null;
                    if (t.getLang() != null) {
                        language = languageRepo.findByCode(t.getLang())
                                .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                    } 
                    MasterProductTranslation translation = new MasterProductTranslation();
                    translation.setTradeName(t.getTradeName());
                    translation.setScientificName(t.getScientificName());
                    translation.setProduct(saved);
                    translation.setLanguage(language);
                    return translation;
                })
                .collect(java.util.stream.Collectors.toSet());
            masterProductTranslationRepo.saveAll(translations);
            saved.setTranslations(translations);
        }

        return masterProductMapper.toResponse(saved, lang);
    }

    public MProductDTOResponse insertMasterProduct(PharmaceuticalProductRequest requestDTO, String lang) {
        if(masterProductRepo.findByBarcode(requestDTO.getBarcode()).isPresent()) {
            throw new ConflictException("Barcode already exists");
        }
        MasterProduct product = masterProductMapper.toEntity(requestDTO);
        MasterProduct saved = masterProductRepo.save(product);

        // حفظ الترجمات إذا وجدت
        if (requestDTO.getTranslations() != null && !requestDTO.getTranslations().isEmpty()) {
            Set<MasterProductTranslation> translations = requestDTO.getTranslations().stream()
                    .map(t -> {
                        Language language = null;
                        if (t.getLang() != null) {
                            language = languageRepo.findByCode(t.getLang())
                                    .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                        }
                        MasterProductTranslation translation = new MasterProductTranslation();
                        translation.setTradeName(t.getTradeName());
                        translation.setScientificName(t.getScientificName());
                        translation.setProduct(saved);
                        translation.setLanguage(language);
                        return translation;
                    })
                    .collect(java.util.stream.Collectors.toSet());
            masterProductTranslationRepo.saveAll(translations);
            saved.setTranslations(translations);
        }

        return masterProductMapper.toResponse(saved, lang);
    }


    public MProductDTOResponse editMasterProduct(Long id, MProductDTORequest requestDTO, String lang) {
        return masterProductRepo.findByIdWithTranslations(id).map(existing -> {
            MasterProduct updated = masterProductMapper.updateRequestToEntity(requestDTO);
            updated.setId(existing.getId());
            MasterProduct saved = masterProductRepo.save(updated);
            return masterProductMapper.toResponse(saved, lang);
        }).orElseThrow(() -> new EntityNotFoundException("Master Product with ID " + id + " not found"));
    }

    public void deleteMasterProduct(Long id) {
        if(!masterProductRepo.existsById(id)) {
            throw new EntityNotFoundException("Master Product with ID " + id + " not found!") ;
        }
        
        MasterProduct masterProduct = masterProductRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Master Product with ID " + id + " not found!"));
        
        // Check if there are pharmacy products using the same barcode
        if (pharmacyProductRepo.existsByBarcode(masterProduct.getBarcode())) {
            throw new ConflictException("Cannot delete master product. There are pharmacy products using the same barcode: " + masterProduct.getBarcode());
        }
        
        // Check if there are stock items for this master product in any pharmacy
        if (stockItemRepo.existsByProductIdAndProductType(id, ProductType.MASTER)) {
            throw new ConflictException("Cannot delete master product. It has stock items in pharmacies. Please remove all stock items first.");
        }
        
        masterProductRepo.deleteById(id);
    }

    public List<ProductMultiLangDTOResponse> getMasterProductsMultiLang() {
        List<MasterProduct> masterProducts = masterProductRepo.findAll();
        
        return masterProducts.stream()
                .map(masterProductMapper::toMultiLangResponse)
                .collect(Collectors.toList());
    }

    public ProductMultiLangDTOResponse getMasterProductByIdMultiLang(Long id) {
        MasterProduct product = masterProductRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Master Product with ID " + id + " not found"));
        return masterProductMapper.toMultiLangResponse(product);
    }

    
        public MProductDTOResponse updateMasterProductMinStockLevel(Long id, Integer minStockLevel, String lang) {
       User currentUser = getCurrentUser();
       if (!(currentUser instanceof Employee)) {
        throw new UnAuthorizedException("Only pharmacy employees can update master products");
       }

        MasterProduct product = masterProductRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Master Product with ID " + id + " not found"));

        if (minStockLevel != null && minStockLevel < 0) {
            throw new ConflictException("Minimum stock level must be greater than or equal to 0");
        }

        product.setMinStockLevel(minStockLevel);
        product.setLastModifiedBy(currentUser.getId());
        product.setUpdatedAt(java.time.LocalDateTime.now());

        MasterProduct saved = masterProductRepo.save(product);
        return masterProductMapper.toResponse(saved, lang);
    }
    
    public void updateMasterProductMinStockLevelFromPurchaseInvoice(Long masterProductId, Integer minStockLevel) {
        MasterProduct product = masterProductRepo.findById(masterProductId)
                .orElseThrow(() -> new EntityNotFoundException("Master Product with ID " + masterProductId + " not found"));

        if (minStockLevel != null && minStockLevel < 0) {
            throw new ConflictException("Minimum stock level must be greater than or equal to 0");
        }

        product.setMinStockLevel(minStockLevel);
        product.setUpdatedAt(java.time.LocalDateTime.now());
        
        masterProductRepo.save(product);
    }

    /**
     * Insert multiple master products in bulk
     * إدراج عدة منتجات رئيسية بشكل مجمع
     * Uses the existing insertMasterProduct method to prevent code duplication
     */
    public List<MProductDTOResponse> insertMasterProductsBulk(List<MProductDTORequest> requestDTOs, String lang) {
        if (requestDTOs == null || requestDTOs.isEmpty()) {
            throw new ConflictException("Product list cannot be empty");
        }

        List<MProductDTOResponse> responses = new java.util.ArrayList<>();
        List<String> errors = new java.util.ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        // Process each product individually using the existing insertMasterProduct method
        for (int i = 0; i < requestDTOs.size(); i++) {
            MProductDTORequest requestDTO = requestDTOs.get(i);
            try {
                // Use the existing insertMasterProduct method to ensure consistency
                MProductDTOResponse response = insertMasterProduct(requestDTO, lang);
                responses.add(response);
                successCount++;
                
            } catch (Exception e) {
                String error = String.format("Product %d (%s): %s", 
                    i + 1, requestDTO.getTradeName(), e.getMessage());
                errors.add(error);
                failureCount++;
            }
        }

        // Log errors for debugging if there were any failures
        if (failureCount > 0) {
            System.err.println("Bulk insert completed with errors:");
            errors.forEach(System.err::println);
        }

        return responses;
    }

}
