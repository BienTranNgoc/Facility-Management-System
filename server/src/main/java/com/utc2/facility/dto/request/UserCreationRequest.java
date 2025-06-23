package com.utc2.facility.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank String userId;
    @NotBlank @Size(min = 4, message = "USERNAME_INVALID") String username;
    @NotBlank @Email String email;
    @NotBlank String fullName;
    @NotBlank String roleName;
}
