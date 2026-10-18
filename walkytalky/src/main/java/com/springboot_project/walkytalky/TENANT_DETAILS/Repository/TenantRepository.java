package com.springboot_project.walkytalky.TENANT_DETAILS.Repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot_project.walkytalky.TENANT_DETAILS.Entity.TenantEntity;

import java.util.List;
import java.util.Optional;


public interface TenantRepository extends JpaRepository<TenantEntity,Long>{

    Optional<TenantEntity> findBySlug(String slug);   // Used to resolve tenant workspaces by URL slug (e.g., [walkytalky.com/api/tenant-slug](https://walkytalky.com/api/tenant-slug)
    
    boolean existsBySlug(String slug);   // Used during tenant creation to ensure slug uniqueness.

    @EntityGraph(attributePaths = "tenantMembership") 
    List<TenantEntity> findAll();
}

