package com.project_management.final_project.service.impl;

import com.project_management.final_project.config.SecurityUtil;
import com.project_management.final_project.dto.request.CreateProjectRequest;
import com.project_management.final_project.dto.request.ProjectFilterRequest;
import com.project_management.final_project.dto.request.UpdateProjectRequest;
import com.project_management.final_project.dto.response.PagedResponse;
import com.project_management.final_project.dto.response.ProjectDetailResponse;
import com.project_management.final_project.dto.response.ProjectDropdownResponse;
import com.project_management.final_project.dto.response.ProjectResponse;
import com.project_management.final_project.dto.response.TeamMemberResponse;
import com.project_management.final_project.entities.Project;
import com.project_management.final_project.entities.TeamMember;
import com.project_management.final_project.entities.User;
import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import com.project_management.final_project.repository.ProjectRepository;
import com.project_management.final_project.repository.TeamMemberRepository;
import com.project_management.final_project.repository.UserRepository;
import com.project_management.final_project.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SecurityUtil securityUtil;

    @Autowired
    public ProjectServiceImpl(
            ProjectRepository projectRepository, 
            UserRepository userRepository, 
            TeamMemberRepository teamMemberRepository,
            SecurityUtil securityUtil) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.securityUtil = securityUtil;
    }

    @Override
    public PagedResponse<ProjectResponse> getAllProjects(ProjectFilterRequest filterRequest) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // Create pageable with sorting
            Sort.Direction direction = Sort.Direction.fromString(
                    filterRequest.getSortDirection().equalsIgnoreCase("asc") ? "asc" : "desc"
            );
            
            Pageable pageable = PageRequest.of(
                    filterRequest.getPage(),
                    filterRequest.getSize(),
                    Sort.by(direction, filterRequest.getSortBy())
            );
            
            // Query projects with filters
            Page<Project> projectsPage = projectRepository.findProjectsByFilters(
                    filterRequest.getName(),
                    filterRequest.getStatus(),
                    currentUserId,
                    pageable
            );
            
            // Map to response DTOs
            Page<ProjectResponse> projectResponsePage = projectsPage.map(ProjectResponse::fromEntity);
            
            logger.info("Retrieved {} projects for user ID {}", projectsPage.getTotalElements(), currentUserId);
            
            return PagedResponse.fromPage(projectResponsePage);
        } catch (Exception e) {
            logger.error("Error retrieving projects: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // Get user entity
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            
            // Create new project
            Project project = Project.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .status(Project.Status.ACTIVE)
                    .createdBy(currentUser)
                    .teamMembers(new ArrayList<>())
                    .tasks(new ArrayList<>())
                    .build();
            
            // Save project
            Project savedProject = projectRepository.save(project);
            
            logger.info("Created new project with ID {} by user ID {}", savedProject.getId(), currentUserId);
            
            return ProjectResponse.fromEntity(savedProject);
        } catch (Exception e) {
            logger.error("Error creating project: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public ProjectResponse updateProject(Integer id, UpdateProjectRequest request) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // Find the project by ID
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found with ID: " + id));
            
            // Check if the current user is the creator of the project
            if (!project.getCreatedBy().getId().equals(currentUserId)) {
                logger.warn("User ID {} attempted to update project ID {} created by user ID {}", 
                        currentUserId, id, project.getCreatedBy().getId());
                throw new AppException(ErrorCode.UNAUTHORIZED, "You are not authorized to update this project");
            }
            
            // Update only the fields that are not null
            if (request.getName() != null) {
                project.setName(request.getName());
            }
            
            if (request.getDescription() != null) {
                project.setDescription(request.getDescription());
            }
            
            if (request.getStartDate() != null) {
                project.setStartDate(request.getStartDate());
            }
            
            if (request.getEndDate() != null) {
                project.setEndDate(request.getEndDate());
            }
            
            if (request.getStatus() != null) {
                project.setStatus(request.getStatus());
            }
            
            // Save updated project
            Project updatedProject = projectRepository.save(project);
            
            logger.info("Updated project ID {} by user ID {}", id, currentUserId);
            
            return ProjectResponse.fromEntity(updatedProject);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating project ID {}: {}", id, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to update project");
        }
    }
    
    @Override
    public ProjectResponse getProjectById(Integer id) {
        try {
            // Find the project by ID
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found with ID: " + id));
            
            logger.info("Retrieved project with ID {}", id);
            
            return ProjectResponse.fromEntity(project);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving project ID {}: {}", id, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve project");
        }
    }
    
    @Override
    public ProjectDetailResponse getProjectDetail(Integer id) {
        try {
            // Find the project by ID
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found with ID: " + id));
            
            // Get top 4 recent team members
            List<TeamMember> recentTeamMembers = teamMemberRepository.findTopNByProjectIdOrderByAddedAtDesc(
                    id, PageRequest.of(0, 4));
            
            // Map team members to response DTOs
            List<TeamMemberResponse> teamMemberResponses = recentTeamMembers.stream()
                    .map(TeamMemberResponse::fromEntity)
                    .collect(Collectors.toList());
            
            logger.info("Retrieved project detail with ID {} and {} team members", id, teamMemberResponses.size());
            
            return ProjectDetailResponse.fromEntity(project, teamMemberResponses);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving project detail ID {}: {}", id, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve project detail");
        }
    }
    
    @Override
    public PagedResponse<ProjectDropdownResponse> getProjectsForDropdown(String search, Integer page, Integer size) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // Create pageable with sorting by name
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
            
            // Query projects with name filter and created by current user
            Page<Project> projectsPage = projectRepository.findProjectsByFilters(
                    search, // Only filter by name
                    null,   // No status filter
                    currentUserId, // Only projects created by current user
                    pageable
            );
            
            // Map to dropdown response DTOs
            Page<ProjectDropdownResponse> projectDropdownPage = projectsPage.map(ProjectDropdownResponse::fromEntity);
            
            logger.info("Retrieved {} projects for dropdown for user ID {}", projectsPage.getTotalElements(), currentUserId);
            
            return PagedResponse.fromPage(projectDropdownPage);
        } catch (Exception e) {
            logger.error("Error retrieving projects for dropdown: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve projects for dropdown");
        }
    }

    @Override
    public PagedResponse<ProjectResponse> getMyProjects(ProjectFilterRequest filterRequest) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            logger.info("Getting projects for team member with user ID: {}", currentUserId);
            
            // Create pageable with sorting
            Sort.Direction direction = Sort.Direction.fromString(
                    filterRequest.getSortDirection().equalsIgnoreCase("asc") ? "asc" : "desc"
            );
            
            Pageable pageable = PageRequest.of(
                    filterRequest.getPage(),
                    filterRequest.getSize(),
                    Sort.by(direction, filterRequest.getSortBy())
            );
            
            // Query projects where the user is a team member with filters
            Page<Project> projectsPage = projectRepository.findProjectsByTeamMemberUserIdWithFilters(
                    currentUserId,
                    filterRequest.getName(),
                    filterRequest.getStatus(),
                    pageable
            );
            
            // Map to response DTOs
            Page<ProjectResponse> projectResponsePage = projectsPage.map(ProjectResponse::fromEntity);
            
            logger.info("Retrieved {} projects where user ID {} is a team member", 
                    projectsPage.getTotalElements(), currentUserId);
            
            return PagedResponse.fromPage(projectResponsePage);
        } catch (Exception e) {
            logger.error("Error retrieving projects for team member with user ID {}: {}", 
                    securityUtil.getCurrentUserId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public PagedResponse<ProjectDropdownResponse> getMyProjectsForDropdown(String search, Integer page, Integer size) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // Create pageable with sorting by name
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
            
            // Query projects where the user is a team member with name filter
            Page<Project> projectsPage = projectRepository.findProjectsByTeamMemberUserIdWithFilters(
                    currentUserId,
                    search, // Only filter by name
                    null,   // No status filter
                    pageable
            );
            
            // Map to dropdown response DTOs
            Page<ProjectDropdownResponse> projectDropdownPage = projectsPage.map(ProjectDropdownResponse::fromEntity);
            
            logger.info("Retrieved {} projects for dropdown where user ID {} is a team member", 
                    projectsPage.getTotalElements(), currentUserId);
            
            return PagedResponse.fromPage(projectDropdownPage);
        } catch (Exception e) {
            logger.error("Error retrieving projects for dropdown where user ID {} is a team member: {}", 
                    securityUtil.getCurrentUserId(), e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve projects for dropdown");
        }
    }
} 