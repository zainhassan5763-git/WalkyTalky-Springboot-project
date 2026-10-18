package com.springboot_project.walkytalky.USERS_DETAILS.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 
public class LoginResponseDTO {

    private String jwtToken;
    private String refreshToken;


    private Long userId;
    private String email;
    private String fullName;
    private String avatarUrl;

}
