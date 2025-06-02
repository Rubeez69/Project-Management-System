package com.project_management.final_project.service.impl;

import com.project_management.final_project.dto.request.UserFilterRequest;
import com.project_management.final_project.dto.response.PagedResponse;
import com.project_management.final_project.dto.response.UserResponse;
import com.project_management.final_project.entities.User;
import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import com.project_management.final_project.repository.UserRepository;
import com.project_management.final_project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    
    // Roles to include and exclude
    private static final List<String> INCLUDED_ROLES = Arrays.asList("PROJECT_MANAGER", "DEVELOPER");
    private static final List<String> EXCLUDED_ROLES = Collections.singletonList("ADMIN");

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public PagedResponse<UserResponse> getUsersForTeamSelection(UserFilterRequest filterRequest) {
        try {
            // Set default values if not provided
            int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
            int size = filterRequest.getSize() != null ? filterRequest.getSize() : 10;
            String sortBy = filterRequest.getSortBy() != null ? filterRequest.getSortBy() : "name";
            String sortDirection = filterRequest.getSortDirection() != null ? filterRequest.getSortDirection() : "asc";
            
            // Create pageable with sorting
            Sort.Direction direction = Sort.Direction.fromString(
                    sortDirection.equalsIgnoreCase("asc") ? "asc" : "desc"
            );
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            // Use roles from filter if provided, otherwise use default roles
            List<String> rolesToInclude = filterRequest.getRoles() != null && !filterRequest.getRoles().isEmpty() 
                    ? filterRequest.getRoles() 
                    : INCLUDED_ROLES;
            
            // Query users with filters
            Page<User> usersPage = userRepository.findByRolesAndSearchTerm(
                    rolesToInclude,
                    EXCLUDED_ROLES,
                    filterRequest.getSearch(),
                    pageable
            );
            
            // Map to response DTOs
            Page<UserResponse> userResponsePage = usersPage.map(UserResponse::fromEntity);
            
            logger.info("Retrieved {} users for team selection", usersPage.getTotalElements());
            
            return PagedResponse.fromPage(userResponsePage);
        } catch (Exception e) {
            logger.error("Error retrieving users for team selection: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public boolean hasRole(Integer userId, String roleName) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found with ID: " + userId));
            
            if (user.getRole() == null) {
                logger.warn("User ID {} has no role assigned", userId);
                return false;
            }
            
            boolean hasRole = user.getRole().getName().equals(roleName);
            logger.debug("User ID {} {} role {}", userId, hasRole ? "has" : "does not have", roleName);
            
            return hasRole;
        } catch (AppException e) {
            logger.warn("Error checking role for user ID {}: {}", userId, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error checking role for user ID {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
} 