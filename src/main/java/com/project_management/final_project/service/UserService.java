package com.project_management.final_project.service;

import com.project_management.final_project.dto.request.UserFilterRequest;
import com.project_management.final_project.dto.response.PagedResponse;
import com.project_management.final_project.dto.response.UserResponse;

public interface UserService {
    
    /**
     * Get users for team member selection (project managers and developers)
     * @param filterRequest Filter parameters including search term and pagination
     * @return Paged response of users
     */
    PagedResponse<UserResponse> getUsersForTeamSelection(UserFilterRequest filterRequest);
} 