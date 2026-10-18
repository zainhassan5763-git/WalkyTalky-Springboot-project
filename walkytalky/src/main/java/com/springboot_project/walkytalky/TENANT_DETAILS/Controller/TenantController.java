package com.springboot_project.walkytalky.TENANT_DETAILS.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot_project.walkytalky.TENANT_DETAILS.DTO.CreateTenantRequest;
import com.springboot_project.walkytalky.TENANT_DETAILS.DTO.TenantResponse;
import com.springboot_project.walkytalky.TENANT_DETAILS.Service.TenantService;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping("/api/tenants") 
public class TenantController {

    private final TenantService tenantService;

    // POST /api/tenants - Create a new workspace
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody  CreateTenantRequest tenantrequest  , @AuthenticationPrincipal UserEntity currentUser){        
        TenantResponse response = tenantService.createTenant(tenantrequest , currentUser.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/tenants/slug/{slug} - Get tenant by slug
    @GetMapping("/slug/{slug}")
    public ResponseEntity<TenantResponse> getTenantBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(tenantService.getTenantBySlug(slug));
    }


    // GET /api/tenants/{tenantId} - Get tenant by ID
    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable Long tenantId) {
        return ResponseEntity.ok(tenantService.getTenantById(tenantId));
    }


}
