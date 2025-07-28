package iuh.fit.se.config;

import java.util.HashSet;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import iuh.fit.se.entity.Permission;
import iuh.fit.se.entity.Role;
import iuh.fit.se.entity.User;
import iuh.fit.se.enums.UserRoleEnum;
import iuh.fit.se.repository.PermissionRepository;
import iuh.fit.se.repository.RoleRepository;
import iuh.fit.se.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(
            UserRepository userRepository, PermissionRepository permissionRepository, RoleRepository roleRepository) {
        return args -> {
            // Initialize permissions
            var permissions = new HashSet<Permission>();
            permissions.add(Permission.builder()
                    .name("CREATE_USER")
                    .description("Create a new user")
                    .build());
            permissions.add(Permission.builder()
                    .name("UPDATE_USER")
                    .description("Update an existing user")
                    .build());
            permissions.add(Permission.builder()
                    .name("DELETE_USER")
                    .description("Delete an existing user")
                    .build());
            permissions.add(Permission.builder()
                    .name("VIEW_USER")
                    .description("View user details")
                    .build());
            permissionRepository.saveAll(permissions);

            // Initialize roles
            var roles = new HashSet<Role>();
            roles.add(Role.builder()
                    .name(UserRoleEnum.MANAGER.name())
                    .description("Manager role")
                    .permissions(permissions)
                    .build());
            roleRepository.saveAll(roles);
            var roles1 = new HashSet<Role>();
            roles1.add(Role.builder()
                    .name(UserRoleEnum.CUSTOMER.name())
                    .description("Customer role")
                    .permissions(permissions)
                    .build());
            roleRepository.saveAll(roles1);

            // Initialize admin user
            if (userRepository.findByEmail("admin").isEmpty()) {
                User user = User.builder()
                        .email("admin")
                        .password(passwordEncoder.encode("admin"))
                        .roles(roles)
                        .build();
                userRepository.save(user);
                log.warn("admin user has been created with default password: admin");
            }
        };
    }
}
