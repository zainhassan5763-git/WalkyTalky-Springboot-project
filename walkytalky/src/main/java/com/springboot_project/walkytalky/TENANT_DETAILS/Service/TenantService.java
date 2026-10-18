package com.springboot_project.walkytalky.TENANT_DETAILS.Service;

import org.springframework.stereotype.Service;

import com.springboot_project.walkytalky.TENANT_DETAILS.DTO.CreateTenantRequest;
import com.springboot_project.walkytalky.TENANT_DETAILS.DTO.TenantResponse;
import com.springboot_project.walkytalky.TENANT_DETAILS.Entity.TenantEntity;
import com.springboot_project.walkytalky.TENANT_DETAILS.Repository.TenantRepository;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Entity.TenantMemberShipEntity;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Repository.TenantMemberShipRepository;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;
import com.springboot_project.walkytalky.USERS_DETAILS.Repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMemberShipRepository tenantMemberShipRepository;
    private final UserRepository userRepository;
    
    // creating tenenat means etting up a dedicated, isolated space or account for an organization or user group within a software application, cloud platform, or IT system
    @Transactional 
    public TenantResponse createTenant(CreateTenantRequest tenantrequest  , Long ownerUserId){

        // 1) here we verify slug(URL) if its present or not
        if(tenantRepository.existsBySlug(tenantrequest.getSlug())){
            throw new IllegalArgumentException("Tenant slug already taken: " + tenantrequest.getSlug());
        }


        // 2)
        // What it does: Pulls the complete UserEntity object from your users table corresponding to the userId extracted from the JWT token.
        // Why it's essential: TenantMemberShipEntity requires a direct foreign key mapping (@ManyToOne) to UserEntity. We need the full entity reference to pass into the builder in Step 4.
        UserEntity owner = userRepository.findById(ownerUserId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: "+ownerUserId));


                    
        // 3) 
        // What it does: Constructs a new TenantEntity using Lombok's @Builder pattern, defaulting its status to "ACTIVE", and persists it to MySQL.
        // Why it's essential: MySQL assigns an auto-incremented primary key (id / tenantId) upon saving. savedTenant now holds the newly generated ID, which is required for the foreign key link in Step 4.
        TenantEntity newTenant = TenantEntity.builder()
                    .name(tenantrequest.getName())
                    .slug(tenantrequest.getSlug())
                    .status("ACTIVE")
                    .build();
        TenantEntity savedTenant = tenantRepository.save(newTenant);
        
               
        // 4)
        // What it does: Creates the relationship row in tenant_memberships. It explicitly sets alluser = owner, alltenant = savedTenant, and sets the role string to "OWNER".
        // Why it's essential: This is where Tenant Admin rights are assigned! By assigning "OWNER" at the workspace level, this specific user receives administrative capabilities over this workspace (inviting members, updating workspace settings, deleting workspace channels) without giving them global database access.
        TenantMemberShipEntity membership = TenantMemberShipEntity.builder()
                    .alluser(owner)
                    .alltenant(savedTenant)
                    .role("OWNER")
                    .build();
        tenantMemberShipRepository.save(membership);


        // 5)
        // What it does: Maps the persistent database entity into a clean output DTO (TenantResponse)
        // Why it's essential: Hides internal JPA entity references and cyclic mapping properties (like bidirectional @OneToMany references) before sending data back across the HTTP API layer.
        return TenantResponse.builder()
                    .tenantId(savedTenant.getTenantId())
                    .name(savedTenant.getName())
                    .slug(savedTenant.getSlug())
                    .status(savedTenant.getStatus())
                    .createdAt(savedTenant.getCreatedAt())
                    .build();
    }


    @Transactional(readOnly=true) 
    public TenantResponse getTenantBySlug(String slug){
        TenantEntity tenant = tenantRepository.findBySlug(slug)
                    .orElseThrow(() -> new IllegalArgumentException("Tenant not found with slug: " + slug));

        return TenantResponse.builder()
                    .tenantId(tenant.getTenantId()) 
                    .name(tenant.getName())
                    .slug(tenant.getSlug())
                    .status(tenant.getStatus())
                    .createdAt(tenant.getCreatedAt())
                    .build();
    }



    @Transactional(readOnly = true)
    public TenantResponse getTenantById(Long tenantId) {
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + tenantId));

        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .status(tenant.getStatus())
                .createdAt(tenant.getCreatedAt())
                .build();
    }

}
