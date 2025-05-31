package com.project_management.final_project.service;

import com.project_management.final_project.dto.request.AddTeamMemberRequest;

import java.util.List;

public interface TeamMemberService {
    
    /**
     * Add team members to a project
     * @param projectId The ID of the project to add members to
     * @param requests List of team member requests containing user ID and specialization ID
     * @return Number of team members added
     */
    int addTeamMembers(Integer projectId, List<AddTeamMemberRequest> requests);
} 