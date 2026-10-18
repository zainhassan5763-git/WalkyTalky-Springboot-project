package com.springboot_project.walkytalky.USERS_DETAILS.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot_project.walkytalky.USERS_DETAILS.Entity.RefreshTokenEntity;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByRefreshtoken(String refreshtoken);

    int deleteByUserstoken(UserEntity userstoken);

    int deleteByRefreshtoken(String refreshtoken);
}