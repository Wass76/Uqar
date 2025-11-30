package com.Teryaq.sale.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Teryaq.moneybox.service.SalesIntegrationService;
import com.Teryaq.notification.dto.NotificationRequest;
import com.Teryaq.notification.enums.NotificationType;
import com.Teryaq.notification.service.NotificationService;
import com.Teryaq.product.Enum.PaymentType;
import com.Teryaq.product.entity.StockItem;
import com.Teryaq.product.mapper.StockItemMapper;
import com.Teryaq.product.repo.StockItemRepo;
import com.Teryaq.product.service.StockService;
import com.Teryaq.sale.dto.SaleInvoiceDTORequest;
import com.Teryaq.sale.dto.SaleInvoiceDTOResponse;
import com.Teryaq.sale.dto.SaleInvoiceItemDTORequest;
import com.Teryaq.sale.dto.SaleRefundDTORequest;
import com.Teryaq.sale.dto.SaleRefundDTOResponse;
import com.Teryaq.sale.dto.SaleRefundItemDTORequest;
import com.Teryaq.sale.entity.SaleInvoice;
import com.Teryaq.sale.entity.SaleInvoiceItem;
import com.Teryaq.sale.entity.SaleRefund;
import com.Teryaq.sale.entity.SaleRefundItem;
import com.Teryaq.sale.enums.InvoiceStatus;
import com.Teryaq.sale.enums.PaymentStatus;
import com.Teryaq.sale.enums.RefundStatus;
import com.Teryaq.sale.mapper.SaleMapper;
import com.Teryaq.sale.mapper.SaleRefundMapper;
import com.Teryaq.sale.repo.SaleInvoiceItemRepository;
import com.Teryaq.sale.repo.SaleInvoiceRepository;
import com.Teryaq.sale.repo.SaleRefundItemRepo;
import com.Teryaq.sale.repo.SaleRefundRepo;
import com.Teryaq.user.Enum.Currency;
import com.Teryaq.user.config.RoleConstants;
import com.Teryaq.user.entity.Customer;
import com.Teryaq.user.entity.CustomerDebt;
import com.Teryaq.user.entity.Employee;
import com.Teryaq.user.entity.Pharmacy;
import com.Teryaq.user.mapper.CustomerDebtMapper;
import com.Teryaq.user.repository.CustomerDebtRepository;
import com.Teryaq.user.repository.CustomerRepo;
import com.Teryaq.user.repository.EmployeeRepository;
import com.Teryaq.user.repository.UserRepository;
import com.Teryaq.user.service.BaseSecurityService;
import com.Teryaq.utils.exception.ConflictException;
import com.Teryaq.utils.exception.RequestNotValidException;
import com.Teryaq.utils.exception.UnAuthorizedException;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SaleService extends BaseSecurityService {
    private static final Logger logger = LoggerFactory.getLogger(SaleService.class);

    @Autowired
    private SaleInvoiceRepository saleInvoiceRepository;
    @Autowired
    private SaleInvoiceItemRepository saleInvoiceItemRepository;
    @Autowired
    private CustomerRepo customerRepository;
    @Autowired
    private StockItemRepo stockItemRepo;
    @Autowired
    private StockService stockService;
    @Autowired
    private DiscountCalculationService discountCalculationService;
    @Autowired
    private PaymentValidationService paymentValidationService;
    @Autowired
    private CustomerDebtRepository customerDebtRepository;
    @Autowired
    private StockItemMapper stockItemMapper;
    @Autowired
    private SalesIntegrationService salesIntegrationService;

    @Autowired
    private SaleMapper saleMapper;
    
    @Autowired
    private CustomerDebtMapper customerDebtMapper;

    @Autowired
    private SaleRefundRepo saleRefundRepo;
    @Autowired
    private SaleRefundItemRepo saleRefundItemRepo;
    @Autowired
    private SaleRefundMapper saleRefundMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EmployeeRepository employeeRepository;

        public SaleService(SaleInvoiceRepository saleInvoiceRepository,
                       SaleInvoiceItemRepository saleInvoiceItemRepository,
                       CustomerRepo customerRepository,
                       StockItemRepo stockItemRepo,
                       StockService stockService,
                       DiscountCalculationService discountCalculationService,
                       PaymentValidationService paymentValidationService,
                       CustomerDebtRepository customerDebtRepository,
                       NotificationService notificationService,
                       EmployeeRepository employeeRepository,
                       SaleMapper saleMapper,
                       StockItemMapper stockItemMapper,
                       SalesIntegrationService salesIntegrationService,
                       CustomerDebtMapper customerDebtMapper,
                       UserRepository userRepository) {
        super(userRepository);
        this.saleInvoiceRepository = saleInvoiceRepository;
        this.saleInvoiceItemRepository = saleInvoiceItemRepository;
        this.customerRepository = customerRepository;
        this.stockItemRepo = stockItemRepo;
        this.stockService = stockService;
        this.discountCalculationService = discountCalculationService;
        this.paymentValidationService = paymentValidationService;   
        this.customerDebtRepository = customerDebtRepository;
        this.saleMapper = saleMapper;
        this.stockItemMapper = stockItemMapper;
        this.salesIntegrationService = salesIntegrationService;
        this.customerDebtMapper = customerDebtMapper;
        this.notificationService = notificationService;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public SaleInvoiceDTOResponse createSaleInvoice(SaleInvoiceDTORequest requestDTO) {
        Pharmacy currentPharmacy = getCurrentUserPharmacy();
        if (currentPharmacy == null) {
            throw new UnAuthorizedException("You are not authorized to create a sale invoice");
        }
        
        // ✅ ADD CURRENCY VALIDATION
        if (requestDTO.getCurrency() == null) {
            requestDTO.setCurrency(Currency.SYP);
            logger.info("Currency not specified, defaulting to SYP for sale invoice");
        }
        
        // Validate currency is supported
        if (!Arrays.asList(Currency.SYP, Currency.USD, Currency.EUR).contains(requestDTO.getCurrency())) {
            throw new RequestNotValidException("Unsupported currency: " + requestDTO.getCurrency() + 
                ". Supported currencies are: SYP, USD, EUR");
        }
        
        Customer customer = null;
        
        // التحقق من أن عمليات الدين تتطلب زبون محدد
        if (requestDTO.getPaymentType() == PaymentType.CREDIT && requestDTO.getCustomerId() == null) {
            throw new ConflictException("Credit sales require a specific customer. Please select a customer for credit transactions.");
        }
        
        if (requestDTO.getCustomerId() != null) {
            customer = customerRepository.findById(requestDTO.getCustomerId()).orElse(null);
        } else {
            customer = getOrCreateCashCustomer(currentPharmacy);
        }
        
        if (!paymentValidationService.validatePayment(requestDTO.getPaymentType(), requestDTO.getPaymentMethod())) {
            throw new ConflictException("the payment type and payment method are not compatible");
        }
        
        if (customer == null) {
            throw new ConflictException("Cannot create sale invoice without a customer");
        }
        
        // التحقق من أن العميل ينتمي للصيدلية الحالية
        if (!customer.getPharmacy().getId().equals(currentPharmacy.getId())) {
            throw new ConflictException("Customer with ID " + customer.getId() + 
                " does not belong to the current pharmacy. Customer belongs to pharmacy: " + 
                customer.getPharmacy().getName());
        }
        
        float paidAmount = requestDTO.getPaidAmount() != null ? requestDTO.getPaidAmount() : 0;
        if (paidAmount < 0) {
            throw new ConflictException("the paid amount cannot be negative");
        }
        
        SaleInvoice invoice = saleMapper.toEntityWithCustomerAndDate(requestDTO, customer, currentPharmacy);
        
        // Generate invoice number
        String invoiceNumber = "INV-" + System.currentTimeMillis() + "-" + currentPharmacy.getId();
        invoice.setInvoiceNumber(invoiceNumber);
        
        List<Long> stockItemIds = requestDTO.getItems().stream()
            .map(SaleInvoiceItemDTORequest::getStockItemId)
            .collect(Collectors.toList());
        
        List<StockItem> stockItems = stockItemRepo.findAllById(stockItemIds);
        
        if (stockItems.size() != stockItemIds.size()) {
            List<Long> foundIds = stockItems.stream().map(StockItem::getId).collect(Collectors.toList());
            List<Long> missingIds = stockItemIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());
            throw new EntityNotFoundException("Stock items not found with IDs: " + missingIds);
        }
        
        List<SaleInvoiceItem> items = saleMapper.toEntityList(requestDTO.getItems(), stockItems, requestDTO.getCurrency());
        
        float total = 0;
        
        for (SaleInvoiceItem item : items) {
            StockItem product = item.getStockItem();
            
            if (!stockService.isQuantityAvailable(product.getProductId(), item.getQuantity(), product.getProductType())) {
                String productName = stockItemMapper.getProductName(product.getProductId(), product.getProductType());
                throw new RequestNotValidException("Insufficient stock for product: " + productName + 
                    " (ID: " + product.getProductId() + "). Available: " + 
                    stockItemRepo.getTotalQuantity(product.getProductId(), getCurrentUserPharmacyId(), product.getProductType()) + 
                    ", Requested: " + item.getQuantity());
            }
            
            if (product.getExpiryDate() != null && product.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                String productName = stockItemMapper.getProductName(product.getProductId(), product.getProductType());
                throw new RequestNotValidException("Product expired: " + productName + 
                    " (ID: " + product.getProductId() + "). Expiry date: " + product.getExpiryDate());
            }
            
            product.setQuantity(product.getQuantity() - item.getQuantity());
            stockItemRepo.save(product);
            
            item.setSaleInvoice(invoice);
            
            float subTotal = item.getUnitPrice() * item.getQuantity();
            item.setSubTotal(subTotal);
            total += subTotal;
        }
        
        float invoiceDiscount = discountCalculationService.calculateDiscount(
            total, 
            invoice.getDiscountType(), 
            invoice.getDiscount()
        );
        
        invoice.setTotalAmount(total - invoiceDiscount);
        
        if (requestDTO.getPaymentType() == PaymentType.CASH && paidAmount == 0) {
            paidAmount = invoice.getTotalAmount();
        }
        
        if (!paymentValidationService.validatePaidAmount(invoice.getTotalAmount(), paidAmount, requestDTO.getPaymentType())) {
            throw new RequestNotValidException("the paid amount is not valid for payment type: " + requestDTO.getPaymentType());
        }
        
        float remainingAmount = paymentValidationService.calculateRemainingAmount(invoice.getTotalAmount(), paidAmount);
        
        if (requestDTO.getPaymentType() == PaymentType.CASH) {
            if (remainingAmount > 0) {
                throw new RequestNotValidException("Cash payment must be complete. Remaining amount: " + remainingAmount);
            }
            remainingAmount = 0; 
        }
        
        invoice.setPaidAmount(paidAmount);
        invoice.setRemainingAmount(remainingAmount);
        
        // حساب الحالات الجديدة
        calculateInvoiceStatuses(invoice);
        
        invoice.setItems(items);
        
        SaleInvoice savedInvoice = saleInvoiceRepository.save(invoice);
        saleInvoiceItemRepository.saveAll(items);
        
        // Integrate with Money Box for cash payments - CRITICAL FIX: Only record paidAmount, not totalAmount
        if (requestDTO.getPaymentMethod() == com.Teryaq.product.Enum.PaymentMethod.CASH && savedInvoice.getPaidAmount() > 0) {
            try {
                // Get current pharmacy ID for MoneyBox integration
                Long currentPharmacyId = getCurrentUserPharmacyId();
                salesIntegrationService.recordSalePayment(
                    currentPharmacyId,
                    savedInvoice.getId(),
                    java.math.BigDecimal.valueOf(savedInvoice.getPaidAmount()), // ✅ FIXED: Use paidAmount instead of totalAmount
                    requestDTO.getCurrency()
                );
                logger.info("Cash sale recorded in Money Box for invoice: {} - Amount: {} (paidAmount only)", 
                           savedInvoice.getId(), savedInvoice.getPaidAmount());
            } catch (Exception e) {
                logger.warn("Failed to record cash sale in Money Box for invoice {}: {}", 
                           savedInvoice.getId(), e.getMessage());
                // Don't fail the sale if Money Box integration fails
            }
        }
        
        if (customer != null && remainingAmount > 0 && !isCashCustomer(customer)) {
            createCustomerDebt(customer, remainingAmount, savedInvoice, requestDTO);
        }
        
        return saleMapper.toResponse(savedInvoice);
    }
    
    private void createCustomerDebt(Customer customer, float remainingAmount, SaleInvoice invoice, SaleInvoiceDTORequest request) {
        try {
            // التحقق من أن الفاتورة في حالة SOLD قبل إنشاء الدين
            if (invoice.getStatus() != InvoiceStatus.SOLD) {
                logger.warn("Cannot create debt for invoice {} with status: {}", invoice.getId(), invoice.getStatus());
                return;
            }
            
            LocalDate dueDate = request.getDebtDueDate() != null ? request.getDebtDueDate() : LocalDate.now().plusMonths(1);
            
            CustomerDebt debt = customerDebtMapper.toEntityFromSaleInvoice(request, remainingAmount, dueDate);
            debt.setCustomer(customer);
            debt.setNotes("Debt from sale invoice: " + invoice.getId());
            
            customerDebtRepository.save(debt);
            logger.info("Created customer debt: {} for invoice: {} using mapper", remainingAmount, invoice.getId());
        } catch (Exception e) {
            logger.error("Error creating customer debt for invoice {}: {}", invoice.getId(), e.getMessage(), e);
        }
    }

  
    private Customer getOrCreateCashCustomer(Pharmacy pharmacy) {
        try {
            Customer cashCustomer = customerRepository.findByNameAndPharmacyId("cash customer", pharmacy.getId())
                .orElse(null);
            
            if (cashCustomer == null) {
                cashCustomer = new Customer();
                cashCustomer.setName("cash customer");
                cashCustomer.setPhoneNumber("0000000000");
                cashCustomer.setAddress("pharmacy " + pharmacy.getName());
                cashCustomer.setPharmacy(pharmacy);
                cashCustomer.setNotes("cash customer");
                
                cashCustomer = customerRepository.save(cashCustomer);
                logger.info("Created cash customer for pharmacy: {}", pharmacy.getId());
            }
            
            return cashCustomer;
        } catch (Exception e) {
            logger.error("Error creating cash customer for pharmacy {}: {}", pharmacy.getId(), e.getMessage());
            return null;
        }
    }
    
    private boolean isCashCustomer(Customer customer) {
        return customer != null && "cash customer".equalsIgnoreCase(customer.getName());
    }

   
    public SaleInvoiceDTOResponse getSaleById(Long saleId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        SaleInvoice saleInvoice = saleInvoiceRepository.findByIdAndPharmacyId(saleId, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Sale invoice not found with ID: " + saleId));
        return saleMapper.toResponse(saleInvoice);
    }

    
    @Transactional
    public void cancelSale(Long saleId) {
        // Get current user's pharmacy ID for security
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        SaleInvoice saleInvoice = saleInvoiceRepository.findByIdAndPharmacyId(saleId, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Sale invoice not found with ID: " + saleId));

        // التحقق من أن الفاتورة في حالة SOLD قبل الإلغاء
        if (saleInvoice.getStatus() != InvoiceStatus.SOLD) {
            throw new RequestNotValidException("Cannot cancel invoice with status: " + saleInvoice.getStatus() + 
                ". Only SOLD invoices can be cancelled.");
        }

        if (saleInvoice.getRemainingAmount() <= 0) {
            throw new RequestNotValidException("Cannot cancel a fully paid sale invoice");
        }

        for (SaleInvoiceItem item : saleInvoice.getItems()) {
            StockItem stockItem = item.getStockItem();
            if (stockItem != null) {
                stockItem.setQuantity(stockItem.getQuantity() + item.getQuantity());
                stockItemRepo.save(stockItem);
            }
        }

        if (saleInvoice.getCustomer() != null) {
            List<CustomerDebt> relatedDebts = customerDebtRepository.findByCustomerId(saleInvoice.getCustomer().getId());
            relatedDebts.stream()
                    .filter(debt -> debt.getNotes() != null && debt.getNotes().contains("دين من فاتورة بيع رقم: " + saleId))
                    .forEach(debt -> customerDebtRepository.delete(debt));
        }

        saleInvoiceItemRepository.deleteBySaleInvoiceId(saleId);

        saleInvoiceRepository.delete(saleInvoice);
    }


    public List<SaleInvoiceDTOResponse> getAllSales() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdOrderByInvoiceDateDesc(currentPharmacyId);
        
        return saleInvoices.stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<SaleInvoiceDTOResponse> searchSaleInvoiceByDate(LocalDate createdDate) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        LocalDateTime startOfDay = createdDate.atStartOfDay();
        LocalDateTime endOfDay = createdDate.atTime(23, 59, 59);
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdAndInvoiceDateBetween(currentPharmacyId, startOfDay, endOfDay);
        if (saleInvoices.isEmpty()) {
            throw new EntityNotFoundException("No sale invoices found for date: " + createdDate);
        }
        return saleInvoices.stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<SaleInvoiceDTOResponse> searchSaleInvoiceByDateRange(LocalDate startDate, LocalDate endDate) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdAndInvoiceDateBetween(
            currentPharmacyId, 
            startDate.atStartOfDay(), 
            endDate.atTime(23, 59, 59)
        );
        
        if (saleInvoices.isEmpty()) {
            throw new EntityNotFoundException("No sale invoices found between " + startDate + " and " + endDate);
        }
        
        return saleInvoices.stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SaleRefundDTOResponse processRefund(Long saleId, SaleRefundDTORequest request) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        SaleInvoice saleInvoice = saleInvoiceRepository.findByIdAndPharmacyId(saleId, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Sale invoice not found with ID: " + saleId));

        // التحقق من أن الفاتورة لم يتم إرجاعها كلياً
        if (saleInvoice.getRefundStatus() == RefundStatus.FULLY_REFUNDED) {
            throw new RequestNotValidException("Sale invoice has already been fully refunded");
        }

        SaleRefund refund = saleRefundMapper.toEntity(request, saleInvoice, getCurrentUserPharmacy());
        List<SaleRefundItem> refundItems = new ArrayList<>();
        float totalRefundAmount = 0.0f;

        // معالجة المنتجات المرتجعة
        totalRefundAmount = processRefundItems(saleInvoice, request, refund, refundItems);

        refund.setTotalRefundAmount(totalRefundAmount);
        refund.setRefundItems(refundItems);
        
        // تحديث حالة المرتجع
        if (totalRefundAmount > 0) {
            refund.setRefundStatus(RefundStatus.PARTIALLY_REFUNDED);
        } else {
            refund.setRefundStatus(RefundStatus.NO_REFUND);
        }
        
        // حفظ المرتجعات
        SaleRefund savedRefund = saleRefundRepo.save(refund);
        saleRefundItemRepo.saveAll(refundItems);
        
        // تحديث المخزون
        restoreStock(refundItems);
        refund.setStockRestored(true);
        saleRefundRepo.save(refund);

        // معالجة المرتجع حسب نوع الدفع وحالة الفاتورة
        handleRefundPayment(saleInvoice, totalRefundAmount, currentPharmacyId, saleId);

        // تحديث حالة الفاتورة
        updateInvoiceStatus(saleInvoice);

        // حساب معلومات الدين للعميل
        Customer customer = savedRefund.getSaleInvoice().getCustomer();
        List<CustomerDebt> customerDebts = customerDebtRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(
            customer.getId(), "ACTIVE");
        float totalCustomerDebt = customerDebts.stream()
                .map(CustomerDebt::getRemainingAmount)
                .reduce(0f, Float::sum);

        return saleRefundMapper.toResponseWithDebtInfo(savedRefund, totalCustomerDebt, customerDebts.size());
    }

    private float processRefundItems(SaleInvoice saleInvoice, SaleRefundDTORequest request, 
                                   SaleRefund refund, List<SaleRefundItem> refundItems) {
        float totalRefundAmount = 0.0f;
        
        if (request.getRefundItems() == null || request.getRefundItems().isEmpty()) {
            throw new RequestNotValidException("Refund items list is required");
        }

        for (SaleRefundItemDTORequest refundRequest : request.getRefundItems()) {
            SaleInvoiceItem originalItem = saleInvoice.getItems().stream()
                    .filter(item -> item.getId().equals(refundRequest.getItemId()))
                    .findFirst()
                    .orElseThrow(() -> new RequestNotValidException("Item not found with ID: " + refundRequest.getItemId()));

            // حساب الكمية المتاحة للإرجاع (الكمية المباعة - الكمية المرتجعة مسبقاً)
            int availableForRefund = originalItem.getQuantity() - originalItem.getRefundedQuantity();
            
            if (refundRequest.getQuantity() > availableForRefund) {
                throw new RequestNotValidException("Refund quantity cannot exceed available quantity for item ID: " + 
                    refundRequest.getItemId() + ". Available: " + availableForRefund + ", Requested: " + refundRequest.getQuantity());
            }

            SaleRefundItem refundItem = new SaleRefundItem();
            refundItem.setSaleRefund(refund);
            refundItem.setSaleInvoiceItem(originalItem);
            refundItem.setRefundQuantity(refundRequest.getQuantity());
            refundItem.setUnitPrice(originalItem.getUnitPrice());
            refundItem.setSubtotal(originalItem.getUnitPrice() * refundRequest.getQuantity());
            refundItem.setItemRefundReason(refundRequest.getItemRefundReason());
            refundItem.setStockRestored(false);
            
            refundItems.add(refundItem);
            totalRefundAmount += refundItem.getSubtotal();
            
            // تحديث الكمية المرتجعة في العنصر الأصلي
            originalItem.setRefundedQuantity(originalItem.getRefundedQuantity() + refundRequest.getQuantity());
        }
        
        return totalRefundAmount;
    }

    private void restoreStock(List<SaleRefundItem> refundItems) {
        for (SaleRefundItem refundItem : refundItems) {
            SaleInvoiceItem originalItem = refundItem.getSaleInvoiceItem();
            StockItem stockItem = originalItem.getStockItem();
            
            if (stockItem != null) {
                stockItem.setQuantity(stockItem.getQuantity() + refundItem.getRefundQuantity());
                stockItemRepo.save(stockItem);
                refundItem.setStockRestored(true);
            }
        }
    }

   
    private void updateInvoiceStatus(SaleInvoice saleInvoice) {
        boolean allItemsFullyRefunded = true;
        boolean hasAnyRefund = false;
        
        for (SaleInvoiceItem item : saleInvoice.getItems()) {
            if (item.getRefundedQuantity() > 0) {
                hasAnyRefund = true;
            }
            if (item.getRefundedQuantity() < item.getQuantity()) {
                allItemsFullyRefunded = false;
            }
        }
        
        // تحديث حالة المرتجعات
        if (allItemsFullyRefunded && hasAnyRefund) {
            saleInvoice.setRefundStatus(RefundStatus.FULLY_REFUNDED);
        } else if (hasAnyRefund) {
            saleInvoice.setRefundStatus(RefundStatus.PARTIALLY_REFUNDED);
        } else {
            saleInvoice.setRefundStatus(RefundStatus.NO_REFUND);
        }
        
        // تحديث حالة الدفع (إذا كان هناك مرتجعات، قد تحتاج لتحديث المبالغ)
        if (saleInvoice.getRemainingAmount() == 0) {
            saleInvoice.setPaymentStatus(PaymentStatus.FULLY_PAID);
        } else if (saleInvoice.getPaidAmount() > 0) {
            saleInvoice.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        } else {
            saleInvoice.setPaymentStatus(PaymentStatus.UNPAID);
        }
        
        // حالة الفاتورة الأساسية تبقى SOLD
        saleInvoice.setStatus(InvoiceStatus.SOLD);
        
        saleInvoiceRepository.save(saleInvoice);
    }

  
    private void calculateInvoiceStatuses(SaleInvoice invoice) {
        // حساب حالة الدفع
        if (invoice.getRemainingAmount() == 0) {
            invoice.setPaymentStatus(PaymentStatus.FULLY_PAID);
        } else if (invoice.getPaidAmount() > 0) {
            invoice.setPaymentStatus(PaymentStatus.PARTIALLY_PAID);
        } else {
            invoice.setPaymentStatus(PaymentStatus.UNPAID);
        }
        
        // حساب حالة المرتجعات (افتراضياً لا توجد مرتجعات عند الإنشاء)
        invoice.setRefundStatus(RefundStatus.NO_REFUND);
        
        // حالة الفاتورة الأساسية
        invoice.setStatus(InvoiceStatus.SOLD);
    }

    public List<SaleRefundDTOResponse> getRefundsBySaleId(Long saleId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleRefund> refunds = saleRefundRepo.findBySaleInvoiceIdAndPharmacyId(saleId, currentPharmacyId);
        
        return refunds.stream()
                .map(refund -> {
                    // حساب معلومات الدين للعميل
                    Customer customer = refund.getSaleInvoice().getCustomer();
                    List<CustomerDebt> customerDebts = customerDebtRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(
                        customer.getId(), "ACTIVE");
                    float totalCustomerDebt = customerDebts.stream()
                            .map(CustomerDebt::getRemainingAmount)
                            .reduce(0f, Float::sum);
                    
                    return saleRefundMapper.toResponseWithDebtInfo(refund, totalCustomerDebt, customerDebts.size());
                })
                .collect(Collectors.toList());
    }

    public List<SaleRefundDTOResponse> getAllRefunds() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleRefund> refunds = saleRefundRepo.findByPharmacyIdOrderByRefundDateDesc(currentPharmacyId);
        
        return refunds.stream()
                .map(refund -> {
                    // حساب معلومات الدين للعميل
                    Customer customer = refund.getSaleInvoice().getCustomer();
                    List<CustomerDebt> customerDebts = customerDebtRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(
                        customer.getId(), "ACTIVE");
                    float totalCustomerDebt = customerDebts.stream()
                            .map(CustomerDebt::getRemainingAmount)
                            .reduce(0f, Float::sum);
                    
                    return saleRefundMapper.toResponseWithDebtInfo(refund, totalCustomerDebt, customerDebts.size());
                })
                .collect(Collectors.toList());
    }

    public List<SaleRefundDTOResponse> getRefundsByDateRange(LocalDate startDate, LocalDate endDate) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleRefund> refunds = saleRefundRepo.findByPharmacyIdAndRefundDateBetween(
            currentPharmacyId, 
            startDate.atStartOfDay(), 
            endDate.atTime(23, 59, 59)
        );
        
        return refunds.stream()
                .map(refund -> {
                    // حساب معلومات الدين للعميل
                    Customer customer = refund.getSaleInvoice().getCustomer();
                    List<CustomerDebt> customerDebts = customerDebtRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(
                        customer.getId(), "ACTIVE");
                    float totalCustomerDebt = customerDebts.stream()
                            .map(CustomerDebt::getRemainingAmount)
                            .reduce(0f, Float::sum);
                    
                    return saleRefundMapper.toResponseWithDebtInfo(refund, totalCustomerDebt, customerDebts.size());
                })
                .collect(Collectors.toList());
    }

    
    public List<SaleInvoiceDTOResponse> getFullyPaidSales() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdOrderByInvoiceDateDesc(currentPharmacyId);
        
        return saleInvoices.stream()
                .filter(invoice -> invoice.getPaymentStatus() == PaymentStatus.FULLY_PAID)
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
   
    public List<SaleInvoiceDTOResponse> getInvoicesWithDebt() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdOrderByInvoiceDateDesc(currentPharmacyId);
        
        return saleInvoices.stream()
                .filter(invoice -> invoice.getPaymentStatus() == PaymentStatus.PARTIALLY_PAID || 
                                 invoice.getPaymentStatus() == PaymentStatus.UNPAID)
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    
    public List<SaleInvoiceDTOResponse> getSalesByPaymentStatus(PaymentStatus paymentStatus) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdOrderByInvoiceDateDesc(currentPharmacyId);
        
        return saleInvoices.stream()
                .filter(invoice -> invoice.getPaymentStatus() == paymentStatus)
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
  
    public List<SaleInvoiceDTOResponse> getSalesByRefundStatus(RefundStatus refundStatus) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdOrderByInvoiceDateDesc(currentPharmacyId);
        
        return saleInvoices.stream()
                .filter(invoice -> invoice.getRefundStatus() == refundStatus)
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
 
    public List<SaleInvoiceDTOResponse> getSalesByInvoiceStatus(InvoiceStatus invoiceStatus) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        List<SaleInvoice> saleInvoices = saleInvoiceRepository.findByPharmacyIdOrderByInvoiceDateDesc(currentPharmacyId);
        
        return saleInvoices.stream()
                .filter(invoice -> invoice.getStatus() == invoiceStatus)
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }

 
    private void handleRefundPayment(SaleInvoice saleInvoice, float totalRefundAmount, Long currentPharmacyId, Long saleId) {
        float remainingAmount = saleInvoice.getRemainingAmount();
        float paidAmount = saleInvoice.getPaidAmount();
        
        // الحالة 1: فاتورة نقدية مدفوعة بالكامل - CRITICAL FIX: Only refund actual cash paid
        if (saleInvoice.getPaymentType() == PaymentType.CASH && remainingAmount == 0) {
            // إرجاع النقد للصندوق - Only refund the amount that was actually paid in cash
            float refundAmount = Math.min(totalRefundAmount, saleInvoice.getPaidAmount());
            if (refundAmount > 0) {
                try {
                    salesIntegrationService.recordSaleRefund(
                        currentPharmacyId,
                        saleId,
                        java.math.BigDecimal.valueOf(refundAmount), // ✅ FIXED: Only refund actual cash paid
                        saleInvoice.getCurrency()
                    );
                    logger.info("Cash refund recorded in Money Box for invoice: {} - Amount: {} (actual cash paid)", 
                               saleId, refundAmount);
                } catch (Exception e) {
                    logger.warn("Failed to record cash refund in Money Box for invoice {}: {}", 
                               saleId, e.getMessage());
                }
            }
        }
        
        // الحالة 2: فاتورة دين مدفوعة جزئياً
        else if (saleInvoice.getPaymentType() == PaymentType.CREDIT && paidAmount > 0) {
            // أولاً: حساب الدين الكلي للعميل (يشمل دين الفاتورة الحالية)
            // دين الفاتورة الحالية = remainingAmount
            float invoiceDebt = Math.max(0, remainingAmount);
            float totalCustomerDebt = getCurrentCustomerDebt(saleInvoice.getCustomer());
            
            // الدين الكلي = دين الفاتورة الحالية + ديون أخرى
            float totalDebt = invoiceDebt + totalCustomerDebt;
            
            logger.info("Refund calculation for sale {}: totalRefundAmount={}, invoiceDebt={}, otherDebts={}, totalDebt={}, paidAmount={}", 
                       saleId, totalRefundAmount, invoiceDebt, totalCustomerDebt, totalDebt, paidAmount);
            
            // ثانياً: خصم قيمة المرتجع من الدين الكلي (حسب الحد الأدنى)
            float debtReduction = Math.min(totalRefundAmount, totalDebt);
            logger.info("Refund calculation: debtReduction={} (min of {} and {})", 
                       debtReduction, totalRefundAmount, totalDebt);
            
            if (debtReduction > 0) {
                try {
                    // خصم من دين الفاتورة الحالية أولاً
                    float invoiceDebtReduction = Math.min(debtReduction, invoiceDebt);
                    if (invoiceDebtReduction > 0) {
                        // تحديث الفاتورة مباشرة
                        float newRemaining = Math.max(0, remainingAmount - invoiceDebtReduction);
                        saleInvoice.setRemainingAmount(newRemaining);
                        saleInvoiceRepository.save(saleInvoice);
                        logger.info("Invoice debt reduced by {} for refund: new remaining={}", 
                                   invoiceDebtReduction, newRemaining);
                    }
                    
                    // خصم من الديون الأخرى
                    float otherDebtReduction = debtReduction - invoiceDebtReduction;
                    if (otherDebtReduction > 0) {
                        reduceCustomerDebt(saleInvoice.getCustomer(), otherDebtReduction, saleId);
                        logger.info("Other customer debts reduced by {} for refund invoice: {}", otherDebtReduction, saleId);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to reduce customer debt for refund invoice {}: {}", 
                               saleId, e.getMessage());
                }
            }
            
            // ثالثاً: إرجاع أي مبلغ زائد (المرتجع - الدين المخصوم)
            // المبلغ الزائد = المرتجع الكلي - الدين المخصوم
            float excessRefund = totalRefundAmount - debtReduction;
            logger.info("Refund calculation: excessRefund={} ({} - {})", 
                       excessRefund, totalRefundAmount, debtReduction);
            
            // إذا كان هناك مبلغ زائد عن الدين، إخراجه من الصندوق
            // هذا المبلغ هو الفرق بين قيمة المرتجع والدين المتبقي
            if (excessRefund > 0) {
                try {
                    salesIntegrationService.recordSaleRefund(
                        currentPharmacyId,
                        saleId,
                        java.math.BigDecimal.valueOf(excessRefund),
                        saleInvoice.getCurrency()
                    );
                    logger.info("✅ Excess refund recorded in Money Box for credit invoice: {} - Amount: {} (refund {} - debt {})", 
                               saleId, excessRefund, totalRefundAmount, debtReduction);
                } catch (Exception e) {
                    logger.warn("Failed to record excess refund in Money Box for invoice {}: {}", 
                               saleId, e.getMessage());
                }
            } else {
                logger.info("No excess refund to record: excessRefund={}, totalRefundAmount={}, debtReduction={}", 
                           excessRefund, totalRefundAmount, debtReduction);
            }
        }
        
        // الحالة 3: فاتورة دين غير مدفوعة
        else if (saleInvoice.getPaymentType() == PaymentType.CREDIT && paidAmount == 0) {
            // أولاً: حساب الدين الحالي للعميل
            float currentDebt = getCurrentCustomerDebt(saleInvoice.getCustomer());
            
            // ثانياً: خصم من دين العميل (حسب الحد الأدنى بين المرتجع والدين)
            float debtReduction = Math.min(totalRefundAmount, currentDebt);
            if (debtReduction > 0) {
                try {
                    reduceCustomerDebt(saleInvoice.getCustomer(), debtReduction, saleId);
                    logger.info("Customer debt reduced by {} for unpaid credit invoice refund: {}", debtReduction, saleId);
                } catch (Exception e) {
                    logger.warn("Failed to reduce customer debt for unpaid credit invoice refund {}: {}", 
                               saleId, e.getMessage());
                }
            }
            
            // ثالثاً: إذا كان المرتجع أكبر من الدين، إخراج الفرق من الصندوق
            float excessRefund = totalRefundAmount - debtReduction;
            if (excessRefund > 0) {
                try {
                    salesIntegrationService.recordSaleRefund(
                        currentPharmacyId,
                        saleId,
                        java.math.BigDecimal.valueOf(excessRefund),
                        saleInvoice.getCurrency()
                    );
                    logger.info("Excess refund recorded in Money Box for unpaid credit invoice: {} - Amount: {} (refund {} - debt {})", 
                               saleId, excessRefund, totalRefundAmount, debtReduction);
                } catch (Exception e) {
                    logger.warn("Failed to record excess refund in Money Box for invoice {}: {}", 
                               saleId, e.getMessage());
                }
            }
        }
        
        // الحالة 4: فاتورة نقدية مدفوعة جزئياً (نادرة)
        else if (saleInvoice.getPaymentType() == PaymentType.CASH && remainingAmount > 0) {
            // إرجاع النقد المدفوع للصندوق
            float refundToCash = Math.min(totalRefundAmount, paidAmount);
            if (refundToCash > 0) {
                try {
                    salesIntegrationService.recordSaleRefund(
                        currentPharmacyId,
                        saleId,
                        java.math.BigDecimal.valueOf(refundToCash),
                        saleInvoice.getCurrency()
                    );
                    logger.info("Partial cash refund recorded in Money Box for partially paid cash invoice: {}", saleId);
                } catch (Exception e) {
                    logger.warn("Failed to record partial cash refund in Money Box for invoice {}: {}", 
                               saleId, e.getMessage());
                }
            }
        }
    }

   
    private void reduceCustomerDebt(Customer customer, float amount, Long saleId) {
        if (customer == null || amount <= 0) {
            return;
        }
        
        // البحث عن أحدث دين نشط للعميل
        List<CustomerDebt> debts = customerDebtRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(
            customer.getId(), "ACTIVE");
        
        if (debts.isEmpty()) {
            logger.warn("No active debt found for customer {} to reduce refund amount {}", 
                       customer.getId(), amount);
            return;
        }
        
        // خصم من أحدث دين نشط
        CustomerDebt debt = debts.get(0);
        float currentRemaining = debt.getRemainingAmount();
        float newRemaining = Math.max(0, currentRemaining - amount);
        
        debt.setRemainingAmount(newRemaining);
        debt.setPaidAmount(debt.getPaidAmount() + (currentRemaining - newRemaining));
        
        // إذا تم سداد الدين بالكامل
        if (newRemaining == 0) {
            debt.setStatus("PAID");
            debt.setPaidAt(LocalDate.now());
            notifyDebtPaidFromRefund(debt, saleId);
        }
        
        customerDebtRepository.save(debt);
        logger.info("Reduced customer debt: customer={}, debt={}, amount={}, newRemaining={}", 
                   customer.getId(), debt.getId(), amount, newRemaining);
    }
    
    /**
     * الحصول على إجمالي الدين الحالي للعميل
     */
    private float getCurrentCustomerDebt(Customer customer) {
        if (customer == null) {
            logger.warn("getCurrentCustomerDebt called with null customer");
            return 0;
        }
        
        List<CustomerDebt> debts = customerDebtRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(
            customer.getId(), "ACTIVE");
        
        float totalDebt = (float) debts.stream()
            .mapToDouble(CustomerDebt::getRemainingAmount)
            .sum();
        
        logger.debug("getCurrentCustomerDebt: customerId={}, activeDebtsCount={}, totalDebt={}", 
                    customer.getId(), debts.size(), totalDebt);
        
        return totalDebt;
    }

    
    public Object getRefundDetails(Long refundId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        logger.info("Fetching refund details for refundId: {} and pharmacyId: {}", refundId, currentPharmacyId);
        
        SaleRefund refund = saleRefundRepo.findByIdAndPharmacyId(refundId, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Refund not found with ID: " + refundId + " in pharmacy: " + currentPharmacyId));
        
        try {
            SaleInvoice saleInvoice = refund.getSaleInvoice();
            Customer customer = saleInvoice.getCustomer();
            
           
            List<CustomerDebt> customerDebts = customerDebtRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(
                customer.getId(), "ACTIVE");
            float totalCustomerDebt = customerDebts.stream()
                    .map(CustomerDebt::getRemainingAmount)
                    .reduce(0f, Float::sum);
            
            Map<String, Object> result = saleRefundMapper.toRefundDetailsMap(refund, totalCustomerDebt, customerDebts.size());
            
            logger.info("Successfully retrieved refund details for refundId: {}", refundId);
            return result;
        } catch (Exception e) {
            logger.error("Error retrieving refund details for refundId {}: {}", refundId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve refund details: " + e.getMessage(), e);
        }
    }

    private void notifyDebtPaidFromRefund(CustomerDebt debt, Long saleId) {
        if (debt == null || debt.getCustomer() == null || debt.getCustomer().getPharmacy() == null) {
            return;
        }

        Long pharmacyId = debt.getCustomer().getPharmacy().getId();
        List<Long> recipients = getEligiblePharmacyStaff(pharmacyId);
        if (recipients.isEmpty()) {
            logger.debug("No eligible staff found for pharmacy {} to notify debt payment from refund {}", pharmacyId, debt.getId());
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("debtId", debt.getId());
        data.put("customerId", debt.getCustomer().getId());
        data.put("customerName", debt.getCustomer().getName());
        data.put("source", "REFUND");
        data.put("saleId", saleId);

        String body = String.format("تم إغلاق دين العميل %s بعد معالجة مرتجع.", debt.getCustomer().getName());

        for (Long userId : recipients) {
            try {
                NotificationRequest request = new NotificationRequest();
                request.setUserId(userId);
                request.setTitle("تم سداد دين عبر مرتجع");
                request.setBody(body);
                request.setNotificationType(NotificationType.DEBT_PAID);
                request.setData(new HashMap<>(data));
                notificationService.sendNotification(request);
            } catch (Exception e) {
                logger.warn("Failed to send debt paid notification from refund for user {}: {}", userId, e.getMessage());
                // Don't fail the refund transaction if notification fails
            }
        }
    }

    private List<Long> getEligiblePharmacyStaff(Long pharmacyId) {
        return employeeRepository.findByPharmacy_Id(pharmacyId).stream()
            .filter(employee -> employee.getRole() != null)
            .filter(employee -> {
                String roleName = employee.getRole().getName();
                return RoleConstants.PHARMACY_MANAGER.equals(roleName) || RoleConstants.PHARMACY_EMPLOYEE.equals(roleName);
            })
            .map(Employee::getId)
            .collect(Collectors.toList());
    }
    

  

 
    

} 