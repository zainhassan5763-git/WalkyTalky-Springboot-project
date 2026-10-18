package com.springboot_project.walkytalky.MESSAGE_DETAILS.Controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import com.springboot_project.walkytalky.MESSAGE_DETAILS.DTO.MessageResponseDTO;
import com.springboot_project.walkytalky.MESSAGE_DETAILS.DTO.SendMessageRequestDTO;
import com.springboot_project.walkytalky.MESSAGE_DETAILS.Service.MessageService;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    
    // Destination: /app/tenants/{tenantId}/channels/{channelId}/send     
    @MessageMapping("/tenants/{tenantId}/channels/{channelId}/send")
    public void sendRealtimeMessage(
            @DestinationVariable Long tenantId,
            @DestinationVariable Long channelId,
            @Payload SendMessageRequestDTO request,
            Principal principal) {

        if (principal == null) {
            log.error("Unauthorized real-time message attempt for tenant ID: {}, channel ID: {}", tenantId, channelId);
            return;
        }

        // Extract authenticated UserEntity from Principal set by WebSocketJwtInterceptor
        UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) principal;
        UserEntity currentUser = (UserEntity) authToken.getPrincipal();

        // 1. Save message to database
        MessageResponseDTO savedMessage = messageService.sendMessage(tenantId, channelId, request, currentUser.getUserId());

        // 2. Broadcast saved message to channel topic subscribers
        String destination = String.format("/topic/tenants/%d/channels/%d", tenantId, channelId);
        messagingTemplate.convertAndSend(destination, savedMessage);

        log.info("Broadcasted real-time message ID {} from user ID {} to destination: {}", 
                savedMessage.getMessageId(), currentUser.getUserId(), destination);
    }
}
