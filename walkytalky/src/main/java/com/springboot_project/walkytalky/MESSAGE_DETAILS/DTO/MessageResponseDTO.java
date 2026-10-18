package com.springboot_project.walkytalky.MESSAGE_DETAILS.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {

    private Long messageId;
    private String content;
    private Long senderId;
    private String senderFullName;
    private String senderAvatarUrl;
    private Long channelId;
    private Boolean isEdited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
