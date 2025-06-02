package com.project_management.final_project.service;

import com.project_management.final_project.dto.request.CreateProjectRequest;
import com.project_management.final_project.dto.request.ProjectFilterRequest;
import com.project_management.final_project.dto.request.UpdateProjectRequest;
import com.project_management.final_project.dto.response.PagedResponse;
import com.project_management.final_project.dto.response.ProjectDetailResponse;
import com.project_management.final_project.dto.response.ProjectDropdownResponse;
import com.project_management.final_project.dto.response.ProjectResponse;
import com.project_management.final_project.entities.Project;

public interface ProjectService {
    /**
     * Get all projects with filtering, searching and pagination
     * @param filterRequest The filter request containing search, filter and pagination parameters
     * @return A paged response of projects
     */
    PagedResponse<ProjectResponse> getAllProjects(ProjectFilterRequest filterRequest);
    
    /**
     * Create a new project
     * @param request The project creation request
     * @return The created project
     */
    ProjectResponse createProject(CreateProjectRequest request);
    
    /**
     * Update an existing project
     * @param id The ID of the project to update
     * @param request The project update request
     * @return The updated project
     */
    ProjectResponse updateProject(Integer id, UpdateProjectRequest request);
    
    /**
     * Get a project by ID
     * @param id The ID of the project to retrieve
     * @return The project
     */
    ProjectResponse getProjectById(Integer id);
    
    /**
     * Get detailed information about a project including team members
     * @param id The ID of the project to retrieve
     * @return The project details with team members
     */
    ProjectDetailResponse getProjectDetail(Integer id);
    
    /**
     * Get projects where the current user is a team member with filtering
     * @param filterRequest The filter request containing search, filter and pagination parameters
     * @return A paged response of projects
     */
    PagedResponse<ProjectResponse> getMyProjects(ProjectFilterRequest filterRequest);
    
    /**
     * Get projects for dropdown selection
     * @param search Optional search term
     * @param page Page number
     * @param size Page size
     * @return A paged response of project dropdown items
     */
    PagedResponse<ProjectDropdownResponse> getProjectsForDropdown(String search, Integer page, Integer size);
    
    /**
     * Get projects where the current user is a team member for dropdown selection
     * @param search Optional search term
     * @param page Page number
     * @param size Page size
     * @return A paged response of project dropdown items
     */
    PagedResponse<ProjectDropdownResponse> getMyProjectsForDropdown(String search, Integer page, Integer size);
} 