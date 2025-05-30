package com.project_management.final_project.service;

import com.project_management.final_project.dto.request.AddTeamMemberRequest;
import com.project_management.final_project.dto.request.CreateProjectRequest;
import com.project_management.final_project.dto.request.ProjectFilterRequest;
import com.project_management.final_project.dto.request.UpdateProjectRequest;
import com.project_management.final_project.dto.response.PagedResponse;
import com.project_management.final_project.dto.response.ProjectDetailResponse;
import com.project_management.final_project.dto.response.ProjectResponse;

import java.util.List;

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
     * Add team members to a project
     * @param projectId The ID of the project to add members to
     * @param requests List of team member requests containing user ID and specialization ID
     * @return Number of team members added
     */
    int addTeamMembers(Integer projectId, List<AddTeamMemberRequest> requests);
} 