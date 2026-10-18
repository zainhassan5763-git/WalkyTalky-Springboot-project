package com.springboot_project.walkytalky.USERS_DETAILS.Security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.springboot_project.walkytalky.USERS_DETAILS.Entity.RefreshTokenEntity;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;
import com.springboot_project.walkytalky.USERS_DETAILS.Repository.RefreshTokenRepository;
import com.springboot_project.walkytalky.USERS_DETAILS.Repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class AuthenticationUtil {

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private static final long REFRESH_TOKEN_VALIDITY = 7L * 24 * 60 * 60 * 1000; // 7 days in ms

    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));        
    }

    // _______ GENERATING JWT TOKEN
    public String generateToken(UserEntity user){
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .subject(user.getEmail())
            .claim("UserId", user.getUserId())
            .claim("role", user.getRole())
            .issuedAt(new Date(now)) // FIXED: Current timestamp
            .expiration(new Date(now + 1000 * 60 * 60)) // FIXED: 1 Hour expiration
            .signWith(getSecretKey())
            .compact();
    }

    // _______ GENERATING REFRESH TOKEN
    @Transactional 
    public RefreshTokenEntity createRefreshToken(UserEntity user){
        refreshTokenRepository.deleteByUserstoken(user);
        refreshTokenRepository.flush();

        RefreshTokenEntity refreshtoken = RefreshTokenEntity.builder()
                .userstoken(user)
                .refreshtoken(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(REFRESH_TOKEN_VALIDITY)) // FIXED: Changed to expiryDate
                .build();

        return refreshTokenRepository.save(refreshtoken);
    }

    @Transactional 
    public RefreshTokenEntity checkRefreshToken(Long userId){
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        return createRefreshToken(user);
    }

    public Optional<RefreshTokenEntity> findByToken(String refreshtoken){
        return refreshTokenRepository.findByRefreshtoken(refreshtoken);
    }

    public RefreshTokenEntity verifyExpiration(RefreshTokenEntity verifyRefreshtoken){
        if (verifyRefreshtoken.getExpiryDate().compareTo(Instant.now()) < 0){ // FIXED: Changed getter to getExpiryDate()
            refreshTokenRepository.delete(verifyRefreshtoken);      
            throw new IllegalArgumentException("Refresh token was expired. Please make a new signin request.");      
        }
        return verifyRefreshtoken;
    }

    @Transactional 
    public int deleteByRefreshtoken(String deleteRefreshtoken){
        return refreshTokenRepository.deleteByRefreshtoken(deleteRefreshtoken);
    }

    @Transactional 
    public int checkdeleteRefreshtoken(String deleteRefreshtoken){
        return deleteByRefreshtoken(deleteRefreshtoken);
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenExpired(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return expiration.before(new Date());
    }
}