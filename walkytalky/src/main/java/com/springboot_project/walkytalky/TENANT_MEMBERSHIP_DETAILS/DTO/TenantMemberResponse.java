package com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantMemberResponse {

    private Long membershipId;
    private Long userId;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String role;
    private LocalDateTime joinedAt;
}
