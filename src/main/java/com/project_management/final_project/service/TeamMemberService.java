package com.project_management.final_project.service;

import com.project_management.final_project.dto.request.AddTeamMemberRequest;
import com.project_management.final_project.dto.request.TeamMemberFilterRequest;
import com.project_management.final_project.dto.response.TeamMemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TeamMemberService {
    
    /**
     * Add team members to a project
     * @param projectId The ID of the project to add members to
     * @param requests List of team member requests containing user ID and specialization ID
     * @return Number of team members added
     */
    int addTeamMembers(Integer projectId, List<AddTeamMemberRequest> requests);
    
    /**
     * Get team members for a project with filtering
     * @param projectId The ID of the project to get members for
     * @param filterRequest Filter criteria including search term and specialization ID
     * @param pageable Pagination information
     * @return Page of team member responses
     */
    Page<TeamMemberResponse> getProjectTeamMembers(Integer projectId, TeamMemberFilterRequest filterRequest, Pageable pageable);
    
    /**
     * Delete a team member from a project
     * @param teamMemberId The ID of the team member to delete
     * @return true if the team member was deleted successfully, false otherwise
     */
    boolean deleteTeamMember(Integer teamMemberId);
} 