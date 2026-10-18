package com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddMemberRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @Pattern(regexp = "^(ADMIN|MEMBER)$", message = "Role must be either ADMIN or MEMBER")
    private String role = "MEMBER";  // here by default we assign role as MEMBER but we can change it to OWNER also

    @Builder
    public AddMemberRequestDTO(Long userId, String role) {
        this.userId = userId;
        if (role != null) {
            this.role = role;
        }
    }
}