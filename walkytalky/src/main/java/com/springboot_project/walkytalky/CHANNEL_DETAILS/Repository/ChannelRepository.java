package com.springboot_project.walkytalky.CHANNEL_DETAILS.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.springboot_project.walkytalky.CHANNEL_DETAILS.Entity.ChannelEntity;

public interface ChannelRepository extends JpaRepository<ChannelEntity, Long> {

    List<ChannelEntity> findByTenantchannel_TenantId(Long tenantId);

    List<ChannelEntity> findByTenantchannel_TenantIdAndIsPrivateFalse(Long tenantId);

    Optional<ChannelEntity> findByTenantchannel_TenantIdAndName(Long tenantId, String name);

    boolean existsByTenantchannel_TenantIdAndName(Long tenantId, String name);
}
