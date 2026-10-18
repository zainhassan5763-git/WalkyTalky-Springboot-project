package com.springboot_project.walkytalky.TENANT_DETAILS.DTO;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantResponse {

    private Long tenantId;
    private String name;
    private String slug;
    private String status;
    private LocalDateTime createdAt;

}
