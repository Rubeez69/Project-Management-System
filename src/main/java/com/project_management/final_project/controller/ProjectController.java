package com.project_management.final_project.controller;

import com.project_management.final_project.dto.request.AddTeamMemberRequest;
import com.project_management.final_project.dto.request.CreateProjectRequest;
import com.project_management.final_project.dto.request.ProjectFilterRequest;
import com.project_management.final_project.dto.request.UpdateProjectRequest;
import com.project_management.final_project.dto.response.ApiResponse;
import com.project_management.final_project.dto.response.PagedResponse;
import com.project_management.final_project.dto.response.ProjectDetailResponse;
import com.project_management.final_project.dto.response.ProjectResponse;
import com.project_management.final_project.entities.Project;
import com.project_management.final_project.service.ProjectService;
import com.project_management.final_project.util.ApiResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    
    private final ProjectService projectService;
    
    @GetMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('PROJECT_VIEW')")
    public ApiResponse<PagedResponse<ProjectResponse>> getAllProjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Project.Status statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = Project.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, will be treated as null (no filter)
            }
        }
        
        ProjectFilterRequest filterRequest = ProjectFilterRequest.builder()
                .name(name)
                .status(statusEnum)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        
        PagedResponse<ProjectResponse> response = projectService.getAllProjects(filterRequest);
        return ApiResponseUtil.success(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('PROJECT_VIEW')")
    public ApiResponse<ProjectResponse> getProjectById(@PathVariable Integer id) {
        ProjectResponse project = projectService.getProjectById(id);
        return ApiResponseUtil.success(project);
    }
    
    @GetMapping("/{id}/detail")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('PROJECT_VIEW')")
    public ApiResponse<ProjectDetailResponse> getProjectDetail(@PathVariable Integer id) {
        ProjectDetailResponse projectDetail = projectService.getProjectDetail(id);
        return ApiResponseUtil.success(projectDetail);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('PROJECT_CREATE')")
    public ApiResponse<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse createdProject = projectService.createProject(request);
        return ApiResponseUtil.success(createdProject);
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('PROJECT_UPDATE')")
    public ApiResponse<ProjectResponse> updateProject(
            @PathVariable Integer id,
            @RequestBody UpdateProjectRequest request) {
        ProjectResponse updatedProject = projectService.updateProject(id, request);
        return ApiResponseUtil.success(updatedProject);
    }
    
    @PostMapping("/{id}/add-members")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TEAM_CREATE')")
    public ApiResponse<Map<String, Object>> addTeamMembers(
            @PathVariable Integer id,
            @Valid @RequestBody List<AddTeamMemberRequest> requests) {
        int addedCount = projectService.addTeamMembers(id, requests);
        return ApiResponseUtil.success(Map.of(
                "message", "Team members added successfully",
                "addedCount", addedCount
        ));
    }
} 