package com.Uqar.user.service;

import com.Uqar.user.Enum.PharmacyType;
import com.Uqar.user.Enum.UserStatus;
import com.Uqar.user.config.RoleConstants;
import com.Uqar.user.dto.PharmacyCreateRequestDTO;
import com.Uqar.user.entity.*;
import com.Uqar.user.mapper.PharmacyMapper;
import com.Uqar.user.repository.*;
import com.Uqar.utils.annotation.Audited;
import com.Uqar.utils.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Uqar.user.dto.AuthenticationRequest;
import com.Uqar.user.dto.UserAuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import com.Uqar.utils.exception.TooManyRequestException;
import com.Uqar.config.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.Uqar.config.JwtService;
import java.util.HashSet;
import com.Uqar.user.dto.PharmacyResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.Uqar.utils.exception.UnAuthorizedException;

@Service
public class PharmacyService {
    @Autowired
    private AreaRepository areaRepository;
    @Autowired
    private PharmacyRepository pharmacyRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RateLimiterConfig rateLimiterConfig;
    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    
   

    Logger logger = Logger.getLogger(PharmacyService.class.getName());

    @Transactional
    @Audited(action = "CREATE_PHARMACY", targetType = "PHARMACY", includeArgs = false)
    public PharmacyResponseDTO createPharmacy(PharmacyCreateRequestDTO dto) {
        logger.info("Starting pharmacy creation for: " + dto.getPharmacyName());
        
        if (pharmacyRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            logger.warning("Pharmacy with license number " + dto.getLicenseNumber() + " already exists");
            throw new IllegalArgumentException("Pharmacy with this license number already exists");
        }
        
        // Create and save pharmacy
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName(dto.getPharmacyName());
        pharmacy.setLicenseNumber(dto.getLicenseNumber());
        pharmacy.setPhoneNumber(dto.getPhoneNumber());
        pharmacy.setType(PharmacyType.MAIN);
        
        // Set initial isActive status (false until registration is completed)
        pharmacy.setIsActive(false);
        
        pharmacy = pharmacyRepository.save(pharmacy);
        logger.info("Pharmacy saved with ID: " + pharmacy.getId());

        // Create manager as Employee
        Role managerRole = roleRepository.findByName(RoleConstants.PHARMACY_MANAGER).orElseThrow();
        Employee manager = new Employee();

        // Generate professional manager email using @Uqar domain
        String managerEmail = generateManagerEmail(dto, pharmacy);
        logger.info("Generated manager email: " + managerEmail);

        manager.setEmail(managerEmail);
        manager.setPassword(passwordEncoder.encode(dto.getManagerPassword()));
        manager.setRole(managerRole);
        manager.setFirstName("Pharmacy Manager");
        manager.setLastName("");
        manager.setPharmacy(pharmacy);
        manager.setStatus(UserStatus.ACTIVE);
        employeeRepository.save(manager);
        logger.info("Manager created with ID: " + manager.getId());
        
        logger.info("Pharmacy creation completed successfully");
        return PharmacyMapper.toResponseDTO(pharmacy, manager);
    }

    /**
     * Generates a professional manager email using the @Uqar domain
     * @param dto Pharmacy creation request
     * @param pharmacy The created pharmacy entity
     * @return Unique professional email address
     */
    private String generateManagerEmail(PharmacyCreateRequestDTO dto, Pharmacy pharmacy) {
        // Convert Arabic pharmacy name to English transliteration for email generation
        String transliteratedName = transliterateArabicToEnglish(dto.getPharmacyName());
        
        // Clean transliterated name for email generation
        String cleanPharmacyName = transliteratedName
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase();
        
        // Clean license number for email generation (shorter version for uniqueness)
        String cleanLicenseNumber = dto.getLicenseNumber()
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase();
        
        // Use first 4 characters of license number to ensure uniqueness
        String licenseSuffix = cleanLicenseNumber.length() > 4 ? 
            cleanLicenseNumber.substring(0, 4) : cleanLicenseNumber;
        
        // Generate base email - combining transliterated pharmacy name and license suffix for uniqueness
        String baseEmail = "manager." + cleanPharmacyName + "." + licenseSuffix + "@Uqar.com";
        
        // Ensure email uniqueness (this will handle any remaining conflicts)
        return ensureEmailUniqueness(baseEmail);
    }
    
    /**
     * Transliterates Arabic text to English for email generation
     * @param arabicText The Arabic text to transliterate
     * @return English transliteration
     */
    private String transliterateArabicToEnglish(String arabicText) {
        if (arabicText == null || arabicText.trim().isEmpty()) {
            return "pharmacy";
        }
        
        // Common Arabic pharmacy names and their English equivalents
        String transliterated = arabicText
            // Common pharmacy prefixes
            .replaceAll("صيدلية\\s*", "pharmacy") // صيدلية -> pharmacy
            .replaceAll("صيدلية", "pharmacy")
            .replaceAll("صيدليات\\s*", "pharmacies") // صيدليات -> pharmacies
            .replaceAll("صيدليات", "pharmacies")
            
            // Common Arabic words
            .replaceAll("ال", "al") // ال -> al (the)
            .replaceAll("بن", "bin") // بن -> bin (son of)
            .replaceAll("أبو", "abu") // أبو -> abu (father of)
            .replaceAll("أم", "um") // أم -> um (mother of)
            
            // Common Arabic letters transliteration
            .replaceAll("أ", "a")
            .replaceAll("إ", "i")
            .replaceAll("آ", "aa")
            .replaceAll("ع", "a")
            .replaceAll("غ", "gh")
            .replaceAll("ح", "h")
            .replaceAll("خ", "kh")
            .replaceAll("ق", "q")
            .replaceAll("ف", "f")
            .replaceAll("ث", "th")
            .replaceAll("ص", "s")
            .replaceAll("ض", "d")
            .replaceAll("ط", "t")
            .replaceAll("ك", "k")
            .replaceAll("م", "m")
            .replaceAll("ن", "n")
            .replaceAll("ه", "h")
            .replaceAll("و", "w")
            .replaceAll("ي", "y")
            .replaceAll("ة", "a")
            .replaceAll("ى", "a")
            
            // Remove remaining Arabic characters and special symbols
            .replaceAll("[\\u0600-\\u06FF]", "") // Remove all Arabic Unicode characters
            .replaceAll("[^a-zA-Z0-9\\s]", "") // Remove special characters except spaces
            .trim();
        
        // If transliteration resulted in empty string, use fallback
        if (transliterated.isEmpty() || transliterated.matches("\\s*")) {
            return "pharmacy";
        }
        
        // Clean up multiple spaces and convert to single words
        transliterated = transliterated.replaceAll("\\s+", "");
        
        return transliterated;
    }
    
    /**
     * Ensures email uniqueness by appending numbers if needed
     * @param baseEmail The base email to check
     * @return Unique email address
     */
    private String ensureEmailUniqueness(String baseEmail) {
        String email = baseEmail;
        int counter = 1;
        
        while (isEmailExists(email)) {
            // Extract username and domain parts
            String[] parts = baseEmail.split("@");
            String username = parts[0];
            String domain = parts[1];
            
            // Append counter to username
            email = username + counter + "@" + domain;
            counter++;
            
            // Prevent infinite loop (safety check)
            if (counter > 1000) {
                logger.severe("Unable to generate unique email after 1000 attempts for base: " + baseEmail);
                throw new RuntimeException("Unable to generate unique email. Please contact support.");
            }
        }
        
        return email;
    }
    
    /**
     * Checks if an email already exists in the system
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    private boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent() || 
               employeeRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public PharmacyResponseDTO completeRegistration(String newPassword, String location, String managerFirstName, String managerLastName, String pharmacyPhone, String pharmacyEmail, String openingHours, Long areaId) {
        Employee manager = (Employee) userService.getCurrentUser();
        Pharmacy pharmacy = manager.getPharmacy();
        
        // Update pharmacy fields
        if (location != null && !location.isEmpty()) {
            pharmacy.setAddress(location);
        }
        if (pharmacyEmail != null && !pharmacyEmail.isEmpty()) {
            pharmacy.setEmail(pharmacyEmail);
        }
        if (openingHours != null && !openingHours.isEmpty()) {
            pharmacy.setOpeningHours(openingHours);
        }
        if (pharmacyPhone != null && !pharmacyPhone.isEmpty()) {
            pharmacy.setPhoneNumber(pharmacyPhone);
        }
        // Update area if provided
        if (areaId != null) {
            Area area = areaRepository.findById(areaId)
                    .orElseThrow(() -> new RuntimeException("Area not found with id: " + areaId));
            pharmacy.setArea(area);
        }


        // Update the isActive status based on registration completion
        PharmacyMapper.updatePharmacyActiveStatus(pharmacy);
        
        pharmacyRepository.save(pharmacy);
        
        // Update manager info
        manager.setPassword(passwordEncoder.encode(newPassword));
        if(managerFirstName != null && !managerFirstName.isEmpty()) {
            manager.setFirstName(managerFirstName);
        }
        if(managerLastName != null && !managerLastName.isEmpty()) {
            manager.setLastName(managerLastName);
        }
        manager.setPharmacy(pharmacy);
        employeeRepository.save(manager);
        
        logger.info("Pharmacy registration completed. isActive: " + pharmacy.getIsActive());
        return PharmacyMapper.toResponseDTO(pharmacy, manager);
    }



    public UserAuthenticationResponse adminLogin(AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        String userIp = httpServletRequest.getRemoteAddr();
        if (rateLimiterConfig.getBlockedIPs().contains(userIp)) {
            throw new TooManyRequestException("Too many login attempts. Please try again later.");
        }
        String rateLimiterKey = "adminLoginRateLimiter-" + userIp;
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterKey);

        if (rateLimiter.acquirePermission()) {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword(),
                            new HashSet<>()
                    ));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            var user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                    () -> new RuntimeException("Admin email not found")
            );
            if (!RoleConstants.PLATFORM_ADMIN.equals(user.getRole().getName())) {
                throw new AccessDeniedException("Not a system admin");
            }
            var jwtToken = jwtService.generateToken(user);
            UserAuthenticationResponse response = new UserAuthenticationResponse();
            response.setToken(jwtToken);
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setRole(user.getRole().getName());
            
            // Platform admin accounts are always active
            response.setIsActive(true);
            
            return response;
        } else {
            rateLimiterConfig.blockIP(userIp);
            throw new TooManyRequestException("Too many login attempts, Please try again later.");
        }
    }

    public UserAuthenticationResponse pharmacyLogin(AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        String userIp = httpServletRequest.getRemoteAddr();
        if (rateLimiterConfig.getBlockedIPs().contains(userIp)) {
            throw new TooManyRequestException("Too many login attempts. Please try again later.");
        }
        String rateLimiterKey = "pharmacyLoginRateLimiter-" + userIp;
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterKey);
        if (rateLimiter.acquirePermission()) {
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword(),
                                new HashSet<>()
                        ));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                var employee = employeeRepository.findByEmail(request.getEmail()).orElseThrow(
                        () -> new RuntimeException("Employee email not found")
                );
                
                // Check if user has a valid pharmacy role
                if (employee.getRole() == null) {
                    throw new AccessDeniedException("Employee has no role assigned");
                }
                
                String roleName = employee.getRole().getName();
                
                // Check if user has a valid pharmacy role - using the correct role constants
                if (!RoleConstants.PHARMACY_MANAGER.equals(roleName) && 
                    !RoleConstants.PHARMACY_EMPLOYEE.equals(roleName) && 
                    !RoleConstants.PHARMACY_TRAINEE.equals(roleName)) {
                    throw new AccessDeniedException("User does not have a valid pharmacy role. Current role: " + roleName);
                }
                
                var jwtToken = jwtService.generateToken(employee);
                UserAuthenticationResponse response = new UserAuthenticationResponse();
                response.setToken(jwtToken);
                response.setEmail(employee.getEmail());
                response.setFirstName(employee.getFirstName());
                response.setLastName(employee.getLastName());
                response.setRole(employee.getRole().getName());
                
                // Set isActive based on pharmacy registration completion
                Boolean isActive = false;
                if (employee.getPharmacy() != null) {
                    Pharmacy pharmacy = employee.getPharmacy();
                    if (pharmacy.getIsActive() != null) {
                        isActive = pharmacy.getIsActive();
                    } else {
                        // Fallback to calculated value if isActive is not set
                        isActive = PharmacyMapper.isPharmacyAccountActive(pharmacy);
                    }
                }
                response.setIsActive(isActive);
                
                return response;
            } catch (Exception e) {
                throw e;
            }
        } else {
            rateLimiterConfig.blockIP(userIp);
            throw new TooManyRequestException("Too many login attempts, Please try again later.");
        }
    }

    public List<PharmacyResponseDTO> getAllPharmacies() {
        List<Pharmacy> pharmacies = pharmacyRepository.findAll();
        return pharmacies.stream()
                .map(pharmacy -> {
                    // Find manager for this pharmacy
                    Employee manager = employeeRepository.findAll().stream()
                        .filter(e -> e.getPharmacy() != null && e.getPharmacy().getId().equals(pharmacy.getId()) && e.getRole() != null && "PHARMACY_MANAGER".equals(e.getRole().getName()))
                        .findFirst().orElse(null);
                    return PharmacyMapper.toResponseDTO(pharmacy, manager);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get pharmacy by ID with authorization check to ensure the current user has access to this pharmacy
     * @param pharmacyId The ID of the pharmacy to retrieve
     * @return PharmacyResponseDTO of the pharmacy
     * @throws UnAuthorizedException if the current user doesn't have access to this pharmacy
     */
    public PharmacyResponseDTO getPharmacyByIdWithAuth(Long pharmacyId) {
        // Get current user and validate they are an employee
        User currentUser = userService.getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access pharmacy data");
        }
        
        Employee currentEmployee = (Employee) currentUser;
        if (currentEmployee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = currentEmployee.getPharmacy().getId();
        
        // Check if the requested pharmacy ID matches the current user's pharmacy
        if (!currentPharmacyId.equals(pharmacyId)) {
            throw new UnAuthorizedException("You can only access your own pharmacy");
        }
        
        // Get the pharmacy and find its manager
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found"));
        
        // Find manager for this pharmacy
        Employee manager = employeeRepository.findAll().stream()
            .filter(e -> e.getPharmacy() != null && e.getPharmacy().getId().equals(pharmacy.getId()) && e.getRole() != null && "PHARMACY_MANAGER".equals(e.getRole().getName()))
            .findFirst().orElse(null);
        
        return PharmacyMapper.toResponseDTO(pharmacy, manager);
    }
    
    /**
     * Update the isActive status for all pharmacies based on their registration completion
     * This method is useful for data migration or bulk updates
     */
    @Transactional
    public void updateAllPharmacyActiveStatus() {
        List<Pharmacy> pharmacies = pharmacyRepository.findAll();
        int updatedCount = 0;
        
        for (Pharmacy pharmacy : pharmacies) {
            boolean wasActive = pharmacy.getIsActive() != null ? pharmacy.getIsActive() : false;
            PharmacyMapper.updatePharmacyActiveStatus(pharmacy);
            
            if (pharmacy.getIsActive() != wasActive) {
                updatedCount++;
            }
        }
        
        pharmacyRepository.saveAll(pharmacies);
        logger.info("Updated isActive status for " + updatedCount + " pharmacies");
    }


} 