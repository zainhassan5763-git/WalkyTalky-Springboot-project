package com.springboot_project.walkytalky.USERS_DETAILS.Security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot_project.walkytalky.TENANT_DETAILS.Entity.TenantEntity;
import com.springboot_project.walkytalky.TENANT_DETAILS.Repository.TenantRepository;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Entity.TenantMemberShipEntity;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Repository.TenantMemberShipRepository;
import com.springboot_project.walkytalky.USERS_DETAILS.DTO.LoginRequestDTO;
import com.springboot_project.walkytalky.USERS_DETAILS.DTO.LoginResponseDTO;
import com.springboot_project.walkytalky.USERS_DETAILS.DTO.RefreshTokenRequestDTO;
import com.springboot_project.walkytalky.USERS_DETAILS.DTO.SignUpRequestDTO;
import com.springboot_project.walkytalky.USERS_DETAILS.DTO.SignUpResponseDTO;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.RefreshTokenEntity;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;
import com.springboot_project.walkytalky.USERS_DETAILS.Repository.UserRepository;

@Service 
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuthenticationUtil authenticationUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TenantMemberShipRepository tenantMembershipRepository; 
    private final TenantRepository tenantRepository;

    // Explicit constructor
    public AuthenticationService(UserRepository userRepository,
                                 AuthenticationUtil authenticationUtil,
                                 PasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager,
                                 TenantMemberShipRepository tenantMembershipRepository,
                                 TenantRepository tenantRepository) {
        this.userRepository = userRepository;
        this.authenticationUtil = authenticationUtil;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tenantMembershipRepository = tenantMembershipRepository;
        this.tenantRepository = tenantRepository;
    }

    // (SIGN_UP)
    @Transactional
    public SignUpResponseDTO SignUp(SignUpRequestDTO signUpRequestDTO){
        if (userRepository.existsByEmail(signUpRequestDTO.getEmail())){
            throw new IllegalArgumentException("Email already exists!");
        }

        UserEntity newUser = UserEntity.builder()
                .email(signUpRequestDTO.getEmail())
                .fullName(signUpRequestDTO.getFullName())
                .password(passwordEncoder.encode(signUpRequestDTO.getPassword()))
                .avatarUrl(signUpRequestDTO.getAvatarUrl())
                .role("ROLE_USER")
                .status("ACTIVE")
                .provider("LOCAL")
                .emailVerified(false)
                .build();

        UserEntity savedUser = userRepository.save(newUser);

        // Fetch default workspace (Tenant ID 1)
        TenantEntity defaultTenant = tenantRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Default workspace tenant not found with ID: 1"));

        // Automatically assign new user to default workspace as a MEMBER
        TenantMemberShipEntity membership = TenantMemberShipEntity.builder()
                .alltenant(defaultTenant)
                .alluser(savedUser)
                .role("MEMBER")
                .build();
        
        tenantMembershipRepository.save(membership);

        return new SignUpResponseDTO(savedUser.getUserId(), savedUser.getFullName());
    }

    // (LOGIN)
    @Transactional
    public LoginResponseDTO Login(LoginRequestDTO loginRequestDTO) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequestDTO.getEmail(),
                loginRequestDTO.getPassword()
            )
        ); 

        UserEntity user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user email or password!"));

        String accessToken = authenticationUtil.generateToken(user);
        RefreshTokenEntity refreshTokenEntity = authenticationUtil.createRefreshToken(user);

        return LoginResponseDTO.builder()
                .jwtToken(accessToken)
                .refreshToken(refreshTokenEntity.getRefreshtoken())
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    // (REFRESH TOKEN)
    @Transactional 
    public LoginResponseDTO RefreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO){
        RefreshTokenEntity tokenEntity = authenticationUtil.findByToken(refreshTokenRequestDTO.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found in database!"));

        RefreshTokenEntity verifiedToken = authenticationUtil.verifyExpiration(tokenEntity);

        UserEntity user = verifiedToken.getUserstoken();
        String newAccessToken = authenticationUtil.generateToken(user);

        return LoginResponseDTO.builder()
                .jwtToken(newAccessToken)
                .refreshToken(verifiedToken.getRefreshtoken())
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    // (LOGOUT)
    @Transactional
    public void Logout(RefreshTokenRequestDTO refreshTokenRequestDTO){
        authenticationUtil.deleteByRefreshtoken(refreshTokenRequestDTO.getRefreshToken());
    }
}