package com.springboot_project.walkytalky.TENANT_DETAILS.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class CreateTenantRequest {

    @NotBlank(message = "Tenant name is required")
    @Size(max = 100, message = "Tenant name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Tenant slug is required")
    @Size(max = 100, message = "Slug cannot exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    private String slug;
}
