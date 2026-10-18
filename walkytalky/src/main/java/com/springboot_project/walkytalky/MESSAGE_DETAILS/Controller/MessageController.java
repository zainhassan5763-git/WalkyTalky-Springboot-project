package com.springboot_project.walkytalky.MESSAGE_DETAILS.Controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springboot_project.walkytalky.MESSAGE_DETAILS.DTO.MessageResponseDTO;
import com.springboot_project.walkytalky.MESSAGE_DETAILS.DTO.SendMessageRequestDTO;
import com.springboot_project.walkytalky.MESSAGE_DETAILS.Service.MessageService;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tenants/{tenantId}/channels/{channelId}/messages")
public class MessageController {

    private final MessageService messageService;

    // POST /api/tenants/{tenantId}/channels/{channelId}/messages - Send message
    @PostMapping
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @PathVariable Long tenantId,
            @PathVariable Long channelId,
            @Valid @RequestBody SendMessageRequestDTO request,
            @AuthenticationPrincipal UserEntity currentUser) {

        MessageResponseDTO response = messageService.sendMessage(tenantId, channelId, request, currentUser.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/tenants/{tenantId}/channels/{channelId}/messages - Get paginated messages
    @GetMapping
    public ResponseEntity<Page<MessageResponseDTO>> getChannelMessages(
            @PathVariable Long tenantId,
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserEntity currentUser) {

        Page<MessageResponseDTO> messages = messageService.getChannelMessages(tenantId, channelId, page, size, currentUser.getUserId());
        return ResponseEntity.ok(messages);
    }

    // PUT /api/tenants/{tenantId}/channels/{channelId}/messages/{messageId} - Edit message
    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponseDTO> updateMessage(
            @PathVariable Long tenantId,
            @PathVariable Long channelId,
            @PathVariable Long messageId,
            @Valid @RequestBody SendMessageRequestDTO request,
            @AuthenticationPrincipal UserEntity currentUser) {

        MessageResponseDTO response = messageService.updateMessage(tenantId, channelId, messageId, request, currentUser.getUserId());
        return ResponseEntity.ok(response);
    }

    // DELETE /api/tenants/{tenantId}/channels/{channelId}/messages/{messageId} - Delete message
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long tenantId,
            @PathVariable Long channelId,
            @PathVariable Long messageId,
            @AuthenticationPrincipal UserEntity currentUser) {

        messageService.deleteMessage(tenantId, channelId, messageId, currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}
