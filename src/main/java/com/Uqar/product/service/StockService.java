package com.Uqar.product.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Uqar.product.Enum.ProductType;
import com.Uqar.product.dto.StockItemDTOResponse;
import com.Uqar.product.dto.StockItemDetailDTOResponse;
import com.Uqar.product.dto.StockProductOverallDTOResponse;
import com.Uqar.product.entity.StockItem;
import com.Uqar.product.mapper.StockItemMapper;
import com.Uqar.product.repo.StockItemRepo;
import com.Uqar.user.Enum.Currency;
import com.Uqar.user.entity.Employee;
import com.Uqar.user.entity.User;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.user.service.BaseSecurityService;
import com.Uqar.utils.exception.ConflictException;
import com.Uqar.utils.exception.UnAuthorizedException;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class StockService extends BaseSecurityService {

    private final StockItemRepo stockItemRepo;
    private final StockItemMapper stockItemMapper;

    public StockService(StockItemRepo stockItemRepo,
                                @Lazy StockItemMapper stockItemMapper,
                                UserRepository userRepository) {
        super(userRepository);
        this.stockItemRepo = stockItemRepo;
        this.stockItemMapper = stockItemMapper;
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

} 