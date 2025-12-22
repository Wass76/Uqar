package com.Uqar.product.mapper;

import java.util.HashSet;
//import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.Uqar.product.Enum.ProductType;
import com.Uqar.product.dto.MProductDTORequest;
import com.Uqar.product.dto.MProductDTOResponse;
import com.Uqar.product.dto.PharmaceuticalProductRequest;
import com.Uqar.product.dto.ProductMultiLangDTOResponse;
//import com.Uqar.product.dto.MProductTranslationDTOResponse;
import com.Uqar.product.entity.Category;
import com.Uqar.product.entity.FormTranslation;
import com.Uqar.product.entity.ManufacturerTranslation;
import com.Uqar.product.entity.MasterProduct;
import com.Uqar.product.entity.MasterProductTranslation;
import com.Uqar.product.entity.TypeTranslation;
import com.Uqar.product.repo.CategoryRepo;
import com.Uqar.product.repo.FormRepo;
import com.Uqar.product.repo.ManufacturerRepo;
import com.Uqar.product.repo.TypeRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MasterProductMapper {

    private final CategoryRepo categoryRepo;
    private final TypeRepo typeRepo;
    private final FormRepo formRepo;
    private final ManufacturerRepo manufacturerRepo;
   // private final MasterProductTranslationMapper translationMapper;

    public MasterProduct toEntity(MProductDTORequest dto) {
        MasterProduct product = new MasterProduct();

        product.setTradeName(dto.getTradeName());
        product.setScientificName(dto.getScientificName());
        product.setConcentration(dto.getConcentration());
        product.setSize(dto.getSize());
        product.setRefPurchasePrice(dto.getRefPurchasePrice());
        product.setRefSellingPrice(dto.getRefSellingPrice());
        product.setNotes(dto.getNotes());
        product.setTax(dto.getTax());
        product.setBarcode(dto.getBarcode());
        product.setRequiresPrescription(dto.getRequiresPrescription());
        product.setNumberOfPartsPerBox(dto.getNumberOfPartsPerBox());

        if (dto.getCategoryIds() != null) {
            product.setCategories(new HashSet<>(categoryRepo.findAllById(dto.getCategoryIds())));
        }

        if (dto.getTypeId() != null)
            product.setType(typeRepo.findById(dto.getTypeId()).orElse(null));

        if (dto.getFormId() != null)
            product.setForm(formRepo.findById(dto.getFormId()).orElse(null));

        if (dto.getManufacturerId() != null)
            product.setManufacturer(manufacturerRepo.findById(dto.getManufacturerId()).orElse(null));

        return product;
    }

    public MasterProduct toEntity(PharmaceuticalProductRequest dto) {
        MasterProduct product = new MasterProduct();

        product.setTradeName(dto.getTradeName());
        product.setScientificName(dto.getScientificName());
        product.setConcentration(dto.getConcentration());
        product.setSize(dto.getSize());
        product.setRefPurchasePrice(dto.getRefPurchasePrice());
        product.setRefSellingPrice(dto.getRefSellingPrice());
        product.setNotes(dto.getNotes());
        product.setTax(dto.getTax());
        product.setBarcode(dto.getBarcode());
        product.setRequiresPrescription(false);

        if (dto.getFormId() != null)
            product.setForm(formRepo.findById(dto.getFormId()).orElse(null));

        if (dto.getManufacturerId() != null)
            product.setManufacturer(manufacturerRepo.findById(dto.getManufacturerId()).orElse(null));

        return product;
    }

    public MProductDTOResponse toResponse(MasterProduct product, String lang) {
        MasterProductTranslation translation = product.getTranslations() != null
                ? product.getTranslations().stream()
                .filter(t -> lang.equalsIgnoreCase(t.getLanguage().getCode()))
                .findFirst()
                .orElse(null)
                : null;

        // Set<MProductTranslationDTOResponse> allTranslations = product.getTranslations() != null 
        //     ? product.getTranslations().stream()
        //         .map(translationMapper::toResponse)
        //         .collect(Collectors.toSet())
        //     : new HashSet<>();

    
        return MProductDTOResponse.builder()
                .id(product.getId())
                .tradeName(translation != null ? translation.getTradeName() : product.getTradeName())
                .scientificName(translation != null ? translation.getScientificName() : product.getScientificName())
                .concentration(product.getConcentration())
                .size(product.getSize())
                .refPurchasePrice(product.getRefPurchasePrice())
                .refSellingPrice(product.getRefSellingPrice())
                .minStockLevel(product.getMinStockLevel())
                .notes(product.getNotes())
                .tax(product.getTax())
                .barcode(product.getBarcode())
                .productTypeName(ProductType.MASTER.getTranslatedName(lang))
                .requiresPrescription(product.getRequiresPrescription())
                .numberOfPartsPerBox(product.getNumberOfPartsPerBox())
                .typeId(product.getType() != null ? product.getType().getId() : null)
                .formId(product.getForm() != null ? product.getForm().getId() : null)
                .manufacturerId(product.getManufacturer() != null ? product.getManufacturer().getId() : null)
                .categoryIds(product.getCategories() != null ? product.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet()) : new HashSet<>())

                .categories(
                    product.getCategories() != null ? product.getCategories().stream()
                            .map(category -> {
                                if (category.getTranslations() == null) return category.getName();
                                return category.getTranslations().stream()
                                        .filter(t -> lang.equalsIgnoreCase(t.getLanguage().getCode()))
                                        .findFirst()
                                        .map(com.Uqar.product.entity.CategoryTranslation::getName)
                                        .orElse(category.getName());
                            })
                            .collect(Collectors.toSet()) : new HashSet<>()
            )

                .type(
                        product.getType() != null
                                ? product.getType().getTranslations().stream()
                                .filter(t -> lang.equalsIgnoreCase(t.getLanguage().getCode()))
                                .findFirst()
                                .map(TypeTranslation::getName)
                                .orElse(product.getType().getName())
                                : null
                )

                .form(
                        product.getForm() != null
                                ? product.getForm().getTranslations().stream()
                                .filter(t -> lang.equalsIgnoreCase(t.getLanguage().getCode()))
                                .findFirst()
                                .map(FormTranslation::getName)
                                .orElse(product.getForm().getName())
                                : null
                )

                .manufacturer(
                        product.getManufacturer() != null
                                ? product.getManufacturer().getTranslations().stream()
                                .filter(t -> lang.equalsIgnoreCase(t.getLanguage().getCode()))
                                .findFirst()
                                .map(ManufacturerTranslation::getName)
                                .orElse(product.getManufacturer().getName())
                                : null
                )

                .build();
    }

    public MasterProduct updateRequestToEntity(MProductDTORequest dto) {
        MasterProduct product = new MasterProduct();

        if(dto.getTradeName() != null){
            product.setTradeName(dto.getTradeName());
        }

        if (dto.getScientificName()!= null){
            product.setScientificName(dto.getScientificName());
        }
        if (dto.getScientificName()!= null){
            product.setConcentration(dto.getConcentration());
        }
        if (dto.getScientificName()!= null){
            product.setSize(dto.getSize());
        }
        if (dto.getScientificName()!= null){
            product.setRefPurchasePrice(dto.getRefPurchasePrice());
        }
        if (dto.getScientificName()!= null){
            product.setRefSellingPrice(dto.getRefSellingPrice());
        }

        if (dto.getScientificName()!= null){
            product.setNotes(dto.getNotes());
        }
        if (dto.getScientificName()!= null){
            product.setTax(dto.getTax());
        }
        if (dto.getScientificName()!= null){
            product.setBarcode(dto.getBarcode());
        }
        if (dto.getScientificName()!= null){
            product.setRequiresPrescription(dto.getRequiresPrescription());
        }

        if (dto.getCategoryIds() != null) {
            product.setCategories(new HashSet<>(categoryRepo.findAllById(dto.getCategoryIds())));
        }

        if (dto.getTypeId() != null)
            product.setType(typeRepo.findById(dto.getTypeId()).orElse(null));

        if (dto.getFormId() != null)
            product.setForm(formRepo.findById(dto.getFormId()).orElse(null));

        if (dto.getManufacturerId() != null)
            product.setManufacturer(manufacturerRepo.findById(dto.getManufacturerId()).orElse(null));

        return product;
    }

    public ProductMultiLangDTOResponse toMultiLangResponse(MasterProduct product) {
        if (product == null) return null;

        // استخراج الاسم التجاري باللغتين
        String tradeNameAr = product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "ar".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(MasterProductTranslation::getTradeName)
                .findFirst()
                .orElse(product.getTradeName());

        String tradeNameEn = product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "en".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(MasterProductTranslation::getTradeName)
                .findFirst()
                .orElse(product.getTradeName());

        // استخراج الاسم العلمي باللغتين
        String scientificNameAr = product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "ar".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(MasterProductTranslation::getScientificName)
                .findFirst()
                .orElse(product.getScientificName());

        String scientificNameEn = product.getTranslations().stream()
                .filter(t -> t.getLanguage() != null && "en".equalsIgnoreCase(t.getLanguage().getCode()))
                .map(MasterProductTranslation::getScientificName)
                .findFirst()
                .orElse(product.getScientificName());

        return ProductMultiLangDTOResponse.builder()
                .id(product.getId())
                .tradeNameAr(tradeNameAr)
                .tradeNameEn(tradeNameEn)
                .scientificNameAr(scientificNameAr)
                .scientificNameEn(scientificNameEn)
                .build();
    }

}
