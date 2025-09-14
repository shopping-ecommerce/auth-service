package iuh.fit.se.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email", unique = true)  // Tạo index cho email, unique nếu cần
        })
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String email;

    @JsonIgnore
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password;

    @ManyToMany
    Set<Role> roles;

    @Column(name = "created_time")
    LocalDateTime createdTime;

    @Column(name = "modified_time")
    LocalDateTime modifiedTime;

    @PrePersist
    void GenerateValue() {
        if (this.roles == null || this.roles.isEmpty()) {
            this.roles = new HashSet<>();
            //            this.roles.add(UserRoleEnum.CUSTOMER);
        } else {
            this.roles = roles;
        }
        this.createdTime = LocalDateTime.now();
        this.modifiedTime = LocalDateTime.now();
    }
}
