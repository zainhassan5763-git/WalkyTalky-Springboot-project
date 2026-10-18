package com.springboot_project.walkytalky.CHANNEL_DETAILS.Entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.springboot_project.walkytalky.TENANT_DETAILS.Entity.TenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@NoArgsConstructor 
@Getter 
@Setter 
@Table(name = "channels") 
public class ChannelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id")
    private Long channelId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenantchannel;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder 
    public ChannelEntity(Long channelId, String name, String description, Boolean isPrivate, TenantEntity tenantchannel,
            LocalDateTime createdAt) {
        this.channelId = channelId;
        this.name = name;
        this.description = description;
        if (isPrivate != null) {
            this.isPrivate = isPrivate;
        }
        this.tenantchannel = tenantchannel;
        this.createdAt = createdAt;
    }


    @PrePersist
    public void prePersist() {
        if (this.isPrivate == null) {
            this.isPrivate = false; // Default to public channel
        }
    }
}