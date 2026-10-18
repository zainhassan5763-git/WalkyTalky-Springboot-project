package com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.DTO.AddMemberRequestDTO;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.DTO.TenantMemberResponse;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Entity.TenantMemberShipEntity;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Service.TenantMemberShipService;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tenants/{tenantId}/members")
@RequiredArgsConstructor
public class TenantMemberShipController {

    private final TenantMemberShipService tenantMemberShipService;

    // 1. Get all members in a workspace
    @GetMapping
    public ResponseEntity<List<TenantMemberResponse>> getMembersByTenant(
            @PathVariable Long tenantId) {
        return ResponseEntity.ok(tenantMemberShipService.getMembersByTenant(tenantId));
    }

    // 2. Add a new member to a workspace (Requires OWNER or ADMIN role)
    @PostMapping
    public ResponseEntity<TenantMemberResponse> addMember(
            @PathVariable Long tenantId,
            @Valid @RequestBody AddMemberRequestDTO memberReq,
            @AuthenticationPrincipal UserEntity currentUser) {
        
        TenantMemberResponse response = tenantMemberShipService.addMember(tenantId, memberReq, currentUser.getUserId());
        return ResponseEntity.ok(response);
    }

    // 3. Update a member's role (e.g., MEMBER -> ADMIN)
    @PutMapping("/{userId}")
    public ResponseEntity<TenantMemberShipEntity> updateMemberRole(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @RequestBody TenantMemberShipEntity updatedMember,
            @AuthenticationPrincipal UserEntity currentUser) {

        TenantMemberShipEntity response = tenantMemberShipService.updateMembersByTenant(
                tenantId, userId, currentUser.getUserId(), updatedMember);
        return ResponseEntity.ok(response);
    }

    // 4. Remove a member from a workspace
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> removeMember(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserEntity currentUser) {

        boolean deleted = tenantMemberShipService.deleteMembersByTenant(tenantId, userId, currentUser.getUserId());
        if (deleted) {
            return ResponseEntity.ok("Member removed successfully from workspace.");
        }
        return ResponseEntity.badRequest().body("Failed to remove member.");
    }
}