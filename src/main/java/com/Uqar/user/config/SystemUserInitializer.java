package com.Uqar.user.config;

import com.Uqar.user.Enum.UserStatus;
import com.Uqar.user.entity.User;
import com.Uqar.user.repository.RoleRepository;
import com.Uqar.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Order(2) // Run after SystemRolesInitializer
public class SystemUserInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SystemUserInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_ADMIN_PASSWORD = "Password!1";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Initializing system users...");
        
        if (userRepository.findAll().isEmpty()) {
            createSystemUsers();
            log.info("System users initialized successfully");
        } else {
            log.info("Users already exist, skipping user initialization");
        }
    }

    private void createSystemUsers() {
        // Create Super Admin user
        createSuperAdmin();
        
        // Create Platform Admin user
        createPlatformAdmin();
    }

    private void createSuperAdmin() {
        if (userRepository.findByEmail("super.admin@Uqar.com").isPresent()) {
            log.info("Super admin user already exists");
            return;
        }

        var superAdminRole = roleRepository.findByName(RoleConstants.PLATFORM_ADMIN)
                .orElseThrow(() -> new RuntimeException("PLATFORM_ADMIN role not found"));

        User superAdmin = User.builder()
                .firstName("Wassem")
                .lastName("Tenbakji")
                .email("super.admin@Uqar.com")
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .role(superAdminRole)
                .status(UserStatus.ACTIVE)
                .position(
                        "SUPER_ADMIN"
                )
                .build();

        userRepository.save(superAdmin);
        log.info("Super admin user created:");
        log.info("Email: {}", superAdmin.getEmail());
        log.info("Password: {}", DEFAULT_ADMIN_PASSWORD);
        log.info("Role: {}", superAdmin.getRole());
    }

    private void createPlatformAdmin() {
        if (userRepository.findByEmail("admin@Uqar.com").isPresent()) {
            log.info("Platform admin user already exists");
            return;
        }

        var platformAdminRole = roleRepository.findByName(RoleConstants.PLATFORM_ADMIN)
                .orElseThrow(() -> new RuntimeException("PLATFORM_ADMIN role not found"));

        User platformAdmin = User.builder()
                .firstName("platform")
                .lastName("admin")
                .email("admin@Uqar.com")
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .role(platformAdminRole)
                .position(
                        "PLATFORM_ADMIN"
                )
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(platformAdmin);
        log.info("Platform admin user created:");
        log.info("Email: {}", platformAdmin.getEmail());
        log.info("Password: {}", DEFAULT_ADMIN_PASSWORD);
        log.info("Role: {}", platformAdmin.getRole());
    }
} 