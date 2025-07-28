package iuh.fit.se.dto.request;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import iuh.fit.se.enums.UserRoleEnum;
import iuh.fit.se.enums.UserStatusEnum;
import iuh.fit.se.enums.UserTierEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    String id;

    @Email(message = "EMAIL_INVALID")
    String email;

    //    @Size(min = 8, message = "PASSWORD_INVALID")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "PASSWORD_INVALID")
    String password;

    @NotEmpty(message = "First name must not be empty")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "NAME_INVALID")
    String firstName;

    @NotEmpty(message = "Last name must not be empty")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "NAME_INVALID")
    String lastName;

    String accountId;
    //    UserRoleEnum role;
    private Set<UserRoleEnum> roles;
    int points;
    UserTierEnum tier;
    String address;
    UserStatusEnum status;
    String publicId;
    LocalDateTime createdTime;
    LocalDateTime modifiedTime;
}
