package iuh.fit.se.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @JsonProperty("oldPassword")
    String oldPassword;
    @JsonProperty("newPassword")
    String newPassword;
}
