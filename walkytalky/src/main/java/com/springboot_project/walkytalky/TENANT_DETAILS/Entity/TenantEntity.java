package com.springboot_project.walkytalky.TENANT_DETAILS.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
public class TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long tenantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version 
    @Column(nullable = false) 
    private Integer version = 0;

    @Builder
    public TenantEntity(Long tenantId, String name, String slug, String status, LocalDateTime createdAt, LocalDateTime updatedAt, Integer version) {
        this.tenantId = tenantId;
        this.name = name;
        this.slug = slug;
        if (status != null) this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        if (version != null) this.version = version;
    }

    @PrePersist 
    protected void onCreate(){
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now; 
    }

    @PreUpdate 
    protected void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}