package com.Uqar.purchase.service;

import com.Uqar.product.Enum.OrderStatus;
import com.Uqar.product.Enum.ProductType;
import com.Uqar.product.dto.*;
import com.Uqar.purchase.dto.PurchaseOrderDTORequest;
import com.Uqar.purchase.dto.PurchaseOrderDTOResponse;
import com.Uqar.purchase.dto.PurchaseOrderItemDTORequest;
import com.Uqar.purchase.entity.PurchaseOrder;
import com.Uqar.purchase.entity.PurchaseOrderItem;
import com.Uqar.product.entity.PharmacyProduct;
import com.Uqar.product.entity.MasterProduct;
import com.Uqar.purchase.mapper.PurchaseOrderMapper;
import com.Uqar.purchase.repository.PurchaseOrderRepo;
import com.Uqar.product.repo.PharmacyProductRepo;
import com.Uqar.product.repo.MasterProductRepo;
import com.Uqar.user.entity.Pharmacy;
import com.Uqar.user.entity.Supplier;
import com.Uqar.user.entity.User;
import com.Uqar.user.entity.Employee;
import com.Uqar.user.repository.SupplierRepository;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.user.service.BaseSecurityService;
import com.Uqar.moneybox.service.ExchangeRateService;
import com.Uqar.user.Enum.Currency;
import com.Uqar.utils.annotation.Audited;
import com.Uqar.utils.exception.ConflictException;
import com.Uqar.utils.exception.ResourceNotFoundException;
import com.Uqar.utils.exception.UnAuthorizedException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

@Service
@Transactional
public class PurchaseOrderService extends BaseSecurityService {
    private final PurchaseOrderRepo purchaseOrderRepo;
    private final PharmacyProductRepo pharmacyProductRepo;
    private final MasterProductRepo masterProductRepo;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final ExchangeRateService exchangeRateService;

    public PurchaseOrderService(PurchaseOrderRepo purchaseOrderRepo,
                              PharmacyProductRepo pharmacyProductRepo,
                              MasterProductRepo masterProductRepo,
                              SupplierRepository supplierRepository,
                              PurchaseOrderMapper purchaseOrderMapper,
                              UserRepository userRepository,
                              ExchangeRateService exchangeRateService) {
        super(userRepository);
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.pharmacyProductRepo = pharmacyProductRepo;
        this.masterProductRepo = masterProductRepo;
        this.supplierRepository = supplierRepository;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.exchangeRateService = exchangeRateService;
    }

    @Transactional
    @Audited(action = "CREATE_PURCHASE_ORDER", targetType = "PURCHASE_ORDER", includeArgs = false)
    public PurchaseOrderDTOResponse create(PurchaseOrderDTORequest request, String language) {
        Employee employee = validateAndGetEmployee();
        Pharmacy pharmacy = employee.getPharmacy();
        Supplier supplier = getSupplier(request.getSupplierId());
        List<PurchaseOrderItem> items = createOrderItems(request);
        
        PurchaseOrder order = purchaseOrderMapper.toEntity(request, supplier, pharmacy, items);
        PurchaseOrder saved = purchaseOrderRepo.save(order);
        
        List<PharmacyProduct> pharmacyProducts = getPharmacyProducts(saved);
        List<MasterProduct> masterProducts = getMasterProducts(saved);
        
        return purchaseOrderMapper.toResponse(saved, pharmacyProducts, masterProducts, language);
    }

    public PurchaseOrderDTOResponse create(PurchaseOrderDTORequest request) {
        return create(request, "ar");
    }

    public PurchaseOrderDTOResponse getById(Long id, String language) {
        Employee employee = validateAndGetEmployee();
        Long pharmacyId = employee.getPharmacy().getId();
        
        PurchaseOrder order = getOrderByIdAndValidatePharmacy(id, pharmacyId);
        
        List<PharmacyProduct> pharmacyProducts = getPharmacyProducts(order);
        List<MasterProduct> masterProducts = getMasterProducts(order);
        
        return purchaseOrderMapper.toResponse(order, pharmacyProducts, masterProducts, language);
    }

    public PurchaseOrderDTOResponse getById(Long id) {
        return getById(id, "ar");
    }

    @Transactional
    public PurchaseOrderDTOResponse edit(Long id, PurchaseOrderDTORequest request, String language) {
        Employee employee = validateAndGetEmployee();
        Long pharmacyId = employee.getPharmacy().getId();
        
        PurchaseOrder order = getOrderByIdAndValidatePharmacy(id, pharmacyId);
        validateOrderCanBeEdited(order);
        
        Supplier supplier = getSupplier(request.getSupplierId());
        List<PurchaseOrderItem> newItems = createOrderItems(request);
        
        // Set the purchaseOrder reference on each new item
        newItems.forEach(item -> item.setPurchaseOrder(order));
        
        // Update order properties
        order.setSupplier(supplier);
        order.setCurrency(request.getCurrency());
        
        // Properly manage the items collection to avoid Hibernate cascade issues
        order.getItems().clear();
        order.getItems().addAll(newItems);
        
        // Recalculate total with proper currency handling
        Currency orderCurrency = order.getCurrency();
        BigDecimal totalInSYP = newItems.stream()
            .map(item -> {
                // Convert each item price to SYP for consistent calculation
                BigDecimal itemPriceInSYP = convertItemPriceToSYP(item, orderCurrency);
                return BigDecimal.valueOf(item.getQuantity()).multiply(itemPriceInSYP);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Convert total from SYP to order currency for storage
        BigDecimal totalInOrderCurrency = exchangeRateService.convertFromSYP(totalInSYP, orderCurrency);
        order.setTotal(totalInOrderCurrency.doubleValue());
        
        PurchaseOrder saved = purchaseOrderRepo.save(order);
        
        List<PharmacyProduct> pharmacyProducts = getPharmacyProducts(saved);
        List<MasterProduct> masterProducts = getMasterProducts(saved);
        
        return purchaseOrderMapper.toResponse(saved, pharmacyProducts, masterProducts, language);
    }

    public PurchaseOrderDTOResponse edit(Long id, PurchaseOrderDTORequest request) {
        return edit(id, request, "ar");
    }

    public PaginationDTO<PurchaseOrderDTOResponse> listAllPaginated(int page, int size, String language) {
        Employee employee = validateAndGetEmployee();
        Long pharmacyId = employee.getPharmacy().getId();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrder> orderPage = purchaseOrderRepo.findByPharmacyId(pharmacyId, pageable);
        
        List<PurchaseOrder> orders = orderPage.getContent();
        List<PharmacyProduct> allPharmacyProducts = getAllPharmacyProducts(orders);
        List<MasterProduct> allMasterProducts = getAllMasterProducts(orders);
        
        List<PurchaseOrderDTOResponse> responses = orders.stream()
            .map(order -> purchaseOrderMapper.toResponse(order, allPharmacyProducts, allMasterProducts, language))
            .toList();
            
        return new PaginationDTO<>(responses, page, size, orderPage.getTotalElements());
    }

    public PaginationDTO<PurchaseOrderDTOResponse> listAllPaginated(int page, int size) {
        return listAllPaginated(page, size, "ar");
    }



    public PaginationDTO<PurchaseOrderDTOResponse> getByStatusPaginated(OrderStatus status, int page, int size, String language) {
        Employee employee = validateAndGetEmployee();
        Long pharmacyId = employee.getPharmacy().getId();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrder> orderPage = purchaseOrderRepo.findByPharmacyIdAndStatus(pharmacyId, status, pageable);
        
        List<PurchaseOrder> orders = orderPage.getContent();
        List<PharmacyProduct> allPharmacyProducts = getAllPharmacyProducts(orders);
        List<MasterProduct> allMasterProducts = getAllMasterProducts(orders);
        
        List<PurchaseOrderDTOResponse> responses = orders.stream()
            .map(order -> purchaseOrderMapper.toResponse(order, allPharmacyProducts, allMasterProducts, language))
            .toList();
            
        return new PaginationDTO<>(responses, page, size, orderPage.getTotalElements());
    }

    public PaginationDTO<PurchaseOrderDTOResponse> getByStatusPaginated(OrderStatus status, int page, int size) {
        return getByStatusPaginated(status, page, size, "ar");
    }

    // New method for filtering by time range
    public PaginationDTO<PurchaseOrderDTOResponse> getByTimeRangePaginated(
            LocalDateTime startDate, LocalDateTime endDate, int page, int size, String language) {
        Employee employee = validateAndGetEmployee();
        Long pharmacyId = employee.getPharmacy().getId();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrder> orderPage = purchaseOrderRepo.findByPharmacyIdAndCreatedAtBetween(
            pharmacyId, startDate, endDate, pageable);
        
        List<PurchaseOrder> orders = orderPage.getContent();
        List<PharmacyProduct> allPharmacyProducts = getAllPharmacyProducts(orders);
        List<MasterProduct> allMasterProducts = getAllMasterProducts(orders);
        
        List<PurchaseOrderDTOResponse> responses = orders.stream()
            .map(order -> purchaseOrderMapper.toResponse(order, allPharmacyProducts, allMasterProducts, language))
            .toList();
            
        return new PaginationDTO<>(responses, page, size, orderPage.getTotalElements());
    }

    public PaginationDTO<PurchaseOrderDTOResponse> getByTimeRangePaginated(
            LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        return getByTimeRangePaginated(startDate, endDate, page, size, "ar");
    }

    // New method for filtering by supplier
    public PaginationDTO<PurchaseOrderDTOResponse> getBySupplierPaginated(
            Long supplierId, int page, int size, String language) {
        Employee employee = validateAndGetEmployee();
        Long pharmacyId = employee.getPharmacy().getId();
        
        // Validate supplier exists
        getSupplier(supplierId);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseOrder> orderPage = purchaseOrderRepo.findByPharmacyIdAndSupplierId(
            pharmacyId, supplierId, pageable);
        
        List<PurchaseOrder> orders = orderPage.getContent();
        List<PharmacyProduct> allPharmacyProducts = getAllPharmacyProducts(orders);
        List<MasterProduct> allMasterProducts = getAllMasterProducts(orders);
        
        List<PurchaseOrderDTOResponse> responses = orders.stream()
            .map(order -> purchaseOrderMapper.toResponse(order, allPharmacyProducts, allMasterProducts, language))
            .toList();
            
        return new PaginationDTO<>(responses, page, size, orderPage.getTotalElements());
    }

    public PaginationDTO<PurchaseOrderDTOResponse> getBySupplierPaginated(
            Long supplierId, int page, int size) {
        return getBySupplierPaginated(supplierId, page, size, "ar");
    }

    @Transactional
    public void cancel(Long id) {
        Employee employee = validateAndGetEmployee();
        Long pharmacyId = employee.getPharmacy().getId();
        
        PurchaseOrder order = getOrderByIdAndValidatePharmacy(id, pharmacyId);
        validateOrderCanBeCancelled(order);
        
        order.setStatus(OrderStatus.CANCELLED);
        purchaseOrderRepo.save(order);
    }

    // Private helper methods for validation and authorization
    private Employee validateAndGetEmployee() {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can perform this operation");
        }
        
        Employee employee = (Employee) currentUser;
        if (employee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        return employee;
    }

    private Supplier getSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }

    private PurchaseOrder getOrderByIdAndValidatePharmacy(Long id, Long pharmacyId) {
        PurchaseOrder order = purchaseOrderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        
        if (!order.getPharmacy().getId().equals(pharmacyId)) {
            throw new UnAuthorizedException("You can only access purchase orders from your own pharmacy");
        }
        
        return order;
    }

    private void validateOrderCanBeCancelled(PurchaseOrder order) {
        if (order.getStatus() == OrderStatus.DONE) {
            throw new ConflictException("Cannot cancel a completed order");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException("Order is already cancelled");
        }
    }

    private void validateOrderCanBeEdited(PurchaseOrder order) {
        if (order.getStatus() == OrderStatus.DONE) {
            throw new ConflictException("Cannot edit a completed order");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException("Cannot edit a cancelled order");
        }
    }

    // Private helper methods for order item creation
    private List<PurchaseOrderItem> createOrderItems(PurchaseOrderDTORequest request) {
        Currency orderCurrency = request.getCurrency();
        List<PurchaseOrderItem> items = request.getItems().stream()
            .map(item -> createOrderItem(item, orderCurrency))
            .collect(Collectors.toList());
        
        if (items.isEmpty()) {
            throw new ConflictException("Order must have at least one item");
        }
        
        return items;
    }

    private PurchaseOrderItem createOrderItem(PurchaseOrderItemDTORequest itemDto, Currency orderCurrency) {
        String barcode = itemDto.getBarcode();
        Double price = itemDto.getPrice();
        
        if (itemDto.getProductType() == ProductType.PHARMACY) {
            PharmacyProduct product = getPharmacyProduct(itemDto.getProductId());
            barcode = getBarcodeForPharmacyProduct(product, barcode);
            price = getPriceForPharmacyProduct(product, price);
        } else if (itemDto.getProductType() == ProductType.MASTER) {
            MasterProduct product = getMasterProduct(itemDto.getProductId());
            barcode = getBarcodeForMasterProduct(product, barcode);
            price = getPriceForMasterProduct(product, orderCurrency);
        } else {
            throw new ConflictException("Invalid productType: " + itemDto.getProductType());
        }
        
        return purchaseOrderMapper.toItemEntity(itemDto, barcode, price);
    }

    private PharmacyProduct getPharmacyProduct(Long productId) {
        return pharmacyProductRepo.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("PharmacyProduct not found: " + productId));
    }

    private MasterProduct getMasterProduct(Long productId) {
        return masterProductRepo.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("MasterProduct not found: " + productId));
    }

    private String getBarcodeForPharmacyProduct(PharmacyProduct product, String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return product.getBarcodes().stream().findFirst().map(b -> b.getBarcode()).orElse(null);
        }
        return barcode;
    }

    private String getBarcodeForMasterProduct(MasterProduct product, String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return product.getBarcode();
        }
        return barcode;
    }

    private Double getPriceForPharmacyProduct(PharmacyProduct product, Double price) {
        if (price == null) {
            return (double) product.getRefPurchasePrice();
        }
        return price;
    }

    private Double getPriceForMasterProduct(MasterProduct product, Currency orderCurrency) {
        double priceInSYP = product.getRefPurchasePrice();
        
        // If the order currency is SYP, return as is
        if (orderCurrency == Currency.SYP) {
            return priceInSYP;
        }
        
        // Convert from SYP to the order currency
        BigDecimal priceInSYPBigDecimal = BigDecimal.valueOf(priceInSYP);
        BigDecimal convertedPrice = exchangeRateService.convertFromSYP(priceInSYPBigDecimal, orderCurrency);
        
        return convertedPrice.doubleValue();
    }

    // Private helper methods for product retrieval
    private List<PharmacyProduct> getPharmacyProducts(PurchaseOrder order) {
        List<Long> pharmacyProductIds = order.getItems().stream()
            .filter(i -> i.getProductType() == ProductType.PHARMACY)
            .map(PurchaseOrderItem::getProductId)
            .toList();
        return pharmacyProductRepo.findAllById(pharmacyProductIds);
    }

    private List<MasterProduct> getMasterProducts(PurchaseOrder order) {
        List<Long> masterProductIds = order.getItems().stream()
            .filter(i -> i.getProductType() == ProductType.MASTER)
            .map(PurchaseOrderItem::getProductId)
            .toList();
        return masterProductRepo.findAllById(masterProductIds);
    }

    private List<PharmacyProduct> getAllPharmacyProducts(List<PurchaseOrder> orders) {
        List<Long> allPharmacyProductIds = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.PHARMACY)
            .map(PurchaseOrderItem::getProductId)
            .distinct()
            .toList();
        return pharmacyProductRepo.findAllById(allPharmacyProductIds);
    }

    private List<MasterProduct> getAllMasterProducts(List<PurchaseOrder> orders) {
        List<Long> allMasterProductIds = orders.stream()
            .flatMap(order -> order.getItems().stream())
            .filter(item -> item.getProductType() == ProductType.MASTER)
            .map(PurchaseOrderItem::getProductId)
            .distinct()
            .toList();
        return masterProductRepo.findAllById(allMasterProductIds);
    }
    
    /**
     * Convert item price to SYP for consistent total calculation
     * This handles the mixed currency issue where pharmacy products and master products
     * might be in different currencies
     */
    private BigDecimal convertItemPriceToSYP(PurchaseOrderItem item, Currency orderCurrency) {
        BigDecimal itemPrice = BigDecimal.valueOf(item.getPrice());
        
        if (item.getProductType() == ProductType.PHARMACY) {
            // Pharmacy products: price is stored in SYP (no conversion in getPriceForPharmacyProduct)
            return itemPrice; // Already in SYP
        } else if (item.getProductType() == ProductType.MASTER) {
            // Master products: price is converted to order currency in getPriceForMasterProduct
            // We need to convert it back to SYP for consistent calculation
            if (orderCurrency == Currency.SYP) {
                return itemPrice; // Order currency is SYP, so price is already in SYP
            } else {
                // Convert from order currency back to SYP
                return exchangeRateService.convertToSYP(itemPrice, orderCurrency);
            }
        } else {
            throw new ConflictException("Invalid productType: " + item.getProductType());
        }
    }
} 