package com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.springboot_project.walkytalky.TENANT_DETAILS.Entity.TenantEntity;
import com.springboot_project.walkytalky.TENANT_DETAILS.Repository.TenantRepository;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.DTO.AddMemberRequestDTO;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.DTO.TenantMemberResponse;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Entity.TenantMemberShipEntity;
import com.springboot_project.walkytalky.TENANT_MEMBERSHIP_DETAILS.Repository.TenantMemberShipRepository;
import com.springboot_project.walkytalky.USERS_DETAILS.Entity.UserEntity;
import com.springboot_project.walkytalky.USERS_DETAILS.Repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Service 
@RequiredArgsConstructor 
public class TenantMemberShipService {

    private final TenantMemberShipRepository tenantMemberShipRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    private void verifyAdminOrOwnerAccess(Long tenantId , Long requestingUserId){
        TenantMemberShipEntity checkMembers = tenantMemberShipRepository.findByAlluser_UserIdAndAlltenant_TenantId(requestingUserId, tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Access denied: You are not a member of this workspace"));

        //here we are checking is the member is owner or admin
        if(!"OWNER".equals(checkMembers.getRole()) && !"ADMIN".equals(checkMembers.getRole())){
            throw new IllegalArgumentException("Access denied: Only workspace Owners or Admins can perform this action");
        }
    }


    // here we maps tenanrmembership to TenantMemberResponse DTO
    private TenantMemberResponse maptoResponse(TenantMemberShipEntity tenantMemberShipEntity){
        UserEntity tenantMemberUser = tenantMemberShipEntity.getAlluser();
        return TenantMemberResponse.builder()
                .membershipId(tenantMemberShipEntity.getMembershipId())
                .userId(tenantMemberUser.getUserId())
                .fullName(tenantMemberUser.getFullName())
                .email(tenantMemberUser.getEmail())
                .avatarUrl(tenantMemberUser.getAvatarUrl())
                .role(tenantMemberShipEntity.getRole())
                .joinedAt(tenantMemberShipEntity.getCreatedAt())
                .build();
    }


    // adding memebr to workspace
    @Transactional 
    public TenantMemberResponse addMember(Long tenantId , AddMemberRequestDTO memberReq , Long requestingUserId){

        // ensure that requester has permission to add members
        verifyAdminOrOwnerAccess(tenantId, requestingUserId);

        // check whether user is already in workspace or not
        if (tenantMemberShipRepository.existsByAlltenant_TenantIdAndAlluser_UserId(tenantId, memberReq.getUserId())){
            throw new IllegalArgumentException("User is already a member of this workspace");
        } 

        // Retrieve tenant and target user entities
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found with ID: " + tenantId));

        UserEntity userToAdd = userRepository.findById(memberReq.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + memberReq.getUserId()));

        // save new members
        TenantMemberShipEntity savingMembers = TenantMemberShipEntity.builder()
                .alltenant(tenant)
                .alluser(userToAdd)
                .role(memberReq.getRole())
                .build();
        TenantMemberShipEntity saved = tenantMemberShipRepository.save(savingMembers);
        
        return maptoResponse(saved);
    } 



    // getting members
    @Transactional(readOnly = true)
    public List<TenantMemberResponse> getMembersByTenant(Long tenantId){
        return tenantMemberShipRepository.findByAlltenant_TenantId(tenantId).stream()
                        .map(this::maptoResponse)
                        .collect(Collectors.toList());
    }


    // delete members
    @Transactional 
    public boolean deleteMembersByTenant(Long tenantId , Long userID , Long requestingUserId){

        // ensure that whom is requesting has permission or not
        verifyAdminOrOwnerAccess(tenantId , requestingUserId); 

        TenantMemberShipEntity memberToDelete = tenantMemberShipRepository
                    .findByAlluser_UserIdAndAlltenant_TenantId(userID, tenantId)
                    .orElse(null);

        if (memberToDelete != null) {
            log.info("Deleting member with user id: {} from tenant: {}", userID, tenantId);
            tenantMemberShipRepository.delete(memberToDelete);
            return true;
        }

        log.warn("Attempted to delete non-existent member with user id: {} in tenant: {}", userID, tenantId);
        return false;

    }



    // update members
    @Transactional 
    public TenantMemberShipEntity updateMembersByTenant(Long tenantId , Long userID , Long requestingUserId , TenantMemberShipEntity updatedMembers){

        // ensure that whom is requesting has permission or not
        verifyAdminOrOwnerAccess(tenantId , requestingUserId); 

        TenantMemberShipEntity foundMember = tenantMemberShipRepository.findByAlluser_UserIdAndAlltenant_TenantId(userID, tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found in this workspace"));

        foundMember.setRole(updatedMembers.getRole());
        tenantMemberShipRepository.save(foundMember);

        log.info("Updating membership role for user ID: {} in tenant ID: {}", userID, tenantId);
        return foundMember;
    }

    

}
