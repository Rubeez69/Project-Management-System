package com.project_management.final_project.controller;

import com.project_management.final_project.dto.request.AddTeamMemberRequest;
import com.project_management.final_project.dto.request.TeamMemberFilterRequest;
import com.project_management.final_project.dto.response.ApiResponse;
import com.project_management.final_project.dto.response.PagedResponse;
import com.project_management.final_project.dto.response.TeamMemberResponse;
import com.project_management.final_project.dto.response.TeamMemberWithWorkloadResponse;
import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import com.project_management.final_project.service.TeamMemberService;
import com.project_management.final_project.util.ApiResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/team-members")
@RequiredArgsConstructor
public class TeamMemberController {
    
    private static final Logger logger = LoggerFactory.getLogger(TeamMemberController.class);
    private final TeamMemberService teamMemberService;
    
    @PostMapping("/projects/{projectId}/add")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TEAM_CREATE')")
    public ApiResponse<Map<String, Object>> addTeamMembers(
            @PathVariable Integer projectId,
            @Valid @RequestBody List<AddTeamMemberRequest> requests) {
        try {
            logger.info("Received request to add {} team members to project ID: {}", requests.size(), projectId);
            
            if (requests.isEmpty()) {
                logger.warn("Empty team member request list for project ID: {}", projectId);
                throw new AppException(ErrorCode.INVALID_REQUEST, "No team members provided");
            }
            
            int addedCount = teamMemberService.addTeamMembers(projectId, requests);
            
            logger.info("Successfully added {} team members to project ID: {}", addedCount, projectId);
            
            return ApiResponseUtil.success(Map.of(
                    "message", "Team members added successfully",
                    "addedCount", addedCount
            ));
        } catch (AppException e) {
            logger.error("Application exception while adding team members to project ID {}: {}", projectId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while adding team members to project ID {}: {}", projectId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to add team members: " + e.getMessage());
        }
    }
    
    @GetMapping("/projects/{projectId}/member-list")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TEAM_VIEW')")
    public ApiResponse<PagedResponse<TeamMemberResponse>> getProjectTeamMembers(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer specializationId,
            @PageableDefault(size = 5, sort = "user.name", direction = Sort.Direction.ASC) Pageable pageable) {
        
        TeamMemberFilterRequest filterRequest = TeamMemberFilterRequest.builder()
                .search(search)
                .specializationId(specializationId)
                .build();
        
        Page<TeamMemberResponse> teamMembers = teamMemberService.getProjectTeamMembers(
                projectId, filterRequest, pageable);
        
        return ApiResponseUtil.success(PagedResponse.fromPage(teamMembers));
    }
    
    @GetMapping("/projects/{projectId}/members-with-workload")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TEAM_VIEW')")
    public ApiResponse<PagedResponse<TeamMemberWithWorkloadResponse>> getProjectTeamMembersWithWorkload(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer specializationId,
            @PageableDefault(size = 5, sort = "user.name", direction = Sort.Direction.ASC) Pageable pageable) {
        
        TeamMemberFilterRequest filterRequest = TeamMemberFilterRequest.builder()
                .search(search)
                .specializationId(specializationId)
                .build();
        
        Page<TeamMemberWithWorkloadResponse> teamMembers = teamMemberService.getProjectTeamMembersWithWorkload(
                projectId, filterRequest, pageable);
        
        return ApiResponseUtil.success(PagedResponse.fromPage(teamMembers));
    }
    
    @DeleteMapping("/{teamMemberId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TEAM_DELETE')")
    public ApiResponse<Map<String, Object>> deleteTeamMember(@PathVariable Integer teamMemberId) {
        try {
            logger.info("Received request to delete team member with ID: {}", teamMemberId);
            
            if (teamMemberId == null || teamMemberId <= 0) {
                logger.warn("Invalid team member ID: {}", teamMemberId);
                throw new AppException(ErrorCode.INVALID_KEY, "Invalid team member ID");
            }
            
            boolean deleted = teamMemberService.deleteTeamMember(teamMemberId);
            logger.info("Team member with ID {} was successfully deleted: {}", teamMemberId, deleted);
            
            return ApiResponseUtil.success(Map.of(
                    "message", "Team member removed successfully",
                    "deleted", deleted
            ));
        } catch (AppException e) {
            logger.error("Application exception while deleting team member ID {}: {}", teamMemberId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while deleting team member ID {}: {}", teamMemberId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to delete team member: " + e.getMessage());
        }
    }
    
    /**
     * Get team members for a project excluding the current user (for developers)
     *
     * @param projectId The ID of the project
     * @param search Optional search term for user name or email
     * @param specializationId Optional specialization ID filter
     * @param pageable Pagination information
     * @return Page of team members
     */
    @GetMapping("/projects/{projectId}/my-team")
    @PreAuthorize("hasRole('DEVELOPER') and hasAuthority('TEAM_VIEW')")
    public ApiResponse<PagedResponse<TeamMemberResponse>> getMyTeamMembers(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer specializationId,
            @PageableDefault(size = 5, sort = "user.name", direction = Sort.Direction.ASC) Pageable pageable) {
        
        logger.info("Getting team members for project ID {} excluding current user", projectId);
        
        TeamMemberFilterRequest filterRequest = TeamMemberFilterRequest.builder()
                .search(search)
                .specializationId(specializationId)
                .build();
        
        Page<TeamMemberResponse> teamMembers = teamMemberService.getMyTeamMembers(
                projectId, filterRequest, pageable);
        
        return ApiResponseUtil.success(PagedResponse.fromPage(teamMembers));
    }
} 