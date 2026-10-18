package com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.springboot_project.walkytalky.TENANT_DETAILS.Entity.TenantEntity;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tenant_memberships")
@Getter
@Setter
@NoArgsConstructor
public class TenantMemberShipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long membershipId;

    @Column(nullable = false, length = 20)
    private String role = "MEMBER";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @JsonBackReference("user-tenantmembership") 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity alluser;

    @JsonBackReference("tenant-tenantmembership")     
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity alltenant;

    @Version 
    @Column(nullable = false) 
    private Integer version = 0;

    @Builder
    public TenantMemberShipEntity(Long membershipId, String role, LocalDateTime createdAt, LocalDateTime updatedAt, UserEntity alluser, TenantEntity alltenant, Integer version) {
        this.membershipId = membershipId;
        if (role != null) this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.alluser = alluser;
        this.alltenant = alltenant;
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