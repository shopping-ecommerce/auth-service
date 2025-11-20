package iuh.fit.se.config;

import java.util.HashSet;
import java.util.Set;

import iuh.fit.se.dto.request.UserClientRequest;
import iuh.fit.se.repository.httpclient.UserClient;
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
    UserClient userClient;
    @Bean
    ApplicationRunner applicationRunner(
            UserRepository userRepository, PermissionRepository permissionRepository, RoleRepository roleRepository) {
        return args -> {
            // Initialize permissions
            Set<Permission> allPermissions = new HashSet<>();
            allPermissions.add(Permission.builder().name("CREATE_USER").description("Create a new user").build());
            allPermissions.add(Permission.builder().name("UPDATE_USER").description("Update an existing user").build());
            allPermissions.add(Permission.builder().name("DELETE_USER").description("Delete an existing user").build());
            allPermissions.add(Permission.builder().name("VIEW_USER").description("View user details").build());
            allPermissions.add(Permission.builder().name("CREATE_SELLER").description("Create a new seller").build());
            allPermissions.add(Permission.builder().name("UPDATE_SELLER").description("Update an existing seller").build());
            allPermissions.add(Permission.builder().name("DELETE_SELLER").description("Delete an existing seller").build());
            allPermissions.add(Permission.builder().name("VIEW_SELLER").description("View seller details").build());
            allPermissions.add(Permission.builder().name("CREATE_PRODUCT").description("Create a new product").build());
            allPermissions.add(Permission.builder().name("UPDATE_PRODUCT").description("Update an existing product").build());
            allPermissions.add(Permission.builder().name("DELETE_PRODUCT").description("Delete an existing product").build());
            allPermissions.add(Permission.builder().name("VIEW_PRODUCT").description("View product details").build());
            allPermissions.add(Permission.builder().name("CREATE_ORDER").description("Create a new order").build());
            allPermissions.add(Permission.builder().name("UPDATE_ORDER").description("Update an existing order").build());
            allPermissions.add(Permission.builder().name("DELETE_ORDER").description("Delete an existing order").build());
            allPermissions.add(Permission.builder().name("VIEW_ORDER").description("View order details").build());
            allPermissions.add(Permission.builder().name("UPLOAD_FILE").description("Upload file to S3").build());
            permissionRepository.saveAll(allPermissions);

            // Define permissions for each role
            Set<Permission> adminPermissions = new HashSet<>(allPermissions); // ADMIN có tất cả quyền
            Set<Permission> sellerPermissions = new HashSet<>();
            sellerPermissions.addAll(allPermissions.stream()
                    .filter(p -> p.getName().equals("CREATE_PRODUCT") ||
                            p.getName().equals("UPDATE_PRODUCT") ||
                            p.getName().equals("DELETE_PRODUCT") ||
                            p.getName().equals("VIEW_PRODUCT") ||
                            p.getName().equals("VIEW_ORDER")||
                            p.getName().equals("UPDATE_SELLER") ||
                            p.getName().equals("VIEW_SELLER")||
                            p.getName().equals("UPDATE_ORDER"))
                    .toList());
            Set<Permission> customerPermissions = new HashSet<>();
            customerPermissions.addAll(allPermissions.stream()
                    .filter(p -> p.getName().equals("VIEW_PRODUCT") ||
                            p.getName().equals("CREATE_ORDER") ||
                            p.getName().equals("VIEW_ORDER") ||
                            p.getName().equals("CREATE_SELLER") ||
                            p.getName().equals("UPDATE_USER") ||
                            p.getName().equals("UPLOAD_FILE"))
                    .toList());

            // Initialize roles
            Set<Role> roles = new HashSet<>();
            roles.add(Role.builder()
                    .name(UserRoleEnum.ADMIN.name())
                    .description("Admin role with full permissions")
                    .permissions(adminPermissions)
                    .build());
            roles.add(Role.builder()
                    .name(UserRoleEnum.SELLER.name())
                    .description("Seller role with product and order management permissions")
                    .permissions(sellerPermissions)
                    .build());
            roles.add(Role.builder()
                    .name(UserRoleEnum.CUSTOMER.name())
                    .description("Customer role with shopping permissions")
                    .permissions(customerPermissions)
                    .build());
            roleRepository.saveAll(roles);

            // Initialize admin user
            if (userRepository.findByEmail("admin").isEmpty()) {
                User user = User.builder()
                        .email("admin")
                        .password(passwordEncoder.encode("admin"))
                        .roles(Set.of(roles.stream()
                                .filter(r -> r.getName().equals(UserRoleEnum.ADMIN.name()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Admin role not found")))
                        )
                        .build();
                userRepository.save(user);
                log.warn("Admin user has been created with default password: admin");
                userClient.createUser(UserClientRequest.builder()
                                .accountId(user.getId())
                                .firstName("Quản trị")
                                .lastName("viên")
                        .build());
            }
        };
    }
}