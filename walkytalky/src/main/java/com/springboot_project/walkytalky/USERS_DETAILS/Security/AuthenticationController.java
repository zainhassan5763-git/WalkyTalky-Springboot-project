package com.springboot_project.walkytalky.USERS_DETAILS.Security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot_project.walkytalky.USERS_DETAILS.DTO.LoginRequestDTO;
import com.springboot_project.walkytalky.USERS_DETAILS.DTO.LoginResponseDTO;
import com.springboot_project.walkytalky.USERS_DETAILS.DTO.RefreshTokenRequestDTO;
import com.springboot_project.walkytalky.USERS_DETAILS.DTO.SignUpRequestDTO;
import com.springboot_project.walkytalky.USERS_DETAILS.DTO.SignUpResponseDTO;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/userauth/") 
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    // Explicit constructor to bypass Lombok Java 25 bytecode issue
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // SignUp controller
    @PostMapping("/signup") 
    public ResponseEntity<SignUpResponseDTO> SignUp(@Valid @RequestBody SignUpRequestDTO signUpRequestDTO){
        return ResponseEntity.ok(authenticationService.SignUp(signUpRequestDTO));
    }

    // Login controller
    @PostMapping("/login") 
    public ResponseEntity<LoginResponseDTO> Login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            return ResponseEntity.ok(authenticationService.Login(loginRequestDTO));
        } catch (Throwable t) {
            t.printStackTrace(); 
            throw t;
        }
    }

    // Refreshtoken controller
    @PostMapping("/refresh") 
    public ResponseEntity<LoginResponseDTO> RefreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO){
        return ResponseEntity.ok(authenticationService.RefreshToken(refreshTokenRequestDTO));
    }

    // Logout controller
    @PostMapping("/logout") 
    public ResponseEntity<String> Logout(@Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO){
        authenticationService.Logout(refreshTokenRequestDTO);
        return ResponseEntity.ok("Logged out successfully!");
    }
}