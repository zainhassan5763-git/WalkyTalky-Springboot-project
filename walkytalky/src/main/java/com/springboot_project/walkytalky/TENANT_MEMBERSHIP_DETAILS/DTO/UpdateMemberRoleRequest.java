package com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRoleRequest {

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(OWNER|ADMIN|MEMBER)$", message = "Role must be OWNER, ADMIN, or MEMBER")
    private String role;
    
}
