package com.Uqar.user.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.Uqar.user.dto.CustomerDTORequest;
import com.Uqar.user.dto.CustomerDTOResponse;
import com.Uqar.user.entity.Customer;
import com.Uqar.user.entity.CustomerDebt;
import com.Uqar.user.entity.Employee;
import com.Uqar.user.entity.User;
import com.Uqar.user.mapper.CustomerMapper;
import com.Uqar.user.repository.CustomerDebtRepository;
import com.Uqar.user.repository.CustomerRepo;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.utils.exception.ConflictException;
import com.Uqar.utils.exception.UnAuthorizedException;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CustomerService extends BaseSecurityService {

    private final CustomerRepo customerRepo;
    private final CustomerMapper customerMapper;
    private final CustomerDebtRepository customerDebtRepository;

    public CustomerService(CustomerRepo customerRepo, CustomerMapper customerMapper, 
                         CustomerDebtRepository customerDebtRepository, UserRepository userRepository) {
        super(userRepository);
        this.customerRepo = customerRepo;
        this.customerMapper = customerMapper;
        this.customerDebtRepository = customerDebtRepository;
    }

    public List<CustomerDTOResponse> getAllCustomers() {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        return customerRepo.findByPharmacyId(currentPharmacyId)
                .stream()
                .filter(customer -> !isCashCustomer(customer)) // Filter out cash customer
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    private boolean isCashCustomer(Customer customer) {
        return customer.getName() != null && 
               customer.getName().toLowerCase().trim().equals("cash customer");
    }

    public CustomerDTOResponse getCustomerById(Long id) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        Customer customer = customerRepo.findByIdAndPharmacyId(id, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + id + " in this pharmacy"));
        return customerMapper.toResponse(customer);
    }

    public CustomerDTOResponse getCustomerByName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new ConflictException("Customer name cannot be empty");
        }
        
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        Customer customer = customerRepo.findByNameAndPharmacyId(name, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with name: " + name + " in this pharmacy"));
        return customerMapper.toResponse(customer);
    }

    public List<CustomerDTOResponse> searchCustomersByName(String name) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        
        if (!StringUtils.hasText(name)) {
            return customerRepo.findByPharmacyId(currentPharmacyId)
                    .stream()
                    .filter(customer -> !isCashCustomer(customer)) // Filter out cash customer
                    .map(customerMapper::toResponse)
                    .collect(Collectors.toList());
        }
        
        return customerRepo.findByNameContainingIgnoreCaseAndPharmacyId(name, currentPharmacyId)
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }



    public List<CustomerDTOResponse> getCustomersWithActiveDebts() {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        
        // استخدام CustomerDebtRepository للحصول على الزبائن الذين لديهم ديون نشطة
        List<Customer> customersWithActiveDebts = customerDebtRepository.findCustomersWithActiveDebtsByPharmacyId(currentPharmacyId);
        
        return customersWithActiveDebts.stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersWithOverdueDebts() {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        
        // استخدام CustomerDebtRepository للحصول على الزبائن الذين لديهم ديون متأخرة
        List<Customer> customersWithOverdueDebts = customerDebtRepository.findCustomersWithOverdueDebtsByPharmacyId(currentPharmacyId);
        
        return customersWithOverdueDebts.stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CustomerDTOResponse createCustomer(CustomerDTORequest dto) {
        validateCustomerRequest(dto);
        
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can create customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        
        if (StringUtils.hasText(dto.getName()) && !"cash customer".equals(dto.getName())) {
            customerRepo.findByNameAndPharmacyId(dto.getName(), currentPharmacyId)
                    .ifPresent(existingCustomer -> { 
                        throw new ConflictException("Customer with name '" + dto.getName() + "' already exists in this pharmacy");
                    });
        }
        
        Customer customer = customerMapper.toEntity(dto);
        customer.setPharmacy(employee.getPharmacy());
        
        // تعيين createdBy يدوياً
        customer.setCreatedBy(currentUser.getId());
        customer.setCreatedByUserType(currentUser.getClass().getSimpleName());
        
        customer = customerRepo.save(customer);
        return customerMapper.toResponse(customer);
    }

    public CustomerDTOResponse updateCustomer(Long id, CustomerDTORequest dto) {
        validateCustomerRequest(dto);
        
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can update customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        Customer customer = customerRepo.findByIdAndPharmacyId(id, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Customer with ID " + id + " not found in this pharmacy"));

        if (StringUtils.hasText(dto.getName()) && !dto.getName().equals(customer.getName())) {
            customerRepo.findByNameAndPharmacyId(dto.getName(), currentPharmacyId)
                    .ifPresent(existingCustomer -> {
                        if (!existingCustomer.getId().equals(id)) {
                            throw new ConflictException("Customer with name '" + dto.getName() + "' already exists in this pharmacy");
                        }
                    });
        }

        customerMapper.updateEntityFromDto(customer, dto);
        customer = customerRepo.save(customer);
        return customerMapper.toResponse(customer);
    }

    public void deleteCustomer(Long id) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can delete customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        // التحقق من وجود العميل
        customerRepo.findByIdAndPharmacyId(id, currentPharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Customer with ID " + id + " not found in this pharmacy"));
        
        // التحقق من الديون النشطة باستخدام CustomerDebtRepository
        List<CustomerDebt> activeDebts = customerDebtRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(id, "ACTIVE");
        boolean hasActiveDebts = activeDebts.stream()
                .anyMatch(debt -> debt.getRemainingAmount() > 0);
            
            if (hasActiveDebts) {
                throw new ConflictException("Cannot delete customer with active debts. Please settle all debts first.");
        }
        
        customerRepo.deleteById(id);
    }

    private void validateCustomerRequest(CustomerDTORequest dto) {
        if (dto == null) {
            throw new ConflictException("Customer request cannot be null");
        }
        
        if (StringUtils.hasText(dto.getPhoneNumber())) {
            if (!dto.getPhoneNumber().matches("^[0-9]{10}$")) {
                throw new ConflictException("Phone number must be 10 digits");
            }
        }
    }

    public List<CustomerDTOResponse> getCustomersByDebtRange(Float minDebt, Float maxDebt) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        
        // استخدام CustomerDebtRepository للحصول على الزبائن الذين لديهم ديون نشطة
        List<Customer> customersWithDebts = customerDebtRepository.findCustomersWithActiveDebtsByPharmacyId(currentPharmacyId);
        
        return customersWithDebts.stream()
                .map(customerMapper::toResponse)
                .filter(response -> {
                    Float remainingDebt = response.getRemainingDebt();
                    return remainingDebt >= minDebt && remainingDebt <= maxDebt;
                })
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersByPharmacyId(Long pharmacyId) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        validatePharmacyAccess(pharmacyId);
        
        return customerRepo.findByPharmacyId(pharmacyId)
                .stream()
                .filter(customer -> !isCashCustomer(customer)) // Filter out cash customer
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CustomerDTOResponse getCustomerByIdAndPharmacyId(Long id, Long pharmacyId) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        validatePharmacyAccess(pharmacyId);
        
        Customer customer = customerRepo.findByIdAndPharmacyId(id, pharmacyId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + id + " in pharmacy: " + pharmacyId));
        return customerMapper.toResponse(customer);
    }

    public List<CustomerDTOResponse> searchCustomersByNameAndPharmacyId(String name, Long pharmacyId) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        validatePharmacyAccess(pharmacyId);
        
        if (!StringUtils.hasText(name)) {
            return customerRepo.findByPharmacyId(pharmacyId)
                    .stream()
                    .filter(customer -> !isCashCustomer(customer)) // Filter out cash customer
                    .map(customerMapper::toResponse)
                    .collect(Collectors.toList());
        }
        
        return customerRepo.findByNameContainingIgnoreCaseAndPharmacyId(name, pharmacyId)
                .stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersWithDebtsByPharmacyId(Long pharmacyId) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        validatePharmacyAccess(pharmacyId);
        
        // استخدام CustomerDebtRepository للحصول على الزبائن الذين لديهم ديون نشطة
        List<Customer> customersWithDebts = customerDebtRepository.findCustomersWithActiveDebtsByPharmacyId(pharmacyId);
        
        return customersWithDebts.stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersWithActiveDebtsByPharmacyId(Long pharmacyId) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        validatePharmacyAccess(pharmacyId);
        
        // استخدام CustomerDebtRepository للحصول على الزبائن الذين لديهم ديون نشطة
        List<Customer> customersWithActiveDebts = customerDebtRepository.findCustomersWithActiveDebtsByPharmacyId(pharmacyId);
        
        return customersWithActiveDebts.stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersByDebtRangeAndPharmacyId(Float minDebt, Float maxDebt, Long pharmacyId) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        validatePharmacyAccess(pharmacyId);
        
        // استخدام CustomerDebtRepository للحصول على الزبائن الذين لديهم ديون نشطة
        List<Customer> customersWithDebts = customerDebtRepository.findCustomersWithActiveDebtsByPharmacyId(pharmacyId);
        
        return customersWithDebts.stream()
                .map(customerMapper::toResponse)
                .filter(response -> {
                    Float remainingDebt = response.getRemainingDebt();
                    return remainingDebt >= minDebt && remainingDebt <= maxDebt;
                })
                .collect(Collectors.toList());
    }

    public Object getDebtStatistics() {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access debt statistics");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        
        // استخدام CustomerDebtRepository للحصول على إحصائيات الديون
        return customerDebtRepository.getDebtStatisticsByPharmacyId(currentPharmacyId);
    }

    public List<CustomerDTOResponse> getAllCustomersWithDebts() {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        
        // استخدام CustomerDebtRepository للحصول على جميع الزبائن الذين لديهم ديون (بما في ذلك الصفرية)
        List<Customer> allCustomersWithDebts = customerDebtRepository.findAllCustomersWithDebtsByPharmacyId(currentPharmacyId);
        
        return allCustomersWithDebts.stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<CustomerDTOResponse> getCustomersWithZeroDebts() {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access customers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = employee.getPharmacy().getId();
        
        // استخدام CustomerDebtRepository للحصول على الزبائن الذين لديهم ديون صفرية
        List<Customer> customersWithZeroDebts = customerDebtRepository.findCustomersWithZeroDebtsByPharmacyId(currentPharmacyId);
        
        return customersWithZeroDebts.stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }
} 