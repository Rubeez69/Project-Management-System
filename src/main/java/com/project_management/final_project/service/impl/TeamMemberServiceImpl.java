package com.project_management.final_project.service.impl;

import com.project_management.final_project.config.SecurityUtil;
import com.project_management.final_project.dto.request.AddTeamMemberRequest;
import com.project_management.final_project.dto.request.TeamMemberFilterRequest;
import com.project_management.final_project.dto.response.TeamMemberResponse;
import com.project_management.final_project.dto.response.TeamMemberWithWorkloadResponse;
import com.project_management.final_project.entities.Project;
import com.project_management.final_project.entities.Specialization;
import com.project_management.final_project.entities.Task;
import com.project_management.final_project.entities.TeamMember;
import com.project_management.final_project.entities.User;
import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import com.project_management.final_project.repository.ProjectRepository;
import com.project_management.final_project.repository.SpecializationRepository;
import com.project_management.final_project.repository.TaskRepository;
import com.project_management.final_project.repository.TeamMemberRepository;
import com.project_management.final_project.repository.UserRepository;
import com.project_management.final_project.service.TaskHistoryService;
import com.project_management.final_project.service.TeamMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeamMemberServiceImpl implements TeamMemberService {

    private static final Logger logger = LoggerFactory.getLogger(TeamMemberServiceImpl.class);
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SpecializationRepository specializationRepository;
    private final TaskRepository taskRepository;
    private final TaskHistoryService taskHistoryService;
    private final SecurityUtil securityUtil;

    @Autowired
    public TeamMemberServiceImpl(
            ProjectRepository projectRepository, 
            UserRepository userRepository, 
            TeamMemberRepository teamMemberRepository,
            SpecializationRepository specializationRepository,
            TaskRepository taskRepository,
            TaskHistoryService taskHistoryService,
            SecurityUtil securityUtil) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.specializationRepository = specializationRepository;
        this.taskRepository = taskRepository;
        this.taskHistoryService = taskHistoryService;
        this.securityUtil = securityUtil;
    }

    @Override
    @Transactional
    public int addTeamMembers(Integer projectId, List<AddTeamMemberRequest> requests) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // Find the project by ID
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found with ID: " + projectId));
            
            // Check if the current user is the creator of the project
            if (!project.getCreatedBy().getId().equals(currentUserId)) {
                logger.warn("User ID {} attempted to add members to project ID {} created by user ID {}", 
                        currentUserId, projectId, project.getCreatedBy().getId());
                throw new AppException(ErrorCode.UNAUTHORIZED, "You are not authorized to add members to this project");
            }
            
            // First, check if any of the users are already members of the project
            for (AddTeamMemberRequest request : requests) {
                boolean isAlreadyMember = teamMemberRepository.existsByUserIdAndProjectId(
                        request.getUserId(), projectId);
                
                if (isAlreadyMember) {
                    User user = userRepository.findById(request.getUserId()).orElse(null);
                    String userName = user != null ? user.getName() : "Unknown";
                    logger.warn("User ID {} ({}) is already a member of project ID {}", 
                            request.getUserId(), userName, projectId);
                    throw new AppException(ErrorCode.DUPLICATE_ENTITY, 
                            "User " + userName + " is already a member of this project");
                }
            }
            
            List<TeamMember> teamMembersToAdd = new ArrayList<>();
            
            for (AddTeamMemberRequest request : requests) {
                // Check if user exists
                User user = userRepository.findById(request.getUserId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, 
                                "User not found with ID: " + request.getUserId()));
                
                // Check if specialization exists
                Specialization specialization = specializationRepository.findById(request.getSpecializationId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, 
                                "Specialization not found with ID: " + request.getSpecializationId()));
                
                // Create new team member
                TeamMember teamMember = TeamMember.builder()
                        .user(user)
                        .project(project)
                        .specialization(specialization)
                        .build();
                
                teamMembersToAdd.add(teamMember);
            }
            
            // Save all team members
            List<TeamMember> savedTeamMembers = teamMemberRepository.saveAll(teamMembersToAdd);
            
            logger.info("Added {} team members to project ID {}", savedTeamMembers.size(), projectId);
            
            return savedTeamMembers.size();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error adding team members to project ID {}: {}", projectId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to add team members to project");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<TeamMemberResponse> getProjectTeamMembers(Integer projectId, TeamMemberFilterRequest filterRequest, Pageable pageable) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // Find the project by ID
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found with ID: " + projectId));
            
            // Check if the current user is the creator of the project
            if (!project.getCreatedBy().getId().equals(currentUserId)) {
                logger.warn("User ID {} attempted to view members of project ID {} created by user ID {}", 
                        currentUserId, projectId, project.getCreatedBy().getId());
                throw new AppException(ErrorCode.UNAUTHORIZED, "You are not authorized to view members of this project");
            }
            
            // Get team members with filters
            Page<TeamMember> teamMembers = teamMemberRepository.findByProjectIdWithFilters(
                    projectId,
                    filterRequest.getSearch(),
                    filterRequest.getSpecializationId(),
                    pageable
            );
            
            // Map to response DTOs using the existing fromEntity method
            return teamMembers.map(TeamMemberResponse::fromEntity);
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving team members for project ID {}: {}", projectId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve team members");
        }
    }
    
    @Override
    @Transactional
    public boolean deleteTeamMember(Integer teamMemberId) {
        try {
            logger.info("Attempting to delete team member with ID: {}", teamMemberId);
            
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            logger.debug("Current user ID: {}", currentUserId);
            
            // Find the team member by ID
            TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
                    .orElseThrow(() -> {
                        logger.warn("Team member not found with ID: {}", teamMemberId);
                        return new AppException(ErrorCode.NOT_FOUND, "Team member not found with ID: " + teamMemberId);
                    });
            
            // Get the project associated with the team member
            Project project = teamMember.getProject();
            if (project == null) {
                logger.error("Team member ID {} has no associated project", teamMemberId);
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Team member has no associated project");
            }
            
            User projectCreator = project.getCreatedBy();
            if (projectCreator == null) {
                logger.error("Project ID {} has no creator", project.getId());
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Project has no creator");
            }
            
            // Check if the current user is the creator of the project
            if (!projectCreator.getId().equals(currentUserId)) {
                logger.warn("User ID {} attempted to delete team member ID {} from project ID {} created by user ID {}", 
                        currentUserId, teamMemberId, project.getId(), projectCreator.getId());
                throw new AppException(ErrorCode.UNAUTHORIZED, "You are not authorized to remove members from this project");
            }
            
            // Get the user ID of the team member
            Integer userId = teamMember.getUser().getId();
            Integer projectId = project.getId();
            
            // Find all tasks assigned to this team member in this project
            List<Task> assignedTasks = taskRepository.findByAssigneeIdAndProjectId(userId, projectId);
            
            // Get current user as the one who made the changes
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            
            // Create task history records for all tasks that will be unassigned
            if (!assignedTasks.isEmpty()) {
                // Create task history records
                int historyRecordsCreated = taskHistoryService.createTaskStatusHistoryForTeamMemberRemoval(
                        assignedTasks, 
                        Task.Status.UNASSIGNED, 
                        currentUser);
                
                logger.info("Created {} task history records for tasks being unassigned", historyRecordsCreated);
            }
            
            // Unassign all tasks assigned to this team member in this project
            int unassignedTasksCount = taskRepository.unassignTasksForTeamMember(userId, projectId);
            
            logger.info("Unassigned {} tasks from team member ID {} (user ID {}) in project ID {}", 
                    unassignedTasksCount, teamMemberId, userId, projectId);
            
            // Delete the team member
            teamMemberRepository.delete(teamMember);
            
            logger.info("Successfully deleted team member ID {} from project ID {}", teamMemberId, project.getId());
            
            return true;
            
        } catch (AppException e) {
            logger.error("Application exception while deleting team member ID {}: {}", teamMemberId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting team member ID {}: {}", teamMemberId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to delete team member: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamMemberWithWorkloadResponse> getProjectTeamMembersWithWorkload(
            Integer projectId, TeamMemberFilterRequest filterRequest, Pageable pageable) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // Find the project by ID
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found with ID: " + projectId));
            
            // Check if the current user is the creator of the project
            if (!project.getCreatedBy().getId().equals(currentUserId)) {
                logger.warn("User ID {} attempted to view members with workload of project ID {} created by user ID {}", 
                        currentUserId, projectId, project.getCreatedBy().getId());
                throw new AppException(ErrorCode.UNAUTHORIZED, "You are not authorized to view members of this project");
            }
            
            // Get team members with filters
            Page<TeamMember> teamMembers = teamMemberRepository.findByProjectIdWithFilters(
                    projectId,
                    filterRequest.getSearch(),
                    filterRequest.getSpecializationId(),
                    pageable
            );
            
            // Map to response DTOs with workload information
            List<TeamMemberWithWorkloadResponse> teamMemberResponses = teamMembers.getContent().stream()
                    .map(teamMember -> {
                        Integer userId = teamMember.getUser() != null ? teamMember.getUser().getId() : null;
                        int taskCount = 0;
                        if (userId != null) {
                            taskCount = taskRepository.countByAssigneeId(userId);
                        }
                        return TeamMemberWithWorkloadResponse.fromEntityWithWorkload(teamMember, taskCount);
                    })
                    .collect(Collectors.toList());
            
            return new PageImpl<>(teamMemberResponses, pageable, teamMembers.getTotalElements());
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving team members with workload for project ID {}: {}", 
                    projectId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve team members with workload");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeamMemberResponse> getMyTeamMembers(Integer projectId, TeamMemberFilterRequest filterRequest, Pageable pageable) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            logger.info("Getting team members for project ID {} excluding current user ID {}", projectId, currentUserId);
            
            // Find the project by ID
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found with ID: " + projectId));
            
            // Check if the current user is a member of the project
            boolean isCurrentUserInProject = teamMemberRepository.existsByUserIdAndProjectId(currentUserId, projectId);
            if (!isCurrentUserInProject) {
                logger.warn("User ID {} attempted to view team members of project ID {} but is not a member", 
                        currentUserId, projectId);
                throw new AppException(ErrorCode.UNAUTHORIZED, "You are not a member of this project");
            }
            
            // Get team members with filters, excluding the current user
            Page<TeamMember> teamMembers = teamMemberRepository.findByProjectIdExcludingUserWithFilters(
                    projectId,
                    currentUserId,
                    filterRequest.getSearch(),
                    filterRequest.getSpecializationId(),
                    pageable
            );
            
            // Map to response DTOs
            Page<TeamMemberResponse> teamMemberResponses = teamMembers.map(TeamMemberResponse::fromEntity);
            
            logger.info("Retrieved {} team members for project ID {} excluding user ID {}", 
                    teamMembers.getTotalElements(), projectId, currentUserId);
            
            return teamMemberResponses;
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving team members for project ID {}: {}", projectId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve team members");
        }
    }
} 