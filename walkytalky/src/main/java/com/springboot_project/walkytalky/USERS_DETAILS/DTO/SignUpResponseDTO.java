package com.springboot_project.walkytalky.USERS_DETAILS.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class SignUpResponseDTO {

    private Long userId;
    private String fullName;
}
