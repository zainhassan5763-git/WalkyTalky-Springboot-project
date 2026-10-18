package com.springboot_project.walkytalky.CHANNEL_DETAILS.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChannelRequestDTO {

    @NotBlank(message = "Channel name is required")
    @Size(min = 2, max = 80, message = "Channel name must be between 2 and 80 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Channel name can only contain lowercase letters, numbers, and hyphens")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private Boolean isPrivate;
}
