package com.Teryaq.notification.scheduler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Teryaq.notification.dto.NotificationRequest;
import com.Teryaq.notification.enums.NotificationType;
import com.Teryaq.notification.service.NotificationService;
import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.mapper.StockItemMapper;
import com.Teryaq.product.repo.StockItemRepo;
import com.Teryaq.user.entity.CustomerDebt;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.repository.CustomerDebtRepository;
import com.Teryaq.user.repository.EmployeeRepository;
import com.Teryaq.user.repository.PharmacyRepository;

@Service
@Transactional
public class NotificationSchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationSchedulerService.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private StockItemRepo stockItemRepo;
    
    @Autowired
    private CustomerDebtRepository customerDebtRepository;
    
    @Autowired
    private PharmacyRepository pharmacyRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private StockItemMapper stockItemMapper;
    
    /**
     * التحقق من المخزون المنخفض - كل يوم الساعة 9:00 صباحاً
     */
    @Scheduled(cron = "0 0 9 * * *") // كل يوم الساعة 9:00 صباحاً
    public void checkLowStock() {
        logger.info("Starting low stock check...");
        
        try {
            List<Pharmacy> pharmacies = pharmacyRepository.findAll();
            
            for (Pharmacy pharmacy : pharmacies) {
                if (pharmacy.getIsActive() == null || !pharmacy.getIsActive()) {
                    continue; // تخطي الصيدليات غير النشطة
                }
                
                // الحصول على جميع الموظفين في الصيدلية (بدون تصفية)
                List<Employee> managers = employeeRepository.findByPharmacy_Id(pharmacy.getId());
                
                // الحصول على جميع المنتجات الفريدة في الصيدلية
                List<Object[]> uniqueProducts = stockItemRepo.findUniqueProductsByPharmacyId(pharmacy.getId());
                
                List<String> lowStockProducts = new ArrayList<>();
                
                for (Object[] productData : uniqueProducts) {
                    Long productId = (Long) productData[0];
                    com.Teryaq.product.Enum.ProductType productType = (com.Teryaq.product.Enum.ProductType) productData[1];
                    
                    // الحصول على إجمالي الكمية
                    Integer totalQuantity = stockItemRepo.getTotalQuantity(productId, pharmacy.getId(), productType);
                    
                    // الحصول على minStockLevel
                    Integer minStockLevel = stockItemMapper.getMinStockLevel(productId, productType);
                    
                    if (minStockLevel != null && totalQuantity != null && totalQuantity <= minStockLevel) {
                        String productName = stockItemMapper.getProductName(productId, productType);
                        lowStockProducts.add(String.format("%s (المتاح: %d، الحد الأدنى: %d)", 
                            productName, totalQuantity, minStockLevel));
                    }
                }
                
                // إرسال إشعار إذا كان هناك منتجات منخفضة
                if (!lowStockProducts.isEmpty()) {
                    String title = "تنبيه: مخزون منخفض";
                    String body = String.format("يوجد %d منتج بمخزون منخفض:\n%s", 
                        lowStockProducts.size(), 
                        String.join("\n", lowStockProducts));
                    
                    Map<String, Object> data = new HashMap<>();
                    data.put("pharmacyId", pharmacy.getId());
                    data.put("pharmacyName", pharmacy.getName());
                    data.put("lowStockCount", lowStockProducts.size());
                    
                    // إرسال إشعار لكل مدير
                    for (Employee manager : managers) {
                        NotificationRequest request = new NotificationRequest();
                        request.setUserId(manager.getId());
                        request.setTitle(title);
                        request.setBody(body);
                        request.setNotificationType(NotificationType.STOCK_LOW);
                        request.setData(data);
                        
                        notificationService.sendNotification(request);
                    }
                    
                    logger.info("Low stock notification sent for pharmacy: {} - {} products", 
                        pharmacy.getId(), lowStockProducts.size());
                }
            }
            
            logger.info("Low stock check completed");
        } catch (Exception e) {
            logger.error("Error in low stock check: {}", e.getMessage(), e);
        }
    }
    
    /**
     * التحقق من المنتجات المنتهية الصلاحية - كل يوم الساعة 9:00 صباحاً
     */
    @Scheduled(cron = "0 0 9 * * *") // كل يوم الساعة 9:00 صباحاً
    public void checkExpiringProducts() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        logger.info("Starting expiring products check... Today: {}, 30 days from now: {}", today, thirtyDaysFromNow);

        try {
            List<Pharmacy> pharmacies = pharmacyRepository.findAll();
            logger.info("Found {} pharmacies to check", pharmacies.size());
            
            for (Pharmacy pharmacy : pharmacies) {
                if (pharmacy.getIsActive() == null || !pharmacy.getIsActive()) {
                    continue;
                }
                
                // الحصول على جميع الموظفين في الصيدلية (بدون تصفية)
                List<Employee> managers = employeeRepository.findByPharmacy_Id(pharmacy.getId());
                
                if (managers.isEmpty()) {
                    logger.debug("No employees found for pharmacy: {}", pharmacy.getId());
                    continue;
                }
                
                // المنتجات المنتهية الصلاحية
                List<StockItem> expiredItems = stockItemRepo.findExpiredItems(today, pharmacy.getId());
                logger.info("Pharmacy {}: Checking expired products - Today: {}, Found {} expired items", 
                    pharmacy.getId(), today, expiredItems.size());
                
                // Log details of expired items for debugging
                if (!expiredItems.isEmpty()) {
                    expiredItems.forEach(item -> {
                        logger.info("Expired item - ProductId: {}, ProductType: {}, ExpiryDate: {}, Quantity: {}, PharmacyId: {}", 
                            item.getProductId(), item.getProductType(), item.getExpiryDate(), item.getQuantity(), pharmacy.getId());
                    });
                } else {
                    logger.info("Pharmacy {}: No expired items found", pharmacy.getId());
                }
                
                // المنتجات القريبة من الانتهاء (خلال 30 يوم)
                List<StockItem> expiringSoonItems = stockItemRepo.findItemsExpiringSoon(today, thirtyDaysFromNow, pharmacy.getId());
                logger.info("Pharmacy {}: Checking expiring products - Today: {}, 30 days from now: {}, Found {} items expiring soon", 
                    pharmacy.getId(), today, thirtyDaysFromNow, expiringSoonItems.size());
                
                // Log details of expiring items for debugging
                if (!expiringSoonItems.isEmpty()) {
                    expiringSoonItems.forEach(item -> {
                        logger.info("Expiring item - ProductId: {}, ProductType: {}, ExpiryDate: {}, Quantity: {}, PharmacyId: {}", 
                            item.getProductId(), item.getProductType(), item.getExpiryDate(), item.getQuantity(), pharmacy.getId());
                    });
                }
                
                // إرسال إشعار للمنتجات المنتهية
                if (!expiredItems.isEmpty()) {
                    logger.info("Preparing to send expired products notification for pharmacy: {} - {} items", 
                        pharmacy.getId(), expiredItems.size());
                    
                    List<String> expiredProducts = expiredItems.stream()
                        .map(item -> {
                            String productName = stockItemMapper.getProductName(item.getProductId(), item.getProductType());
                            return String.format("%s (انتهت: %s، الكمية: %d)", 
                                productName, 
                                item.getExpiryDate(), 
                                item.getQuantity());
                        })
                        .collect(Collectors.toList());
                    
                    String title = "تنبيه: منتجات منتهية الصلاحية";
                    String body = String.format("يوجد %d منتج منتهي الصلاحية:\n%s", 
                        expiredProducts.size(), 
                        String.join("\n", expiredProducts));
                    
                    Map<String, Object> data = new HashMap<>();
                    data.put("pharmacyId", pharmacy.getId());
                    data.put("pharmacyName", pharmacy.getName());
                    data.put("expiredCount", expiredProducts.size());
                    
                    logger.info("Sending expired products notification to {} employees for pharmacy: {}", 
                        managers.size(), pharmacy.getId());
                    
                    int notificationCount = 0;
                    for (Employee manager : managers) {
                        try {
                            NotificationRequest request = new NotificationRequest();
                            request.setUserId(manager.getId());
                            request.setTitle(title);
                            request.setBody(body);
                            request.setNotificationType(NotificationType.STOCK_EXPIRED);
                            request.setData(new HashMap<>(data));
                            
                            notificationService.sendNotification(request);
                            notificationCount++;
                            logger.info("Expired products notification sent to user: {} for pharmacy: {}", 
                                manager.getId(), pharmacy.getId());
                        } catch (Exception e) {
                            logger.error("Failed to send expired products notification to user {}: {}", 
                                manager.getId(), e.getMessage(), e);
                        }
                    }
                    
                    logger.info("Expired products notification sent for pharmacy: {} - {} products, {} notifications sent", 
                        pharmacy.getId(), expiredProducts.size(), notificationCount);
                } else {
                    logger.info("No expired items to notify for pharmacy: {}", pharmacy.getId());
                }
                
                // إرسال إشعار للمنتجات القريبة من الانتهاء
                if (!expiringSoonItems.isEmpty()) {
                    List<String> expiringProducts = expiringSoonItems.stream()
                        .map(item -> {
                            String productName = stockItemMapper.getProductName(item.getProductId(), item.getProductType());
                            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, item.getExpiryDate());
                            return String.format("%s (ينتهي خلال %d يوم، الكمية: %d، تاريخ الانتهاء: %s)", 
                                productName, 
                                daysUntilExpiry, 
                                item.getQuantity(),
                                item.getExpiryDate());
                        })
                        .collect(Collectors.toList());
                    
                    String title = "تنبيه: منتجات قريبة من انتهاء الصلاحية";
                    String body = String.format("يوجد %d منتج سينتهي خلال 30 يوم:\n%s", 
                        expiringProducts.size(), 
                        String.join("\n", expiringProducts));
                    
                    Map<String, Object> data = new HashMap<>();
                    data.put("pharmacyId", pharmacy.getId());
                    data.put("pharmacyName", pharmacy.getName());
                    data.put("expiringSoonCount", expiringProducts.size());
                    
                    logger.info("Sending expiring soon notification to {} employees for pharmacy: {}", 
                        managers.size(), pharmacy.getId());
                    
                    for (Employee manager : managers) {
                        try {
                            NotificationRequest request = new NotificationRequest();
                            request.setUserId(manager.getId());
                            request.setTitle(title);
                            request.setBody(body);
                            request.setNotificationType(NotificationType.STOCK_EXPIRING_SOON);
                            request.setData(new HashMap<>(data));
                            
                            notificationService.sendNotification(request);
                            logger.info("Expiring soon notification sent to user: {} for pharmacy: {}", 
                                manager.getId(), pharmacy.getId());
                        } catch (Exception e) {
                            logger.error("Failed to send expiring soon notification to user {}: {}", 
                                manager.getId(), e.getMessage(), e);
                        }
                    }
                    
                    logger.info("Expiring soon products notification sent for pharmacy: {} - {} products", 
                        pharmacy.getId(), expiringProducts.size());
                } else {
                    logger.info("No expiring soon items found for pharmacy: {}", pharmacy.getId());
                }
            }
            
            logger.info("Expiring products check completed");
        } catch (Exception e) {
            logger.error("Error in expiring products check: {}", e.getMessage(), e);
        }
    }
    
    /**
     * التحقق من الديون المتأخرة - كل يوم الساعة 9:00 صباحاً
     */
    @Scheduled(cron = "0 0 9 * * *") // كل يوم الساعة 9:00 صباحاً
    public void checkOverdueDebts() {
        logger.info("Starting overdue debts check...");
        
        try {
            List<Pharmacy> pharmacies = pharmacyRepository.findAll();
            
            for (Pharmacy pharmacy : pharmacies) {
                if (pharmacy.getIsActive() == null || !pharmacy.getIsActive()) {
                    continue;
                }
                
                // الحصول على مدير الصيدلية
                List<Employee> managers = employeeRepository.findByPharmacy_Id(pharmacy.getId())
                    .stream()
                    .filter(e -> e.getRole() != null && "PHARMACY_MANAGER".equals(e.getRole().getName()))
                    .collect(Collectors.toList());
                
                if (managers.isEmpty()) {
                    continue;
                }
                
                // الحصول على الديون المتأخرة
                List<CustomerDebt> overdueDebts = customerDebtRepository.getOverdueDebtsByPharmacyId(pharmacy.getId());
                
                if (!overdueDebts.isEmpty()) {
                    Float totalOverdueAmount = customerDebtRepository.getTotalOverdueDebtsByPharmacyId(pharmacy.getId());
                    
                    List<String> debtDetails = overdueDebts.stream()
                        .map(debt -> {
                            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(debt.getDueDate(), LocalDate.now());
                            return String.format("العميل: %s - المبلغ: %.2f - متأخر %d يوم", 
                                debt.getCustomer().getName(), 
                                debt.getRemainingAmount(), 
                                daysOverdue);
                        })
                        .limit(10) // أول 10 ديون فقط
                        .collect(Collectors.toList());
                    
                    String title = "تنبيه: ديون متأخرة";
                    String body = String.format("يوجد %d دين متأخر بإجمالي %.2f:\n%s%s", 
                        overdueDebts.size(), 
                        totalOverdueAmount != null ? totalOverdueAmount : 0f,
                        String.join("\n", debtDetails),
                        overdueDebts.size() > 10 ? "\n... والمزيد" : "");
                    
                    Map<String, Object> data = new HashMap<>();
                    data.put("pharmacyId", pharmacy.getId());
                    data.put("pharmacyName", pharmacy.getName());
                    data.put("overdueCount", overdueDebts.size());
                    data.put("totalOverdueAmount", totalOverdueAmount);
                    
                    for (Employee manager : managers) {
                        NotificationRequest request = new NotificationRequest();
                        request.setUserId(manager.getId());
                        request.setTitle(title);
                        request.setBody(body);
                        request.setNotificationType(NotificationType.DEBT_OVERDUE);
                        request.setData(data);
                        
                        notificationService.sendNotification(request);
                    }
                    
                    logger.info("Overdue debts notification sent for pharmacy: {} - {} debts", 
                        pharmacy.getId(), overdueDebts.size());
                }
            }
            
            logger.info("Overdue debts check completed");
        } catch (Exception e) {
            logger.error("Error in overdue debts check: {}", e.getMessage(), e);
        }
    }
    
}
