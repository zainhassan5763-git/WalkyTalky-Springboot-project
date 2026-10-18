package com.springboot_project.walkytalky.SECURITY;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;
import com.springboot_project.walkytalky.USERS_DETAILS.Repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedHeadAdmin();
    }

    private void seedHeadAdmin() {
        if (userRepository.findByEmail("admin@walkytalky.com").isEmpty()) {
            UserEntity headAdmin = UserEntity.builder()
                    .email("admin@walkytalky.com")
                    .password(passwordEncoder.encode("1z2a3i"))
                    .fullName("ZainAdmin")
                    .status("ACTIVE")
                    .provider("LOCAL")
                    .emailVerified(true)
                    .build();

            userRepository.save(headAdmin);
            log.info("--- HEAD ADMIN ACCOUNT CREATED (Email: admin@walkytalky.com | Name: ZainAdmin) ---");
        }
    }
}