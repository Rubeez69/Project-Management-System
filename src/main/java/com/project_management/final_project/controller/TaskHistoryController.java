package com.project_management.final_project.controller;

import com.project_management.final_project.dto.response.ApiResponse;
import com.project_management.final_project.dto.response.TaskHistoryResponse;
import com.project_management.final_project.service.TaskHistoryService;
import com.project_management.final_project.util.ApiResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/task-history")
public class TaskHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(TaskHistoryController.class);
    private final TaskHistoryService taskHistoryService;
    private static final int MAX_RECENT_HISTORY_RECORDS = 7;

    @Autowired
    public TaskHistoryController(TaskHistoryService taskHistoryService) {
        this.taskHistoryService = taskHistoryService;
    }

    /**
     * Get the most recent task history records for tasks in projects created by the current user
     * Limited to 7 records maximum
     * Only accessible by project managers
     *
     * @return List of recent task history entries with formatted messages
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TASK_HISTORY_VIEW')")
    public ApiResponse<List<TaskHistoryResponse>> getRecentTaskHistory() {
        logger.info("Project Manager getting most recent task history records for their projects (max {})", MAX_RECENT_HISTORY_RECORDS);
        
        List<TaskHistoryResponse> taskHistory = taskHistoryService.getRecentTaskHistoryForMyProjects(MAX_RECENT_HISTORY_RECORDS);
        
        return ApiResponseUtil.success(taskHistory);
    }
    
    /**
     * Get the most recent task history records for tasks assigned to the current user
     * Limited to 7 records maximum
     * Accessible by developers
     *
     * @return List of recent task history entries with formatted messages
     */
    @GetMapping("/my-recent")
    @PreAuthorize("hasRole('DEVELOPER') and hasAuthority('TASK_HISTORY_VIEW')")
    public ApiResponse<List<TaskHistoryResponse>> getMyRecentTaskHistory() {
        logger.info("Developer getting most recent task history records (max {})", MAX_RECENT_HISTORY_RECORDS);
        
        List<TaskHistoryResponse> taskHistory = taskHistoryService.getMyRecentTaskHistory(MAX_RECENT_HISTORY_RECORDS);
        
        return ApiResponseUtil.success(taskHistory);
    }

    /**
     * Get the history of status changes for a specific task
     * Only accessible by project managers
     *
     * @param taskId The ID of the task
     * @return List of task history entries with formatted messages
     */
    @GetMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TASK_HISTORY_VIEW')")
    public ApiResponse<List<TaskHistoryResponse>> getTaskHistory(@PathVariable Integer taskId) {
        logger.info("Getting task history for task ID: {}", taskId);
        
        List<TaskHistoryResponse> taskHistory = taskHistoryService.getTaskHistory(taskId);
        
        return ApiResponseUtil.success(taskHistory);
    }
    
    /**
     * Get the history of status changes for a specific task
     * Accessible by developers for tasks assigned to them
     *
     * @param taskId The ID of the task
     * @return List of task history entries with formatted messages
     */
    @GetMapping("/my-tasks/{taskId}")
    @PreAuthorize("hasRole('DEVELOPER') and hasAuthority('TASK_HISTORY_VIEW')")
    public ApiResponse<List<TaskHistoryResponse>> getMyTaskHistory(@PathVariable Integer taskId) {
        logger.info("Developer getting task history for task ID: {}", taskId);
        
        // The service will verify that the task is assigned to the current user
        List<TaskHistoryResponse> taskHistory = taskHistoryService.getTaskHistory(taskId);
        
        return ApiResponseUtil.success(taskHistory);
    }
} 