package iuh.fit.se.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

// DTO cho việc reset password với OTP
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email;

    @NotBlank(message = "OTP is required")
    String otp;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String newPassword;
}