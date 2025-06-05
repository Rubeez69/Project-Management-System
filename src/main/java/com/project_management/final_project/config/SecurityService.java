package com.project_management.final_project.config;

import com.project_management.final_project.entities.Project;
import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import com.project_management.final_project.repository.ProjectRepository;
import com.project_management.final_project.repository.TaskRepository;
import com.project_management.final_project.repository.TeamMemberRepository;
import com.project_management.final_project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("securityService")
public class SecurityService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);
    private final SecurityUtil securityUtil;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;
    
    @Autowired
    public SecurityService(
            SecurityUtil securityUtil,
            TeamMemberRepository teamMemberRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            UserService userService) {
        this.securityUtil = securityUtil;
        this.teamMemberRepository = teamMemberRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
    }
    
    /**
     * Check if the current user can view tasks of another team member
     * 
     * @param projectId The project ID
     * @param userId The user ID whose tasks are being viewed
     * @return true if the current user can view the tasks, false otherwise
     * @throws AppException if the user is not authorized to view the tasks
     */
    public boolean canViewMemberTasks(Integer projectId, Integer userId) {
        Integer currentUserId = securityUtil.getCurrentUserId();
        
        logger.info("Checking authorization: current user ID {} requesting to view tasks of user ID {} in project ID {}", 
                currentUserId, userId, projectId);
        
        // Step 1: Check if the project exists
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            logger.warn("Project ID {} not found", projectId);
            throw new AppException(ErrorCode.NOT_FOUND, "Project not found");
        }
        
        Project project = projectOpt.get();
        
        // Step 2: Check if the user is trying to view their own tasks through this endpoint
        if (currentUserId.equals(userId)) {
            logger.warn("User ID {} attempted to view their own tasks through the member tasks endpoint", currentUserId);
            throw new AppException(ErrorCode.UNAUTHORIZED, 
                    "Developers should use the /my-tasks endpoint to view their own tasks");
        }
        
        // Step 3: Check if the current user has access to this project
        boolean isProjectCreator = project.getCreatedBy() != null && 
                                   project.getCreatedBy().getId().equals(currentUserId);
        boolean isCurrentUserInProject = teamMemberRepository.existsByUserIdAndProjectId(currentUserId, projectId);
        boolean isProjectManager = userService.hasRole(currentUserId, "PROJECT_MANAGER");
        
        logger.info("Current user access check - Is creator: {}, Is team member: {}, Is PM: {}", 
                isProjectCreator, isCurrentUserInProject, isProjectManager);
        
        // For project managers, they must be either the creator or a team member
        // For developers, they must be a team member
        boolean hasProjectAccess = (isProjectManager && isProjectCreator) || isCurrentUserInProject;
        
        if (!hasProjectAccess) {
            logger.warn("User ID {} does not have access to project ID {}", currentUserId, projectId);
            throw new AppException(ErrorCode.UNAUTHORIZED, 
                    "You don't have access to this project");
        }
        
        // Step 4: Check if the target user is a member of the project
        boolean isTargetInProject = teamMemberRepository.existsByUserIdAndProjectId(userId, projectId);
        logger.info("Is target user ID {} a member of project ID {}: {}", userId, projectId, isTargetInProject);
        
        if (!isTargetInProject) {
            logger.warn("User ID {} is not a member of project ID {}", userId, projectId);
            throw new AppException(ErrorCode.UNAUTHORIZED, 
                    "The specified user is not a member of this project");
        }
        
        // Step 5: Final authorization check
        // Project managers can view tasks of any team member in their project
        // Developers can view tasks of other team members (not themselves) in the same project
        boolean canView = isProjectManager || !currentUserId.equals(userId);
        
        logger.info("Authorization result: User ID {} {} view tasks of user ID {} in project ID {}", 
                currentUserId, canView ? "can" : "cannot", userId, projectId);
        
        return canView;
    }
    
    /**
     * Check if the current user can update the status of a task
     * 
     * @param taskId The task ID
     * @param projectId The project ID
     * @return true if the current user can update the task status, false otherwise
     */
    public boolean canUpdateTaskStatus(Integer taskId, Integer projectId) {
        Integer currentUserId = securityUtil.getCurrentUserId();
        
        logger.info("Checking authorization: current user ID {} requesting to update status of task ID {} in project ID {}", 
                currentUserId, taskId, projectId);
        
        // Check if the user is a project manager
        if (userService.hasRole(currentUserId, "PROJECT_MANAGER")) {
            // Project managers can update if they are the project creator
            boolean isAuthorized = taskRepository.existsByIdAndProjectIdAndProject_CreatedBy_Id(taskId, projectId, currentUserId);
            logger.info("Project manager authorization check for task update: {}", isAuthorized);
            return isAuthorized;
        }
        
        // Developers can update if they are the task assignee
        if (userService.hasRole(currentUserId, "DEVELOPER")) {
            boolean isAuthorized = taskRepository.existsByIdAndProjectIdAndAssigneeId(taskId, projectId, currentUserId);
            logger.info("Developer authorization check for task update: {}", isAuthorized);
            return isAuthorized;
        }
        
        logger.warn("User ID {} with unknown role attempted to update task ID {}", currentUserId, taskId);
        return false;
    }
} 