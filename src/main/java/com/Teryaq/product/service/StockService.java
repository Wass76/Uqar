package com.Teryaq.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Teryaq.moneybox.service.ExchangeRateService;
import com.Teryaq.product.Enum.ProductType;
import com.Teryaq.product.dto.InventoryAdjustmentRequest;
import com.Teryaq.product.dto.StockItemDTOResponse;
import com.Teryaq.product.dto.StockItemDetailDTOResponse;
import com.Teryaq.product.dto.StockProductOverallDTOResponse;
import com.Teryaq.product.entity.MasterProduct;
import com.Teryaq.product.entity.PharmacyProduct;
import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.mapper.StockItemMapper;
import com.Teryaq.product.repo.MasterProductRepo;
import com.Teryaq.product.repo.PharmacyProductRepo;
import com.Teryaq.product.repo.StockItemRepo;
import com.Teryaq.user.Enum.Currency;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.entity.User;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.user.service.BaseSecurityService;
import com.Teryaq.utils.exception.ConflictException;
import com.Teryaq.utils.exception.ResourceNotFoundException;
import com.Teryaq.utils.exception.UnAuthorizedException;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class StockService extends BaseSecurityService {
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private final StockItemRepo stockItemRepo;
    private final StockItemMapper stockItemMapper;
    private final MasterProductRepo masterProductRepo;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final ExchangeRateService exchangeRateService;

    public StockService(StockItemRepo stockItemRepo,
                                @Lazy StockItemMapper stockItemMapper,
                                UserRepository userRepository,
                                MasterProductRepo masterProductRepo,
                                PharmacyProductRepo pharmacyProductRepo,
                                ExchangeRateService exchangeRateService) {
        super(userRepository);
        this.stockItemRepo = stockItemRepo;
        this.stockItemMapper = stockItemMapper;
        this.masterProductRepo = masterProductRepo;
        this.pharmacyProductRepo = pharmacyProductRepo;
        this.exchangeRateService = exchangeRateService;
    }

    public StockItemDTOResponse editStockQuantity(Long stockItemId, Integer newQuantity, 
                                       String reasonCode, String additionalNotes) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("only pharmacy employees can edit the stock");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("the employee is not associated with any pharmacy");
        }
        

        StockItem stockItem = stockItemRepo.findById(stockItemId)
            .orElseThrow(() -> new EntityNotFoundException("stock item not found"));
        
        if (!stockItem.getPharmacy().getId().equals(employee.getPharmacy().getId())) {
            throw new UnAuthorizedException("you can't edit stock of another pharmacy");
        }
        
        if (newQuantity < 0) {
            throw new IllegalArgumentException("the quantity can't be negative");
        }
        
        stockItem.setQuantity(newQuantity);
        
        if (newQuantity == 0) {
            stockItemRepo.delete(stockItem);
            return null;
        }
        
        stockItem.setLastModifiedBy(currentUser.getId());
        stockItem.setUpdatedAt(LocalDateTime.now());
        
        StockItem savedStockItem = stockItemRepo.save(stockItem);
        
        StockItemDTOResponse response = stockItemMapper.toResponse(savedStockItem);
        
        response.setPharmacyId(savedStockItem.getPharmacy().getId());
        
        if (savedStockItem.getPurchaseInvoice() != null) {
            response.setPurchaseInvoiceNumber(savedStockItem.getPurchaseInvoice().getInvoiceNumber());
        }
        
        return response;
    }
    
    public StockItemDTOResponse editStockQuantityAndExpiryDate(Long stockItemId, Integer newQuantity, 
                                                              LocalDate newExpiryDate, Integer newMinStockLevel, 
                                                              String reasonCode, String additionalNotes) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("only pharmacy employees can edit the stock");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("the employee is not associated with any pharmacy");
        }
        
        StockItem stockItem = stockItemRepo.findById(stockItemId)
            .orElseThrow(() -> new EntityNotFoundException("stock item not found"));
        
        if (!stockItem.getPharmacy().getId().equals(employee.getPharmacy().getId())) {
            throw new UnAuthorizedException("you can't edit stock of another pharmacy");
        }
        
        if (newQuantity != null && newQuantity < 0) {
            throw new ConflictException("the quantity can't be negative");
        }
        
        if (newExpiryDate != null && newExpiryDate.isBefore(LocalDate.now())) {
            throw new ConflictException("expiry date cannot be in the past");
        }
        
        if (newMinStockLevel != null && newMinStockLevel < 0) {
            throw new ConflictException("minimum stock level cannot be negative");
        }
        
        if (newQuantity != null) {
            stockItem.setQuantity(newQuantity);
            
            if (newQuantity == 0) {
                stockItemRepo.delete(stockItem);
                return null;
            }
        }
        
        if (newExpiryDate != null) {
            stockItem.setExpiryDate(newExpiryDate);
        }
        
        if (newMinStockLevel != null) {
            stockItem.setMinStockLevel(newMinStockLevel);
        }
        
        stockItem.setLastModifiedBy(currentUser.getId());
        stockItem.setUpdatedAt(LocalDateTime.now());
        
        StockItem savedStockItem = stockItemRepo.save(stockItem);
        
        StockItemDTOResponse response = stockItemMapper.toResponse(savedStockItem);
        
        response.setPharmacyId(savedStockItem.getPharmacy().getId());
        
        if (savedStockItem.getPurchaseInvoice() != null) {
            response.setPurchaseInvoiceNumber(savedStockItem.getPurchaseInvoice().getInvoiceNumber());
        }
        
        return response;
    }
    
    public List<StockProductOverallDTOResponse> stockItemSearch(String keyword) {
        return stockItemSearch(keyword, "en"); // Default to English
    }
    
    public List<StockProductOverallDTOResponse> stockItemSearch(String keyword, String lang) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<StockItem> matchingStockItems = stockItemRepo.searchStockItems(keyword, currentPharmacyId);
        
        if (matchingStockItems.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String, List<StockItem>> groupedByProduct = matchingStockItems.stream()
            .collect(Collectors.groupingBy(item -> 
                item.getProductId() + "_" + item.getProductType()));
        
        return groupedByProduct.values().stream()
            .map(stockItems -> {
                if (stockItems.isEmpty()) return null;
                
                StockItem firstItem = stockItems.get(0);
                Long productId = firstItem.getProductId();
                ProductType productType = firstItem.getProductType();
                
                return stockItemMapper.toProductSummaryWithLang(productId, productType, stockItems, currentPharmacyId, true, lang);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    public List<StockItem> getExpiredItems() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        return stockItemRepo.findExpiredItems(LocalDate.now(), currentPharmacyId);
    }
   
    public List<StockItem> getItemsExpiringSoon() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        return stockItemRepo.findItemsExpiringSoon(today, thirtyDaysFromNow, currentPharmacyId);
    }
    
    public Map<String, Object> getStockReportByProductType(ProductType productType) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<StockItem> stockItems = stockItemRepo.findByProductTypeAndPharmacyId(productType, currentPharmacyId);
        
        Map<String, Object> report = new HashMap<>();
        report.put("productType", productType);
        report.put("totalItems", stockItems.size());
        report.put("totalQuantity", stockItems.stream().mapToInt(StockItem::getQuantity).sum());
        report.put("totalValue", stockItems.stream()
            .mapToDouble(item -> item.getQuantity() * item.getActualPurchasePrice()).sum());
        
        long expiredCount = stockItems.stream()
            .filter(item -> item.getExpiryDate() != null && item.getExpiryDate().isBefore(LocalDate.now()))
            .count();
        report.put("expiredItems", expiredCount);
        
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        long expiringSoonCount = stockItems.stream()
            .filter(item -> item.getExpiryDate() != null && 
                item.getExpiryDate().isAfter(LocalDate.now()) && 
                item.getExpiryDate().isBefore(thirtyDaysFromNow))
            .count();
        report.put("expiringSoonItems", expiringSoonCount);
        
        return report;
    }
    

 
    public Map<String, Object> getComprehensiveStockReport() {
        Map<String, Object> report = new HashMap<>();
        
        report.put("pharmacyProducts", getStockReportByProductType(ProductType.PHARMACY));
        
        report.put("masterProducts", getStockReportByProductType(ProductType.MASTER));
        
        report.put("expiredItems", getExpiredItems());
        
        report.put("expiringSoonItems", getItemsExpiringSoon());
        
        return report;
    }
    
  

    public List<StockItemDTOResponse> getAllStockItems() {
        return getAllStockItems(null);
    }
    
    public List<StockItemDTOResponse> getAllStockItems(Currency currency) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<StockItem> stockItems = stockItemRepo.findByPharmacyId(currentPharmacyId);
        return stockItems.stream()
            .map(stockItem -> stockItemMapper.toResponse(stockItem, currency))
            .collect(Collectors.toList());
    }
    
    public List<StockProductOverallDTOResponse> getAllStockProductsOverall() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<Object[]> uniqueProducts = stockItemRepo.findUniqueProductsCombined(currentPharmacyId);
        
        return uniqueProducts.stream()
            .map(productData -> {
                Long productId = (Long) productData[0];
                ProductType productType = (ProductType) productData[1];
                
                List<StockItem> stockItems = stockItemRepo.findByProductIdAndProductTypeAndPharmacyId(productId, productType, currentPharmacyId);
                
                return stockItemMapper.toProductSummary(productId, productType, stockItems, currentPharmacyId, true); 
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }



    public boolean isQuantityAvailable(Long productId, Integer requiredQuantity, ProductType productType) {
        Integer availableQuantity = stockItemRepo.getTotalQuantity(productId, getCurrentUserPharmacyId(), productType);
        return availableQuantity >= requiredQuantity;
    }
    
    
    public Map<String, Object> getProductStockDetails(Long productId, ProductType productType) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<StockItem> stockItems = stockItemRepo.findByProductIdAndProductTypeAndPharmacyId(productId, productType, currentPharmacyId);
        StockItem stockItem = stockItemRepo.findByProductIdAndProductTypeOrderByDateAddedDesc(productId, productType).get(0);

        Map<String, Object> details = new HashMap<>();
        details.put("productId", productId);
        details.put("productType", productType);
        details.put("totalQuantity", stockItems.stream().mapToInt(StockItem::getQuantity).sum());
        details.put("stockItems", stockItems.stream()
            .map(item -> stockItemMapper.toResponse(item, Currency.USD)) // Always use USD for dual currency
            .collect(Collectors.toList()));
        details.put("minStockLevel", stockItemMapper.getMinStockLevel(stockItem.getProductId(), stockItem.getProductType()));
        
        return details;
    }
    

    
    public StockItemDetailDTOResponse getStockItemDetail(Long stockItemId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        StockItem stockItem = stockItemRepo.findById(stockItemId)
            .orElseThrow(() -> new EntityNotFoundException("Stock item not found"));
        
        if (!stockItem.getPharmacy().getId().equals(currentPharmacyId)) {
            throw new UnAuthorizedException("You can't access stock item from another pharmacy");
        }
        
        return stockItemMapper.toDetailResponse(stockItem);
    }
        
    public Map<String, Object> getStockSummary() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<StockItem> stockItems = stockItemRepo.findByPharmacyId(currentPharmacyId);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProducts", stockItems.size());
        summary.put("totalQuantity", stockItems.stream().mapToInt(StockItem::getQuantity).sum());
        summary.put("expiredProducts", getExpiredItems().size());
        summary.put("expiringSoonProducts", getItemsExpiringSoon().size());
        summary.put("totalValue", stockItems.stream()
            .mapToDouble(item -> item.getQuantity() * item.getActualPurchasePrice()).sum());
        
        return summary;
    }
    
    public Map<String, Object> getStockValue() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<StockItem> stockItems = stockItemRepo.findByPharmacyId(currentPharmacyId);
        
        Map<String, Object> stockValue = new HashMap<>();
        double totalPurchaseValue = stockItems.stream()
            .mapToDouble(item -> item.getQuantity() * item.getActualPurchasePrice()).sum();
        double totalSellingValue = stockItems.stream()
            .mapToDouble(item -> item.getQuantity() * stockItemMapper.getProductSellingPrice(item.getProductId(), item.getProductType()))
            .sum();
        
        stockValue.put("totalPurchaseValue", totalPurchaseValue);
        stockValue.put("totalSellingValue", totalSellingValue);
        stockValue.put("potentialProfit", totalSellingValue - totalPurchaseValue);
        stockValue.put("profitMargin", totalPurchaseValue > 0 ? ((totalSellingValue - totalPurchaseValue) / totalPurchaseValue) * 100 : 0);
        
        return stockValue;
    }

    public StockItemDTOResponse deleteStockItem(Long stockItemId) {
        StockItem stockItem = stockItemRepo.findById(stockItemId)
            .orElseThrow(() -> new EntityNotFoundException("stock item not found"));
        
        stockItemRepo.delete(stockItem);
        return stockItemMapper.toResponse(stockItem);
    }

    /**
     * إضافة مخزون بدون فاتورة شراء
     * Add stock items without purchase invoice
     */
    @Transactional
    public StockItemDTOResponse addStockWithoutInvoice(InventoryAdjustmentRequest request) {
        // 1. التحقق من الصلاحيات
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can add stock items");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Pharmacy pharmacy = employee.getPharmacy();
        
        // 2. التحقق من وجود المنتج والحصول على معلوماته
        Object product = getProductForAdjustment(request.getProductId(), request.getProductType(), pharmacy.getId());
        
        // 3. التحقق من تاريخ الصلاحية (إن وجد)
        if (request.getExpiryDate() != null) {
            validateExpiryDate(request.getExpiryDate());
        }
        
        // 4. تحديد العملة (افتراضي: SYP)
        Currency requestCurrency = request.getCurrency() != null ? request.getCurrency() : Currency.SYP;
        
        // 5. تحديد سعر الشراء حسب نوع المنتج (بالعملة الأصلية)
        Double actualPurchasePrice = determinePurchasePrice(request, product);
        
        // 6. تحويل سعر الشراء من العملة المحددة إلى SYP قبل الحفظ
        Double actualPurchasePriceInSYP = convertPriceToSYP(actualPurchasePrice, requestCurrency);
        
        if (requestCurrency != Currency.SYP) {
            logger.info("Converted purchase price from {} {} to {} SYP for product {}", 
                       actualPurchasePrice, requestCurrency, actualPurchasePriceInSYP, request.getProductId());
        }
        
        // 7. إنشاء StockItem جديد
        StockItem stockItem = new StockItem();
        
        // المعلومات الأساسية
        stockItem.setProductId(request.getProductId());
        stockItem.setProductType(request.getProductType());
        stockItem.setPharmacy(pharmacy);
        
        // الكميات
        int bonusQty = request.getBonusQty() != null ? request.getBonusQty() : 0;
        int totalQuantity = request.getQuantity() + bonusQty;
        stockItem.setQuantity(totalQuantity);
        stockItem.setBonusQty(bonusQty);
        
        // الأسعار - استخدام السعر المحول إلى SYP
        stockItem.setActualPurchasePrice(actualPurchasePriceInSYP);
        
        // معلومات الدفعة والصلاحية
        stockItem.setExpiryDate(request.getExpiryDate());
        stockItem.setBatchNo(request.getBatchNo());
        stockItem.setInvoiceNumber(request.getInvoiceNumber());
        
        // الحد الأدنى للمخزون
        if (request.getMinStockLevel() != null) {
            stockItem.setMinStockLevel(request.getMinStockLevel());
        }
        
        // ⚠️ المهم: عدم ربط بفاتورة شراء
        stockItem.setPurchaseInvoice(null); // NULL - بدون فاتورة شراء
        
        // معلومات التدقيق (Audit)
        // createdAt و createdBy يتم تعيينهما تلقائياً من AuditedEntity
        stockItem.setReason(request.getReason());
        stockItem.setNotes(request.getNotes());
        
        // 8. حفظ في قاعدة البيانات
        StockItem savedStockItem = stockItemRepo.save(stockItem);
        
        // 9. تحديث معلومات المنتج (refPurchasePrice و refSellingPrice) إذا لزم الأمر
        updateProductInformationIfNeeded(request, product, actualPurchasePriceInSYP, requestCurrency);
        
        // 10. إرجاع الاستجابة مع dual currency display (استخدام عملة الطلب)
        StockItemDTOResponse response = stockItemMapper.toResponse(savedStockItem, requestCurrency);
        response.setPharmacyId(pharmacy.getId());
        // purchaseInvoiceNumber سيكون null لأنها ليست مرتبطة بفاتورة
        
        logger.info("Stock item added without invoice. StockItem ID: {}, Product ID: {}, Reason: {}", 
                   savedStockItem.getId(), request.getProductId(), request.getReason());
        
        return response;
    }

    /**
     * Method مساعد للحصول على المنتج للتحقق من وجوده
     * Helper method to get product for validation
     */
    private Object getProductForAdjustment(Long productId, ProductType productType, Long pharmacyId) {
        if (productType == ProductType.PHARMACY) {
            return pharmacyProductRepo.findByIdAndPharmacyIdWithTranslations(productId, pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "PharmacyProduct not found: " + productId + " for pharmacy: " + pharmacyId));
        } else if (productType == ProductType.MASTER) {
            return masterProductRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "MasterProduct not found: " + productId));
        } else {
            throw new ConflictException("Invalid productType: " + productType);
        }
    }

    /**
     * Method مساعد لتحديد سعر الشراء حسب نوع المنتج
     * Helper method to determine purchase price based on product type
     */
    private Double determinePurchasePrice(InventoryAdjustmentRequest request, Object product) {
        if (request.getProductType() == ProductType.MASTER) {
            // للمنتجات MASTER: استخدام refPurchasePrice من المنتج
            MasterProduct masterProduct = (MasterProduct) product;
            Double refPrice = (double) masterProduct.getRefPurchasePrice();
            
            if (refPrice <= 0) {
                throw new ConflictException(
                    "MasterProduct with ID " + request.getProductId() + 
                    " has invalid refPurchasePrice: " + refPrice);
            }
            
            // تحذير إذا تم إرسال سعر مختلف في الـ request
            if (request.getActualPurchasePrice() != null && 
                !request.getActualPurchasePrice().equals(refPrice)) {
                logger.warn(
                    "Purchase price {} provided in request for MASTER product {} will be ignored. " +
                    "Using refPurchasePrice {} from product instead.",
                    request.getActualPurchasePrice(), request.getProductId(), refPrice);
            }
            
            return refPrice;
        } else if (request.getProductType() == ProductType.PHARMACY) {
            // للمنتجات PHARMACY: السعر مطلوب من الـ request
            if (request.getActualPurchasePrice() == null || request.getActualPurchasePrice() <= 0) {
                throw new ConflictException(
                    "Purchase price is required and must be greater than 0 for PHARMACY products");
            }
            return request.getActualPurchasePrice();
        } else {
            throw new ConflictException("Invalid productType: " + request.getProductType());
        }
    }

    /**
     * Method مساعد للتحقق من تاريخ الصلاحية
     * Helper method to validate expiry date
     */
    private void validateExpiryDate(LocalDate expiryDate) {
        if (expiryDate == null) {
            return; // Optional field
        }
        
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) {
            throw new ConflictException("Cannot add items with expired date: " + expiryDate);
        }
        
        // تحذير للأدوية التي تنتهي خلال 6 أشهر
        LocalDate sixMonthsFromNow = today.plusMonths(6);
        if (expiryDate.isBefore(sixMonthsFromNow)) {
            logger.warn("Item with expiry date {} is less than 6 months from now", expiryDate);
        }
    }

    /**
     * Method مساعد لتحديث معلومات المنتج (refPurchasePrice و refSellingPrice)
     * Helper method to update product information if needed
     */
    private void updateProductInformationIfNeeded(InventoryAdjustmentRequest request, Object product, Double actualPurchasePriceInSYP, Currency requestCurrency) {
        if (request.getProductType() == ProductType.PHARMACY) {
            updatePharmacyProductInformation(request, (PharmacyProduct) product, actualPurchasePriceInSYP, requestCurrency);
        }
        // للمنتجات MASTER: لا يتم تحديث refPurchasePrice (لأنه ثابت)
        // ولا يتم تحديث refSellingPrice (لأنه ثابت أيضاً)
    }

    /**
     * Method مساعد لتحديث معلومات المنتج PHARMACY
     * Helper method to update PharmacyProduct information
     */
    private void updatePharmacyProductInformation(InventoryAdjustmentRequest request, PharmacyProduct product, Double actualPurchasePriceInSYP, Currency requestCurrency) {
        boolean updated = false;
        
        // تحديث refPurchasePrice إذا كان مختلفاً (بالـ SYP)
        if (actualPurchasePriceInSYP != null && !actualPurchasePriceInSYP.equals((double) product.getRefPurchasePrice())) {
            product.setRefPurchasePrice(actualPurchasePriceInSYP.floatValue());
            updated = true;
            logger.info("Updated refPurchasePrice for PharmacyProduct {} from {} to {} SYP", 
                       product.getId(), product.getRefPurchasePrice(), actualPurchasePriceInSYP);
        }
        
        // تحديث refSellingPrice إذا تم إرساله في الـ request
        if (request.getSellingPrice() != null && request.getSellingPrice() > 0) {
            // تحويل سعر البيع من العملة المحددة إلى SYP قبل الحفظ
            Double sellingPriceInSYP = convertPriceToSYP(request.getSellingPrice(), requestCurrency);
            
            if (!sellingPriceInSYP.equals((double) product.getRefSellingPrice())) {
                product.setRefSellingPrice(sellingPriceInSYP.floatValue());
                updated = true;
                logger.info("Updated refSellingPrice for PharmacyProduct {} from {} to {} SYP (converted from {} {})", 
                           product.getId(), product.getRefSellingPrice(), sellingPriceInSYP, 
                           request.getSellingPrice(), requestCurrency);
            }
        }
        
        // حفظ المنتج إذا تم تحديث أي حقل
        if (updated) {
            pharmacyProductRepo.save(product);
        }
    }

    /**
     * Method مساعد لتحويل السعر من العملة المحددة إلى SYP
     * Helper method to convert price from specified currency to SYP
     */
    private Double convertPriceToSYP(Double price, Currency fromCurrency) {
        if (fromCurrency == null || fromCurrency == Currency.SYP) {
            return price; // السعر بالفعل بـ SYP
        }
        
        java.math.BigDecimal priceBigDecimal = java.math.BigDecimal.valueOf(price);
        java.math.BigDecimal priceInSYP = exchangeRateService.convertToSYP(priceBigDecimal, fromCurrency);
        
        return priceInSYP.doubleValue();
    }

} 