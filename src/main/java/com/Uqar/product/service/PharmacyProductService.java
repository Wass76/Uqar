package com.Uqar.product.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Uqar.language.Language;
import com.Uqar.language.LanguageRepo;
import com.Uqar.product.Enum.ProductType;
import com.Uqar.product.dto.PaginationDTO;
import com.Uqar.product.dto.PharmacyProductDTORequest;
import com.Uqar.product.dto.PharmacyProductDTOResponse;
import com.Uqar.product.dto.PharmacyProductIdsMaultiLangDTOResponse;
import com.Uqar.product.dto.PharmacyProductListDTO;
import com.Uqar.product.dto.ProductMultiLangDTOResponse;
import com.Uqar.product.entity.PharmacyProduct;
import com.Uqar.product.entity.PharmacyProductTranslation;
import com.Uqar.product.mapper.PharmacyProductMapper;
import com.Uqar.product.repo.MasterProductRepo;
import com.Uqar.product.repo.PharmacyProductBarcodeRepo;
import com.Uqar.product.repo.PharmacyProductRepo;
import com.Uqar.product.repo.PharmacyProductTranslationRepo;
import com.Uqar.product.repo.StockItemRepo;
import com.Uqar.user.entity.Employee;
import com.Uqar.user.entity.User;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.user.service.BaseSecurityService;
import com.Uqar.utils.exception.ConflictException;
import com.Uqar.utils.exception.UnAuthorizedException;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class PharmacyProductService extends BaseSecurityService {

    private final PharmacyProductRepo pharmacyProductRepo;
    private final PharmacyProductBarcodeRepo pharmacyProductBarcodeRepo;
    private final PharmacyProductMapper pharmacyProductMapper;
    private final LanguageRepo languageRepo;
    private final PharmacyProductTranslationRepo pharmacyProductTranslationRepo;
    private final MasterProductRepo masterProductRepo;
    private final StockItemRepo stockItemRepo;

    public PharmacyProductService(PharmacyProductRepo pharmacyProductRepo,
                                PharmacyProductBarcodeRepo pharmacyProductBarcodeRepo,
                                PharmacyProductMapper pharmacyProductMapper,
                                LanguageRepo languageRepo,
                                PharmacyProductTranslationRepo pharmacyProductTranslationRepo,
                                MasterProductRepo masterProductRepo,
                                StockItemRepo stockItemRepo,
                                UserRepository userRepository) {
        super(userRepository);
        this.pharmacyProductRepo = pharmacyProductRepo;
        this.pharmacyProductBarcodeRepo = pharmacyProductBarcodeRepo;
        this.pharmacyProductMapper = pharmacyProductMapper;
        this.languageRepo = languageRepo;
        this.pharmacyProductTranslationRepo = pharmacyProductTranslationRepo;
        this.masterProductRepo = masterProductRepo;
        this.stockItemRepo = stockItemRepo;
    }

//    public Page<PharmacyProductListDTO> getPharmacyProduct(String lang, Pageable pageable) {
//        Long currentPharmacyId = getCurrentUserPharmacyId();
//        return pharmacyProductRepo.findByPharmacyId(currentPharmacyId, pageable).map(
//                product -> pharmacyProductMapper.toListDTO(product, lang)
//        );
//    }

    public PaginationDTO<PharmacyProductListDTO> getPharmacyProductByPharmacyIdPaginated(Long pharmacyId, String lang, int page, int size) {
        // Validate that the user has access to the requested pharmacy
        validatePharmacyAccess(pharmacyId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PharmacyProduct> productPage = pharmacyProductRepo.findByPharmacyId(pharmacyId, pageable);
        
        List<PharmacyProductListDTO> responses = productPage.getContent().stream()
                .map(product -> pharmacyProductMapper.toListDTO(product, lang))
                .collect(Collectors.toList());
        
        return new PaginationDTO<>(responses, page, size, productPage.getTotalElements());
    }
    
    public Page<PharmacyProductListDTO> getPharmacyProductByPharmacyId(Long pharmacyId, String lang, Pageable pageable) {
        // Validate that the user has access to the requested pharmacy
        validatePharmacyAccess(pharmacyId);
        return pharmacyProductRepo.findByPharmacyId(pharmacyId, pageable).map(
                product -> pharmacyProductMapper.toListDTO(product, lang)
        );
    }

    public PaginationDTO<PharmacyProductDTOResponse> getPharmacyProductByPharmacyIdWithTranslationPaginated(Long pharmacyId, String lang, int page, int size) {
        // Validate that the user has access to the requested pharmacy
        validatePharmacyAccess(pharmacyId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PharmacyProduct> productPage = pharmacyProductRepo.findByPharmacyId(pharmacyId, pageable);
        
        List<PharmacyProductDTOResponse> responses = productPage.getContent().stream()
                .map(product -> pharmacyProductMapper.toResponse(product, lang))
                .collect(Collectors.toList());
        
        return new PaginationDTO<>(responses, page, size, productPage.getTotalElements());
    }
    
    public Page<PharmacyProductDTOResponse> getPharmacyProductByPharmacyIdWithTranslation(Long pharmacyId, String lang, Pageable pageable) {
        // Validate that the user has access to the requested pharmacy
        validatePharmacyAccess(pharmacyId);
        return pharmacyProductRepo.findByPharmacyId(pharmacyId, pageable).map(
                product -> pharmacyProductMapper.toResponse(product, lang)
        );
    }

    public PaginationDTO<PharmacyProductListDTO> getPharmacyProductPaginated(String lang, int page, int size) {
        // Validate that the current user is an employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PharmacyProduct> productPage = pharmacyProductRepo.findByPharmacyId(currentPharmacyId, pageable);
        
        List<PharmacyProductListDTO> responses = productPage.getContent().stream()
                .map(product -> pharmacyProductMapper.toListDTO(product, lang))
                .collect(Collectors.toList());
        
        return new PaginationDTO<>(responses, page, size, productPage.getTotalElements());
    }
    
    public Page<PharmacyProductListDTO> getPharmacyProduct(String lang, Pageable pageable) {
        // Validate that the current user is an employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        return pharmacyProductRepo.findByPharmacyId(currentPharmacyId, pageable).map(
                product -> pharmacyProductMapper.toListDTO(product, lang)
        );
    }

    public PharmacyProductDTOResponse getByID(long id, String lang) {
        // Validate that the current user is an employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        PharmacyProduct product = pharmacyProductRepo.findByIdAndPharmacyIdWithTranslations(id, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy Product with ID " + id + " not found"));
        return pharmacyProductMapper.toResponse(product, lang);
    }

    public PharmacyProductDTOResponse insertPharmacyProduct(PharmacyProductDTORequest requestDTO, 
                                                            String lang) {
        // Validate that the current user is an employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can create pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }

        
        Long currentPharmacyId = employee.getPharmacy().getId();
        
        if (requestDTO.getBarcodes() != null && !requestDTO.getBarcodes().isEmpty()) {
            for (String barcode : requestDTO.getBarcodes()) {
                if (pharmacyProductRepo.existsByBarcodeAndPharmacyId(barcode, currentPharmacyId)) {
                    throw new ConflictException("Barcode " + barcode + " already exists in this pharmacy");
                }
                if (masterProductRepo.findByBarcode(barcode).isPresent()) {
                    throw new ConflictException("Barcode " + barcode + " already exists in master products");
                }
            }
        }
        
        PharmacyProduct product = pharmacyProductMapper.toEntity(requestDTO, currentPharmacyId);
        PharmacyProduct saved = pharmacyProductRepo.save(product);

        if (requestDTO.getTranslations() != null && !requestDTO.getTranslations().isEmpty()) {
            List<PharmacyProductTranslation> translations = requestDTO.getTranslations().stream()
                .map(t -> {
                    Language language = languageRepo.findByCode(t.getLang())
                            .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                    return new PharmacyProductTranslation(t.getTradeName(), t.getScientificName(), saved, language);
                })
                .collect(Collectors.toList());

            pharmacyProductTranslationRepo.saveAll(translations);
            saved.setTranslations(new HashSet<>(translations));
        }

        return pharmacyProductMapper.toResponse(saved, lang);
    }
    // Helper method to fetch product with translations
    private PharmacyProduct fetchProductWithTranslations(Long productId, Long pharmacyId) {
        System.out.println("🔍 DEBUG: fetchProductWithTranslations called with productId: " + productId + ", pharmacyId: " + pharmacyId);
        PharmacyProduct product = pharmacyProductRepo.findByIdAndPharmacyIdWithTranslations(productId, pharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy Product with ID " + productId + " not found"));
        System.out.println("🔍 DEBUG: fetchProductWithTranslations returned product with " + 
                          (product.getTranslations() != null ? product.getTranslations().size() : 0) + " translations");
        return product;
    }

    @Transactional(rollbackFor = Exception.class)
    public PharmacyProductDTOResponse editPharmacyProduct(Long id,
                                                         PharmacyProductDTORequest requestDTO,
                                                         String lang) {
        // Validate that the current user is an employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can update pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();

        return pharmacyProductRepo.findByIdAndPharmacyIdWithTranslations(id, currentPharmacyId).map(existing -> {
            if (requestDTO.getBarcodes() != null && !requestDTO.getBarcodes().isEmpty()) {
                for (String barcode : requestDTO.getBarcodes()) {
                    boolean barcodeExistsInOtherProducts = pharmacyProductBarcodeRepo.existsByBarcodeAndProductIdNotAndPharmacyId(barcode, id, currentPharmacyId);
                    if (barcodeExistsInOtherProducts) {
                        throw new ConflictException("Barcode " + barcode + " already exists in another product in this pharmacy");
                    }
                    if (masterProductRepo.findByBarcode(barcode).isPresent()) {
                        throw new ConflictException("Barcode " + barcode + " already exists in master products");
                    }
                }
            }
            
            System.out.println("🔍 DEBUG: Before update - tradeName: " + existing.getTradeName() + ", scientificName: " + existing.getScientificName());
            
            pharmacyProductMapper.updateEntityFromRequest(existing, requestDTO, currentPharmacyId);
            
            System.out.println("🔍 DEBUG: After update - tradeName: " + existing.getTradeName() + ", scientificName: " + existing.getScientificName());
            
            // تحديث timestamp للتأكد من حفظ التغييرات
            existing.setUpdatedAt(java.time.LocalDateTime.now());
            
            PharmacyProduct saved = pharmacyProductRepo.save(existing);
            
            System.out.println("🔍 DEBUG: After save - tradeName: " + saved.getTradeName() + ", scientificName: " + saved.getScientificName());
            
            // Handle translations
            PharmacyProduct updatedProduct = saved;
            if (requestDTO.getTranslations() != null && !requestDTO.getTranslations().isEmpty()) {
                System.out.println("🔍 DEBUG: Processing translations - count: " + requestDTO.getTranslations().size());
                
                // Clear existing translations from the entity
                saved.getTranslations().clear();
                
                // Create new translations
                Set<PharmacyProductTranslation> newTranslations = requestDTO.getTranslations().stream()
                    .map(t -> {
                        System.out.println("🔍 DEBUG: Processing translation - lang: " + t.getLang() + ", tradeName: [" + t.getTradeName() + "], scientificName: [" + t.getScientificName() + "]");
                        System.out.println("🔍 DEBUG: Raw bytes - tradeName: " + (t.getTradeName() != null ? java.util.Arrays.toString(t.getTradeName().getBytes()) : "null"));
                        Language language = languageRepo.findByCode(t.getLang())
                                .orElseThrow(() -> new EntityNotFoundException("Language not found: " + t.getLang()));
                        // استخدام الحقول من الترجمة نفسها
                        String translationTradeName = t.getTradeName() != null ? t.getTradeName() : saved.getTradeName();
                        String translationScientificName = t.getScientificName() != null ? t.getScientificName() : saved.getScientificName();
                        System.out.println("✅ DEBUG: Created translation - tradeName: [" + translationTradeName + "], scientificName: [" + translationScientificName + "]");
                        return new PharmacyProductTranslation(translationTradeName, translationScientificName, saved, language);
                    })
                    .collect(Collectors.toSet());

                // Add new translations to the entity
                saved.getTranslations().addAll(newTranslations);
                
                // Save the entity with updated translations
                updatedProduct = pharmacyProductRepo.save(saved);
                System.out.println("✅ DEBUG: Saved product with " + updatedProduct.getTranslations().size() + " translations");
            } else {
                // تحديث الترجمة الموجودة بالحقول الجديدة إذا لم يتم إرسال ترجمات جديدة
                Set<PharmacyProductTranslation> existingTranslations = saved.getTranslations();
                
                for (PharmacyProductTranslation translation : existingTranslations) {
                    // تحديث الترجمة بالحقول الجديدة من الطلب الرئيسي
                    if (requestDTO.getTradeName() != null) {
                        translation.setTradeName(requestDTO.getTradeName());
                    }
                    if (requestDTO.getScientificName() != null) {
                        translation.setScientificName(requestDTO.getScientificName());
                    }
                }
                
                // Save the entity with updated translations
                updatedProduct = pharmacyProductRepo.save(saved);
                System.out.println("✅ DEBUG: Updated existing translations, total count: " + updatedProduct.getTranslations().size());
            }
            
            // Get the final product (either updated or original saved)
            PharmacyProduct finalProduct = (requestDTO.getTranslations() != null && !requestDTO.getTranslations().isEmpty()) || 
                                          (requestDTO.getTradeName() != null || requestDTO.getScientificName() != null) 
                                          ? updatedProduct : saved;
            
            // Fetch the product again with fresh translations using helper method
            System.out.println("🔍 DEBUG: About to fetch final product with ID: " + finalProduct.getId());
            finalProduct = fetchProductWithTranslations(finalProduct.getId(), currentPharmacyId);
            System.out.println("🔍 DEBUG: Final product fetched successfully");
            
            System.out.println("🔍 DEBUG: Final product translations count: " + (finalProduct.getTranslations() != null ? finalProduct.getTranslations().size() : 0));
            if (finalProduct.getTranslations() != null) {
                finalProduct.getTranslations().forEach(t -> 
                    System.out.println("🔍 DEBUG: Final translation - lang: " + t.getLanguage().getCode() + ", tradeName: " + t.getTradeName())
                );
            }
            
            return pharmacyProductMapper.toResponse(finalProduct, lang);
        }).orElseThrow(() -> new EntityNotFoundException("Pharmacy Product with ID " + id + " not found"));
    }

    public void deletePharmacyProduct(Long id) {
        // Validate that the current user is an employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can delete pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        if(!pharmacyProductRepo.existsByIdAndPharmacyId(id, currentPharmacyId)) {
            throw new EntityNotFoundException("Pharmacy Product with ID " + id + " not found in this pharmacy!") ;
        }
        
        // Check if product has stock items
        Long stockCount = stockItemRepo.countByProductIdAndProductTypeAndPharmacyId(id, ProductType.PHARMACY, currentPharmacyId);
        if (stockCount > 0) {
            throw new ConflictException("Cannot delete pharmacy product. It has " + stockCount + " stock items. Please remove all stock items first.");
        }
        
        pharmacyProductRepo.deleteById(id);
    }

    public List<ProductMultiLangDTOResponse> getPharmacyProductsMultiLang() {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can get pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        List<PharmacyProduct> pharmacyProducts = pharmacyProductRepo.findAllWithTranslations(currentPharmacyId);
        
        return pharmacyProducts.stream()
                .map(pharmacyProductMapper::toMultiLangResponse)
                .collect(Collectors.toList());
    }

    public ProductMultiLangDTOResponse getPharmacyProductByIdMultiLang(Long id) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can get pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        PharmacyProduct product = pharmacyProductRepo.findByIdAndPharmacyIdWithTranslations(id, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy Product with ID " + id + " not found"));
        return pharmacyProductMapper.toMultiLangResponse(product);
    }

    public List<PharmacyProductIdsMaultiLangDTOResponse> getPharmacyProductsWithIdsMultiLang() {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can get pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        List<PharmacyProduct> pharmacyProducts = pharmacyProductRepo.findAllWithTranslations(currentPharmacyId);
        
        return pharmacyProducts.stream()
                .map(pharmacyProductMapper::toPharmacyProductIdsMaultiLangResponse)
                .collect(Collectors.toList());
    }

    public PharmacyProductIdsMaultiLangDTOResponse getPharmacyProductByIdWithIdsMultiLang(Long id) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can get pharmacy products");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        PharmacyProduct product = pharmacyProductRepo.findByIdAndPharmacyIdWithTranslations(id, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Pharmacy Product with ID " + id + " not found"));
        return pharmacyProductMapper.toPharmacyProductIdsMaultiLangResponse(product);
    }



}


