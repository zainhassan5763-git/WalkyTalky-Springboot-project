package com.springboot_project.walkytalky.USERS_DETAILS.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;


public interface UserRepository extends JpaRepository<UserEntity,Long>{

    Optional<UserEntity> findByEmail(String email);    // Used during user authentication/login.
    Optional<UserEntity> findByFullName(String fullName);
    boolean existsByEmail(String email);    // Used during registration to check if an email is already taken.

    @EntityGraph(attributePaths = "userMembership") 
    List<UserEntity> findAll();
}





