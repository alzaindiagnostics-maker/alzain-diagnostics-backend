package com.alzain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLoginDTO {

    // Can accept either email or username
    private String email;

    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
