package com.project_management.final_project.repository;

import com.project_management.final_project.entities.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    
    /**
     * Find tasks by project ID
     * @param projectId The project ID
     * @return List of tasks
     */
    List<Task> findByProjectId(Integer projectId);
    
    /**
     * Find tasks assigned to a specific user
     * @param assigneeId The assignee user ID
     * @return List of tasks
     */
    List<Task> findByAssigneeId(Integer assigneeId);
    
    /**
     * Check if a task exists in a project
     * @param taskId The task ID
     * @param projectId The project ID
     * @return true if the task exists in the project, false otherwise
     */
    boolean existsByIdAndProjectId(Integer taskId, Integer projectId);
    
    /**
     * Count tasks assigned to a specific user
     * @param assigneeId The assignee user ID
     * @return Count of tasks assigned to the user
     */
    int countByAssigneeId(Integer assigneeId);
    
    /**
     * Find unassigned tasks with optional filtering by project ID, search term, and priority
     * @param projectId The project ID (optional)
     * @param search Optional search term for task title or description
     * @param priority Optional priority filter
     * @param pageable Pagination information
     * @return Page of unassigned tasks
     */
    @Query("SELECT t FROM Task t " +
           "WHERE t.status = com.project_management.final_project.entities.Task.Status.UNASSIGNED " +
           "AND (:projectId IS NULL OR t.project.id = :projectId) " +
           "AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "    OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:priority IS NULL OR t.priority = :priority)")
    Page<Task> findUnassignedTasksWithFilters(
            @Param("projectId") Integer projectId,
            @Param("search") String search,
            @Param("priority") Task.Priority priority,
            Pageable pageable);
            
    /**
     * Find tasks assigned to a specific user in a specific project with optional filtering
     * @param projectId The project ID
     * @param assigneeId The assignee user ID
     * @param pageable Pagination information
     * @return Page of tasks
     */
    @Query("SELECT t FROM Task t " +
           "WHERE t.project.id = :projectId " +
           "AND t.assignee.id = :assigneeId")
    Page<Task> findTasksByProjectIdAndAssigneeId(
            @Param("projectId") Integer projectId,
            @Param("assigneeId") Integer assigneeId,
            Pageable pageable);
    
    /**
     * Find all tasks assigned to a specific user in a specific project
     * @param projectId The project ID
     * @param assigneeId The assignee user ID
     * @return List of tasks
     */
    @Query("SELECT t FROM Task t " +
           "WHERE t.project.id = :projectId " +
           "AND t.assignee.id = :assigneeId " +
           "ORDER BY t.dueDate ASC")
    List<Task> findAllTasksByProjectIdAndAssigneeId(
            @Param("projectId") Integer projectId,
            @Param("assigneeId") Integer assigneeId);
} 