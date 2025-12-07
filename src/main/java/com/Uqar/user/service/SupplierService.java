package com.Uqar.user.service;

import com.Uqar.purchase.repository.PurchaseOrderRepo;
import com.Uqar.user.dto.SupplierDTORequest;
import com.Uqar.user.dto.SupplierDTOResponse;
import com.Uqar.user.entity.Supplier;
import com.Uqar.user.entity.User;
import com.Uqar.user.entity.Employee;
import com.Uqar.user.entity.Pharmacy;
import com.Uqar.user.mapper.SupplierMapper;
import com.Uqar.user.repository.SupplierRepository;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.utils.exception.ResourceNotFoundException;
import com.Uqar.utils.exception.ConflictException;
import com.Uqar.utils.exception.UnAuthorizedException;
import com.Uqar.purchase.repository.PurchaseInvoiceRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierService extends BaseSecurityService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final PurchaseInvoiceRepo purchaseInvoiceRepo;
    private final PurchaseOrderRepo purchaseOrderRepo;

    public SupplierService(SupplierRepository supplierRepository,
                           SupplierMapper supplierMapper,
                           UserRepository userRepository,
                           PurchaseInvoiceRepo purchaseInvoiceRepo, PurchaseOrderRepo purchaseOrderRepo) {
        super(userRepository);
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
        this.purchaseInvoiceRepo = purchaseInvoiceRepo;
        this.purchaseOrderRepo = purchaseOrderRepo;
    }

    @Transactional
    public SupplierDTOResponse create(SupplierDTORequest request) {
        // Validate that the current user is a pharmacy employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can create suppliers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Pharmacy pharmacy = employee.getPharmacy();
        
        // Check for name uniqueness within the pharmacy context
        if (supplierRepository.existsByNameAndPharmacyId(request.getName(), pharmacy.getId())) {
            throw new ConflictException("Supplier name must be unique within this pharmacy");
        }
        
        Supplier supplier = supplierMapper.toEntity(request, pharmacy);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierDTOResponse update(Long id, SupplierDTORequest request) {
        // Validate that the current user is a pharmacy employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can update suppliers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long pharmacyId = employee.getPharmacy().getId();
        
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        
        // Validate that the supplier belongs to the current user's pharmacy
        if (!supplier.getPharmacy().getId().equals(pharmacyId)) {
            throw new UnAuthorizedException("You can only update suppliers from your own pharmacy");
        }
        
        if (!supplier.getName().equals(request.getName()) && supplierRepository.existsByNameAndPharmacyIdAndIdNot(request.getName(), pharmacyId, id)) {
            throw new ConflictException("Supplier name must be unique within this pharmacy");
        }
        
        supplierMapper.updateEntity(supplier, request);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void delete(Long id) {
        // Validate that the current user is a pharmacy employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can delete suppliers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long pharmacyId = employee.getPharmacy().getId();
        
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        
        // Validate that the supplier belongs to the current user's pharmacy
        if (!supplier.getPharmacy().getId().equals(pharmacyId)) {
            throw new UnAuthorizedException("You can only delete suppliers from your own pharmacy");
        }
        
        // Check if supplier has any purchase invoices
        if (purchaseInvoiceRepo.countByPharmacyIdAndSupplierId(pharmacyId, id) > 0) {
            throw new ConflictException("Cannot delete supplier. Supplier has associated purchase invoices. Please delete all purchase invoices first.");
        }

        // Check if supplier has any purchase order
        if (purchaseOrderRepo.countByPharmacyIdAndSupplierId(pharmacyId, id) > 0) {
            throw new ConflictException("Cannot delete supplier. Supplier has associated purchase invoices. Please delete all purchase invoices first.");
        }
        
        supplierRepository.delete(supplier);
    }

    public SupplierDTOResponse getById(Long id) {
        // Validate that the current user is a pharmacy employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access suppliers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long pharmacyId = employee.getPharmacy().getId();
        
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        
        // Validate that the supplier belongs to the current user's pharmacy
        if (!supplier.getPharmacy().getId().equals(pharmacyId)) {
            throw new UnAuthorizedException("You can only access suppliers from your own pharmacy");
        }
        
        return supplierMapper.toResponse(supplier);
    }

    public List<SupplierDTOResponse> listAll() {
        // Validate that the current user is a pharmacy employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access suppliers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long pharmacyId = employee.getPharmacy().getId();
        
        return supplierRepository.findByPharmacyId(pharmacyId).stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<SupplierDTOResponse> searchByName(String name) {
        // Validate that the current user is a pharmacy employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can search suppliers");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long pharmacyId = employee.getPharmacy().getId();
        
        return supplierRepository.findByPharmacyIdAndNameContainingIgnoreCase(pharmacyId, name).stream()
                .map(supplierMapper::toResponse)
                .collect(Collectors.toList());
    }
} 