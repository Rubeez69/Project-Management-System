package com.project_management.final_project.service.impl;

import com.project_management.final_project.config.SecurityUtil;
import com.project_management.final_project.dto.request.AssignTaskRequest;
import com.project_management.final_project.dto.request.CreateTaskRequest;
import com.project_management.final_project.dto.request.UnassignedTaskFilterRequest;
import com.project_management.final_project.dto.response.AssignedTaskResponse;
import com.project_management.final_project.dto.response.TaskResponse;
import com.project_management.final_project.dto.response.UnassignedTaskResponse;
import com.project_management.final_project.entities.Project;
import com.project_management.final_project.entities.Task;
import com.project_management.final_project.entities.User;
import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import com.project_management.final_project.repository.ProjectRepository;
import com.project_management.final_project.repository.TaskRepository;
import com.project_management.final_project.repository.TeamMemberRepository;
import com.project_management.final_project.repository.UserRepository;
import com.project_management.final_project.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SecurityUtil securityUtil;

    @Autowired
    public TaskServiceImpl(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            TeamMemberRepository teamMemberRepository,
            SecurityUtil securityUtil) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.securityUtil = securityUtil;
    }

    @Override
    @Transactional
    public TaskResponse createTask(Integer projectId, CreateTaskRequest request) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // Find the project by ID
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found with ID: " + projectId));
            
            // Check if the current user is the creator of the project
            if (!project.getCreatedBy().getId().equals(currentUserId)) {
                logger.warn("User ID {} attempted to create task in project ID {} created by user ID {}", 
                        currentUserId, projectId, project.getCreatedBy().getId());
                throw new AppException(ErrorCode.UNAUTHORIZED, "You are not authorized to create tasks in this project");
            }
            
            // Get current user entity
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            
            // Initialize task status as UNASSIGNED
            Task.Status taskStatus = Task.Status.UNASSIGNED;
            
            // Initialize assignee as null
            User assignee = null;
            
            // Check if assignee is provided
            if (request.getAssigneeId() != null) {
                // Check if user exists
                assignee = userRepository.findById(request.getAssigneeId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, 
                                "User not found with ID: " + request.getAssigneeId()));
                
                // Check if the assignee is a member of the project
                boolean isMember = teamMemberRepository.existsByUserIdAndProjectId(
                        request.getAssigneeId(), projectId);
                
                if (!isMember) {
                    logger.warn("User ID {} attempted to assign task to non-member user ID {} in project ID {}", 
                            currentUserId, request.getAssigneeId(), projectId);
                    throw new AppException(ErrorCode.UNAUTHORIZED, 
                            "Cannot assign task to a user who is not a member of the project");
                }
                
                // Set task status to TODO since it's assigned
                taskStatus = Task.Status.TODO;
            }
            
            // Set default priority if not provided
            Task.Priority priority = request.getPriority() != null ? 
                    request.getPriority() : Task.Priority.MEDIUM;
            
            // Create new task
            Task task = Task.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .priority(priority)
                    .status(taskStatus)
                    .startDate(request.getStartDate())
                    .dueDate(request.getDueDate())
                    .project(project)
                    .assignee(assignee)
                    .createdBy(currentUser)
                    .build();
            
            // Save task
            Task savedTask = taskRepository.save(task);
            
            logger.info("Created new task with ID {} in project ID {} by user ID {}", 
                    savedTask.getId(), projectId, currentUserId);
            
            return TaskResponse.fromEntity(savedTask);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating task in project ID {}: {}", projectId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to create task");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UnassignedTaskResponse> getUnassignedTasks(Integer projectId, UnassignedTaskFilterRequest filterRequest, Pageable pageable) {
        try {
            logger.info("Getting unassigned tasks for project ID: {}, with filters: {}", projectId, filterRequest);
            
            // Fetch unassigned tasks with filters
            Page<Task> tasks = taskRepository.findUnassignedTasksWithFilters(
                    projectId,
                    filterRequest.getSearch(),
                    filterRequest.getPriority(),
                    pageable
            );
            
            // Map to response DTOs
            return tasks.map(UnassignedTaskResponse::fromEntity);
            
        } catch (Exception e) {
            logger.error("Error retrieving unassigned tasks for project ID {}: {}", projectId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve unassigned tasks");
        }
    }
    
    @Override
    @Transactional
    public TaskResponse assignTask(Integer taskId, AssignTaskRequest request) {
        try {
            logger.info("Assigning task ID {} to user ID {}", taskId, request.getUserId());
            
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // Find the task by ID
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Task not found with ID: " + taskId));
            
            // Check if the task already has an assignee
            if (task.getAssignee() != null) {
                logger.warn("Task ID {} already has an assignee (User ID: {})", taskId, task.getAssignee().getId());
                throw new AppException(ErrorCode.INVALID_KEY, "This task already has an assignee");
            }
            
            // Get the project associated with the task
            Project project = task.getProject();
            if (project == null) {
                logger.error("Task ID {} has no associated project", taskId);
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Task has no associated project");
            }
            
            // Check if the current user is the creator of the project
            if (!project.getCreatedBy().getId().equals(currentUserId)) {
                logger.warn("User ID {} attempted to assign task ID {} in project ID {} created by user ID {}", 
                        currentUserId, taskId, project.getId(), project.getCreatedBy().getId());
                throw new AppException(ErrorCode.UNAUTHORIZED, "You are not authorized to assign tasks in this project");
            }
            
            // Find the user to assign the task to
            User assignee = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, 
                            "User not found with ID: " + request.getUserId()));
            
            // Check if the assignee is a member of the project
            boolean isMember = teamMemberRepository.existsByUserIdAndProjectId(
                    request.getUserId(), project.getId());
            
            if (!isMember) {
                logger.warn("User ID {} attempted to assign task ID {} to non-member user ID {} in project ID {}", 
                        currentUserId, taskId, request.getUserId(), project.getId());
                throw new AppException(ErrorCode.UNAUTHORIZED, 
                        "Cannot assign task to a user who is not a member of the project");
            }
            
            // Update the task
            task.setAssignee(assignee);
            task.setStatus(Task.Status.TODO);
            
            // Save the updated task
            Task updatedTask = taskRepository.save(task);
            
            logger.info("Successfully assigned task ID {} to user ID {}", taskId, request.getUserId());
            
            return TaskResponse.fromEntity(updatedTask);
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error assigning task ID {}: {}", taskId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to assign task");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignedTaskResponse> getMyTasksInProject(Integer projectId) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            logger.info("Getting all tasks assigned to user ID {} in project ID {}", currentUserId, projectId);
            
            // Check if the project exists
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found with ID: " + projectId));
            
            // Check if the current user is a member of the project
            boolean isMember = teamMemberRepository.existsByUserIdAndProjectId(currentUserId, projectId);
            
            if (!isMember) {
                logger.warn("User ID {} attempted to view tasks in project ID {} but is not a member", 
                        currentUserId, projectId);
                throw new AppException(ErrorCode.UNAUTHORIZED, 
                        "You are not authorized to view tasks in this project");
            }
            
            // Get all tasks assigned to the current user in the specified project
            List<Task> tasks = taskRepository.findAllTasksByProjectIdAndAssigneeId(
                    projectId, 
                    currentUserId
            );
            
            // Map to response DTOs
            List<AssignedTaskResponse> taskResponses = tasks.stream()
                    .map(AssignedTaskResponse::fromEntity)
                    .collect(java.util.stream.Collectors.toList());
            
            logger.info("Retrieved {} tasks assigned to user ID {} in project ID {}", 
                    tasks.size(), currentUserId, projectId);
            
            return taskResponses;
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving tasks for user ID {} in project ID {}: {}", 
                    securityUtil.getCurrentUserId(), projectId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve assigned tasks");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignedTaskResponse> getMemberTasksInProject(Integer projectId, Integer userId) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            logger.info("Getting all tasks assigned to user ID {} in project ID {}, requested by user ID {}", 
                    userId, projectId, currentUserId);
            
            // Check if the project exists
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Project not found with ID: " + projectId));
            
            // Check if the target user exists
            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User not found with ID: " + userId));
            
            // Check if the target user is a member of the project
            boolean isTargetMember = teamMemberRepository.existsByUserIdAndProjectId(userId, projectId);
            
            if (!isTargetMember) {
                logger.warn("User ID {} is not a member of project ID {}", userId, projectId);
                throw new AppException(ErrorCode.UNAUTHORIZED, 
                        "The specified user is not a member of this project");
            }
            
            // Get all tasks assigned to the specified user in the project
            List<Task> tasks = taskRepository.findAllTasksByProjectIdAndAssigneeId(
                    projectId, 
                    userId
            );
            
            // Map to response DTOs
            List<AssignedTaskResponse> taskResponses = tasks.stream()
                    .map(AssignedTaskResponse::fromEntity)
                    .collect(java.util.stream.Collectors.toList());
            
            logger.info("Retrieved {} tasks assigned to user ID {} in project ID {}", 
                    tasks.size(), userId, projectId);
            
            return taskResponses;
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving tasks for user ID {} in project ID {}: {}", 
                    userId, projectId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve assigned tasks");
        }
    }
} 