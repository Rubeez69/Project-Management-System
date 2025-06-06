package com.project_management.final_project.controller;

import com.project_management.final_project.dto.request.CreateProjectRequest;
import com.project_management.final_project.dto.request.ProjectFilterRequest;
import com.project_management.final_project.dto.request.UpdateProjectRequest;
import com.project_management.final_project.dto.response.ApiResponse;
import com.project_management.final_project.dto.response.PagedResponse;
import com.project_management.final_project.dto.response.ProjectDetailResponse;
import com.project_management.final_project.dto.response.ProjectDropdownResponse;
import com.project_management.final_project.dto.response.ProjectResponse;
import com.project_management.final_project.entities.Project;
import com.project_management.final_project.service.ProjectService;
import com.project_management.final_project.util.ApiResponseUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectService projectService;
    
    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }
    
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
    
    @GetMapping("/dropdown")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('PROJECT_VIEW')")
    public ApiResponse<PagedResponse<ProjectDropdownResponse>> getAllProjectsDropdown(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size) {
        
        PagedResponse<ProjectDropdownResponse> response = projectService.getProjectsForDropdown(search, page, size);
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
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse createdProject = projectService.createProject(request);
        
        ApiResponse<ProjectResponse> response = ApiResponse.<ProjectResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Project created successfully")
                .result(createdProject)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('PROJECT_UPDATE')")
    public ResponseEntity<ApiResponse<PagedResponse<ProjectResponse>>> updateProject(
            @PathVariable Integer id,
            @RequestBody UpdateProjectRequest request) {
        
        PagedResponse<ProjectResponse> updatedProjectsList = projectService.updateProjectAndReturnAll(id, request);
        
        ApiResponse<PagedResponse<ProjectResponse>> response = ApiResponse.<PagedResponse<ProjectResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Project updated successfully")
                .result(updatedProjectsList)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get projects where the current user is a team member
     *
     * @param name          Optional project name filter
     * @param status        Optional project status filter
     * @param page          Page number (zero-based)
     * @param size          Page size
     * @param sortBy        Field to sort by
     * @param sortDirection Sort direction (asc or desc)
     * @return Paged response of projects
     */
    @GetMapping("/my-projects")
    @PreAuthorize("hasRole('DEVELOPER') and hasAuthority('PROJECT_VIEW')")
    public ApiResponse<PagedResponse<ProjectResponse>> getMyProjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        logger.info("Getting projects where current user is a team member");
        
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
        
        PagedResponse<ProjectResponse> response = projectService.getMyProjects(filterRequest);
        return ApiResponseUtil.success(response);
    }
    
    /**
     * Get dropdown list of projects where the current user is a team member
     *
     * @param search Optional search term for project name
     * @param page Page number (zero-based)
     * @param size Page size
     * @return Paged response of projects with minimal information (id and name)
     */
    @GetMapping("/my-projects/dropdown")
    @PreAuthorize("hasRole('DEVELOPER') and hasAuthority('PROJECT_VIEW')")
    public ApiResponse<PagedResponse<ProjectDropdownResponse>> getMyProjectsDropdown(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size) {
        
        logger.info("Getting dropdown list of projects where current user is a team member");
        
        PagedResponse<ProjectDropdownResponse> response = projectService.getMyProjectsForDropdown(search, page, size);
        return ApiResponseUtil.success(response);
    }
} 