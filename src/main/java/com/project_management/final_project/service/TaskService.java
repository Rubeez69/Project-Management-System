package com.project_management.final_project.service;

import com.project_management.final_project.dto.request.AssignTaskRequest;
import com.project_management.final_project.dto.request.CreateTaskRequest;
import com.project_management.final_project.dto.request.ProjectTaskFilterRequest;
import com.project_management.final_project.dto.request.UnassignedTaskFilterRequest;
import com.project_management.final_project.dto.request.UpdateTaskStatusRequest;
import com.project_management.final_project.dto.response.AssignedTaskResponse;
import com.project_management.final_project.dto.response.PagedResponse;
import com.project_management.final_project.dto.response.ProjectTaskResponse;
import com.project_management.final_project.dto.response.TaskResponse;
import com.project_management.final_project.dto.response.UnassignedTaskResponse;
import com.project_management.final_project.dto.response.UpcomingDueTaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    
    /**
     * Create a new task in a project
     * @param projectId The ID of the project to add the task to
     * @param request The task creation request
     * @return The created task
     */
    TaskResponse createTask(Integer projectId, CreateTaskRequest request);
    
    /**
     * Get unassigned tasks for a specific project with filtering
     * @param projectId The ID of the project to get unassigned tasks for
     * @param filterRequest Filter criteria including search term and priority
     * @param pageable Pagination information
     * @return Page of unassigned task responses
     */
    Page<UnassignedTaskResponse> getUnassignedTasks(Integer projectId, UnassignedTaskFilterRequest filterRequest, Pageable pageable);
    
    /**
     * Assign a task to a team member
     * @param taskId The ID of the task to assign
     * @param request The assignment request containing the user ID
     * @return The updated task
     */
    TaskResponse assignTask(Integer taskId, AssignTaskRequest request);
    
    /**
     * Get all tasks assigned to the current user for a specific project
     * @param projectId The ID of the project
     * @return List of assigned task responses
     */
    List<AssignedTaskResponse> getMyTasksInProject(Integer projectId);
    
    /**
     * Get all tasks assigned to a specific team member in a project
     * @param projectId The ID of the project
     * @param userId The ID of the user whose tasks to retrieve
     * @return List of assigned task responses
     */
    List<AssignedTaskResponse> getMemberTasksInProject(Integer projectId, Integer userId);
    
    /**
     * Get tasks that are due within the next 3 days for the current user
     * @return List of upcoming due task responses
     */
    List<UpcomingDueTaskResponse> getMyUpcomingDueTasks();
    
    /**
     * Update the status of a task
     * @param taskId The ID of the task to update
     * @param projectId The ID of the project the task belongs to
     * @param request The status update request
     * @return The updated task
     */
    TaskResponse updateTaskStatus(Integer taskId, Integer projectId, UpdateTaskStatusRequest request);
    
    /**
     * Get all tasks for a project with filtering options
     * @param projectId The ID of the project
     * @param filterRequest Filter criteria including search term, status, and priority
     * @return Paged response of project tasks
     */
    PagedResponse<ProjectTaskResponse> getAllProjectTasks(Integer projectId, ProjectTaskFilterRequest filterRequest);
} 