package com.project_management.final_project.controller;

import com.project_management.final_project.dto.request.UserFilterRequest;
import com.project_management.final_project.dto.response.ApiResponse;
import com.project_management.final_project.dto.response.PagedResponse;
import com.project_management.final_project.dto.response.UserResponse;
import com.project_management.final_project.service.UserService;
import com.project_management.final_project.util.ApiResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/select-member")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('USER_VIEW')")
    public ApiResponse<PagedResponse<UserResponse>> getUsersForTeamSelection(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> roles,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        UserFilterRequest filterRequest = UserFilterRequest.builder()
                .search(search)
                .roles(roles)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        
        PagedResponse<UserResponse> response = userService.getUsersForTeamSelection(filterRequest);
        return ApiResponseUtil.success(response);
    }
} 