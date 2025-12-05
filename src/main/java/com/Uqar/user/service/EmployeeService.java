package com.Uqar.user.service;

import com.Uqar.user.dto.EmployeeCreateRequestDTO;
import com.Uqar.user.dto.EmployeeResponseDTO;
import com.Uqar.user.dto.EmployeeWorkingHoursDTO;
import com.Uqar.user.dto.CreateWorkingHoursRequestDTO;
import com.Uqar.user.dto.UpsertWorkingHoursRequestDTO;
import com.Uqar.user.dto.WorkShiftDTO;
import com.Uqar.user.dto.EmployeeUpdateRequestDTO;
import com.Uqar.user.entity.Employee;
import com.Uqar.user.entity.EmployeeWorkingHours;
import com.Uqar.user.entity.Pharmacy;
import com.Uqar.user.entity.Role;
import com.Uqar.user.entity.User;
import com.Uqar.user.mapper.EmployeeMapper;
import com.Uqar.user.repository.EmployeeRepository;
import com.Uqar.user.repository.EmployeeWorkingHoursRepository;
import com.Uqar.user.repository.RoleRepository;
import com.Uqar.user.repository.UserRepository;
import com.Uqar.utils.exception.ResourceNotFoundException;
import com.Uqar.utils.exception.UnAuthorizedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.time.DayOfWeek;
import java.util.logging.Logger;
import com.Uqar.user.entity.WorkShift;

@Service
@Transactional
public class EmployeeService extends BaseSecurityService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeWorkingHoursRepository employeeWorkingHoursRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public EmployeeService(EmployeeRepository employeeRepository,
                         EmployeeWorkingHoursRepository employeeWorkingHoursRepository,
                         RoleRepository roleRepository,
                         PasswordEncoder passwordEncoder,
                         UserRepository userRepository) {
        super(userRepository);
        this.employeeRepository = employeeRepository;
        this.employeeWorkingHoursRepository = employeeWorkingHoursRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    Logger logger = Logger.getLogger(EmployeeService.class.getName());
    
    @Transactional
    public EmployeeResponseDTO addEmployee(EmployeeCreateRequestDTO dto) {
        // Validate that the current user is a pharmacy manager
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can create employees");
        }
        
        Employee manager = (Employee) currentUser;
        if (manager.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Pharmacy pharmacy = manager.getPharmacy();
        logger.info("Starting to add new employee: " + dto.getFirstName() + " " + dto.getLastName());
        logger.info("Manager pharmacy: " + pharmacy.getName() + " (ID: " + pharmacy.getId() + ")");
        
        // Generate and validate email
        String email = generateEmployeeEmail(dto, pharmacy);
        validateEmployeeEmail(email);
        
        // Create and save employee
        Employee employee = createEmployeeFromDTO(dto, email, pharmacy);
        employee = saveEmployee(employee);
        
        // Handle working hours (support both legacy and new format)
        saveEmployeeWorkingHoursRequests(employee, dto.getWorkingHoursRequests());
        
        logger.info("Employee creation completed successfully");
        return EmployeeMapper.toResponseDTO(employee);
    }
    
    public List<EmployeeResponseDTO> getAllEmployeesInPharmacy() {
        // Validate that the current user is a pharmacy manager
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access employee data");
        }
        
        Employee manager = (Employee) currentUser;
        if (manager.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long pharmacyId = manager.getPharmacy().getId();
        logger.info("Getting all employees for pharmacy ID: " + pharmacyId);
        return employeeRepository.findByPharmacy_Id(pharmacyId)
                .stream()
                .map(EmployeeMapper::toResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public EmployeeResponseDTO updateEmployeeInPharmacy(Long employeeId, EmployeeUpdateRequestDTO dto) {
        // Validate that the current user is a pharmacy manager
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can update employees");
        }
        
        Employee manager = (Employee) currentUser;
        if (manager.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long managerPharmacyId = manager.getPharmacy().getId();
        logger.info("Starting to update employee with ID: " + employeeId);
        
        // Validate and get employee
        Employee employee = validateAndGetEmployee(employeeId, managerPharmacyId);
        
        // Update employee fields using mapper (password is not updated for security)
        updateEmployeeFields(employee, dto);
        
        // Handle working hours update (support both legacy and new format)
        updateEmployeeWorkingHoursRequests(employee, dto.getWorkingHoursRequests());
        
        // Save and return
        employee = saveEmployee(employee);
        logger.info("Employee update completed successfully");
        return EmployeeMapper.toResponseDTO(employee);
    }
    @Transactional
    public void deleteEmployeeInPharmacy(Long employeeId) {
        // Validate that the current user is a pharmacy manager
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can delete employees");
        }
        
        Employee manager = (Employee) currentUser;
        if (manager.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long managerPharmacyId = manager.getPharmacy().getId();
        logger.info("Starting to delete employee with ID: " + employeeId);
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        if (!employee.getPharmacy().getId().equals(managerPharmacyId)) {
            logger.warning("Manager tried to delete employee " + employeeId + " from different pharmacy");
            throw new AccessDeniedException("You can only delete employees in your own pharmacy");
        }
        
        // Delete working hours first
        employeeWorkingHoursRepository.deleteByEmployee_Id(employeeId);
        logger.info("Deleted working hours for employee");
        
        // Delete employee
        employeeRepository.delete(employee);
        logger.info("Employee deleted successfully");
    }
    
    public Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }
    
    /**
     * Get employee by ID with authorization check to ensure the current user has access to this employee
     * (same pharmacy)
     * @param employeeId The ID of the employee to retrieve
     * @return EmployeeResponseDTO of the employee
     * @throws UnAuthorizedException if the current user doesn't have access to this employee
     */
    public EmployeeResponseDTO getEmployeeByIdWithAuth(Long employeeId) {
        // Get current user and validate they are an employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can access employee data");
        }
        
        Employee currentEmployee = (Employee) currentUser;
        if (currentEmployee.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long currentPharmacyId = currentEmployee.getPharmacy().getId();
        
        // Get the employee and validate pharmacy access
        Employee employee = getEmployeeById(employeeId);
        if (!employee.getPharmacy().getId().equals(currentPharmacyId)) {
            throw new UnAuthorizedException("You can only access employees in your own pharmacy");
        }
        
        return EmployeeMapper.toResponseDTO(employee);
    }

    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }
    
    public EmployeeResponseDTO upsertWorkingHoursForEmployee(Long employeeId, UpsertWorkingHoursRequestDTO request) {
        // Validate that the current user is a pharmacy manager
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only pharmacy employees can manage working hours");
        }
        
        Employee manager = (Employee) currentUser;
        if (manager.getPharmacy() == null) {
            throw new UnAuthorizedException("Employee is not associated with any pharmacy");
        }
        
        Long managerPharmacyId = manager.getPharmacy().getId();
        logger.info("Starting to upsert working hours for employee ID: " + employeeId);
        
        // Validate and get employee
        Employee employee = validateAndGetEmployee(employeeId, managerPharmacyId);
        
        // Handle multiple working hours requests
        if (request.getWorkingHoursRequests() != null && !request.getWorkingHoursRequests().isEmpty()) {
            logger.info("Processing " + request.getWorkingHoursRequests().size() + " working hours requests");
            
            for (CreateWorkingHoursRequestDTO workingHoursRequest : request.getWorkingHoursRequests()) {
                upsertWorkingHoursForMultipleDays(employee, workingHoursRequest.getDaysOfWeek(), workingHoursRequest.getShifts());
            }
            
            logger.info("All working hours requests processed successfully");
        } else {
            logger.warning("No working hours requests provided");
        }
        
        logger.info("Working hours upserted successfully for employee");
        return EmployeeMapper.toResponseDTO(employee);
    }
    
    // Helper methods for employee creation
    private String generateEmployeeEmail(EmployeeCreateRequestDTO dto, Pharmacy pharmacy) {
        // Convert Arabic names to English transliteration for email generation
        String transliteratedFirstName = transliterateArabicToEnglish(dto.getFirstName());
        String transliteratedLastName = dto.getLastName() != null && !dto.getLastName().trim().isEmpty() ? 
            transliterateArabicToEnglish(dto.getLastName()) : "";
        
        // Clean transliterated names for email generation
        String cleanFirstName = transliteratedFirstName
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase();
        
        String cleanLastName = transliteratedLastName
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase();
        
        // Clean pharmacy name for uniqueness
        String transliteratedPharmacyName = transliterateArabicToEnglish(pharmacy.getName());
        String cleanPharmacyName = transliteratedPharmacyName
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase();
        
        // Clean license number for uniqueness (shorter version)
        String cleanLicenseNumber = pharmacy.getLicenseNumber()
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase();
        
        // Use first 3 characters of license number to ensure uniqueness
        String licenseSuffix = cleanLicenseNumber.length() > 3 ? 
            cleanLicenseNumber.substring(0, 3) : cleanLicenseNumber;
        
        // Generate base email - combining transliterated names, pharmacy name and license suffix for uniqueness
        String baseEmail;
        if (cleanLastName.isEmpty()) {
            baseEmail = cleanFirstName + "." + cleanPharmacyName + "." + licenseSuffix + "@Uqar.com";
        } else {
            baseEmail = cleanFirstName + "." + cleanLastName + "." + cleanPharmacyName + "." + licenseSuffix + "@Uqar.com";
        }
        
        logger.info("Generated base email for employee: " + baseEmail);
        
        // Ensure email uniqueness
        return ensureEmployeeEmailUniqueness(baseEmail);
    }
    
    private void validateEmployeeEmail(String email) {
        if(employeeRepository.findByEmail(email).isPresent()) {
            throw new ResourceNotFoundException("Employee with email: " + email + " already exists");
        }
    }
    
    /**
     * Transliterates Arabic text to English for email generation
     * @param arabicText The Arabic text to transliterate
     * @return English transliteration
     */
    private String transliterateArabicToEnglish(String arabicText) {
        if (arabicText == null || arabicText.trim().isEmpty()) {
            return "employee";
        }
        
        // Common Arabic words and their English equivalents
        String transliterated = arabicText
            // Common Arabic prefixes
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
            return "employee";
        }
        
        // Clean up multiple spaces and convert to single words
        transliterated = transliterated.replaceAll("\\s+", "");
        
        return transliterated;
    }
    
    /**
     * Ensures employee email uniqueness by appending numbers if needed
     * @param baseEmail The base email to check
     * @return Unique email address
     */
    private String ensureEmployeeEmailUniqueness(String baseEmail) {
        String email = baseEmail;
        int counter = 1;
        
        while (isEmployeeEmailExists(email)) {
            // Extract username and domain parts
            String[] parts = baseEmail.split("@");
            String username = parts[0];
            String domain = parts[1];
            
            // Append counter to username
            email = username + counter + "@" + domain;
            counter++;
            
            // Prevent infinite loop (safety check)
            if (counter > 1000) {
                logger.severe("Unable to generate unique employee email after 1000 attempts for base: " + baseEmail);
                throw new RuntimeException("Unable to generate unique employee email. Please contact support.");
            }
        }
        
        return email;
    }
    
    /**
     * Checks if an employee email already exists in the system
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    private boolean isEmployeeEmailExists(String email) {
        return employeeRepository.findByEmail(email).isPresent();
    }
    
    private Employee createEmployeeFromDTO(EmployeeCreateRequestDTO dto, String email, Pharmacy pharmacy) {
        Employee employee = EmployeeMapper.toEntity(dto);
        employee.setEmail(email);
        employee.setPharmacy(pharmacy);
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        Role role = roleRepository.findById(dto.getRoleId()).orElseThrow(
                () -> new ResourceNotFoundException("Invalid role id: " + dto.getRoleId())
        );
        employee.setRole(role);
        
        return employee;
    }
    
    private Employee saveEmployee(Employee employee) {
        logger.info("Saving employee to database...");
        employee = employeeRepository.save(employee);
        logger.info("Employee saved with ID: " + employee.getId());
        return employee;
    }
    
    private void saveEmployeeWorkingHours(Employee employee, List<EmployeeWorkingHoursDTO> workingHoursDTOs) {
        if (workingHoursDTOs != null && !workingHoursDTOs.isEmpty()) {
            logger.info("Processing working hours for employee (legacy format)...");
            List<EmployeeWorkingHours> workingHoursList = EmployeeMapper.createWorkingHoursFromDTO(employee, workingHoursDTOs);
            
            if (workingHoursList != null) {
                logger.info("Saving " + workingHoursList.size() + " working hours records...");
                employeeWorkingHoursRepository.saveAll(workingHoursList);
                logger.info("Working hours saved successfully");
            }
        }
    }
    
    private void saveEmployeeWorkingHoursRequests(Employee employee, List<CreateWorkingHoursRequestDTO> workingHoursRequests) {
        if (workingHoursRequests != null && !workingHoursRequests.isEmpty()) {
            logger.info("Processing working hours for employee (new format)...");
            
            for (CreateWorkingHoursRequestDTO request : workingHoursRequests) {
                upsertWorkingHoursForMultipleDays(employee, request.getDaysOfWeek(), request.getShifts());
            }
            
            logger.info("Working hours requests processed successfully");
        }
    }
    
    // Helper methods for employee update
    private Employee validateAndGetEmployee(Long employeeId, Long managerPharmacyId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        if (!employee.getPharmacy().getId().equals(managerPharmacyId)) {
            logger.warning("Manager tried to update employee " + employeeId + " from different pharmacy");
            throw new AccessDeniedException("You can only update employees in your own pharmacy");
        }
        
        return employee;
    }
    
    private void updateEmployeeFields(Employee employee, EmployeeUpdateRequestDTO dto) {
        logger.info("Updating employee fields...");
        
        // Use the mapper to update the entity
        EmployeeMapper.updateEntity(employee, dto);
        
        // Handle role update separately
        if(dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId()).orElseThrow(
                    () -> new ResourceNotFoundException("Invalid role id: " + dto.getRoleId())
            );
            employee.setRole(role);
        }
    }
    
    private void updateEmployeeWorkingHours(Employee employee, List<EmployeeWorkingHoursDTO> workingHoursDTOs) {
        if (workingHoursDTOs != null && !workingHoursDTOs.isEmpty()) {
            logger.info("Updating working hours for employee (legacy format)...");
            
            // Delete existing working hours
            employeeWorkingHoursRepository.deleteByEmployee_Id(employee.getId());
            logger.info("Deleted existing working hours");
            
            // Create new working hours
            List<EmployeeWorkingHours> workingHoursList = EmployeeMapper.createWorkingHoursFromDTO(employee, workingHoursDTOs);
            if (workingHoursList != null) {
                employeeWorkingHoursRepository.saveAll(workingHoursList);
                logger.info("Saved " + workingHoursList.size() + " new working hours records");
            }
        }
    }
    
    private void updateEmployeeWorkingHoursRequests(Employee employee, List<CreateWorkingHoursRequestDTO> workingHoursRequests) {
        if (workingHoursRequests != null && !workingHoursRequests.isEmpty()) {
            logger.info("Updating working hours for employee (new format)...");
            
            // Delete existing working hours for the days being updated
            for (CreateWorkingHoursRequestDTO request : workingHoursRequests) {
                if (request.getDaysOfWeek() != null) {
                    for (DayOfWeek dayOfWeek : request.getDaysOfWeek()) {
                        employeeWorkingHoursRepository.deleteByEmployee_IdAndDayOfWeek(employee.getId(), dayOfWeek);
                    }
                }
            }
            
            // Create new working hours
            for (CreateWorkingHoursRequestDTO request : workingHoursRequests) {
                upsertWorkingHoursForMultipleDays(employee, request.getDaysOfWeek(), request.getShifts());
            }
            
            logger.info("Working hours requests updated successfully");
        }
    }
    
    private void upsertWorkingHoursForMultipleDays(Employee employee, List<DayOfWeek> daysOfWeek, List<WorkShiftDTO> shifts) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            logger.warning("No days of week provided for working hours");
            return;
        }
        
        if (shifts == null || shifts.isEmpty()) {
            logger.warning("No shifts provided for working hours");
            return;
        }
        
        logger.info("Upserting working hours for " + daysOfWeek.size() + " days");
        
        for (DayOfWeek dayOfWeek : daysOfWeek) {
            // Delete existing working hours for this day if they exist
            employeeWorkingHoursRepository.deleteByEmployee_IdAndDayOfWeek(employee.getId(), dayOfWeek);
            
            // Create new working hours for this day
            logger.info("Creating new working hours for " + dayOfWeek);
            EmployeeWorkingHours workingHours = new EmployeeWorkingHours();
            workingHours.setEmployee(employee);
            workingHours.setDayOfWeek(dayOfWeek);
            
            // Create new WorkShift entities
            List<WorkShift> newShifts = new ArrayList<>();
            for (WorkShiftDTO shiftDTO : shifts) {
                WorkShift shift = new WorkShift();
                shift.setStartTime(shiftDTO.getStartTime());
                shift.setEndTime(shiftDTO.getEndTime());
                shift.setDescription(shiftDTO.getDescription());
                newShifts.add(shift);
            }
            
            workingHours.setShifts(newShifts);
            
            // Save the new working hours
            employeeWorkingHoursRepository.save(workingHours);
        }
        
        logger.info("Working hours upserted successfully for " + daysOfWeek.size() + " days");
    }
} 