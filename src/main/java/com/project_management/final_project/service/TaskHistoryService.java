package com.project_management.final_project.service;

import com.project_management.final_project.dto.response.TaskHistoryResponse;
import com.project_management.final_project.entities.Task;
import com.project_management.final_project.entities.TaskHistory;
import com.project_management.final_project.entities.User;

import java.util.List;

public interface TaskHistoryService {
    
    /**
     * Create a new task history record for a status change
     * 
     * @param task The task that was updated
     * @param oldStatus The status before the update
     * @param newStatus The status after the update
     * @return The created task history record
     */
    TaskHistory createTaskStatusHistory(Task task, Task.Status oldStatus, Task.Status newStatus);
    
    /**
     * Create task history records for multiple tasks when a team member is removed
     * Each task will have its current status recorded as the oldStatus
     * 
     * @param tasks The list of tasks that were unassigned
     * @param newStatus The status after the update (UNASSIGNED)
     * @param systemUser The system user who is recorded as making the change
     * @return Number of task history records created
     */
    int createTaskStatusHistoryForTeamMemberRemoval(List<Task> tasks, Task.Status newStatus, User systemUser);
    
    /**
     * Get task history for a specific task
     * 
     * @param taskId The ID of the task
     * @return List of task history responses with formatted messages
     */
    List<TaskHistoryResponse> getTaskHistory(Integer taskId);
    
    /**
     * Get the most recent task history records across all tasks
     * 
     * @param limit Maximum number of records to return
     * @return List of task history responses with formatted messages
     */
    List<TaskHistoryResponse> getRecentTaskHistory(int limit);
    
    /**
     * Get the most recent task history records for tasks in projects created by the current user
     * 
     * @param limit Maximum number of records to return
     * @return List of task history responses with formatted messages
     */
    List<TaskHistoryResponse> getRecentTaskHistoryForMyProjects(int limit);
    
    /**
     * Get the most recent task history records for tasks assigned to the current user
     * 
     * @param limit Maximum number of records to return
     * @return List of task history responses with formatted messages
     */
    List<TaskHistoryResponse> getMyRecentTaskHistory(int limit);
} 