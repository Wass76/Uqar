package com.Uqar.user.service;

import static com.Uqar.user.Enum.Currency.SYP;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.Uqar.moneybox.service.SalesIntegrationService;
import com.Uqar.notification.dto.NotificationRequest;
import com.Uqar.notification.enums.NotificationType;
import com.Uqar.notification.service.NotificationService;
import com.Uqar.product.Enum.PaymentMethod;
import com.Uqar.sale.entity.SaleInvoice;
import com.Uqar.sale.repo.SaleInvoiceRepository;
import com.Uqar.user.config.RoleConstants;
import com.Uqar.user.dto.CustomerDebtDTORequest;
import com.Uqar.user.dto.CustomerDebtDTOResponse;
import com.Uqar.utils.annotation.Audited;
import com.Uqar.user.dto.PayCustomerDebtsRequest;
import com.Uqar.user.dto.PayCustomerDebtsResponse;
import com.Uqar.user.dto.PayDebtDTORequest;
import com.Uqar.user.entity.Customer;
import com.Uqar.user.entity.CustomerDebt;
import com.Uqar.user.entity.Employee;
import com.Uqar.user.mapper.CustomerDebtMapper;
import com.Uqar.user.repository.CustomerDebtRepository;
import com.Uqar.user.repository.CustomerRepo;
import com.Uqar.user.repository.EmployeeRepository;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.utils.exception.ConflictException;
import com.Uqar.utils.exception.ResourceNotFoundException;
import com.Uqar.utils.exception.UnAuthorizedException;

@Service
public class CustomerDebtService extends BaseSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerDebtService.class);

    private final CustomerDebtRepository customerDebtRepository;
    private final CustomerRepo customerRepo;
    private final CustomerDebtMapper customerDebtMapper;
    private final SalesIntegrationService salesIntegrationService;
    private final SaleInvoiceRepository saleInvoiceRepository;
    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;
    public CustomerDebtService(CustomerDebtRepository customerDebtRepository,
                       CustomerRepo customerRepo,
                       CustomerDebtMapper customerDebtMapper,
                       SalesIntegrationService salesIntegrationService,
                       SaleInvoiceRepository saleInvoiceRepository,
                       NotificationService notificationService,
                       EmployeeRepository employeeRepository,
                       UserRepository userRepository) {
        super(userRepository);
        this.customerDebtRepository = customerDebtRepository;
        this.customerRepo = customerRepo;
        this.customerDebtMapper = customerDebtMapper;
        this.salesIntegrationService = salesIntegrationService;
        this.saleInvoiceRepository = saleInvoiceRepository;
        this.notificationService = notificationService;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    @Audited(action = "CREATE_CUSTOMER_DEBT", targetType = "CUSTOMER_DEBT", includeArgs = false)
    public CustomerDebtDTOResponse createDebt(CustomerDebtDTORequest request) {
       Long currentPharmacyId = getCurrentUserPharmacyId();
        validateDebtRequest(request);
        
        Customer customer = customerRepo.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));

        if (customer.getPharmacy().getId() != currentPharmacyId) {
            throw new UnAuthorizedException("You are not authorized to create debt for this customer");
        }

        CustomerDebt debt = customerDebtMapper.toEntity(request);
        debt.setCustomer(customer);
        debt.setPaidAmount(0.0f);
        debt.setRemainingAmount(request.getAmount());
        debt.setStatus("ACTIVE");
        
        CustomerDebt savedDebt = customerDebtRepository.save(debt);
        
        // Note: Debt creation doesn't affect MoneyBox as it's just a record of money owed
        // MoneyBox will only be affected when the debt is actually paid (cash payment)
        logger.info("Debt created for customer: {}, amount: {}", customer.getName(), request.getAmount());
        
        return customerDebtMapper.toResponse(savedDebt);
    }

 
    public List<CustomerDebtDTOResponse> getCustomerDebts(Long customerId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        // First check if customer exists and belongs to current pharmacy
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        if (!customer.getPharmacy().getId().equals(currentPharmacyId)) {
            throw new UnAuthorizedException("You are not authorized to access debts for this customer");
        }
        
        // Now search for debts
        List<CustomerDebt> debts = customerDebtRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        if (debts.isEmpty()) {
            throw new ResourceNotFoundException("No debts found for customer with ID: " + customerId);
        }

        return debts.stream()
                .map(customerDebtMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<CustomerDebtDTOResponse> getCustomerDebtsByStatus(Long customerId, String status) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        validateStatus(status);
        
        // First check if customer exists and belongs to current pharmacy
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        if (!customer.getPharmacy().getId().equals(currentPharmacyId)) {
            throw new UnAuthorizedException("You are not authorized to access debts for this customer");
        }
        
        // Now search for debts
        List<CustomerDebt> debts = customerDebtRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status);
        if(debts.isEmpty()){
            throw new ResourceNotFoundException("No debts found for customer with ID: " + customerId);
        }
        
        return debts.stream()
                .map(customerDebtMapper::toResponse)
                .collect(Collectors.toList());
    }


    public CustomerDebtDTOResponse getDebtById(Long debtId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();    
        CustomerDebt debt = customerDebtRepository.findById(debtId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found with ID: " + debtId));
            if (debt.getCustomer().getPharmacy().getId() != currentPharmacyId) {
                throw new UnAuthorizedException("You are not authorized to access this debt");
            }
        return customerDebtMapper.toResponse(debt);
    }

    @Transactional
    @Audited(action = "PAY_CUSTOMER_DEBT", targetType = "CUSTOMER_DEBT", includeArgs = false)
    public CustomerDebtDTOResponse payDebt(PayDebtDTORequest request) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        validatePaymentRequest(request);
        

        CustomerDebt debt = customerDebtRepository.findById(request.getDebtId())
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found with ID: " + request.getDebtId()));

        if (debt.getCustomer().getPharmacy().getId() != currentPharmacyId) {
            throw new UnAuthorizedException("You are not authorized to pay this debt");
        }

        if ("PAID".equals(debt.getStatus())) {
            throw new ConflictException("Debt is already paid");
        }

        Float paymentAmount = request.getPaymentAmount().floatValue();
        Float newPaidAmount = debt.getPaidAmount() + paymentAmount;
        Float newRemainingAmount = debt.getAmount() - newPaidAmount;

        if (newPaidAmount > debt.getAmount()) {
            throw new ConflictException("Payment amount (" + paymentAmount + 
                ") cannot exceed remaining debt amount (" + debt.getRemainingAmount() + 
                "). This would create a negative balance for the customer.");
        }

        debt.setPaidAmount(newPaidAmount);
        debt.setRemainingAmount(newRemainingAmount);
        debt.setPaymentMethod(request.getPaymentMethod());

        if (newRemainingAmount <= 0) {
            debt.setStatus("PAID");
            debt.setPaidAt(LocalDate.now());
        } else if (debt.getDueDate().isBefore(LocalDate.now())) {
            debt.setStatus("OVERDUE");
        }

        if (StringUtils.hasText(request.getNotes())) {
            String paymentNote = "Payment: " + paymentAmount + " - " + request.getNotes();
            debt.setNotes(debt.getNotes() != null ? debt.getNotes() + "\n" + paymentNote : paymentNote);
        }

        CustomerDebt savedDebt = customerDebtRepository.save(debt);
        
        // تسجيل العملية في الصندوق إذا كان الدفع نقدي - CRITICAL FIX: Use debt payment method
        if (request.getPaymentMethod() == PaymentMethod.CASH) {
            try {
                // Get current pharmacy ID for MoneyBox integration
                Long pharmacyId = getCurrentUserPharmacyId();
                salesIntegrationService.recordDebtPayment( // ✅ FIXED: Use recordDebtPayment instead of recordSalePayment
                    pharmacyId,
                    debt.getId(),
                    BigDecimal.valueOf(paymentAmount),
                    SYP
                );
                logger.info("Debt payment recorded in MoneyBox for debt: {} - Amount: {}", debt.getId(), paymentAmount);
            } catch (Exception e) {
                logger.warn("Failed to record debt payment in MoneyBox for debt {}: {}", debt.getId(), e.getMessage());
            }
        }
        
        notifyDebtPayment(savedDebt, paymentAmount);

        return customerDebtMapper.toResponse(savedDebt);
    }

   
    public List<CustomerDebtDTOResponse> getOverdueDebts() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<CustomerDebt> overdueDebts = customerDebtRepository.getOverdueDebtsByPharmacyId(currentPharmacyId);
        return overdueDebts.stream()
                .map(customerDebtMapper::toResponse)
                .collect(Collectors.toList());
    }

  
    public Float getTotalOverdueDebts() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        return customerDebtRepository.getTotalOverdueDebtsByPharmacyId(currentPharmacyId).floatValue();
    }


    public List<CustomerDebtDTOResponse> getDebtsByStatus(String status) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        validateStatus(status);
        List<CustomerDebt> debts = customerDebtRepository.findByStatusOrderByCreatedAtDesc(status);
        if (debts.isEmpty()) {
            throw new ResourceNotFoundException("No debts found with status: " + status);
        }
        if (!debts.stream().allMatch(debt -> debt.getCustomer().getPharmacy().getId() == currentPharmacyId)) {
            throw new UnAuthorizedException("You are not authorized to access debts with status: " + status);
        }
        return debts.stream()
                .map(customerDebtMapper::toResponse)
                .collect(Collectors.toList());
    }

 
    public List<CustomerDebtDTOResponse> getDebtsByDateRange(LocalDate startDate, LocalDate endDate) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<CustomerDebt> debts = customerDebtRepository.findByDateRangeAndPharmacyId(
            startDate.atStartOfDay(), 
            endDate.atTime(23, 59, 59), 
            currentPharmacyId
        );
        return debts.stream()
                .map(customerDebtMapper::toResponse)
                .collect(Collectors.toList());
    }

  
    public List<CustomerDebtDTOResponse> getDebtsByAmountRange(Float minAmount, Float maxAmount) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        List<CustomerDebt> debts = customerDebtRepository.findByAmountRangeAndPharmacyId(minAmount, maxAmount, currentPharmacyId);
        return debts.stream()
                .map(customerDebtMapper::toResponse)
                .collect(Collectors.toList());
    }

  
    @Transactional
    @Audited(action = "UPDATE_DEBT_STATUS", targetType = "CUSTOMER_DEBT", includeArgs = false)
    public CustomerDebtDTOResponse updateDebtStatus(Long debtId, String status) {
       Long currentPharmacyId = getCurrentUserPharmacyId();
        validateStatus(status);
        
        CustomerDebt debt = customerDebtRepository.findById(debtId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found with ID: " + debtId));

        if (debt.getCustomer().getPharmacy().getId() != currentPharmacyId) {
            throw new UnAuthorizedException("You are not authorized to update this debt");
        }

        debt.setStatus(status);
        
        if ("PAID".equals(status)) {
            debt.setPaidAt(LocalDate.now());
        }

        CustomerDebt savedDebt = customerDebtRepository.save(debt);
        return customerDebtMapper.toResponse(savedDebt);
    }

    
    @Transactional
    @Audited(action = "DELETE_DEBT", targetType = "CUSTOMER_DEBT", includeArgs = false)
    public void deleteDebt(Long debtId) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        CustomerDebt debt = customerDebtRepository.findById(debtId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found with ID: " + debtId));

        if (debt.getCustomer().getPharmacy().getId() != currentPharmacyId) {
            throw new UnAuthorizedException("You are not authorized to delete this debt");
        }
        
        if ("PAID".equals(debt.getStatus())) {
            throw new ConflictException("Cannot delete paid debt");
        }
        
        customerDebtRepository.delete(debt);
    }

  
    public DebtStatistics getDebtStatistics() {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        Object[] stats = customerDebtRepository.getDebtStatisticsByPharmacyId(currentPharmacyId);
        
        long totalDebts = ((Number) stats[0]).longValue();
        long activeDebts = ((Number) stats[1]).longValue();
        long paidDebts = ((Number) stats[2]).longValue();
        long overdueDebts = ((Number) stats[3]).longValue();
        
        Float totalAmount = ((Number) stats[4]).floatValue();
        Float totalPaid = ((Number) stats[5]).floatValue();
        Float totalRemaining = ((Number) stats[6]).floatValue();
        
        return DebtStatistics.builder()
                .totalDebts(totalDebts)
                .activeDebts(activeDebts)
                .paidDebts(paidDebts)
                .overdueDebts(overdueDebts)
                .totalAmount(totalAmount)
                .totalPaid(totalPaid)
                .totalRemaining(totalRemaining)
                .build();
    }

    // Validation methods
    private void validateDebtRequest(CustomerDebtDTORequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Debt request cannot be null");
        }
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Debt amount must be greater than zero");
        }
        if (request.getDueDate() == null) {
            throw new IllegalArgumentException("Due date is required");
        }
        
        // التحقق من أن الفاتورة المرتبطة بالدين ليست مرتجعة
        if (request.getSaleInvoiceId() != null) {
            SaleInvoice saleInvoice = saleInvoiceRepository.findById(request.getSaleInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale invoice not found with ID: " + request.getSaleInvoiceId()));
            if (saleInvoice.getStatus() != com.Uqar.sale.enums.InvoiceStatus.SOLD) {
                throw new IllegalArgumentException("Sale invoice is not in SOLD status");
            }
        }
            // يمكن إضافة validation هنا للتأكد من أن الفاتورة في حالة SOLD
            // وليس PARTIALLY_REFUNDED أو FULLY_REFUNDED
        
    }

    private void validatePaymentRequest(PayDebtDTORequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request cannot be null");
        }
        if (request.getDebtId() == null) {
            throw new IllegalArgumentException("Debt ID is required");
        }
        if (request.getPaymentAmount() == null || request.getPaymentAmount().floatValue() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (request.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }

  
    private void validateStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        if (!List.of("ACTIVE", "PAID", "OVERDUE").contains(status)) {
            throw new IllegalArgumentException("Invalid status. Must be ACTIVE, PAID, or OVERDUE");
        }
    }

    @Transactional
    @Audited(action = "AUTO_PAY_CUSTOMER_DEBTS", targetType = "CUSTOMER_DEBT", includeArgs = false)
    public PayCustomerDebtsResponse autoPayDebt(Long customerId, PayCustomerDebtsRequest request) {
        Long currentPharmacyId = getCurrentUserPharmacyId();
        
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        
        if (!customer.getPharmacy().getId().equals(currentPharmacyId)) {
            throw new UnAuthorizedException("Customer does not belong to the current pharmacy");
        }
        
        List<CustomerDebt> activeDebts = customerDebtRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, "ACTIVE");
        if (activeDebts.isEmpty()) {
            throw new ConflictException("No active debts found for this customer");
        }
        
        // حساب إجمالي الديون المتبقية
        float totalRemainingDebt = activeDebts.stream()
                .map(CustomerDebt::getRemainingAmount)
                .reduce(0f, Float::sum);
        
        float requestedPaymentAmount = request.getTotalPaymentAmount().floatValue();
        
        // التحقق من أن المبلغ المدفوع لا يتجاوز إجمالي الديون المتبقية
        if (requestedPaymentAmount > totalRemainingDebt) {
            throw new ConflictException("Payment amount (" + requestedPaymentAmount + 
                ") cannot exceed total remaining debt (" + totalRemainingDebt + 
                "). This would create a negative balance for the customer.");
        }
        
        // ترتيب الديون حسب FIFO (أولاً يدخل أولاً يخرج) - حسب تاريخ الإنشاء
        List<CustomerDebt> sortedDebts = activeDebts.stream()
                .sorted((d1, d2) -> d1.getCreatedAt().compareTo(d2.getCreatedAt()))
                .collect(Collectors.toList());
        
        float remainingPaymentAmount = requestedPaymentAmount;
        List<PayCustomerDebtsResponse.DebtPaymentDetail> paymentDetails = new ArrayList<>();
        
        for (CustomerDebt debt : sortedDebts) {
            if (remainingPaymentAmount <= 0) break;
            
            float debtRemainingAmount = debt.getRemainingAmount();
            float amountToPay = Math.min(remainingPaymentAmount, debtRemainingAmount);
            
            float newPaidAmount = debt.getPaidAmount() + amountToPay;
            float newRemainingAmount = debtRemainingAmount - amountToPay;
            
            debt.setPaidAmount(newPaidAmount);
            debt.setRemainingAmount(newRemainingAmount);
            debt.setPaymentMethod(request.getPaymentMethod());
            
            if (newRemainingAmount <= 0) {
                debt.setStatus("PAID");
                debt.setPaidAt(LocalDate.now());
            } else if (debt.getDueDate().isBefore(LocalDate.now())) {
                debt.setStatus("OVERDUE");
            }
            
            customerDebtRepository.save(debt);
            if (amountToPay > 0) {
                notifyDebtPayment(debt, amountToPay);
            }
            
            PayCustomerDebtsResponse.DebtPaymentDetail detail = PayCustomerDebtsResponse.DebtPaymentDetail.builder()
                    .debtId(debt.getId())
                    .originalAmount(debt.getAmount())
                    .amountPaid(amountToPay)
                    .remainingAmount(newRemainingAmount)
                    .status(debt.getStatus())
                    .dueDate(debt.getDueDate().toString())
                    .build();
            paymentDetails.add(detail);
            
            remainingPaymentAmount -= amountToPay;
        }
        
        if (request.getPaymentMethod() == PaymentMethod.CASH) {
            try {
                float actualPaidAmount = request.getTotalPaymentAmount().floatValue() - remainingPaymentAmount;
                // Get current pharmacy ID for MoneyBox integration
                Long pharmacyId = getCurrentUserPharmacyId();
                salesIntegrationService.recordDebtPayment( // ✅ FIXED: Use recordDebtPayment for multiple debt payments
                    pharmacyId,
                    customerId, // Using customerId as reference ID for multiple debts
                    BigDecimal.valueOf(actualPaidAmount),
                    SYP
                );
                logger.info("Multiple debt payments recorded in Money Box for customer: {} - Amount: {}", 
                           customerId, actualPaidAmount);
            } catch (Exception e) {
                logger.warn("Failed to record debt payment in Money Box for customer {}: {}", 
                           customerId, e.getMessage());
            }
        }
        
        // حساب إجمالي الديون المتبقية بعد الدفع
        float finalTotalRemainingDebt = activeDebts.stream()
                .map(CustomerDebt::getRemainingAmount)
                .reduce(0f, Float::sum);
        
        return PayCustomerDebtsResponse.builder()
                .customerId(customerId)
                .customerName(customer.getName())
                .totalPaymentAmount(request.getTotalPaymentAmount().floatValue())
                .totalRemainingDebt(finalTotalRemainingDebt)
                .debtPayments(paymentDetails)
                .notes(request.getNotes())
                .build();
    }
    
    private void notifyDebtPayment(CustomerDebt debt, float paymentAmount) {
        if (paymentAmount <= 0 || debt == null || debt.getCustomer() == null) {
            return;
        }

        Long pharmacyId = debt.getCustomer().getPharmacy().getId();
        List<Long> recipients = getEligiblePharmacyStaff(pharmacyId);
        if (recipients.isEmpty()) {
            logger.debug("No eligible staff found for pharmacy {} to notify about debt payment {}", pharmacyId, debt.getId());
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("debtId", debt.getId());
        data.put("customerId", debt.getCustomer().getId());
        data.put("customerName", debt.getCustomer().getName());
        data.put("paymentAmount", paymentAmount);
        data.put("remainingAmount", debt.getRemainingAmount());
        data.put("status", debt.getStatus());

        String title = "تم تسجيل دفعة دين";
        String body = String.format("العميل %s سدّد %.2f من دينه. المتبقي %.2f.",
                debt.getCustomer().getName(),
                paymentAmount,
                Math.max(debt.getRemainingAmount(), 0f));

        for (Long userId : recipients) {
            try {
                NotificationRequest request = new NotificationRequest();
                request.setUserId(userId);
                request.setTitle(title);
                request.setBody(body);
                request.setNotificationType(NotificationType.DEBT_PAID);
                request.setData(new HashMap<>(data));
                notificationService.sendNotification(request);
            } catch (Exception e) {
                logger.warn("Failed to send debt payment notification for user {}: {}", userId, e.getMessage());
                // Don't fail the payment transaction if notification fails
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

    @lombok.Data
    @lombok.Builder
    public static class DebtStatistics {
        private long totalDebts;
        private long activeDebts;
        private long paidDebts;
        private long overdueDebts;
        private Float totalAmount;
        private Float totalPaid;
        private Float totalRemaining;
    }
} 