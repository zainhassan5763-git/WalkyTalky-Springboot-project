package com.springboot_project.walkytalky.MESSAGE_DETAILS.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot_project.walkytalky.CHANNEL_DETAILS.Entity.ChannelEntity;
import com.springboot_project.walkytalky.CHANNEL_DETAILS.Repository.ChannelRepository;
import com.springboot_project.walkytalky.MESSAGE_DETAILS.DTO.MessageResponseDTO;
import com.springboot_project.walkytalky.MESSAGE_DETAILS.DTO.SendMessageRequestDTO;
import com.springboot_project.walkytalky.MESSAGE_DETAILS.Entity.MessageEntity;
import com.springboot_project.walkytalky.MESSAGE_DETAILS.Repository.MessageRepository;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Entity.TenantMemberShipEntity;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Repository.TenantMemberShipRepository;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;
import com.springboot_project.walkytalky.USERS_DETAILS.Repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final TenantMemberShipRepository membershipRepository;

    
    private TenantMemberShipEntity verifyWorkspaceMembership(Long tenantId, Long requestingUserId) {
        return membershipRepository.findByAlluser_UserIdAndAlltenant_TenantId(requestingUserId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Access denied: You are not a member of this workspace"));
    }

    
    private ChannelEntity verifyChannelInTenant(Long tenantId, Long channelId) {
        ChannelEntity channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found with ID: " + channelId));

        if (!channel.getTenantchannel().getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Channel does not belong to the specified workspace");
        }
        return channel;
    }

    
    public MessageResponseDTO mapToResponse(MessageEntity entity) {
        return MessageResponseDTO.builder()
                .messageId(entity.getMessageId())
                .content(entity.getContent())
                .senderId(entity.getSender().getUserId())
                .senderFullName(entity.getSender().getFullName())
                .senderAvatarUrl(entity.getSender().getAvatarUrl())
                .channelId(entity.getChannel().getChannelId())
                .isEdited(entity.getIsEdited())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    
    @Transactional
    public MessageResponseDTO sendMessage(Long tenantId, Long channelId, SendMessageRequestDTO request, Long requestingUserId) {
        verifyWorkspaceMembership(tenantId, requestingUserId);
        ChannelEntity channel = verifyChannelInTenant(tenantId, channelId);

        UserEntity sender = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + requestingUserId));

        MessageEntity message = MessageEntity.builder()
                .content(request.getContent())
                .sender(sender)
                .channel(channel)
                .isEdited(false)
                .build();

        MessageEntity saved = messageRepository.save(message);
        log.info("User ID {} sent message ID {} to channel ID {}", requestingUserId, saved.getMessageId(), channelId);

        return mapToResponse(saved);
    }

    
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getChannelMessages(Long tenantId, Long channelId, int page, int size, Long requestingUserId) {
        verifyWorkspaceMembership(tenantId, requestingUserId);
        verifyChannelInTenant(tenantId, channelId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return messageRepository.findByChannel_ChannelIdOrderByCreatedAtDesc(channelId, pageable)
                .map(this::mapToResponse);
    }

    
    @Transactional
    public MessageResponseDTO updateMessage(Long tenantId, Long channelId, Long messageId, SendMessageRequestDTO request, Long requestingUserId) {
        verifyWorkspaceMembership(tenantId, requestingUserId);
        verifyChannelInTenant(tenantId, channelId);

        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

        if (!message.getSender().getUserId().equals(requestingUserId)) {
            throw new IllegalArgumentException("Access denied: You can only edit your own messages");
        }

        message.setContent(request.getContent());
        message.setIsEdited(true);

        MessageEntity updated = messageRepository.save(message);
        log.info("Message ID {} edited by user ID {}", messageId, requestingUserId);

        return mapToResponse(updated);
    }

    
    @Transactional
    public void deleteMessage(Long tenantId, Long channelId, Long messageId, Long requestingUserId) {
        TenantMemberShipEntity membership = verifyWorkspaceMembership(tenantId, requestingUserId);
        verifyChannelInTenant(tenantId, channelId);

        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

        boolean isAuthor = message.getSender().getUserId().equals(requestingUserId);
        boolean isAdminOrOwner = "OWNER".equals(membership.getRole()) || "ADMIN".equals(membership.getRole());

        if (!isAuthor && !isAdminOrOwner) {
            throw new IllegalArgumentException("Access denied: You do not have permission to delete this message");
        }

        messageRepository.delete(message);
        log.info("Message ID {} deleted from channel ID {}", messageId, channelId);
    }
}
