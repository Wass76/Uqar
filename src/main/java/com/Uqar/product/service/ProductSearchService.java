package com.Uqar.product.service;

import com.Uqar.product.dto.ProductSearchDTOResponse;
import com.Uqar.product.entity.MasterProduct;
import com.Uqar.product.entity.PharmacyProduct;
import com.Uqar.product.mapper.ProductSearchMapper;
import com.Uqar.product.repo.MasterProductRepo;
import com.Uqar.product.repo.PharmacyProductRepo;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.user.service.BaseSecurityService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.Uqar.product.dto.PaginationDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSearchService extends BaseSecurityService {

    private final MasterProductRepo masterProductRepo;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final ProductSearchMapper productSearchMapper;

    protected ProductSearchService(UserRepository userRepository, 
                                 MasterProductRepo masterProductRepo, 
                                 PharmacyProductRepo pharmacyProductRepo,
                                 ProductSearchMapper productSearchMapper) {
        super(userRepository);
        this.masterProductRepo = masterProductRepo;
        this.pharmacyProductRepo = pharmacyProductRepo;
        this.productSearchMapper = productSearchMapper;
    }

    public PaginationDTO<ProductSearchDTOResponse> searchProductsPaginated(String keyword, String lang, int page, int size) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MasterProduct> masterProductsPage = masterProductRepo.search(keyword, lang, pageable);
        Page<PharmacyProduct> pharmacyProductsPage = pharmacyProductRepo.searchByPharmacyId(keyword, lang, currentPharmacyId, pageable);
        
        List<ProductSearchDTOResponse> masterResults = masterProductsPage.getContent().stream()
                .map(product -> productSearchMapper.convertMasterProductToUnifiedDTO(product, lang, currentPharmacyId))
                .collect(Collectors.toList());
                
        List<ProductSearchDTOResponse> pharmacyResults = pharmacyProductsPage.getContent().stream()
                .map(product -> productSearchMapper.convertPharmacyProductToUnifiedDTO(product, lang, currentPharmacyId))
                .collect(Collectors.toList());
        
        List<ProductSearchDTOResponse> combinedResults = new ArrayList<>();
        combinedResults.addAll(masterResults);
        combinedResults.addAll(pharmacyResults);
        
        long totalElements = masterProductsPage.getTotalElements() + pharmacyProductsPage.getTotalElements();
        
        return new PaginationDTO<>(combinedResults, page, size, totalElements);
    }
    
    public Page<ProductSearchDTOResponse> searchProducts(String keyword, String lang, Pageable pageable) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        Page<MasterProduct> masterProductsPage = masterProductRepo.search(keyword, lang, pageable);
        Page<PharmacyProduct> pharmacyProductsPage = pharmacyProductRepo.searchByPharmacyId(keyword, lang, currentPharmacyId, pageable);
        
        List<ProductSearchDTOResponse> masterResults = masterProductsPage.getContent().stream()
                .map(product -> productSearchMapper.convertMasterProductToUnifiedDTO(product, lang, currentPharmacyId))
                .collect(Collectors.toList());
                
        List<ProductSearchDTOResponse> pharmacyResults = pharmacyProductsPage.getContent().stream()
                .map(product -> productSearchMapper.convertPharmacyProductToUnifiedDTO(product, lang, currentPharmacyId))
                .collect(Collectors.toList());
        
        List<ProductSearchDTOResponse> combinedResults = new ArrayList<>();
        combinedResults.addAll(masterResults);
        combinedResults.addAll(pharmacyResults);
        
        return new org.springframework.data.domain.PageImpl<>(
            combinedResults,
            pageable,
            masterProductsPage.getTotalElements() + pharmacyProductsPage.getTotalElements()
        );
    }

    public PaginationDTO<ProductSearchDTOResponse> getAllProductsPaginated(String lang, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductSearchDTOResponse> productPage = searchProducts("", lang, pageable);
        
        return new PaginationDTO<>(
            productPage.getContent(),
            page,
            size,
            productPage.getTotalElements()
        );
    }
    
    public Page<ProductSearchDTOResponse> getAllProducts(String lang, Pageable pageable) {
        return searchProducts("", lang, pageable);
    }


} 