package com.project_management.final_project.service;

import com.project_management.final_project.dto.request.AssignTaskRequest;
import com.project_management.final_project.dto.request.CreateTaskRequest;
import com.project_management.final_project.dto.request.UnassignedTaskFilterRequest;
import com.project_management.final_project.dto.response.AssignedTaskResponse;
import com.project_management.final_project.dto.response.TaskResponse;
import com.project_management.final_project.dto.response.UnassignedTaskResponse;
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
} 