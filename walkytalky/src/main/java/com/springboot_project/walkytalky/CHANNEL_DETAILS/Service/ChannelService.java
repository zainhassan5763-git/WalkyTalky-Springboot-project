package com.springboot_project.walkytalky.CHANNEL_DETAILS.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot_project.walkytalky.CHANNEL_DETAILS.DTO.ChannelResponseDTO;
import com.springboot_project.walkytalky.CHANNEL_DETAILS.DTO.CreateChannelRequestDTO;
import com.springboot_project.walkytalky.CHANNEL_DETAILS.Entity.ChannelEntity;
import com.springboot_project.walkytalky.CHANNEL_DETAILS.Repository.ChannelRepository;
import com.springboot_project.walkytalky.MESSAGE_DETAILS.Repository.MessageRepository;
import com.springboot_project.walkytalky.TENANT_DETAILS.Entity.TenantEntity;
import com.springboot_project.walkytalky.TENANT_DETAILS.Repository.TenantRepository;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Entity.TenantMemberShipEntity;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Repository.TenantMemberShipRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor 
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final TenantRepository tenantRepository;
    private final TenantMemberShipRepository membershipRepository;
    private final MessageRepository messageRepository; // Added to clear channel messages before deletion

    private TenantMemberShipEntity verifyWorkspaceMembership(Long tenantId, Long requestingUserId) {
        return membershipRepository.findByAlluser_UserIdAndAlltenant_TenantId(requestingUserId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Access denied: You are not a member of this workspace"));
    }

    private void verifyAdminOrOwnerAccess(Long tenantId, Long requestingUserId) {
        TenantMemberShipEntity membership = verifyWorkspaceMembership(tenantId, requestingUserId);
        if (!"OWNER".equals(membership.getRole()) && !"ADMIN".equals(membership.getRole())) {
            throw new IllegalArgumentException("Access denied: Only workspace Owners or Admins can perform this action");
        }
    }

    private ChannelResponseDTO mapToResponse(ChannelEntity entity) {
        return ChannelResponseDTO.builder()
                .channelId(entity.getChannelId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isPrivate(entity.getIsPrivate())
                .tenantId(entity.getTenantchannel().getTenantId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Transactional
    public ChannelResponseDTO createChannel(Long tenantId, CreateChannelRequestDTO request, Long requestingUserId) {
        // ensure user belongs to the workspace
        verifyWorkspaceMembership(tenantId, requestingUserId);

        // ensure channel name is unique in this workspace
        if (channelRepository.existsByTenantchannel_TenantIdAndName(tenantId, request.getName())) {
            throw new IllegalArgumentException("Channel with name '" + request.getName() + "' already exists in this workspace");
        }

        // fetch parent tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found with ID: " + tenantId));

        // save channel
        ChannelEntity channel = ChannelEntity.builder()
                .name(request.getName().toLowerCase())
                .description(request.getDescription())
                .isPrivate(request.getIsPrivate() != null ? request.getIsPrivate() : false)
                .tenantchannel(tenant)
                .build();

        ChannelEntity saved = channelRepository.save(channel);
        log.info("Created channel '{}' (ID: {}) in tenant ID: {}", saved.getName(), saved.getChannelId(), tenantId);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ChannelResponseDTO> getChannelsByTenant(Long tenantId, Long requestingUserId) {
        // ensure user belongs to the workspace
        verifyWorkspaceMembership(tenantId, requestingUserId);

        return channelRepository.findByTenantchannel_TenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChannelResponseDTO getChannelById(Long tenantId, Long channelId, Long requestingUserId) {
        // ensure user belongs to workspace
        verifyWorkspaceMembership(tenantId, requestingUserId);

        ChannelEntity channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found with ID: " + channelId));

        if (!channel.getTenantchannel().getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Channel does not belong to the specified workspace");
        }

        return mapToResponse(channel);
    }

    @Transactional
    public void deleteChannel(Long tenantId, Long channelId, Long requestingUserId) {
        // ensure requester is OWNER or ADMIN
        verifyAdminOrOwnerAccess(tenantId, requestingUserId);

        // retrieve channel
        ChannelEntity channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found with ID: " + channelId));

        if (!channel.getTenantchannel().getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Channel does not belong to the specified workspace");
        }

        // 1. Delete all messages associated with this channel first to bypass foreign key constraint
        messageRepository.deleteByChannelChannelId(channelId);

        // 2. Now delete the channel safely
        channelRepository.delete(channel);
        log.info("Deleted channel ID: {} and its messages from tenant ID: {}", channelId, tenantId);
    }



    @Transactional
    public ChannelResponseDTO updateChannel(Long tenantId, Long channelId, CreateChannelRequestDTO request, Long requestingUserId) {
        verifyAdminOrOwnerAccess(tenantId, requestingUserId);

        ChannelEntity channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found with ID: " + channelId));

        if (!channel.getTenantchannel().getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Channel does not belong to the specified workspace");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            channel.setName(request.getName().toLowerCase());
        }
        if (request.getDescription() != null) {
            channel.setDescription(request.getDescription());
        }

        ChannelEntity updated = channelRepository.save(channel);
        log.info("Updated channel ID: {} in tenant ID: {}", channelId, tenantId);

        return mapToResponse(updated);
    }
}