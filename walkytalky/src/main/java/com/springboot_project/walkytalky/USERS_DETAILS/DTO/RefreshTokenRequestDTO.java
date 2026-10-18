package com.springboot_project.walkytalky.USERS_DETAILS.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data 
public class RefreshTokenRequestDTO {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
