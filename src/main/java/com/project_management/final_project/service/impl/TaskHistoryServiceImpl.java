package com.project_management.final_project.service.impl;

import com.project_management.final_project.config.SecurityUtil;
import com.project_management.final_project.dto.response.TaskHistoryResponse;
import com.project_management.final_project.entities.Task;
import com.project_management.final_project.entities.TaskHistory;
import com.project_management.final_project.entities.User;
import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import com.project_management.final_project.repository.TaskHistoryRepository;
import com.project_management.final_project.repository.TaskRepository;
import com.project_management.final_project.repository.UserRepository;
import com.project_management.final_project.service.TaskHistoryService;
import com.project_management.final_project.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskHistoryServiceImpl implements TaskHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(TaskHistoryServiceImpl.class);
    private final TaskHistoryRepository taskHistoryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public TaskHistoryServiceImpl(
            TaskHistoryRepository taskHistoryRepository,
            UserRepository userRepository,
            TaskRepository taskRepository,
            SecurityUtil securityUtil,
            UserService userService) {
        this.taskHistoryRepository = taskHistoryRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @Override
    @Transactional
    public TaskHistory createTaskStatusHistory(Task task, Task.Status oldStatus, Task.Status newStatus) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // Get current user entity
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            
            // Create new task history record
            TaskHistory taskHistory = TaskHistory.builder()
                    .task(task)
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .changedBy(currentUser)
                    .build();
            
            // Save task history
            TaskHistory savedTaskHistory = taskHistoryRepository.save(taskHistory);
            
            logger.info("Created task history record ID {} for task ID {}: status changed from {} to {} by user ID {}", 
                    savedTaskHistory.getId(), task.getId(), oldStatus, newStatus, currentUserId);
            
            return savedTaskHistory;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating task history record for task ID {}: {}", task.getId(), e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to create task history record");
        }
    }
    
    @Override
    @Transactional
    public int createTaskStatusHistoryForTeamMemberRemoval(List<Task> tasks, Task.Status newStatus, User systemUser) {
        try {
            if (tasks == null || tasks.isEmpty()) {
                return 0;
            }
            
            List<TaskHistory> historyRecords = new ArrayList<>();
            
            for (Task task : tasks) {
                // Create new task history record with the task's current status as oldStatus
                TaskHistory taskHistory = TaskHistory.builder()
                        .task(task)
                        .oldStatus(task.getStatus())  // Use the task's current status
                        .newStatus(newStatus)
                        .changedBy(systemUser)
                        .build();
                
                historyRecords.add(taskHistory);
            }
            
            // Save all task history records
            List<TaskHistory> savedHistoryRecords = taskHistoryRepository.saveAll(historyRecords);
            
            logger.info("Created {} task history records for team member removal", savedHistoryRecords.size());
            
            return savedHistoryRecords.size();
        } catch (Exception e) {
            logger.error("Error creating task history records for team member removal: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to create task history records");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TaskHistoryResponse> getTaskHistory(Integer taskId) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            // Check if task exists
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Task not found"));
            
            // Check if the user is a developer
            boolean isDeveloper = userService.hasRole(currentUserId, "DEVELOPER");
            
            // If user is a developer, verify they are assigned to this task
            if (isDeveloper) {
                if (task.getAssignee() == null || !task.getAssignee().getId().equals(currentUserId)) {
                    logger.warn("Developer ID {} attempted to access history of task ID {} which is not assigned to them", 
                            currentUserId, taskId);
                    throw new AppException(ErrorCode.UNAUTHORIZED, "You can only view history of tasks assigned to you");
                }
            }
            
            // Get task history records
            List<TaskHistory> historyRecords = taskHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId);
            
            logger.info("Retrieved {} task history records for task ID {}", historyRecords.size(), taskId);
            
            // Transform to response DTOs with formatted messages
            return historyRecords.stream()
                    .map(this::createHistoryResponseWithMessage)
                    .collect(Collectors.toList());
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving task history for task ID {}: {}", taskId, e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve task history");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TaskHistoryResponse> getRecentTaskHistory(int limit) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            logger.info("Retrieving {} most recent task history records", limit);
            
            // Get most recent task history records
            PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "changedAt"));
            List<TaskHistory> historyRecords = taskHistoryRepository.findMostRecentTaskHistory(pageRequest);
            
            logger.info("Retrieved {} recent task history records", historyRecords.size());
            
            // Transform to response DTOs with formatted messages
            return historyRecords.stream()
                    .map(this::createHistoryResponseWithMessage)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving recent task history: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve recent task history");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TaskHistoryResponse> getRecentTaskHistoryForMyProjects(int limit) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            logger.info("Retrieving {} most recent task history records for projects created by user ID {}", 
                    limit, currentUserId);
            
            // Get most recent task history records for projects created by the current user
            PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "changedAt"));
            List<TaskHistory> historyRecords = taskHistoryRepository.findMostRecentTaskHistoryForProjectsCreatedByUser(
                    currentUserId, pageRequest);
            
            logger.info("Retrieved {} recent task history records for projects created by user ID {}", 
                    historyRecords.size(), currentUserId);
            
            // Transform to response DTOs with formatted messages
            return historyRecords.stream()
                    .map(this::createHistoryResponseWithMessage)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving recent task history for projects created by user ID {}: {}", 
                    securityUtil.getCurrentUserId(), e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve recent task history");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TaskHistoryResponse> getMyRecentTaskHistory(int limit) {
        try {
            // Get current user ID
            Integer currentUserId = securityUtil.getCurrentUserId();
            
            logger.info("Retrieving {} most recent task history records for user ID {}", limit, currentUserId);
            
            // Get most recent task history records for the current user
            PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "changedAt"));
            List<TaskHistory> historyRecords = taskHistoryRepository.findMostRecentTaskHistoryForUser(currentUserId, pageRequest);
            
            logger.info("Retrieved {} recent task history records for user ID {}", historyRecords.size(), currentUserId);
            
            // Transform to response DTOs with formatted messages
            return historyRecords.stream()
                    .map(this::createHistoryResponseWithMessage)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving recent task history for user ID {}: {}", securityUtil.getCurrentUserId(), e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to retrieve recent task history");
        }
    }
    
    /**
     * Create a TaskHistoryResponse with a formatted message based on the task history record
     *
     * @param history The task history record
     * @return TaskHistoryResponse with formatted message
     */
    private TaskHistoryResponse createHistoryResponseWithMessage(TaskHistory history) {
        String message;
        String userName = history.getChangedBy().getName();
        String taskTitle = history.getTask().getTitle();
        String formattedTime = history.getChangedAt().format(DATE_FORMATTER);
        
        // Special message for completion
        if (history.getNewStatus() == Task.Status.COMPLETED) {
            message = String.format("%s has completed %s at %s", 
                    userName, taskTitle, formattedTime);
        } else {
            // General status update message
            message = String.format("%s has updated %s's status from %s to %s at %s", 
                    userName, taskTitle, history.getOldStatus(), history.getNewStatus(), formattedTime);
        }
        
        return TaskHistoryResponse.builder()
                .id(history.getId())
                .message(message)
                .changedAt(history.getChangedAt())
                .build();
    }
} 