package iuh.fit.se.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRegisterRequest {
    @Email(message = "EMAIL_INVALID")
    @NotEmpty(message = "Email must not be empty")
    String email;

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "PASSWORD_INVALID")
    @NotEmpty(message = "Password must not be empty")
    String password;

    @NotEmpty(message = "First name must not be empty")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "NAME_INVALID")
    String firstName;

    @NotEmpty(message = "Last name must not be empty")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "NAME_INVALID")
    String lastName;

    String address;
}
