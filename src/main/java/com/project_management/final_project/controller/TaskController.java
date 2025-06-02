package com.project_management.final_project.controller;

import com.project_management.final_project.config.SecurityService;
import com.project_management.final_project.dto.request.AssignTaskRequest;
import com.project_management.final_project.dto.request.CreateTaskRequest;
import com.project_management.final_project.dto.request.UnassignedTaskFilterRequest;
import com.project_management.final_project.dto.response.ApiResponse;
import com.project_management.final_project.dto.response.AssignedTaskResponse;
import com.project_management.final_project.dto.response.PageResponse;
import com.project_management.final_project.dto.response.TaskResponse;
import com.project_management.final_project.dto.response.UnassignedTaskResponse;
import com.project_management.final_project.entities.Task;
import com.project_management.final_project.service.TaskService;
import com.project_management.final_project.util.ApiResponseUtil;
import com.project_management.final_project.util.PaginationUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Create a new task in a project
     *
     * @param projectId The ID of the project
     * @param request   The task creation request
     * @return The created task
     */
    @PostMapping("/projects/{projectId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TASK_CREATE')")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable Integer projectId,
            @Valid @RequestBody CreateTaskRequest request) {
        
        logger.info("Creating new task in project ID: {}", projectId);
        
        TaskResponse taskResponse = taskService.createTask(projectId, request);
        
        ApiResponse<TaskResponse> response = ApiResponse.<TaskResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Task created successfully")
                .result(taskResponse)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get unassigned tasks for a specific project with filtering
     *
     * @param projectId The project ID from the path
     * @param search    The optional search term for title or description
     * @param priority  The optional priority filter
     * @param pageable  The pagination parameters
     * @return Page of unassigned tasks
     */
    @GetMapping("/projects/{projectId}/unassigned")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TASK_VIEW')")
    public ApiResponse<PageResponse<UnassignedTaskResponse>> getUnassignedTasks(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Task.Priority priority,
            @PageableDefault(size = 5, sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable) {
        
        logger.info("Getting unassigned tasks for project ID: {}, Search: {}, Priority: {}", 
                projectId, search, priority);
        
        UnassignedTaskFilterRequest filterRequest = UnassignedTaskFilterRequest.builder()
                .search(search)
                .priority(priority)
                .build();
        
        Page<UnassignedTaskResponse> tasks = taskService.getUnassignedTasks(projectId, filterRequest, pageable);
        
        return ApiResponseUtil.success(PaginationUtil.createPageResponse(tasks));
    }
    
    /**
     * Assign a task to a team member
     *
     * @param taskId  The ID of the task to assign
     * @param request The assignment request containing the user ID
     * @return The updated task
     */
    @PutMapping("/{taskId}/assign")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TASK_UPDATE')")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Integer taskId,
            @Valid @RequestBody AssignTaskRequest request) {
        
        logger.info("Assigning task ID {} to user ID {}", taskId, request.getUserId());
        
        TaskResponse taskResponse = taskService.assignTask(taskId, request);
        
        ApiResponse<TaskResponse> response = ApiResponse.<TaskResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Task assigned successfully")
                .result(taskResponse)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all tasks assigned to the current user for a specific project
     *
     * @param projectId The ID of the project
     * @return List of tasks assigned to the current user
     */
    @GetMapping("/projects/{projectId}/my-tasks")
    @PreAuthorize("hasRole('DEVELOPER') and hasAuthority('TASK_VIEW')")
    public ApiResponse<List<AssignedTaskResponse>> getMyTasksInProject(@PathVariable Integer projectId) {
        
        logger.info("Getting all tasks assigned to current user in project ID: {}", projectId);
        
        List<AssignedTaskResponse> tasks = taskService.getMyTasksInProject(projectId);
        
        return ApiResponseUtil.success(tasks);
    }
    
    /**
     * Get all tasks assigned to a specific team member in a project
     *
     * @param projectId The ID of the project
     * @param userId The ID of the user whose tasks to retrieve
     * @return List of tasks assigned to the specified user
     */
    @GetMapping("/projects/{projectId}/members/{userId}/tasks")
    @PreAuthorize("hasAuthority('TASK_VIEW') and @securityService.canViewMemberTasks(#projectId, #userId)")
    public ApiResponse<List<AssignedTaskResponse>> getMemberTasksInProject(
            @PathVariable Integer projectId,
            @PathVariable Integer userId) {
        
        logger.info("Getting all tasks assigned to user ID {} in project ID {}", userId, projectId);
        
        List<AssignedTaskResponse> tasks = taskService.getMemberTasksInProject(projectId, userId);
        
        return ApiResponseUtil.success(tasks);
    }
} 