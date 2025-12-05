package com.Uqar.user.mapper;

import org.springframework.stereotype.Component;

import com.Uqar.user.dto.CustomerDTORequest;
import com.Uqar.user.dto.CustomerDTOResponse;
import com.Uqar.user.dto.CustomerDebtDTOResponse;
import com.Uqar.user.entity.Customer;
import com.Uqar.user.entity.CustomerDebt;
import com.Uqar.user.repository.CustomerDebtRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerMapper {

    private final CustomerDebtRepository customerDebtRepository;

    public CustomerMapper(CustomerDebtRepository customerDebtRepository) {
        this.customerDebtRepository = customerDebtRepository;
    }

    public CustomerDTOResponse toResponse(Customer customer) {
        if (customer == null) return null;
        
        CustomerDTOResponse response = CustomerDTOResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .pharmacyId(customer.getPharmacy() != null ? customer.getPharmacy().getId() : null)
                .notes(customer.getNotes())
                .build();

        // تحميل الديون من قاعدة البيانات بدلاً من الاعتماد على العلاقة LAZY
        List<CustomerDebt> debts = customerDebtRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId());
        
        if (debts != null && !debts.isEmpty()) {
            Float totalDebt = debts.stream()
                    .map(CustomerDebt::getAmount)
                    .reduce(0f, Float::sum);
            
            Float totalPaid = debts.stream()
                    .map(CustomerDebt::getPaidAmount)
                    .reduce(0f, Float::sum);
            
            int activeDebtsCount = (int) debts.stream()
                    .filter(debt -> "ACTIVE".equals(debt.getStatus()))
                    .count();

            response.setTotalDebt(totalDebt);
            response.setTotalPaid(totalPaid);
            response.setRemainingDebt(totalDebt - totalPaid);
            response.setActiveDebtsCount(activeDebtsCount);
            
            // تحويل الديون إلى DTOs
            List<CustomerDebtDTOResponse> debtDtos = debts.stream()
                    .map(this::toDebtResponse)
                    .collect(Collectors.toList());
            response.setDebts(debtDtos);
        } else {
            response.setTotalDebt(0.0f);
            response.setTotalPaid(0.0f);
            response.setRemainingDebt(0.0f);
            response.setActiveDebtsCount(0);
            response.setDebts(List.of());
        }

        return response;
    }

    public CustomerDebtDTOResponse toDebtResponse(CustomerDebt debt) {
        if (debt == null) return null;
        
        return CustomerDebtDTOResponse.builder()
                .id(debt.getId())
                .customerId(debt.getCustomer().getId())
                .customerName(debt.getCustomer().getName())
                .pharmacyId(debt.getCustomer().getPharmacy() != null ? debt.getCustomer().getPharmacy().getId() : null)
                .amount(debt.getAmount())
                .paidAmount(debt.getPaidAmount())
                .remainingAmount(debt.getRemainingAmount())
                .dueDate(debt.getDueDate())
                .notes(debt.getNotes())
                .status(debt.getStatus())
                .createdAt(debt.getCreatedAt())
                .paidAt(debt.getPaidAt())
                .paymentMethod(debt.getPaymentMethod())
                .build();
    }

    public Customer toEntity(CustomerDTORequest dto) {
        if (dto == null) return null;
        
        // إذا الاسم فارغ أو null، عيّن القيمة الافتراضية
        String name = (dto.getName() == null || dto.getName().isBlank()) ? "cash customer" : dto.getName();
        
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setAddress(dto.getAddress());
        customer.setNotes(dto.getNotes());
        return customer;
    }

    public void updateEntityFromDto(Customer customer, CustomerDTORequest dto) {
        if (dto == null || customer == null) return;
        
        String name = (dto.getName() == null || dto.getName().isBlank()) ? "cash customer" : dto.getName();
        customer.setName(name);
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setAddress(dto.getAddress());
        customer.setNotes(dto.getNotes());
    }
}
