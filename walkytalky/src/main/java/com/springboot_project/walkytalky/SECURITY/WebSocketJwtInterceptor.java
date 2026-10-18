package com.springboot_project.walkytalky.SECURITY;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;
import com.springboot_project.walkytalky.USERS_DETAILS.Repository.UserRepository;
import com.springboot_project.walkytalky.USERS_DETAILS.Security.AuthenticationUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final AuthenticationUtil authenticationUtil;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();

                try {
                    String email = authenticationUtil.getEmailFromToken(token);

                    if (email != null && !authenticationUtil.isTokenExpired(token)) {
                        UserEntity userEntity = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userEntity, null, userEntity.getAuthorities()
                        );

                        accessor.setUser(authToken);
                        log.info("Successfully authenticated STOMP connection for user: {}", email);
                    }
                } catch (Exception e) {
                    log.error("WebSocket STOMP authentication failed: {}", e.getMessage());
                }
            } else {
                log.warn("STOMP CONNECT frame missing Bearer Authorization header");
            }
        }
        return message;
    }
}
