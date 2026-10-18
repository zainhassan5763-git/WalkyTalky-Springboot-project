package com.springboot_project.walkytalky.CHANNEL_DETAILS.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelResponseDTO {

    private Long channelId;
    private String name;
    private String description;
    private Boolean isPrivate;
    private Long tenantId;
    private LocalDateTime createdAt;
}