package com.project_management.final_project.controller;

import com.project_management.final_project.dto.request.AddTeamMemberRequest;
import com.project_management.final_project.dto.response.ApiResponse;
import com.project_management.final_project.service.TeamMemberService;
import com.project_management.final_project.util.ApiResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/team-members")
@RequiredArgsConstructor
public class TeamMemberController {
    
    private final TeamMemberService teamMemberService;
    
    @PostMapping("/projects/{projectId}/add")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TEAM_CREATE')")
    public ApiResponse<Map<String, Object>> addTeamMembers(
            @PathVariable Integer projectId,
            @Valid @RequestBody List<AddTeamMemberRequest> requests) {
        int addedCount = teamMemberService.addTeamMembers(projectId, requests);
        return ApiResponseUtil.success(Map.of(
                "message", "Team members added successfully",
                "addedCount", addedCount
        ));
    }
} 