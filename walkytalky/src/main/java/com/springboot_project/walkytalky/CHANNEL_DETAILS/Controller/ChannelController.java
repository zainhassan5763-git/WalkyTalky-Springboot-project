package com.springboot_project.walkytalky.CHANNEL_DETAILS.Controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.springboot_project.walkytalky.CHANNEL_DETAILS.DTO.ChannelResponseDTO;
import com.springboot_project.walkytalky.CHANNEL_DETAILS.DTO.CreateChannelRequestDTO;
import com.springboot_project.walkytalky.CHANNEL_DETAILS.Service.ChannelService;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tenants/{tenantId}/channels")
public class ChannelController {

    private final ChannelService channelService;

    // POST /api/tenants/{tenantId}/channels - Create a new channel
    @PostMapping
    public ResponseEntity<ChannelResponseDTO> createChannel(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateChannelRequestDTO request,
            @AuthenticationPrincipal UserEntity currentUser) {

        ChannelResponseDTO response = channelService.createChannel(tenantId, request, currentUser.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/tenants/{tenantId}/channels - Get all channels in workspace
    @GetMapping
    public ResponseEntity<List<ChannelResponseDTO>> getChannelsByTenant(
            @PathVariable Long tenantId,
            @AuthenticationPrincipal UserEntity currentUser) {

        List<ChannelResponseDTO> channels = channelService.getChannelsByTenant(tenantId, currentUser.getUserId());
        return ResponseEntity.ok(channels);
    }

    // GET /api/tenants/{tenantId}/channels/{channelId} - Get specific channel details
    @GetMapping("/{channelId}")
    public ResponseEntity<ChannelResponseDTO> getChannelById(
            @PathVariable Long tenantId,
            @PathVariable Long channelId,
            @AuthenticationPrincipal UserEntity currentUser) {

        ChannelResponseDTO channel = channelService.getChannelById(tenantId, channelId, currentUser.getUserId());
        return ResponseEntity.ok(channel);
    }

    // DELETE /api/tenants/{tenantId}/channels/{channelId} - Delete channel
    @Transactional
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(
            @PathVariable Long tenantId,
            @PathVariable Long channelId,
            @AuthenticationPrincipal UserEntity currentUser) {

        channelService.deleteChannel(tenantId, channelId, currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{channelId}")
    public ResponseEntity<ChannelResponseDTO> updateChannel(
            @PathVariable Long tenantId,
            @PathVariable Long channelId,
            @Valid @RequestBody CreateChannelRequestDTO request,
            @AuthenticationPrincipal UserEntity currentUser) {

        ChannelResponseDTO response = channelService.updateChannel(tenantId, channelId, request, currentUser.getUserId());
        return ResponseEntity.ok(response);
    }
}
