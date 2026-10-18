package com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Entity.TenantMemberShipEntity;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;

import java.util.List;
import java.util.Optional;


public interface TenantMemberShipRepository extends JpaRepository<TenantMemberShipEntity , Long>{

    List<TenantMemberShipEntity> findByAlluser(UserEntity alluser);    // Finds all tenant memberships for a specific user.

    Optional<TenantMemberShipEntity> findByAlluser_UserIdAndAlltenant_TenantId(Long userId , Long tenantId); 

    boolean existsByAlltenant_TenantIdAndAlluser_UserId(Long tenantId, Long userId);

    // @EntityGraph -- Fetches all members belonging to a specific workspace (e.g. for team directory UI)
    @EntityGraph(attributePaths = {"alluser"}) 
    List<TenantMemberShipEntity> findByAlltenant_TenantId(Long tenantId);

    // @EntityGraph
    @Override // we override the previous EntityGraph
    @EntityGraph(attributePaths = {"alluser","alltenant"}) 
    List <TenantMemberShipEntity> findAll();
}