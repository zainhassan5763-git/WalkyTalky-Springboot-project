package com.springboot_project.walkytalky.MESSAGE_DETAILS.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequestDTO {

    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 4000, message = "Message content cannot exceed 4000 characters")
    private String content;
}
