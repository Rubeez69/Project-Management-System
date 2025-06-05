package com.project_management.final_project.controller;

import com.project_management.final_project.config.SecurityService;
import com.project_management.final_project.dto.request.AssignTaskRequest;
import com.project_management.final_project.dto.request.CreateTaskRequest;
import com.project_management.final_project.dto.request.ProjectTaskFilterRequest;
import com.project_management.final_project.dto.request.UnassignedTaskFilterRequest;
import com.project_management.final_project.dto.request.UpdateTaskRequest;
import com.project_management.final_project.dto.request.UpdateTaskStatusRequest;
import com.project_management.final_project.dto.response.ApiResponse;
import com.project_management.final_project.dto.response.AssignedTaskResponse;
import com.project_management.final_project.dto.response.PagedResponse;
import com.project_management.final_project.dto.response.ProjectTaskResponse;
import com.project_management.final_project.dto.response.TaskDetailResponse;
import com.project_management.final_project.dto.response.TaskResponse;
import com.project_management.final_project.dto.response.UnassignedTaskResponse;
import com.project_management.final_project.dto.response.UpcomingDueTaskResponse;
import com.project_management.final_project.entities.Task;
import com.project_management.final_project.exception.AppException;
import com.project_management.final_project.exception.ErrorCode;
import com.project_management.final_project.service.TaskService;
import com.project_management.final_project.util.ApiResponseUtil;
import com.project_management.final_project.validator.DateValidator;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;
    private final SecurityService securityService;
    private final DateValidator dateValidator;

    @Autowired
    public TaskController(TaskService taskService, SecurityService securityService, DateValidator dateValidator) {
        this.taskService = taskService;
        this.securityService = securityService;
        this.dateValidator = dateValidator;
    }
    
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        if (binder.getTarget() instanceof CreateTaskRequest || binder.getTarget() instanceof UpdateTaskRequest) {
            binder.addValidators(dateValidator);
        }
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
            @Valid @RequestBody CreateTaskRequest request,
            BindingResult bindingResult) {
        
        logger.info("Creating new task in project ID: {}", projectId);
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError() != null 
                    ? bindingResult.getFieldError().getDefaultMessage() 
                    : "Please fill in all required fields.";
            
            logger.warn("Validation error when creating task: {}", errorMessage);
            
            ApiResponse<TaskResponse> response = ApiResponse.<TaskResponse>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(errorMessage)
                    .build();
            
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            TaskResponse taskResponse = taskService.createTask(projectId, request);
            
            ApiResponse<TaskResponse> response = ApiResponse.<TaskResponse>builder()
                    .code(HttpStatus.CREATED.value())
                    .message("Task created successfully")
                    .result(taskResponse)
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AppException e) {
            String errorMessage;
            
            if (e.getErrorCode() == ErrorCode.DUPLICATE_ENTITY) {
                errorMessage = "Task name already exists, please choose a different name.";
            } else {
                errorMessage = "Unable to create task. Please try again later.";
            }
            
            logger.error("Error creating task: {}", e.getMessage());
            
            ApiResponse<TaskResponse> response = ApiResponse.<TaskResponse>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(errorMessage)
                    .build();
            
            return ResponseEntity.badRequest().body(response);
        }
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
    public ApiResponse<PagedResponse<UnassignedTaskResponse>> getUnassignedTasks(
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
        
        return ApiResponseUtil.success(PagedResponse.fromPage(tasks));
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
    @GetMapping("/projects/{projectId}/members/{userId}/view-tasks")
    @PreAuthorize("hasAuthority('TASK_VIEW') and @securityService.canViewMemberTasks(#projectId, #userId)")
    public ApiResponse<List<AssignedTaskResponse>> getMemberTasksInProject(
            @PathVariable Integer projectId,
            @PathVariable Integer userId) {
        
        logger.info("Getting all tasks assigned to user ID {} in project ID {}", userId, projectId);
        
        List<AssignedTaskResponse> tasks = taskService.getMemberTasksInProject(projectId, userId);
        
        return ApiResponseUtil.success(tasks);
    }
    
    /**
     * Get tasks that are due within the next 3 days for the current user
     *
     * @return List of tasks due within the next 3 days
     */
    @GetMapping("/upcoming-due")
    @PreAuthorize("hasRole('DEVELOPER') and hasAuthority('TASK_VIEW')")
    public ApiResponse<List<UpcomingDueTaskResponse>> getMyUpcomingDueTasks() {
        
        logger.info("Getting upcoming due tasks for current user");
        
        List<UpcomingDueTaskResponse> tasks = taskService.getMyUpcomingDueTasks();
        
        return ApiResponseUtil.success(tasks);
    }

    /**
     * Update the status of a task
     *
     * @param taskId    The ID of the task to update
     * @param projectId The ID of the project the task belongs to
     * @param request   The status update request
     * @return The updated task
     */
    @PatchMapping("/{taskId}/projects/{projectId}/status")
    @PreAuthorize("hasAuthority('TASK_VIEW') and @securityService.canUpdateTaskStatus(#taskId, #projectId)")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Integer taskId,
            @PathVariable Integer projectId,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        
        logger.info("Updating status of task ID {} in project ID {} to {}", taskId, projectId, request.getStatus());
        
        TaskResponse taskResponse = taskService.updateTaskStatus(taskId, projectId, request);
        
        ApiResponse<TaskResponse> response = ApiResponse.<TaskResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Task status updated successfully")
                .result(taskResponse)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all tasks of a project with filtering options
     *
     * @param projectId The ID of the project
     * @param search Optional search term for task title or description
     * @param status Optional status filter
     * @param priority Optional priority filter
     * @param page Page number (default 0)
     * @param size Page size (default 10)
     * @param sortBy Field to sort by (default dueDate)
     * @param sortDirection Sort direction (asc or desc, default asc)
     * @return Paged response of project tasks
     */
    @GetMapping("/projects/{projectId}/all-tasks")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TASK_VIEW')")
    public ApiResponse<PagedResponse<ProjectTaskResponse>> getAllProjectTasks(
            @PathVariable Integer projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Task.Status status,
            @RequestParam(required = false) Task.Priority priority,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        logger.info("Getting all tasks for project ID: {}, Search: {}, Status: {}, Priority: {}", 
                projectId, search, status, priority);
        
        ProjectTaskFilterRequest filterRequest = ProjectTaskFilterRequest.builder()
                .search(search)
                .status(status)
                .priority(priority)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
        
        PagedResponse<ProjectTaskResponse> response = taskService.getAllProjectTasks(projectId, filterRequest);
        
        return ApiResponseUtil.success(response);
    }

    /**
     * Get detailed information about a task by ID
     *
     * @param taskId The ID of the task to retrieve
     * @return The task details
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TASK_VIEW')")
    public ApiResponse<TaskDetailResponse> getTaskById(@PathVariable Integer taskId) {
        
        logger.info("Getting detailed information for task ID: {}", taskId);
        
        TaskDetailResponse taskDetail = taskService.getTaskById(taskId);
        
        return ApiResponseUtil.success(taskDetail);
    }

    /**
     * Update an existing task
     *
     * @param taskId    The ID of the task to update
     * @param projectId The ID of the project the task belongs to
     * @param request   The task update request
     * @return List of all tasks in the project including the updated task
     * 
     * Note: If a task's status is UNASSIGNED and an assignee is provided, 
     * the status will automatically be changed to TODO
     */
    @PatchMapping("/{taskId}/projects/{projectId}/edit-task")
    @PreAuthorize("hasRole('PROJECT_MANAGER') and hasAuthority('TASK_UPDATE')")
    public ResponseEntity<ApiResponse<List<ProjectTaskResponse>>> updateTask(
            @PathVariable Integer taskId,
            @PathVariable Integer projectId,
            @Valid @RequestBody UpdateTaskRequest request,
            BindingResult bindingResult) {
        
        logger.info("Updating task ID {} in project ID {}", taskId, projectId);
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError() != null 
                    ? bindingResult.getFieldError().getDefaultMessage() 
                    : "Please fill in all required fields.";
            
            logger.warn("Validation error when updating task: {}", errorMessage);
            
            ApiResponse<List<ProjectTaskResponse>> response = ApiResponse.<List<ProjectTaskResponse>>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(errorMessage)
                    .build();
            
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            List<ProjectTaskResponse> taskResponses = taskService.updateTask(taskId, projectId, request);
            
            ApiResponse<List<ProjectTaskResponse>> response = ApiResponse.<List<ProjectTaskResponse>>builder()
                    .code(HttpStatus.OK.value())
                    .message("Task updated successfully")
                    .result(taskResponses)
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (AppException e) {
            String errorMessage;
            
            if (e.getErrorCode() == ErrorCode.DUPLICATE_ENTITY) {
                errorMessage = "Task name already exists, please choose a different name.";
            } else if (e.getErrorCode() == ErrorCode.INVALID_REQUEST) {
                errorMessage = e.getMessage();
            } else if (e.getErrorCode() == ErrorCode.NOT_FOUND) {
                errorMessage = "Task or project not found.";
            } else if (e.getErrorCode() == ErrorCode.UNAUTHORIZED) {
                errorMessage = "You are not authorized to update this task.";
            } else {
                errorMessage = "Unable to update task. Please try again later.";
            }
            
            logger.error("Error updating task: {}", e.getMessage());
            
            ApiResponse<List<ProjectTaskResponse>> response = ApiResponse.<List<ProjectTaskResponse>>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(errorMessage)
                    .build();
            
            return ResponseEntity.badRequest().body(response);
        }
    }
} 